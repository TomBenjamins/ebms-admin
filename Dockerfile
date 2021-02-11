FROM openjdk:8-jre-alpine
ARG EBMS_VERSION=2.18.0
ARG EBMS_ADMIN=ebms-admin
ARG LOG4J2=log4j2.xml
ARG START=start.sh
ARG USER=ebms
ARG EBMS_PROPS=ebms-admin.embedded.properties
#ARG JMX_PORT=1999
#ARG JDBC_PORT=3306
ARG HEALTH_PORT=9017
ARG SOAP_PORT=9089
ARG PROXY_PORT=9876

ENV WORKDIR /home/$USER
ENV JAVA_ARGS "-Dlog4j.configurationFile=$LOG4J2"
LABEL maintainer="tombenjamins@lostlemon.nl"
LABEL version=nl.lostlemon.ebms.admin.version=$EBMS_VERSION
WORKDIR $WORKDIR
EXPOSE 1099 1999 3306 ${HEALTH_PORT} ${SOAP_PORT} ${PROXY_PORT}

COPY target/${EBMS_ADMIN}.jar .
ADD resources/docker/${LOG4J2} ${LOG4J2}
ADD resources/docker/${EBMS_PROPS} ${EBMS_PROPS}
#RUN addgroup -S $USER && \
#adduser -S $USER -G $USER && \

RUN touch ${EBMS_PROPS} && \

printf "#!/bin/sh\n" > $START && \
printf "java ${JAVA_ARGS} -cp ${EBMS_ADMIN}.jar nl.clockwork.ebms.admin.StartEmbedded \$@" >> $START && \
chmod u+x $START 
# && \
#chown -R $USER:$USER $WORKDIR
# USER $USER:$USER

# ENTRYPOINT ["./$START -soap -headless -health -jmx true -port ${SOAP_PORT} -healthPort ${HEALTH_PORT}"]

