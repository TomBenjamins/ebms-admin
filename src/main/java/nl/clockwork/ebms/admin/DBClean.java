/**
 * Copyright 2013 Clockwork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.clockwork.ebms.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.querydsl.sql.SQLQueryFactory;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import lombok.experimental.FieldDefaults;
import nl.clockwork.ebms.querydsl.model.QCpa;
import nl.clockwork.ebms.querydsl.model.QDeliveryLog;
import nl.clockwork.ebms.querydsl.model.QDeliveryTask;
import nl.clockwork.ebms.querydsl.model.QEbmsAttachment;
import nl.clockwork.ebms.querydsl.model.QEbmsMessage;
import nl.clockwork.ebms.querydsl.model.QMessageEvent;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DBClean implements SystemInterface
{
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	TextIO textIO = TextIoFactory.getTextIO();

	public static void main(String[] args) throws Exception
	{
		val options = createOptions();
		val cmd = new DefaultParser().parse(options,args);
		if (cmd.hasOption("h"))
			printUsage(options);
		else
		{
			init(cmd);
			try (val context = new AnnotationConfigApplicationContext(DBConfig.class))
			{
				val dbClean = createDBClean(context);
				dbClean.execute(cmd);
			}
		}
		System.exit(0);
	}

	private static Options createOptions()
	{
		val result = new Options();
		result.addOption("h",false,"print this message");
		result.addOption("cmd",true,"objects to clean [values: cpa|messages]");
		result.addOption("cpaId",true,"the cpaId of the CPA to delete");
		result.addOption("dateFrom",true,"the date from which objects will be deleted [format: YYYYMMDD][default: " + dateFormatter.format(LocalDate.now().minusDays(30)) + "]");
		result.addOption("retentionDays", true, "the number of days that will be retained during deletion, overrules occurrence of dateFrom option");
		result.addOption("configDir",true,"set config directory (default=current dir)");
		return result;
	}

	private static void printUsage(Options options)
	{
		val formatter = new HelpFormatter();
		formatter.printHelp("DBClean",options,true);
	}

	private static void init(CommandLine cmd)
	{
		val configDir = cmd.getOptionValue("configDir","");
		System.setProperty("ebms.configDir",configDir);
		System.out.println("Using config directory: " + configDir);
	}

	private static DBClean createDBClean(AnnotationConfigApplicationContext context)
	{
		val queryFactory = context.getBean(SQLQueryFactory.class);
		val transactionManager = context.getBean("dataSourceTransactionManager",PlatformTransactionManager.class);
		val dataSource = context.getBean(DataSource.class);
		val jdbctemplate =  new JdbcTemplate(dataSource);
		return new DBClean(queryFactory,transactionManager, jdbctemplate);
	}

	@NonNull
	SQLQueryFactory queryFactory;
	@NonNull
	PlatformTransactionManager transactionManager;
	@NonNull
	JdbcTemplate jdbcTemplate;

	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	QCpa cpaTable = QCpa.cpa1;
	QEbmsMessage messageTable = QEbmsMessage.ebmsMessage;
	QEbmsAttachment attachmentTable = QEbmsAttachment.ebmsAttachment;
	QMessageEvent messageEventTable = QMessageEvent.ebmsMessageEvent;
	QDeliveryTask deliveryTaskTable = QDeliveryTask.deliveryTask;
	QDeliveryLog deliveryLogTable = QDeliveryLog.deliveryLog;
	
	private void execute(final CommandLine cmd) throws Exception
	{
		switch(cmd.getOptionValue("cmd",""))
		{
			case("cpa"):
				validateCleanCPA(cmd);
				executeCleanCPA(cmd);
				break;
			case("messages"):
				executeCleanMessages(cmd);
				break;
			default:
				println(cmd.getOptionValue("cmd") + " not recognized");
		}
	}

	private boolean validateCleanCPA(CommandLine cmd)
	{
		if (!cmd.hasOption("cpaId"))
		{
			println("Option cpaId missing");
			return false;
		}
		return true;
	}

	private void executeCleanCPA(CommandLine cmd) throws IOException
	{
		val cpaId = cmd.getOptionValue("cpaId");
		val status = transactionManager.getTransaction(null);
		try
		{
			if (queryFactory.select(cpaTable.cpaId).from(cpaTable).where(cpaTable.cpaId.eq(cpaId)).fetchCount() > 0)
			{
				val ok = textIO.newBooleanInputReader()
						.withDefaultValue(false)
						.read("WARNING: This command will delete all messages and data related to cpa " + cpaId + ". Are you sure?");
				if (ok)
					cleanCPA(cpaId);
			}
			else
				println("CPA " + cpaId + " not found!");
			
			transactionManager.commit(status);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		    transactionManager.rollback(status);
		}
	}

	private void executeCleanMessages(CommandLine cmd) throws IOException
	{
		val dateFrom = Objects.nonNull(cmd.getOptionValue("retentionDays")) ? createDateFromRetentionDays(cmd.getOptionValue("retentionDays")) 
		                                                                    : createDateFrom(cmd.getOptionValue("dateFrom"));
		if (dateFrom != null)
		{
			println("using fromDate " + dateFrom);
			val status = transactionManager.getTransaction(null);
			try
			{
				cleanMessages(dateFrom);
				transactionManager.commit(status);
			}
			catch (Exception e)
			{
			    e.printStackTrace();
			    transactionManager.rollback(status);
			}
		}
		else
		{
			println("Unable to parse date " + cmd.getOptionValue("dateFrom"));
		}
	}
	
	private static Instant createDateFromRetentionDays(String retentionDaysString)
    {
        try
        {
            val date = StringUtils.isEmpty(retentionDaysString) ? null : LocalDate.now().minusDays(Integer.parseInt(retentionDaysString));
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
	
	private static Instant createDateFrom(String s)
	{
		try
		{
			val date = StringUtils.isEmpty(s) ? LocalDate.now().minusDays(30) : LocalDate.parse(s,dateFormatter);
			return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
		}
		catch (DateTimeParseException e)
		{
			return null;
		}
	}

	private boolean alternativeAttachmentImplementation() {
		var vendor = "";
		try(Connection connection = jdbcTemplate.getDataSource().getConnection())
		{
			vendor = connection.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
			e.printStackTrace();
		};
		return vendor.equalsIgnoreCase("mysql") || 
		       vendor.equalsIgnoreCase("microsoft sql server") ||
				vendor.equalsIgnoreCase("h2") ||
				vendor.equalsIgnoreCase("mariadb");
	}

	private void cleanCPA(String cpaId)
	{
	    val ids = queryFactory.select(messageTable.messageId).from(messageTable).where(messageTable.cpaId.eq(cpaId)).fetch();

		Function<List<String>, Long> query = idList -> queryFactory.delete(deliveryLogTable).where(deliveryLogTable.messageId.in(idList)).execute();
		defensiveDelete(ids, "deliveryLogs", query);
		
		query = idList -> queryFactory.delete(deliveryTaskTable).where(deliveryTaskTable.messageId.in(ids)).execute();
		defensiveDelete(ids, "deliveryTasks", query);

		query = idList -> queryFactory.delete(messageEventTable).where(messageEventTable.messageId.in(ids)).execute();
		defensiveDelete(ids, "messageEvents", query);

		if (alternativeAttachmentImplementation())
		{
			MapSqlParameterSource parameterSource = new MapSqlParameterSource();
			parameterSource.addValue("cpaId", cpaId);
			List<Integer> idsInteger = jdbcTemplate.queryForList("select id from ebms_message where cpa_id = ?", new Object[]{cpaId} , Integer.class);

	        query = idList -> 
            {
                SqlParameterSource parameters = new MapSqlParameterSource("idsInteger", idsInteger);
                return (long)jdbcTemplate.update("delete from ebms_attachment where ebms_message_id in "
                + "(:idsInteger)", parameters);
            };
		} else {
			query = idList -> queryFactory.delete(attachmentTable).where(attachmentTable.messageId.in(idList)).execute();
		}
		defensiveDelete(ids, "attachments", query);
		
		query = idList -> (long) queryFactory.delete(messageTable).where(messageTable.cpaId.eq(cpaId)).execute();
		defensiveDelete(ids, "messages", query);

		println("delete cpa " + cpaId + " in ebms-admin to delete it from the cache!!!");
	}

	private void cleanMessages(Instant dateFrom)
	{
		val ids = queryFactory.select(messageTable.messageId).from(messageTable).where(messageTable.persistTime.loe(dateFrom)).fetch();
		
		Function<List<String>, Long> query = idList -> queryFactory.delete(deliveryLogTable).where(deliveryLogTable.messageId.in(idList)).execute();
		defensiveDelete(ids, "deliveryLogs", query);

		query = idList -> queryFactory.delete(deliveryTaskTable).where(deliveryTaskTable.messageId.in(idList)).execute();
		defensiveDelete(ids, "deliveryTasks", query);
		
		query = idList -> queryFactory.delete(messageEventTable).where(messageEventTable.messageId.in(idList)).execute();
		defensiveDelete(ids, "messageEvents", query);
		
		if (alternativeAttachmentImplementation())
		{
		    query = idList -> 
		            {
		            SqlParameterSource parameters = new MapSqlParameterSource("ids", idList);
		            return (long)jdbcTemplate.update("delete from ebms_attachment where ebms_message_id in "
					+ "(:ids)", parameters);
		            };
		} else {
			query = idList -> queryFactory.delete(attachmentTable).where(attachmentTable.messageId.in(idList)).execute();
		}
		defensiveDelete(ids, "attachments", query);
		
		var result = queryFactory.delete(messageTable).where(messageTable.persistTime.loe(dateFrom)).execute();
		println(result + " messages deleted");
	}
	
	private void defensiveDelete(List<String> ids, String tableString, Function<List<String>,Long> query){
	    int deleteBlockSize = 4000;//TODO make this configurable?
        
	    List<String> localCopy = new ArrayList<>(ids);
	    
        if (ids.size() == 0) {
            println("no "+tableString+" objects to delete");
        } else {
            List<String> idsBlock = null;
            int stopIndex = Math.min(deleteBlockSize, ids.size());
            int nrOfRowsDeleted = 0;
            do {
                
                idsBlock = localCopy.subList(0, stopIndex);
                if (idsBlock.size() > 0) {
                    nrOfRowsDeleted += query.apply(idsBlock);
                    
                    // corresponding entries in ids list will be cleared
                    idsBlock.clear();
                    stopIndex = Math.min(deleteBlockSize, localCopy.size());
                }
            } while (localCopy.size() > 0);
            println(nrOfRowsDeleted +" "+tableString+ "rows deleted");
        }
	}
	
}
