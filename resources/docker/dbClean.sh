#!/bin/sh
cd /home/ebms
java -Dlog4j.configurationFile=log4j2.dbClean.xml -cp ebms-admin.jar nl.clockwork.ebms.admin.DBClean -configDir /conf/ -cmd messages -retentionDays 90 
