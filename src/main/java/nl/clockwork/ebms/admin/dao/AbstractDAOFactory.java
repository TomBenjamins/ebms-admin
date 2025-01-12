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

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static nl.clockwork.ebms.Predicates.contains;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.FactoryBean;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public abstract class AbstractDAOFactory<T> implements FactoryBean<T>
{
	@NonNull
	DataSource dataSource;

	@Override
	public T getObject()
	{
		return createDAO(dataSource);
	}

	private T createDAO(DataSource dataSource)
	{
		String driverClassName = getDriverClassName(dataSource);
		return Match(driverClassName).of(
				Case($(contains("db2")), o -> createDB2DAO()),
				Case($(contains("hsqldb")), o -> createHSqlDbDAO()),
				Case($(contains("mysql", "mariadb")), o -> createMySqlDAO()),
				Case($(contains("oracle")), o -> createOracleDAO()),
				Case($(contains("postgresql")), o -> createPostgresDAO()),
				Case($(contains("sqlserver")), o -> createMsSqlDAO()),
				Case($(), o ->
				{
					throw new RuntimeException("Jdbc url " + driverClassName + " not recognized!");
				}));
	}

	public static String getDriverClassName(DataSource dataSource)
	{
		return dataSource instanceof HikariDataSource
				? ((HikariDataSource)dataSource).getDriverClassName()
				: dataSource instanceof PoolingDataSource
						? ((PoolingDataSource)dataSource).getClassName()
						: ((AtomikosDataSourceBean)dataSource).getXaDataSourceClassName();
	}

	@Override
	public abstract Class<T> getObjectType();

	@Override
	public boolean isSingleton()
	{
		return true;
	}

	public abstract T createHSqlDbDAO();

	public abstract T createMySqlDAO();

	public abstract T createPostgresDAO();

	public abstract T createOracleDAO();

	public abstract T createMsSqlDAO();

	public abstract T createDB2DAO();

	public abstract static class DefaultDAOFactory<U> extends AbstractDAOFactory<U>
	{
		public DefaultDAOFactory(@NonNull DataSource dataSource)
		{
			super(dataSource);
		}

		@Override
		public U createHSqlDbDAO()
		{
			throw new RuntimeException("HSQLDB not supported!");
		}

		@Override
		public U createMySqlDAO()
		{
			throw new RuntimeException("MySQL not supported!");
		}

		@Override
		public U createPostgresDAO()
		{
			throw new RuntimeException("Postgres not supported!");
		}

		@Override
		public U createOracleDAO()
		{
			throw new RuntimeException("Oracle not supported!");
		}

		@Override
		public U createMsSqlDAO()
		{
			throw new RuntimeException("MSSQL not supported!");
		}

		@Override
		public U createDB2DAO()
		{
			throw new RuntimeException("DB2 not supported!");
		}

	}

}
