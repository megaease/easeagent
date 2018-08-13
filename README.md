
# Installation


## Download 

```
curl -Lk https://github.com/megaease/release/releases/download/easeagent/easeagent.jar -O
```

> **How to get the version of easeagent?**
> 
> `unzip -p easeagent.jar git.properties`

## Extract default configuration files

```
jar xf easeagent.jar application.conf log4j2.xml
```

## Setup JAVA_OPTS, then run your java program with `$JAVA_OPTS`

```
export JAVA_OPTS="${JAVA_OPTS} -javaagent:/path/to/easeagent.jar=/path/to/application.conf -Deaseagent.log.conf=/path/to/log4j2.xml"
```

> Use `CATALINA_OPTS` instead when tomcat was used.

# Build from source

```
mvn clean package -am -pl build
```

A generated `./build/target/easeagent-dep.jar` is the java agent jar with all the dependencies.


# Configuration

configurations can be configured by environment, examples:
[Service name as application of easeagent](https://github.com/megaease/spring-petclinic-microservices/blob/master/entrypoint/application.conf#L3)

Examples:
[spring petclinic microservices demo](https://github.com/megaease/spring-petclinic-microservices/blob/master/entrypoint/application.conf)

Some options:
## Enable callstack
```
requests {
  report = ${host.info} {
    callstack = true
  }
  ...
}
```
## Enable zipkin
configure service_name and send_endpoint
```
zipkin.tracer = ${host.info} {
  service_name = ${SERVICE_NAME}
  send_endpoint = "https://gateway.easeapm.com:10443/v1/zipkin_spans"
}
```
