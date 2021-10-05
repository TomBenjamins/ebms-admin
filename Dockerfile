FROM openjdk:8-jre-alpine

RUN apk add --no-cache tzdata

ARG EBMS_VERSION=2.18.0
ARG EBMS_ADMIN=ebms-admin
ARG LOG4J2=log4j2.xml
ARG USER=ebms

ENV HEALTH_PORT=9017
ENV SOAP_PORT=9089
ENV PROXY_PORT=9876
ENV WORKDIR /home/$USER
ENV JAVA_ARGS "-Dlog4j.configurationFile=$LOG4J2"
ENV START=./start.sh
ENV DBCLEAN=dbClean.sh
ENV DBCLEAN_CRONJOB=dbClean_cronjob
ENV CONFIGDIR /conf/

ENV TZ Europe/Amsterdam

LABEL maintainer="tombenjamins@lostlemon.nl"
LABEL version=nl.lostlemon.ebms.admin.version=$EBMS_VERSION

WORKDIR $WORKDIR
# expose default jmx ports
EXPOSE 1099 1999 

COPY target/${EBMS_ADMIN}.jar .
COPY resources/docker/${LOG4J2} ${LOG4J2}

COPY resources/docker/${DBCLEAN} ./${DBCLEAN}
RUN chmod u+x ./$DBCLEAN 

COPY resources/docker/${DBCLEAN_CRONJOB} /etc/crontabs/root

#RUN addgroup -S $USER && \
#adduser -S $USER -G $USER && \

RUN printf "#!/bin/sh\n" > $START && \
printf "crond -f -d 8 -L /logs/crond.log \n" >> $START && \
printf "java ${JAVA_ARGS} -cp ${EBMS_ADMIN}.jar nl.clockwork.ebms.admin.StartEmbedded -soap -headless -health -jmx true -port \$SOAP_PORT -healthPort \$HEALTH_PORT -configDir \$CONFIGDIR  \$@\n" >> $START && \
chmod u+x $START 
# && \
#chown -R $USER:$USER $WORKDIR
# USER $USER:$USER

CMD ${START}

