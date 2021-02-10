FROM openjdk:8-jre-alpine
ARG EBMS_VERSION=2.18.0
ARG EBMS_ADMIN=ebms-admin
ARG LOG4J2=log4j2.xml
ARG START=start.sh
ARG USER=user
ENV WORKDIR /home/$USER
ENV JAVA_ARGS "-Dlog4j.configurationFile=$LOG4J2"
#LABEL maintainer="eluinstra@luin.dev"
#LABEL version=nl.clockwork.ebms.admin.version=$EBMS_VERSION
WORKDIR $WORKDIR
COPY target/${EBMS_ADMIN}.jar .
COPY ${LOG4J2} .
COPY ebms-admin.embedded.properties .
RUN addgroup -S $USER && \
adduser -S $USER -G $USER && \
touch ebms-admin.embedded.properties && \

#wget https://github.com/eluinstra/ebms-admin/releases/download/${EBMS_ADMIN}/${EBMS_ADMIN}.jar -O ${EBMS_ADMIN}.jar && \
#unzip -p ${EBMS_ADMIN}.jar $LOG4J2 > $LOG4J2 && \
#sed -i 's/ref="File"/ref="Console"/g' $LOG4J2 && \

printf "#!/bin/sh\n" > $START && \
printf "java \$JAVA_ARGS -cp ${EBMS_ADMIN}.jar nl.clockwork.ebms.admin.StartEmbedded \$@" >> $START && \
chmod u+x $START && \
chown -R $USER:$USER $WORKDIR
USER $USER:$USER
# mount logfilefolder and keystore folder

ENTRYPOINT ["./$START -soap -headless -health -jmx true -jmxPort 1099 -port 8089 -healthPort 9017"]
EXPOSE 1099 3306 9017 9089 9876

