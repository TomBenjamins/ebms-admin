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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static java.util.stream.Collectors.toList;

import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import lombok.experimental.FieldDefaults;
import nl.clockwork.ebms.querydsl.model.QCpa;
import nl.clockwork.ebms.querydsl.model.QEbmsAttachment;
import nl.clockwork.ebms.querydsl.model.QEbmsEvent;
import nl.clockwork.ebms.querydsl.model.QEbmsEventLog;
import nl.clockwork.ebms.querydsl.model.QEbmsMessage;
import nl.clockwork.ebms.querydsl.model.QEbmsMessageEvent;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public class DBClean extends Start
{
	private static String OPTION_CMD_CPA = "cpa";
	private static String OPTION_CMD_DATE = "dateFrom";
	private static String FORMAT_DATE = "YYYYMMDD";
	private static int DEFAULT_DAYS = 30;
	
	TransactionTemplate transactionTemplate;
	
	JdbcTemplate jdbcTemplate;
	
	@NonNull
	PlatformTransactionManager transactionManager;
	
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	String SELECT_MESSAGEIDS_FOR_CPA = "select message_id from ebms_message where cpa_id = ?";
	String SELECT_MESSAGEIDS_SINCE = "select message_id from ebms_message where cpa_id = ?";
	
	String DELETE_FROM_EVENTLOG = "delete from delivery_log where message_id = ?";
	String DELETE_FROM_EVENT = "delete from delivery_task where message_id = ?";
	String DELETE_FROM_MESSAGEEVENT = "delete from ebms_event where message_id = ?";
	String DELETE_FROM_ATTACHMENT = "delete from ebms_attachment where message_id = ?";
	String DELETE_FROM_MESSAGE = "delete from ebms_message where message_id = ?";
	
	public static void main(String[] args) throws Exception
	{
		val options = createOptions();
		val cmd = new DefaultParser().parse(options,args);
		if (cmd.hasOption("h"))
			printUsage(options);
		
		try (val context = new AnnotationConfigApplicationContext(DBCleanConfig.class))
		{
			createDBClean(context)
				.execute(cmd);
		}
	}

	private static DBClean createDBClean(AnnotationConfigApplicationContext context)
	{
		val transactionManager = context.getBean("dataSourceTransactionManager", PlatformTransactionManager.class);
		val dbClean = new DBClean(transactionManager);
		
		return dbClean;
	}

	protected static Options createOptions()
	{
		val result = new Options();
		result.addOption("h",false,"print this message");
		result.addOption(OPTION_CMD_CPA, true, "the cpaId of the CPA for which to delete the associated objects");
		result.addOption(OPTION_CMD_DATE, true, String.format("the date from which objects will be deleted [format: %s][default: -%d days, %s]", DATE_FORMAT, DEFAULT_DAYS, dateFormatter.format(LocalDate.now().minusDays(DEFAULT_DAYS))) );
		return result;
	}
	
	private void execute(final org.apache.commons.cli.CommandLine cmd) throws Exception
	{
		if (cmd.hasOption(OPTION_CMD_CPA))
		{
			val cpaId = cmd.getOptionValue(OPTION_CMD_CPA, "");
			if (!cpaId.isEmpty())
			{
				executeCleanCPA(cmd);
			}
			else
			{
				print("missing cpa id on commandline");
			}
		} else
			if (cmd.hasOption(OPTION_CMD_DATE))
			{
				val dateSince = cmd.getOptionValue(OPTION_CMD_DATE, dateFormatter.format(LocalDate.now().minusDays(DEFAULT_DAYS))));
				executeCleanMessages(cmd);
			} else {
				print("do nothing");
			}

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
				print("CPA %s not found!", cpaId);
		}
		catch (Exception e)
		{
			transactionManager.rollback(status);
		}
		transactionManager.commit(status);
	}

	private void executeCleanMessages(CommandLine cmd) throws IOException
	{
		val dateFrom = createDateFrom(cmd.getOptionValue("dateFrom"));
		if (dateFrom != null)
		{
			print("using fromDate " + dateFrom);
			val status = transactionManager.getTransaction(null);
			try
			{
				cleanMessages(dateFrom);
			}
			catch (Exception e)
			{
				transactionManager.rollback(status);
			}
			transactionManager.commit(status);
		}
		print("Unable to parse date %s", cmd.getOptionValue("dateFrom"));
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

	private void cleanCPA(String cpaId)
	{
		List<String> messageIds = jdbcTemplate
				.queryForList(SELECT_MESSAGEIDS_FOR_CPA, String.class, cpaId)
				.stream()
				.collect(toList());
		
		print("attempting to clean %d messages", messageIds.size() );
		
		deleteFor(DELETE_FROM_EVENTLOG, messageIds, "eventlogs");
		deleteFor(DELETE_FROM_EVENT, messageIds, "events");
		deleteFor(DELETE_FROM_MESSAGEEVENT, messageIds, "message events");
		deleteFor(DELETE_FROM_ATTACHMENT, messageIds, "attachments");
		deleteFor(DELETE_FROM_MESSAGE, messageIds, "messages");
		
		print("delete cpa %s in ebms-admin to delete it from the cache!!!", cpaId);
	}
	
	private void deleteFor(String statement, List<String> messageIds, String logStatement) {
		AtomicInteger ctr = new AtomicInteger();
		messageIds.forEach( id ->
			ctr.getAndAdd( jdbcTemplate.update(statement, id) )
		);
		
		print( "%d %s deleted", ctr.intValue(), logStatement );
	}

	private void cleanMessages(Instant dateFrom)
	{
		
		List<String> messageIds = jdbcTemplate
				.queryForList(SELECT_MESSAGEIDS_FOR_CPA, String.class, dateFrom)
				.stream()
				.collect(toList());
		
		deleteFor(DELETE_FROM_EVENTLOG, messageIds, "eventlogs");
		deleteFor(DELETE_FROM_EVENT, messageIds, "events");
		deleteFor(DELETE_FROM_MESSAGEEVENT, messageIds, "message events");
		deleteFor(DELETE_FROM_ATTACHMENT, messageIds, "attachments");
		deleteFor(DELETE_FROM_MESSAGE, messageIds, "messages");
	}

	private void print(String s, Object...args)
	{
		System.out.println(String.format(s, args));
	}

	public DBClean(@NonNull PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate();
		this.transactionManager = transactionManager;
	}
}
