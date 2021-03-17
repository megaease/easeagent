FROM megaease/easeimg-javabuild:latest AS builder

ARG      REPOSITORY1="km-nexus|http://192.168.50.105:18081/nexus/repository/maven-public/|true|true"
ARG      REPOSITORY2
ARG      REPOSITORY3
ARG      MIRROR1="km-nexus|http://192.168.50.105:18081/nexus/repository/maven-public/|central"
ARG      MIRROR2
ARG      MIRROR3
ARG      SERVER1
ARG      SERVER2
ARG      SERVER3

COPY     ./ /easeagent/
WORKDIR  /easeagent

RUN     /bin/rewrite-settings.sh &&  cd /easeagent/ && mvn clean package -am -pl build

FROM alpine:latest
RUN apk --no-cache add curl wget

RUN wget -O jolokia.jar  https://search.maven.org/remotecontent\?filepath\=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar

COPY --from=builder /easeagent/build/target/easeagent-dep.jar   /easeagent-volume/easeagent.jar
RUN cp /jolokia.jar  /easeagent-volume/jolokia.jar
