/*
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
package nl.clockwork.ebms.admin.dao;


import com.querydsl.sql.SQLQueryFactory;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import nl.clockwork.ebms.transaction.TransactionManagerConfig.TransactionManagerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AdminDAOConfig
{
	@Value("${transactionManager.type}")
	TransactionManagerType transactionManagerType;
	@Autowired
	@Qualifier("dataSourceTransactionManager")
	PlatformTransactionManager dataSourceTransactionManager;
	@Autowired
	DataSource dataSource;
	@Autowired
	SQLQueryFactory queryFactory;

	@Bean("ebMSAdminDAO")
	public EbMSDAO ebMSDAO()
	{
		val jdbcTemplate = new JdbcTemplate(dataSource);
		return new EbMSDAOFactory(transactionManagerType, dataSource, jdbcTemplate, queryFactory).getObject();
	}
}
