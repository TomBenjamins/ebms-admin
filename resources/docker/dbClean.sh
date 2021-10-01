#1/bin/bash
java -cp ebms-admin.jar nl.clockwork.ebms.admin.DBClean -configDir /resources/docker/ -cmd messages -retentionDays 90 >>/dbClean.log
