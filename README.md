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
    - [Metric Field](#Metric-Field)
      - [Http Request](#Http-Request)
      - [JDBC Statement](#JDBC-Statement)
      - [JDBC Connection](#JDBC-Connection)
      - [JVM Memory](#JVM-Memory)
      - [JVM GC](#JVM-GC)
      - [Kafka Client](#Kafka-Client)
      - [RabbitMq Producer](#RabbitMq-Producer)
      - [RabbitMq Consumer](#RabbitMq-Consumer)
- [Development Guide](#Development-Guide)  
- [Licenses](#Licenses)


## Overview

### Purpose & Principles
EaseAgent is an APM tool under the Java system, used in a distributed system developed by Java.
It provides cross-service call chain tracking and performance information collection for distributed systems, helps users analyze the overall structure of the distributed system and the call relationships between services and components, thereby improving the efficiency of troubleshooting.

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

EaseAgent use `io.dropwizard.metrics` to collect metric information.
The metric name's rule are as follows:

 Metric | Metric Type| 
---|---|
Timers | 0
Histograms | 1
Meters | 2
Counters | 3
Gauges | 4

 Key Type | Metric Sub Type| Description |
---|---|---|
SUCCESS | 00 | Method invoke d not exception 
ERROR | 01 | Method call has error or http response 500, etc
CHANNEL | 02 | RabbitMq publish message to channel or queue
CONSUMER | 03 | RabbitMq or Kafka consume message
PRODUCER | 04 | Kafka send message to topic
CONSUMER_ERROR | 05 | RabbitMq or Kafka consume message has error
PRODUCER_ERROR | 06 | Kafka send message has error

```
The final key is [Metric Sub Type] + [Metric Type] + subKey
```

For Example:

User send message to Kafka topic `demo-topic`. 
```
subkey = "demo-topic"
The producer's Counters metric key for for success is [04]+[3]+[demo-topic] = 043demo-topic 
The producer's Counters metric key for for error is [06]+[3]+[demo-topic] = 063demo-topic 
```

#### Prometheus Metrics Support
EaseAgent support Prometheus pull metrics info from the following configuration
```
  - job_name: 'user-app'
    static_configs:
    - targets: ['ip:easeagent.server.port']
    metrics_path: "/prometheus/metrics"
```
Please note: Prometheus Label names must match the regex `[a-zA-Z_][a-zA-Z0-9_]*`. The character `-` will be replaced with `_`.

See [Prometheus Metric names and labels rule](https://prometheus.io/docs/concepts/data_model)

#### Metric Field
Metric information output format is JSON

##### Http Request
| Field | Type | Description | Desc2 |
| ----- | ---- | ----------- | ------- |
| url                   |string|the URL of the request          | url |
| cnt       |integer| The total count of the request executed | Topn http request total count|
| m1               |double| The http request executions per second (exponentially-weighted moving average) in last 1 minute | Topn http request M1 rate|
| m5               |double| The http request executions per second (exponentially-weighted moving average) in last 5 minute. | Topn http request M5 rate |
| m15              |double| The http request executions per second (exponentially-weighted moving average) in last 15 minute. | Topn http request M15 rate |
| errcnt |integer| The total error count of the request executed | Topn http request total error count |
| m1err         |integer| The http error request executions per second (exponentially-weighted moving average) in last 1 minute | Topn http request M1 error rate |
| m5err         |integer|| The http error request executions per second (exponentially-weighted moving average) in last 5 minute. | Topn http request M5 error rate |
| m15err        |integer| The http error request executions per second (exponentially-weighted moving average) in last 15 minute | Topn http request M15 error rate |
| m1errpct      |double| error percentage in last 1 minute | Topn http request M1 error percent |
| m5errpct      |double| error percentage in last 5 minute | Topn http request M5 error percent |
| m15errpct     |double| error percentage in last 15 minute | Topn http request M15 error percent |
|min|double|The http-request minimal execution duration in milliseconds.| Topn http request min execution duration |
|max|double|The http-request maximal execution duration in milliseconds.| Topn http request max execution duration |
|mean|double|The http-request mean execution duration in milliseconds.| Topn http request mean execution duration |
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.| Topn http request P25 execution duration |
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.| Topn http request P50 execution duration |
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.| Topn http request P75 execution duration |
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.| Topn http request P95 execution duration |
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.| Topn http request P98 execution duration |
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.| Topn http request P99 execution duration |

##### JDBC Statement
| Field | Type | Description | Desc2 |
| ----- | ---- | ----------- | ------- |
|signature|string|Executed JDBC method signature.| signature |
| cnt | integer |  The total count of JDBC method executed | JDBC total count | 
| m1 | double| The JDBC method executions per second (exponentially-weighted moving average) in last 1 minute. | JDBC M1 rate | 
| m5 | double|  The JDBC method executions per second (exponentially-weighted moving average) in last 5 minutes. | JDBC M5 rate | 
| m15 | double |  The JDBC method executions per second (exponentially-weighted moving average) in last 15 minutes. | JDBC M15 rate |
| m1cnt | integer |  The JDBC method execution count in last 1 minute. | JDBC M1 count |
| m5cnt | integer |  The JDBC method execution count in last 5 minutes. | JDBC M5 count |
| m15cnt | integer |  The JDBC method execution count in last 15 minutes. | JDBC M15 count | 
| m1err         |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 1 minute | Topn JDBC M1 error rate |
| m5err         |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 5 minute. | Topn JDBC M5 error rate | 
| m15err        |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 15 minute | Topn JDBC M15 error rate | 
| min | double | The JDBC method minimal execution duration in milliseconds. | JDBC min execution duration |
| max | double |  The JDBC method maximal execution duration in milliseconds. | JDBC max execution duration | 
| mean | double |  The JDBC method mean execution duration in milliseconds. | JDBC mean execution duration | 
| p25 | double |  TP25: The JDBC method execution duration in milliseconds for 25% user. | JDBC P25 execution duration |
| p50 | double |  TP50: The JDBC method execution duration in milliseconds for 50% user. | JDBC P50 execution duration |
| p75 | double |  TP75: The JDBC method execution duration in milliseconds for 75% user. | JDBC P75 execution duration | 
| p95 | double |  TP95: The JDBC method execution duration in milliseconds for 95% user. | JDBC P95 execution duration |
| p98 | double |  TP98: The JDBC method execution duration in milliseconds for 98% user. | JDBC P98 execution duration |
| p99 | double |  TP99: The JDBC method execution duration in milliseconds for 99% user. | JDBC P99 execution duration |
| p999 | double |  TP99.9: The JDBC method execution duration in milliseconds for 99.9% user. | JDBC P999 execution duration | 

##### JDBC Connection
| Field               |  Type   | Description                                                  | Desc2 | 
| ------------------- |  ------- | ----------------------------------------------------------- | ------- |
| url                 | string  | The total number of database connections                     | url |
| cnt     | integer |  The total number of database connections                     | JDBC Connect total count |
| m1             | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 1 minute. | JDBC Connect M1 rate | 
| m5             | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 5 minutes. | JDBC Connect M5 rate |
| m15            | double  |  The JDBC connection establishment per second (exponentially-weighted moving average) in last 15 minutes. | JDBC Connect M15 rate |
| m1cnt            | integer |  The JDBC connection establishment count in last 1 minute.    | JDBC Connect M1 count |
| m5cnt            | integer |The JDBC connection establishment count in last 5 minutes.   | JDBC Connect M5 count |
| m15cnt           | integer |  The JDBC connection establishment count in last 15 minutes.  | JDBC Connect M15 count |
| m1err         |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 1 minute | JDBC Connect M1 error rate |
| m5err         |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 5 minute. | JDBC Connect M5 error rate |
| m15err        |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 15 minute | JDBC Connect M15 error rate | 
| min  | double  |The JDBC connection minimal establishment duration in milliseconds. | JDBC Connect min execution duration | 
| max  | double  | The JDBC connection maximal establishment duration in milliseconds. | JDBC Connect max execution duration |
| mean | double  | The JDBC connection mean establishment duration in milliseconds. | JDBC Connect mean execution duration |
| p25  | double  | TP25: The JDBC connection establishment duration in milliseconds for 25% user. | JDBC Connect P25 execution duration |
| p50  | double  | TP50: The JDBC connection establishment duration in milliseconds for 50% user. | JDBC Connect P50 execution duration | 
| p75  | double  | TP75: The JDBC connection establishment duration in milliseconds for 75% user. | JDBC Connect P75 execution duration |
| p95  | double  | TP95: The JDBC connection establishment duration in milliseconds for 95% user. | JDBC Connect P95 execution duration | 
| p98  | double  | TP98: The JDBC connection establishment duration in milliseconds for 98% user. | JDBC Connect P98 execution duration |
| p99  | double  | TP99: The JDBC connection establishment duration in milliseconds for 99% user. | JDBC Connect P99 execution duration |
| p999 | double  | TP99.9: The JDBC connection establishment duration in milliseconds for 99.9% user. | JDBC Connect P999 execution duration |

##### JVM Memory
| Field           |  Type   | Description                                                  | Desc2 |
| :-------------- | :-----: | :----------------------------------------------------------- | ------- | 
| bytes-init      | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1). | JVM initial memory |
| bytes-used      | integer | The value represents the amount of memory currently used in bytes unit. | JVM used memory |
| bytes-committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used. | JVM commited memory |
| bytes-max       | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). | JVM max memory |

##### JVM GC
| Field                 |  Type   | Description                                                  | Desc2 | 
| :-------------------- | :-----: | :----------------------------------------------------------- | ------- |
| total_collection_time | integer |The value represents the total time for garbage collection operation in millisecond unit. | JVM Gc time | 
| times                 | integer |  The value represents the total garbage collection times.     | JVM Gc collection times |
| times_rate            | integer |  The number of gc times per second.                           | JVM Gc times per second |


##### Kafka Client
| Field               |  Type   |  Description                                                  | Desc2 | 
| :------------------ | :-----: | :----------------------------------------------------------- | ------- | 
|resource|string|topic name| 主题 |
|prodrm1|double|The executions per second (exponentially-weighted moving average) in last 1 minute (producer)| kafka producer throughput(M1) | 
|prodrm5|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)| kafka producer throughput(M5) | 
|prodrm15|double|The executions per second (exponentially-weighted moving average) in last 15 minute (producer)| kafka producer throughput(M15) | 
|consrm1|double|The executions per second (exponentially-weighted moving average) in last 1 minute (consumer)| kafka consumer throughput(M1) | 
|consrm5|double|The executions per second (exponentially-weighted moving average) in last 5 minute (consumer)| kafka consumer throughput(M5) | 
|consrm15|double|The executions per second (exponentially-weighted moving average) in last 15 minute (consumer)| kafka consumer throughput(M15) | 
|prodrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)| kafka producer error throughput(M1) | 
|prodrm5err|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)| kafka producer error throughput(M5) | 
|prodrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (producer)| kafka producer error throughput(M15) | 
|consrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (consumer)| kafka consumer error throughput(M1) | 
|consrm5err|double|The error executions per second (exponentially-weighted moving average) in last 5 minute (consumer)| kafka consumer error throughput(M5) | 
|consrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (consumer)| kafka consumer error throughput(M15) | 
|prodrmin|double|The minimal execution duration in milliseconds.|  producer min execution duration | 
|prodrmax|double|The maximal execution duration in milliseconds.|  producer max execution duration | 
|prodrmean|double|The mean execution duration in milliseconds.|  producer mean execution duration | 
|prodrp25|double|TP25: The execution duration in milliseconds for 25% user.|  producer P25 execution duration | 
|prodrp50|double|TP50: The execution duration in milliseconds for 50% user.|  producer P50 execution duration | 
|prodrp75|double|TP75: The execution duration in milliseconds for 75% user.|  producer P75 execution duration | 
|prodrp95|double|TP95: The execution duration in milliseconds for 95% user.|  producer P95 execution duration | 
|prodrp98|double|TP98: The execution duration in milliseconds for 98% user.|  producer P98 execution duration | 
|prodrp99|double|TP99: The execution duration in milliseconds for 99% user.|  producer P99 execution duration | 
|prodrp999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|  producer P999 execution duration |
|consrmin|double|The minimal execution duration in milliseconds.|  consumer min execution duration |
|consrmax|double|The maximal execution duration in milliseconds.|  consumer max execution duration |
|consrmean|double|The mean execution duration in milliseconds.|  consumer mean execution duration |
|consrp25|double|TP25: The execution duration in milliseconds for 25% user.|  consumer P25 execution duration |
|consrp50|double|TP50: The execution duration in milliseconds for 50% user.|  consumer P50 execution duration | 
|consrp75|double|TP75: The execution duration in milliseconds for 75% user.|  consumer P75 execution duration |
|consrp95|double|TP95: The execution duration in milliseconds for 95% user.|  consumer P95 execution duration |
|consrp98|double|TP98: The execution duration in milliseconds for 98% user.|  consumer P98 execution duration |
|consrp99|double|TP99: The execution duration in milliseconds for 99% user.|  consumer P99 execution duration |
|consrp999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|  consumer P999 execution duration |

##### RabbitMq Producer
| Field               |  Type   | Description                                                  | Desc2 | 
| :------------------ | :-----: | :----------------------------------------------------------- | ------- | 
|resource|string|rabbitmq exchange or routingkey| rabbit exchange |
|prodrm1|double|The executions of producer per second (exponentially-weighted moving average) in last 1 minute |  producer M1 rate |
|prodrm5|double|The executions of producer per second (exponentially-weighted moving average) in last 5 minute |  producer M5 rate | 
|prodrm15|double|The executionsof producer per second (exponentially-weighted moving average) in last 15 minute |  producer M15 rate |
|prodrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)|  producer M1 error rate |
|prodrm5err|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)|  producer M5 error rate |
|prodrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (producer)|  producer M15 error rate | 
|min|double|The http-request minimal execution duration in milliseconds.|  producer min execution durationTopN |
|max|double|The http-request maximal execution duration in milliseconds.|  producer max execution durationTopN |
|mean|double|The http-request mean execution duration in milliseconds.|  producer mean execution durationTopN | 
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.|  producer P25 execution duration |
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.|  producer P50 execution duration |
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.|  producer P75 execution duration |
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.|  producer P95 execution duration |
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.|  producer P98 execution duration | 
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.|  producer P99 execution duration |

##### RabbitMq Consumer
| Field               |  Type   | Description                                                  | Desc2 |
| :------------------ | :-----: | :----------------------------------------------------------- | ------- |
|resource|string|rabbitmq queue| rabbit queue |
|querym1|double|The executions of queue per second (exponentially-weighted moving average) in last 1 minute |  consumer M1 rate |
|querym5|double|The executions of queue per second (exponentially-weighted moving average) in last 5 minute |  consumer M5 rate |
|querym5|double|The executionsof queue per second (exponentially-weighted moving average) in last 15 minute |  consumer M15 rate |
|querym1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)|  consumer M1 error rate |
|querym5err|double|The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)|  consumer M5 error rate |
|querym5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (queue)|  consumer M15 error rate |
|min|double|The http-request minimal execution duration in milliseconds.|  consumer min execution duration |
|max|double|The http-request maximal execution duration in milliseconds.|  consumer max execution duration |
|mean|double|The http-request mean execution duration in milliseconds.|  consumer mean execution duration |
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.|  consumer P25 execution duration |
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.|  consumer P50 execution duration |
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.|  consumer P75 execution duration | 
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.|  consumer P95 execution duration |
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.|  consumer P98 execution duration |
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.|  consumer P99 execution duration |

## Development-Guide
The main function of EaseAgent is to collect Java method call trace and metrics information.
You need to understand trace and metric before development.
* [metrics](https://github.com/dropwizard/metrics)
* [brave](https://github.com/openzipkin/brave)

EaseAgent use [ByteBuddy](https://github.com/raphw/byte-buddy) to build agent core advice code.

After you understand the above information, you can follow the steps below to develop.

## Example for Apache HttpClient4.5
User want to get tracing information from `Apache HttpClient`.

### Step 1
Add `Apache HttpClient` dependency in `/zipkin/pom.xml`
```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.3</version>
</dependency>
```

### Step 2
Create `XXXAdvice` class in module `sniffer`
```java
package com.megaease.easeagent.sniffer.httpclient.v4_5.advice;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class HttpClientAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.apache.http.client.HttpClient"))) // enhanced client class
                .transform(adviceExecute(named("execute"))) // "execute" is the name of HttpClient
                .end();
    }

    /**
     * This method is will be implements by sub class that generated by EaseAgent
     */
    @AdviceTo(Execute.class)
    protected abstract Definition.Transformer adviceExecute(ElementMatcher<? super MethodDescription> matcher);

    public static class Execute extends AbstractAdvice {

        /**
         * @param supplier     This argument will be injected by com.megaease.easeagent.sniffer.Provider.
         * @param chainInvoker This argument will be injected by com.megaease.easeagent.sniffer.Provider
         */
        @Injection.Autowire
        public Execute(
                @Injection.Qualifier("supplier4HttpClient") Supplier<AgentInterceptorChain.Builder> supplier,
                AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
        }

        /**
         * Refer to net.bytebuddy.asm.Advice
         */
        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        /**
         * Refer to net.bytebuddy.asm.Advice
         */
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.Origin Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
//                  @Advice.Return Object retValue,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);

//            if user want return new result, user should use method:
//            this.doExit(release, Advice, method, args, retValue, throwable)
        }
    }
}

```

### Step 3
Create `AgentInterceptor` in module `zipkin`
```java
package com.megaease.easeagent.zipkin.http.httpclient.v4_5;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

/**
 * About tracing and metric, User can refer to com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor
 */
public class HttpClientTracingInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        // process tracing or metric for method before

        // finally, user can invoke next interceptor
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        // process tracing or metric for method after
        
        // finally, user can invoke next interceptor
        return AgentInterceptor.super.after(methodInfo, context, chain);
    }
}
```
Refer to `FeignClientTracingInterceptor` and other code

### Step 4
Add `HttpClientTracingInterceptor` to `com.megaease.easeagent.sniffer.Provider`
```java
    @Injection.Bean("supplier4HttpClient")
    public Supplier<AgentInterceptorChain.Builder> getSupplier4HttpClient() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new HttpClientTracingInterceptor());
    }
```
### Step 5
Add `HttpClientAdvice.class` to `com.megaease.easeagent.Easeagent`
```java
@Assembly({
        ... other advice class,
        HttpClientAdvice.class,

})
```
### Step 6 For `Junit Test`
Add `HttpClientAdvice.class` to `com.megaease.easeagent.sniffer.GeneratedTest` like Step 4
Users can test whether the method interception works

### Step 7 For `Junit Test`
Add HttpClientTracingInterceptorTest for test.
Refer to `FeignClientTracingInterceptorTest` and other test code

### Step 8
Build and run user application for test
```
mvn clean package -am -pl build
```


## Licenses
EaseAgent is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.
