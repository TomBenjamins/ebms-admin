FROM openjdk:8-jre-alpine
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
ENV CONFIGDIR /conf/
LABEL maintainer="tombenjamins@lostlemon.nl"
LABEL version=nl.lostlemon.ebms.admin.version=$EBMS_VERSION
WORKDIR $WORKDIR
# expose default jmx and default jdbc ports
EXPOSE 1099 1999 3306 

COPY target/${EBMS_ADMIN}.jar .
ADD resources/docker/${LOG4J2} ${LOG4J2}

#RUN addgroup -S $USER && \
#adduser -S $USER -G $USER && \

RUN touch ${EBMS_PROPS} && \

printf "#!/bin/sh\n" > $START && \
printf "java ${JAVA_ARGS} -cp ${EBMS_ADMIN}.jar nl.clockwork.ebms.admin.StartEmbedded -soap -headless -health -jmx true -port \$SOAP_PORT -healthPort \$HEALTH_PORT -configDir \$CONFIGDIR  \$@\n" >> $START && \
chmod u+x $START 
# && \
#chown -R $USER:$USER $WORKDIR
# USER $USER:$USER

CMD ${START}

