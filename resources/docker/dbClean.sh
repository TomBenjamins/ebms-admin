#!/bin/sh
cd /home/embs
java -cp ebms-admin.jar nl.clockwork.ebms.admin.DBClean -configDir /conf/ -cmd messages -retentionDays 90 >>/logs/dbClean.log
