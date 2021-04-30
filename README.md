- [Overview](#Overview)
  - [Purpose & Principles](#Purpose-&-Principles)
  - [Architecture Diagram](#Architecture-Diagram)
- [Features](#Features)
- [QuickStart](#QuickStart)
  - [Download](#Download)
  - [Build From Source](#Build-From-Source)
- [Documentation](#Documentation)
  - [Agent Configuration](#Agent-Configuration)
  - [Health Check Endpoint](#Health-Check-Endpoint)
  - [Readiness Check Endpoint](#Readiness-Check-Endpoint)
  - [Tracing](#Tracing)
  - [Metrics](#Metrics)
    - [Prometheus Metrics Support](#Prometheus-Metrics-Support)
- [Development Guide](#Development-Guide)  
- [Licenses](#Licenses)


## Overview

### Purpose
EaseAgent is an APM tool under the Java system, used in a distributed system developed by Java.
It provides cross-service call chain tracking and performance information collection for distributed systems, helps users analyze the overall structure of the distributed system and the call relationships between services and components, thereby improving the efficiency of troubleshooting.

### Principles
- No invasion
- Service based view
- High performance

### Architecture Diagram
![image](./doc/images/EaseAgent-Architecture.jpg)

## Features

* Collect Metric and Tracing information
    * `JDBC`4.0 SQL
    * `Http Servlet`、`Http Filter`
    * Spring Boot 2.2.x: `WebClient` 、 `RestTemplate`、`FeignClient` 
    * `RabbitMQ Client` 5.x、 `Kafka Client` 2.4.x
    * `Jedis` 3.5.x、 `Lettuce` 5.3.x
* Collect Access Log
    * `Http Servlet`、`Http Filter`
    * `Spring Cloud Gateway`
* Support `traceId` and `spanId` inject into user application
* Support `health check` endpoint
* Support `readiness check` endpoint for `SpringBoot2.2.x`

## QuickStart

### Download
Download `easeagent.jar` from releases [releases](https://github.com/megaease/easeagent/releases)

or

```
curl -Lk https://github.com/megaease/easeagent/releases/download/{tag}/easeagent.jar -O
```

### Build From Source
Download EaseAgent with `git clone https://github.com/megaease/easeagent.git`
```
cd easeagent
mvn clean package -am -pl build
```
A generated `./build/target/easeagent-dep.jar` is the agent jar with all the dependencies.

### Step 1
Extract default configuration files
```
jar xf easeagent.jar agent.properties log4j2.xml
```

### Step 2
* Modify service name, default configuration is unknown-service
```
name=[app-name]
```
* Modify kafka server config, default configuration is `127.0.0.1:9092`
```
observability.outputServer.bootstrapServer = [ip:port]
```
* Modify output configuration, if you want to see log information in console
```
# metric output
observability.metrics.[xxxx].appendType=console

# tracings output
observability.tracings.output.enabled=false
```

### Step 3
Clone demo source code and build
```
git clone https://github.com/akwei/spring-petclinic-microservices.git
cd spring-petclinic-microservices
mvn -DskipTests=true package
```

### Step 4
Run java application with agent in console
```
java -jar spring-petclinic-config-server/target/spring-petclinic-config-server-2.4.2.jar

java -jar spring-petclinic-discovery-server/target/spring-petclinic-discovery-server-2.4.2.jar

java -javaagent:/[user path]/easeagent.jar=/{path}/agent.properties -Deaseagent.server.enabled=false -jar spring-petclinic-vets-service/target/spring-petclinic-vets-service-2.4.2.jar

java -javaagent:/[user path]/easeagent.jar=/{path}/agent.properties -Deaseagent.server.enabled=false -jar spring-petclinic-visits-service/target/spring-petclinic-visits-service-2.4.2.jar

java -javaagent:/[user path]/easeagent.jar=/{path}/agent.properties -Deaseagent.server.enabled=false -jar spring-petclinic-customers-service/target/spring-petclinic-customers-service-2.4.2.jar

java -javaagent:/[user path]/easeagent.jar=/{path}/agent.properties -Deaseagent.server.enabled=false -jar spring-petclinic-api-gateway/target/spring-petclinic-api-gateway-2.4.2.jar
```

### Step 5
Tracing and Metric information can be shown in console  

## Documentation

### Agent Configuration

### Health Check Endpoint
User can use the following URL to support health check and liveness check
```
http://[ip]:[easeagent.server.port]/health
```

### Readiness Check Endpoint
User can use the following URL to support health check in SpringBoot 2.2.x
```
http://[ip]:[easeagent.server.port]/health/readiness
```

### Tracing
EaseAgent use [brave](https://github.com/openzipkin/brave) to collect tracing information.The data format stored in `Kafka`  is [Zipkin Data Model](https://zipkin.io/pages/data_model.html). User can send tracing information to [Zipkin server](https://zipkin.io/pages/quickstart.html).

### Metrics
EaseAgent use [io.dropwizard.metrics](https://github.com/dropwizard/metrics) to collect metric information.

For more metric information, please refer to the [User Manual](./doc/user-manual.md)

## Development-Guide
Refer to [Development-Guide](./doc/development-guide.md)

## Licenses
EaseAgent is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.
