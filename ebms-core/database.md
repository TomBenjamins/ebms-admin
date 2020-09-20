---
sort: 5
---

# Database support

ebms-core v{{ site.data.ebms.core.version }}

The EbMS Adapter supports the following databases:
- [DB2](#db2)
- [H2](#h2)
- [HSQLDB](#hsqldb)
- [MariaDB](#mariadb)
- [MS SQL Server](#ms-sql-server)
- [MySQL](#mysql)
- [Oracle](#oracle)
- [PostgreSQL](#postgresql)

The database master scripts can be found [here](https://github.com/eluinstra/ebms-core/tree/ebms-core-2.17.x/resources/scripts/database/master/)  
The database update scripts can be found [here](https://github.com/eluinstra/ebms-core/tree/ebms-core-2.17.x/src/main/resources/nl/clockwork/ebms/db/migration)  
ebms-core also supports automatic database migration through [Flyway](#flyway)

You can find the JDBC settings for the supported databases below.  

## JDBC Common
```
ebms.jdbc.username=<username>
ebms.jdbc.password=<password>
```
## DB2
```
# JDBC driver
ebms.jdbc.driverClassName=com.ibm.db2.jcc.DB2Driver
# or XA driver
ebms.jdbc.driverClassName=com.ibm.db2.jcc.DB2XADataSource
ebms.jdbc.url=jdbc:db2://<host>:<port>/<dbname>
```
Download drivers [here](https://www.ibm.com/support/pages/db2-jdbc-driver-versions-and-downloads)
## H2
since [v2.17.2]({{ site.baseurl }}/ebms-core/release.html#ebms-core-2172jar)
```
# JDBC and XA driver
ebms.jdbc.driverClassName=org.h2.Driver
# In memory
ebms.jdbc.url=jdbc:h2:mem:<dbname>
# or file
ebms.jdbc.url=jdbc:h2:<path>
# or server
ebms.jdbc.url=jdbc:h2:tcp://<host>:<port>/<path>
```
Download drivers [here](http://www.h2database.com/html/download.html)
## HSQLDB
```
# JDBC driver
ebms.jdbc.driverClassName=org.hsqldb.jdbcDriver
# or XA driver
ebms.jdbc.driverClassName=org.hsqldb.jdbc.pool.JDBCXADataSource
# In memory
ebms.jdbc.url=jdbc:hsqldb:mem:<dbname>
# or file
ebms.jdbc.url=jdbc:hsqldb:file:<path>
# or server
ebms.jdbc.url=jdbc:hsqldb:hsql://<host>:<port>/<dbname>
```
Download drivers [here](https://sourceforge.net/projects/hsqldb/files/hsqldb/)
## MariaDB
```
# JDBC driver
ebms.jdbc.driverClassName=org.mariadb.jdbc.Driver
# or XA driver
ebms.jdbc.driverClassName=
ebms.jdbc.url=jdbc:mysql://<host>:<port>/<dbname>
```
Download drivers [here](https://downloads.mariadb.org/connector-java/)
## MS SQL Server
```
# JDBC driver
ebms.jdbc.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
# or XA driver
ebms.jdbc.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerXADataSource
ebms.jdbc.url=jdbc:sqlserver://<host>:<port>;[instanceName=<instanceName>;]databaseName=<dbname>;
```
Download drivers [here](https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver15)
## MySQL
```
# JDBC driver
ebms.jdbc.driverClassName=com.mysql.cj.jdbc.Driver
# or XA driver
ebms.jdbc.driverClassName=com.mysql.cj.jdbc.MysqlXADataSource
ebms.jdbc.url=jdbc:mysql://<host>:<port>/<dbname>
```
Download drivers [here](https://dev.mysql.com/downloads/connector/j/)
## Oracle
```
# JDBC driver
ebms.jdbc.driverClassName=oracle.jdbc.OracleDriver
# or XA driver
ebms.jdbc.driverClassName=oracle.jdbc.xa.client.OracleXADataSource
ebms.jdbc.url=jdbc:oracle:thin:@<host>:<port>:<dbname>
```
Download drivers [here](https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html)
## PostgreSQL
```
# JDBC driver
ebms.jdbc.driverClassName=org.postgresql.Driver
# or XA driver
ebms.jdbc.driverClassName=org.postgresql.xa.PGXADataSource
ebms.jdbc.url=jdbc:postgresql://<host>:<port>/<dbname>
```
Download drivers [here](https://jdbc.postgresql.org/download.html)
## Flyway
Database migration through Flyway is enabled through the following [EbMS property]({{ site.baseurl }}/ebms-core/properties.html#datastore) (since [v2.17.2]({{ site.baseurl }}/ebms-core/release.html#ebms-core-2172jar))
```
ebms.jdbc.update=true
```
If you already have an existing database and want to use Flyway, then you first have to [initialize Flyway](/ebms-admin/database.html#initialize-flyway). Otherwise you can just enable the property.