FROM megaease/easeimg-javabuild:latest AS builder

ARG      REPOSITORY1
ARG      REPOSITORY2
ARG      REPOSITORY3
ARG      MIRROR1
ARG      MIRROR2
ARG      MIRROR3
ARG      SERVER1
ARG      SERVER2
ARG      SERVER3

COPY     ./ /easeagent/
WORKDIR  /easeagent

RUN     /bin/rewrite-settings.sh &&  cd /easeagent/ && mvn clean package -am -pl build && cd build/target/ && jar xf easeagent.jar log4j2.xml

FROM alpine:latest
RUN apk --no-cache add curl wget


COPY --from=builder /easeagent/build/target/easeagent-dep.jar   /easeagent-volume/easeagent.jar
COPY --from=builder /easeagent/build/target/log4j2.xml   /easeagent-volume/log4j2.xml