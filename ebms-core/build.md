---
sort: 3
---

# Build

### Java
ebms-core is compiled and tested with OpenJDK 8  

### Maven
ebms-core is released in the [Central Maven repository](https://mvnrepository.com/artifact/nl.clockwork.ebms/ebms-core/{{ site.data.ebms.core.version }}):
```
<dependency>
  <groupId>nl.clockwork.ebms</groupId>
  <artifactId>ebms-core</artifactId>
  <version>{{ site.data.ebms.core.version }}</version>
</dependency>
```
ebms-core includes Oracle ojdbc 8. If you don't use Oracle database then you can exclude the jar. Otherwise add the following `<server>` element to the `<servers>` section of the Maven ~/.m2/settings.xml:
```
 <server>
    <id>maven.oracle.com</id>
    <username>username</username>
    <password>password</password>
    <configuration>
      <basicAuthScope>
        <host>ANY</host>
        <port>ANY</port>
        <realm>OAM 11g</realm>
      </basicAuthScope>
      <httpConfiguration>
        <all>
          <params>
            <property>
              <name>http.protocol.allow-circular-redirects</name>
              <value>%b,true</value>
            </property>
          </params>
        </all>
      </httpConfiguration>
    </configuration>
  </server>
```
Replace the `<username>` and `<password>` entries with your OTN user name and password.
```
mvn package
```
### Eclipse
- install https://marketplace.eclipse.org/content/m2e-apt
- install lombok (since [v2.17.0]({{ site.baseurl }}/ebms-core/release.html#ebms-core-2170jar))

### History
ebms-core < [v2.16.0]({{ site.baseurl }}/ebms-core/release.html#ebms-core-2160jar) is compiled and tested with Oracle JDK 6