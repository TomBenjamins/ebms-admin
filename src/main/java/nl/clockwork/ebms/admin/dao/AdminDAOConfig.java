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
package nl.clockwork.ebms.admin.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AdminDAOConfig
{
	@Autowired
	DataSource dataSource;

	@Bean("ebMSAdminDAO")
	public EbMSDAO ebMSDAO() throws Exception
	{
		val transactionManager = new DataSourceTransactionManager(dataSource);
		val transactionTemplate = new TransactionTemplate(transactionManager);
		val jdbcTemplate = new JdbcTemplate(dataSource);
		return new EbMSDAOFactory(dataSource,transactionTemplate,jdbcTemplate).getObject();
	}
}