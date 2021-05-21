  
# User Manual

- [User Manual](#user-manual)
  - [Agent.properties](#agentproperties)
    - [Getting the configuration file](#getting-the-configuration-file)
    - [Internal HTTP Server](#internal-http-server)
    - [Metric](#metric)
    - [Kafka](#kafka)
    - [HTTP Reqeust Metric](#http-reqeust-metric)
    - [JDBC SQL Metric](#jdbc-sql-metric)
    - [JDBC Connection Metric](#jdbc-connection-metric)
    - [RabbitMQ Metric](#rabbitmq-metric)
    - [Kafka Metric](#kafka-metric)
    - [Redis Metric](#redis-metric)
    - [JVM GC Metric](#jvm-gc-metric)
    - [JVM Memory Metric](#jvm-memory-metric)
    - [SQL MD5Dictionary](#sql-md5dictionary)
    - [Tracing](#tracing)
    - [Logging](#logging)
  - [Prometheus Support](#prometheus-support)
  - [Health Check and Readiness Check Endpoint](#health-check-and-readiness-check-endpoint)
  - [Tracing](#tracing-1)
    - [Tracing Component](#tracing-component)
    - [Custom Span Tag](#custom-span-tag)
      - [JDBC](#jdbc)
      - [Cache](#cache)
      - [RabbitMQ Producer And Consumer](#rabbitmq-producer-and-consumer)
      - [Kafka Producer And Consumer](#kafka-producer-and-consumer)
  - [Metric](#metric-1)
    - [Metric Field](#metric-field)
      - [HTTP Request](#http-request)
      - [JDBC Statement](#jdbc-statement)
      - [JDBC Connection](#jdbc-connection)
      - [JVM Memory](#jvm-memory)
      - [JVM GC](#jvm-gc)
      - [Kafka Client](#kafka-client)
      - [RabbitMQ Producer](#rabbitmq-producer)
      - [RabbitMQ Consumer](#rabbitmq-consumer)
  
## Agent.properties
EaseAgent provides dedicated parameters for controlling metrics and tracing collection behavior via agent.properties. These parameters include:
* Data reporting frequency
* Data reporting output type
* Kafka topic of data reporting
* Data collecting and reporting switch
* Queue depth in process for high throughput


### Getting the configuration file
You may extract default configuration from the JAR file or create new properties from a blank file.
```
$ jar xf easeagent.jar agent.properties log4j2.xml
```
Run the user application with EaseAgent
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ java "-javaagent:${EASE_AGENT_PATH}/easeagent.jar=${EASE_AGENT_PATH}/agent.properties" -jar user-app.jar
```

Users can customize the following parameters

### Internal HTTP Server
EaseAgent opens port `9900` by default to receive configuration change notifications and Prometheus requests.

Key| Default Value | Description |
---| ---| ---|
`easeagent.server.enabled` | true | Enable Internal HTTP Server. `false` can disable it. EaseAgent will no longer accept any HTTP requests (`Prometheus`、`Health Check`、`Readiness Check`) when the Internal HTTP Server is disabled. User can add VM parameter:`-Deaseagent.server.enabled=[true or false]` to override.|
`easeagent.server.port` | 9900 | Internal HTTP Server port. User can add VM parameter:`-Deaseagent.server.port=[new port]` to override. |

### Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.enabled` | true | Enable all metrics collection. `false`: Disable all metrics collection |

### Kafka
Tracing and metric data will be output to kafka server.
Key| Default Value | Description |
---| ---| ---|
`observability.outputServer.bootstrapServer`| 127.0.0.1:9092 |Kafka server host and port. Tracing and metric data will be output to kafka. |
`observability.outputServer.timeout`| 10000 | Connect timeout. Time Unit: millisecond. |

### HTTP Reqeust Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.request.enabled` | true | Enable collecting `Servlet` or `Filter` metric data. `false`: Disable collecting. |
`observability.metrics.request.interval`| 30 | Time interval between two outputs. Time Unit: second. | 
`observability.metrics.request.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.request.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JDBC SQL Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jdbcStatement.enabled` | true | Enable collecting metric data of `JDBC SQL`. `false`: Disable collecting. |
`observability.metrics.jdbcStatement.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jdbcStatement.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jdbcStatement.appendType` | kafka  | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JDBC Connection Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jdbcConnection.enabled` | true | Enable collecting metric data of `JDBC Connection`. `false`: Disable collecting. |
`observability.metrics.jdbcConnection.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jdbcConnection.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jdbcConnection.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### RabbitMQ Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.rabbit.enabled` | true |  Enable collecting metric data of `RabbitMQ producer and consumer`. `false`: Disable collecting. |
`observability.metrics.rabbit.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.rabbit.topic` | platform-meter | Send metric data to the specified kafka topic. |
`observability.metrics.rabbit.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Kafka Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.kafka.enabled` | true | Enable collection metric data of `Kafka producer and consumer`. `false`: Disable collecting. |
`observability.metrics.kafka.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.kafka.topic` | platform-meter | Send metric data to the specified kafka topic. |
`observability.metrics.kafka.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Redis Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.redis.enabled` | true | Enable collection metric data of `Redis`.  `false`: Disable collecting. |
`observability.metrics.redis.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.redis.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.redis.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JVM GC Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jvmGc.enabled` | true | Enable collection metric data of `JVM GC`.  `false`: Disable collecting. |
`observability.metrics.jvmGc.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jvmGc.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jvmGc.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JVM Memory Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jvmMemory.enabled` | true | Enable collection metric data of `JVM GC`.  `false`: Disable collecting. |
`observability.metrics.jvmMemory.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jvmMemory.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jvmMemory.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### SQL MD5Dictionary 
When EaseAgent is used with EaseMesh, tracing and metric data will be stored in Elasticsearch. In order to reduce the space occupied by SQL in Elasticsearch, EaseAgent uses md5 to reduce the length of SQL, and then periodically stores it in Kafka, and finally stores it in Elasticsearch. Only one copy of sql will be stored in Elasticsearch.


Key| Default Value | Description |
---| ---| ---|
`observability.metrics.md5Dictionary.enabled` | true | Enable collection of `JVM GC` metric data.  `false`: Disable collecting.|
`observability.metrics.md5Dictionary.interval` | 300 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.md5Dictionary.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.md5Dictionary.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Tracing
Key| Default Value | Description |
---| ---| ---|
`observability.tracings.enabled` | true | Enable all collection of tracing logs. `false`: Disable all collection of tracing logs. |
`observability.tracings.output.enabled` | true | Enable output tracing logs to Kafka. |
`observability.tracings.output.topic` | log-tracing | Send tracing logs to the specified kafka topic. |
`observability.tracings.output.messageMaxBytes` | 999900 | Maximum size of a message. |
`observability.tracings.output.reportThread` | 1 | The number of thread which will send  tracing logs to kafka. |
`observability.tracings.output.queuedMaxSpans` | 1000 | The maximum number of spans to be processed in the queue. |
`observability.tracings.output.queuedMaxSize` | 1000000 | The maximum bytes of spans to be processed in the queue. |
`observability.tracings.output.messageTimeout` | 1000 |  |
`observability.tracings.request.enabled` | true | Enable collection of tracing logs(`Servlet`、 `Filter`). `false`: Disable collecting. |
`observability.tracings.remoteInvoke.enabled` | true | Enable collection of tracing logs(`RestTemplate`、 `FeignClient`、`WebClient`). `false`: Disable collecting. |
`observability.tracings.kafka.enabled`| true | Enable collection of `kafka` tracing logs. `false`: Disable collecting. |
`observability.tracings.jdbc.enabled` | true | Enable collection of `JDBC` tracing logs. `false`: Disable collecting. |
`observability.tracings.redis.enabled` | true | Enable collection of tracing logs(`Jedis`、`Lettuce`). `false`: Disable collecting. |
`observability.tracings.rabbit.enabled` | true | Enable collection of `RabbitMQ` tracing logs. `false`: Disable collecting. |

### Logging
EaseAgent use `Log4j2` for all internal logging, the default log level is `INFO`, and the logs will be outputted to the `Console`. User can modify the log level and appender in the `log4j2.xml` file.
 
After modification, User can run the application with EaseAgent.
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ java "-javaagent:${EASE_AGENT_PATH}/easeagent.jar -Deaseagent.log.conf=${EASE_AGENT_PATH}/log4j2.xml" -jar user-app.jar
```

## Prometheus Support
When Internal HTTP Server is enabled, User can use Prometheus to collect metrics information.
* Adding the following configuration in `prometheus.yml`
```
  - job_name: 'user-app'
    static_configs:
    - targets: ['localhost:9900']
    metrics_path: "/prometheus/metrics"
```

## Health Check and Readiness Check Endpoint
EaseAgent supply the `health check`、`readiness check` endpoint.

* `Health Check` Endpoint
```
[GET] http://[ip]:[easeagent.server.port]/health
The response status will be 200(OK)
```

* `Readiness Check` Endpoint
After Spring sending `ApplicationReadyEvent`, EaseAgent will change readiness status to `true`
```
[GET] http://[ip]:[easeagent.server.port]/health/readiness
The response status will be 200(OK)
```

## Tracing
EaseAgent use [brave](https://github.com/openzipkin/brave) to collect tracing logs.The data format stored in `Kafka`  is [Zipkin Data Model](https://zipkin.io/pages/data_model.html). User can send tracing logs to [Zipkin server](https://zipkin.io/pages/quickstart.html).

### Tracing Component
Component Type | Component | Reference | 
--- | ---| --- |
HTTP Client | `RestTemplate`、 `WebClient`、 `FeignClient` | [brave-instrumentation-http](https://github.com/openzipkin/brave/tree/master/instrumentation/http)
HTTP Server | `Servlet`、`Filter` | [brave-instrumentation-http](https://github.com/openzipkin/brave/tree/master/instrumentation/http)
DataBase | `JDBC` | [Brave](https://github.com/openzipkin/brave/tree/master/brave)
Cache | `Jedis`、`Lettuce` | [Brave](https://github.com/openzipkin/brave/tree/master/brave)
Message | `RabbitMQ`、`Kafka` | [brave-instrumentation-messaging](https://github.com/openzipkin/brave/tree/master/instrumentation/messaging) 、[Brave Kafka instrumentation](https://github.com/openzipkin/brave/tree/master/instrumentation/kafka-clients)
Logging | `Log4j2`、`Logback` | [brave-context-log4j2](https://github.com/openzipkin/brave/tree/master/context/log4j2) 、[brave-context-slf4j](https://github.com/openzipkin/brave/tree/master/context/slf4j)

### Custom Span Tag

#### JDBC
Tag | Description |
--- | ---|
sql | Sql text in user application |
local-component | Default value = 'database' |
url | Connection information. Example: `jdbc:mysql://localhost:3306/db_demo` |
error | SQLException information |

#### Cache
Tag | Description |
--- | ---|
redis.method | Redis command. Example: `MGET`、`GET` |

#### RabbitMQ Producer And Consumer
Tag | Description |
--- | ---|
rabbit.exchange | RabbitMQ exchange |
rabbit.routing_key | RabbitMQ routingKey |
rabbit.queue | RabbitMQ routingKey |

#### Kafka Producer And Consumer
Tag | Description |
--- | ---|
kafka.key | Kafka consumer record Key |
kafka.topic | Kafka topic |
kafka.broker | Kafka url |

## Metric
EaseAgent use [io.dropwizard.metrics](https://github.com/dropwizard/metrics) to collect metric information.


### Metric Field
EaseAgent output metric data to kafka. The data stored in kafka is in JSON format.

For Example: EaseAgent collect metric of HTTP Request. The collected metric data are as follows:
```json
{
  "m15err" : 0,
  "m5err" : 0,
  "cnt" : 1,
  "url" : "GET \/",
  "m5" : 0.050990000000000001,
  "max" : 823,
  "mean" : 823,
  "p98" : 823,
  "errcnt" : 0,
  "host_name" : "akwei",
  "min" : 823,
  "category" : "application",
  "system" : "none",
  "type" : "http-request",
  "mean_rate" : 0,
  "p99" : 823,
  "p95" : 823,
  "m15" : 0.12681999999999999,
  "timestamp" : 1621567320892,
  "service" : "unknown-service",
  "m1" : 0.00022000000000000001,
  "m5errpct" : 0,
  "p25" : 823,
  "p75" : 823,
  "p50" : 823,
  "host_ipv4" : "192.168.2.5",
  "m1errpct" : 0,
  "m15errpct" : 0,
  "m1err" : 0,
  "p999" : 823
}
```

For different kind of metrics, we have different schemas:

#### HTTP Request
HTTP Request schema describes key metrics of service APIs, which include:
* Total execution count (cnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Error throughput percentage (m1errpct, m5errpct, m15errpct)
* Latency (p25, p50, p75, p95, p98, p99)
* Execution duration (min, mean, max)

| Field | Type | Description | 
| :------------------ | :-----: | :----- | 
| url                   |string|the URL of the request|
| cnt       |integer| The total count of the request executed |
| m1               |double| The HTTP request executions per second (exponentially-weighted moving average) in last 1 minute |
| m5               |double| The HTTP request executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15              |double| The HTTP request executions per second (exponentially-weighted moving average) in last 15 minute. |
| errcnt |integer| The total error count of the request executed |
| m1err         |integer| The HTTP error request executions per second (exponentially-weighted moving average) in last 1 minute |
| m5err         |integer| The HTTP error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err        |integer| The HTTP error request executions per second (exponentially-weighted moving average) in last 15 minute |
| m1errpct      |double| error percentage in last 1 minute |
| m5errpct      |double| error percentage in last 5 minute |
| m15errpct     |double| error percentage in last 15 minute |
|min|double|The http-request minimal execution duration in milliseconds.|
|max|double|The http-request maximal execution duration in milliseconds.|
|mean|double|The http-request mean execution duration in milliseconds.|
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.|
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.|
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.|
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.|
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.|
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.|

#### JDBC Statement
JDBC Statement schema describes key metrics of JDBC SQL Statement, which include:
* Execution count (cnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Field | Type | Description |
| :------------------ | :-----: | :----- |
|signature|string|Executed JDBC method signature.|
| cnt | integer |  The total count of JDBC method executed |
| m1 | double| The JDBC method executions per second (exponentially-weighted moving average) in last 1 minute. |
| m5 | double|  The JDBC method executions per second (exponentially-weighted moving average) in last 5 minutes. |
| m15 | double |  The JDBC method executions per second (exponentially-weighted moving average) in last 15 minutes. |
| m1err         |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 1 minute |
| m5err         |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err        |double| The JDBC method error executions per second (exponentially-weighted moving average) in last 15 minute |
| min | double | The JDBC method minimal execution duration in milliseconds. |
| max | double |  The JDBC method maximal execution duration in milliseconds. |
| mean | double |  The JDBC method mean execution duration in milliseconds. |
| p25 | double |  TP25: The JDBC method execution duration in milliseconds for 25% user. |
| p50 | double |  TP50: The JDBC method execution duration in milliseconds for 50% user. |
| p75 | double |  TP75: The JDBC method execution duration in milliseconds for 75% user. | 
| p95 | double |  TP95: The JDBC method execution duration in milliseconds for 95% user. | 
| p98 | double |  TP98: The JDBC method execution duration in milliseconds for 98% user. |
| p99 | double |  TP99: The JDBC method execution duration in milliseconds for 99% user. |
| p999 | double |  TP99.9: The JDBC method execution duration in milliseconds for 99.9% user. |

#### JDBC Connection
JDBC Connection schema describes key metrics of Getting Connection, which include:
* Execution count (cnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Field | Type | Description |
| :------------------ | :-----: | :----- |
| url                 | string  | The total number of database connections                     |
| cnt     | integer |  The total number of database connections|
| m1             | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 1 minute. |
| m5             | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 5 minutes. |
| m15            | double  |  The JDBC connection establishment per second (exponentially-weighted moving average) in last 15 minutes. |
| m1err         |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 1 minute |
| m5err         |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err        |double| The JDBC connection error executions per second (exponentially-weighted moving average) in last 15 minute |
| min  | double  |The JDBC connection minimal establishment duration in milliseconds. | 
| max  | double  | The JDBC connection maximal establishment duration in milliseconds. |
| mean | double  | The JDBC connection mean establishment duration in milliseconds. |
| p25  | double  | TP25: The JDBC connection establishment duration in milliseconds for 25% user. |
| p50  | double  | TP50: The JDBC connection establishment duration in milliseconds for 50% user. |
| p75  | double  | TP75: The JDBC connection establishment duration in milliseconds for 75% user. |
| p95  | double  | TP95: The JDBC connection establishment duration in milliseconds for 95% user. |
| p98  | double  | TP98: The JDBC connection establishment duration in milliseconds for 98% user. |
| p99  | double  | TP99: The JDBC connection establishment duration in milliseconds for 99% user. |
| p999 | double  | TP99.9: The JDBC connection establishment duration in milliseconds for 99.9% user. |

#### JVM Memory
JVM Memory schema describes key metrics of Java memory usage, which include:
* bytes-init
* bytes-used 
* bytes-committed 
* bytes-max


| Field | Type | Description |
| :------------------ | :-----: | :----- |
| bytes-init | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1). |
| bytes-used | integer | The value represents the amount of memory currently used in bytes unit. |
| bytes-committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used. |
| bytes-max | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). |

#### JVM GC
JVM GC schema describes key metrics of JVM garbage collection, which include:
* total_collection_time
* times
* times_rate

| Field | Type | Description |
| :------------------ | :-----: | :----- |
| total_collection_time | integer |The value represents the total time for garbage collection operation in millisecond unit. |
| times                 | integer |  The value represents the total garbage collection times. |
| times_rate            | integer |  The number of gc times per second.                           |

#### Kafka Client
Kafka Client schema describes key metrics of Kafka client invoking, which include:
* Producer
  * Throughput (prodrm1, prodrm5, prodrm15)
  * Error throughput (prodrm1err, prodrm5err, prodrm15err)
  * Execution duration (prodrmin, prodrmean, prodrmax)
  * Latency (prodrp25, prodrp50, prodrp75, prodrp95, prodrp98, prodrp99, prodrp999)
* Consumer
  * Throughput (consrm1, consrm5, consrm15)
  * Error throughput (consrm1err, consrm5err, consrm15err)
  * Execution duration (consrmin, consrmean, consrmax)
  * Latency (consrp25, consrp50, consrp75, consrp95, consrp98, consrp99, consrp999)

| Field               |  Type   |  Description |
| :------------------ | :-----: | :----- |
|resource|string|topic name|
|prodrm1|double|The executions per second (exponentially-weighted moving average) in last 1 minute (producer)|
|prodrm5|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)|
|prodrm15|double|The executions per second (exponentially-weighted moving average) in last 15 minute (producer)|
|consrm1|double|The executions per second (exponentially-weighted moving average) in last 1 minute (consumer)|
|consrm5|double|The executions per second (exponentially-weighted moving average) in last 5 minute (consumer)|
|consrm15|double|The executions per second (exponentially-weighted moving average) in last 15 minute (consumer)|
|prodrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)|
|prodrm5err|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)|
|prodrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (producer)|
|consrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (consumer)| 
|consrm5err|double|The error executions per second (exponentially-weighted moving average) in last 5 minute (consumer)|
|consrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (consumer)|
|prodrmin|double|The minimal execution duration in milliseconds.|
|prodrmax|double|The maximal execution duration in milliseconds.|
|prodrmean|double|The mean execution duration in milliseconds.|
|prodrp25|double|TP25: The execution duration in milliseconds for 25% user.|
|prodrp50|double|TP50: The execution duration in milliseconds for 50% user.|
|prodrp75|double|TP75: The execution duration in milliseconds for 75% user.|
|prodrp95|double|TP95: The execution duration in milliseconds for 95% user.|
|prodrp98|double|TP98: The execution duration in milliseconds for 98% user.|
|prodrp99|double|TP99: The execution duration in milliseconds for 99% user.|
|prodrp999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|
|consrmin|double|The minimal execution duration in milliseconds.|
|consrmax|double|The maximal execution duration in milliseconds.|
|consrmean|double|The mean execution duration in milliseconds.| 
|consrp25|double|TP25: The execution duration in milliseconds for 25% user.|
|consrp50|double|TP50: The execution duration in milliseconds for 50% user.|
|consrp75|double|TP75: The execution duration in milliseconds for 75% user.|
|consrp95|double|TP95: The execution duration in milliseconds for 95% user.|
|consrp98|double|TP98: The execution duration in milliseconds for 98% user.|
|consrp99|double|TP99: The execution duration in milliseconds for 99% user.|
|consrp999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|

#### RabbitMQ Producer
RabbitMQ Producer schema describes key metrics of RabbitMQ client publishing message, which include:
* Throughput (prodrm1, prodrm5, prodrm15)
* Error throughput (prodrm1err, prodrm5err, prodrm15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field               |  Type   | Description |
| :------------------ | :-----: | :---- |
|resource|string|rabbitmq exchange or routingkey|
|prodrm1|double|The executions of producer per second (exponentially-weighted moving average) in last 1 minute |
|prodrm5|double|The executions of producer per second (exponentially-weighted moving average) in last 5 minute |
|prodrm15|double|The executionsof producer per second (exponentially-weighted moving average) in last 15 minute |
|prodrm1err|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)|
|prodrm5err|double|The executions per second (exponentially-weighted moving average) in last 5 minute (producer)|
|prodrm5err|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (producer)|
|min|double|The http-request minimal execution duration in milliseconds.|
|max|double|The http-request maximal execution duration in milliseconds.|
|mean|double|The http-request mean execution duration in milliseconds.| 
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.|
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.|
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.|
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.|
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.| 
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.|
|p999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|

#### RabbitMQ Consumer
RabbitMQ Consumer schema describes key metrics of RabbitMQ client consuming message, which include:
* Throughput (queue_m1_rate, queue_m5_rate, queue_m15_rate)
* Error throughput (queue_m1_error_rate, queue_m5_error_rate, queue_m15_error_rate)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field               |  Type   | Description |
| :------------------ | :-----: | :---------- |
|resource|string|rabbitmq queue| rabbit queue |
|queue_m1_rate|double|The executions of queue per second (exponentially-weighted moving average) in last 1 minute |
|queue_m5_rate|double|The executions of queue per second (exponentially-weighted moving average) in last 5 minute |
|queue_m15_rate|double|The executionsof queue per second (exponentially-weighted moving average) in last 15 minute |
|queue_m1_error_rate|double|The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)|
|queue_m5_error_rate|double|The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)|
|queue_m15_error_rate|double|The error executions per second (exponentially-weighted moving average) in last 15 minute (queue)|
|min|double|The http-request minimal execution duration in milliseconds.|
|max|double|The http-request maximal execution duration in milliseconds.|
|mean|double|The http-request mean execution duration in milliseconds.|
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.|
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.|
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.|
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.|
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.|
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.|
|p999|double|TP99.9: The execution duration in milliseconds for 99.9% user.|