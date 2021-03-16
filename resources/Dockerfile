FROM zhaokundev/maven-mysql:mvn3.5.3-jdk8-sql5.7-slim AS builder

COPY     ./ /easeagent/
WORKDIR  /easeagent

RUN      cd /easeagent/ && mvn clean package -am -pl build

RUN     /easeagent/release

FROM alpine:latest
RUN apk --no-cache add curl wget

RUN wget -O jolokia.jar  https://search.maven.org/remotecontent\?filepath\=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar
RUN wget -O easeagent.jar  https://github.com/megaease/easeagent/releases/download/v0.3.0/easeagent.jar

RUN cp /easeagent.jar /easeagent-volume/easeagent.jar
RUN cp /jolokia.jar  /easeagent-volume/jolokia.jar
