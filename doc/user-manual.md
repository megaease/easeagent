  
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
    - [Metric Key](#metric-key)
      - [Type And SubType Detail](#type-and-subtype-detail)
    - [Metric Field](#metric-field)
      - [HTTP Request](#http-request)
      - [JDBC Statement](#jdbc-statement)
      - [JDBC Connection](#jdbc-connection)
      - [JVM Memory](#jvm-memory)
      - [JVM GC](#jvm-gc)
      - [Kafka Client](#kafka-client)
      - [RabbitMq Producer](#rabbitmq-producer)
      - [RabbitMq Consumer](#rabbitmq-consumer)
  
## Agent.properties
`EaseAgent` uses the `report` module to control the tracing and metric collection behavior of each component. `agent.properties` is configured with various parameters of `report`. Changing these parameters can change the collection behavior . These parameters include: collection frequency, target queue, switch and other settings. Users can customize parameter values.

### Getting the configuration file
Extracting the default configuration file
```
$ jar xf easeagent.jar agent.properties log4j2.xml
```
Run the user application with EaseAgent
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ java "-javaagent:${EASE_AGENT_PATH}/easeagent.jar=${EASE_AGENT_PATH}/agent.properties" -jar user-app.jar
```

### Internal HTTP Server
EaseAgent opens port `9900` by default to receive configuration change notifications and Prometheus requests.

Key| Default Value | Description |
---| ---| ---|
`easeagent.server.enabled` | true | Enable Internal HTTP Server. `False` can disable it. EaseAgent will no longer accept any HTTP requests (`Prometheus`、`Health Check`、`Readiness Check`) when the Internal HTTP Server is disabled. User can add VM parameter:`-Deaseagent.server.enabled=[true or false]` to override.|
`easeagent.server.port` | 9900 | Internal HTTP Server port. User can add VM parameter:`-Deaseagent.server.port=[new port]` to override. |

### Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.enabled` | true | Enable all metrics collection. `False`: Disable all metrics collection |

### Kafka
Tracing and metric data will output to kafka server.
Key| Default Value | Description |
---| ---| ---|
`observability.outputServer.bootstrapServer`| 127.0.0.1:9092 |Kafka server host and port. Tracing and metric data will be output to kafka. |
`observability.outputServer.timeout`| 10000 | Connect timeout. Time Unit: millisecond. |

### HTTP Reqeust Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.request.enabled` | true | Enable collecting `Servlet` or `Filter` metric data |
`observability.metrics.request.interval`| 30 | Time interval between two outputs. Time Unit: second. | 
`observability.metrics.request.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.request.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JDBC SQL Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jdbcStatement.enabled` | true | Enable collecting metric data of `JDBC SQL`. |
`observability.metrics.jdbcStatement.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jdbcStatement.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jdbcStatement.appendType` | kafka  | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JDBC Connection Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jdbcConnection.enabled` | true | Enable collecting metric data of `JDBC Connection`. |
`observability.metrics.jdbcConnection.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jdbcConnection.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jdbcConnection.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### RabbitMQ Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.rabbit.enabled` | true |  Enable collecting metric data of `RabbitMQ producer and consumer`. | Send metric data to the specified kafka topic. |
`observability.metrics.rabbit.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.rabbit.topic` | platform-meter | Send metric data to the specified kafka topic. |
`observability.metrics.rabbit.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Kafka Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.kafka.enabled` | true | Enable collection metric data of `Kafka producer and consumer`. |
`observability.metrics.kafka.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.kafka.topic` | platform-meter | Send metric data to the specified kafka topic. |
`observability.metrics.kafka.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Redis Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.redis.enabled` | true | Enable collection metric data of `Redis`. |
`observability.metrics.redis.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.redis.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.redis.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JVM GC Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jvmGc.enabled` | true | Enable collection metric data of `JVM GC`. |
`observability.metrics.jvmGc.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jvmGc.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jvmGc.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### JVM Memory Metric
Key| Default Value | Description |
---| ---| ---|
`observability.metrics.jvmMemory.enabled` | true | Enable collection metric data of `JVM GC`. |
`observability.metrics.jvmMemory.interval` | 30 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.jvmMemory.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.jvmMemory.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### SQL MD5Dictionary 
When EaseAgent is used with EaseMesh, tracing and metric data will be stored in Elasticsearch. In order to reduce the space occupied by SQL in Elasticsearch, EaseAgent uses md5 to reduce the length of SQL, and then periodically stores it in Kafka, and finally stores it in Elasticsearch. Only one copy of sql will be stored in Elasticsearch.

Key| Default Value | Description |
---| ---| ---|
`observability.metrics.md5Dictionary.enabled` | true | Enable collection of `JVM GC` metric data. |
`observability.metrics.md5Dictionary.interval` | 300 | Time interval between two outputs. Time Unit: second. |
`observability.metrics.md5Dictionary.topic` | application-meter | Send metric data to the specified kafka topic. |
`observability.metrics.md5Dictionary.appendType` | kafka | The value should be `kafka` or `console`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console. |

### Tracing
Key| Default Value | Description |
---| ---| ---|
`observability.tracings.enabled` | true | Enable all collection of tracing logs. `False`: Disable all collection of tracing logs. |
`observability.tracings.output.enabled` | true | Enable output tracing logs to Kafka. |
`observability.tracings.output.topic` | log-tracing | Send tracing logs to the specified kafka topic. |
`observability.tracings.output.messageMaxBytes` | 999900 | Maximum size of a message. |
`observability.tracings.output.reportThread` | 1 | The number of thread which will send  tracing logs to kafka. |
`observability.tracings.output.queuedMaxSpans` | 1000 | The maximum number of spans to be processed in the queue. |
`observability.tracings.output.queuedMaxSize` | 1000000 | The maximum bytes of spans to be processed in the queue. |
`observability.tracings.output.messageTimeout` | 1000 |  |
`observability.tracings.request.enabled` | true | Enable collection of tracing logs(`Servlet`、 `Filter`).|
`observability.tracings.remoteInvoke.enabled` | true | Enable collection of tracing logs(`RestTemplate`、 `FeignClient`、`WebClient`). |
`observability.tracings.kafka.enabled`| true | Enable collection of `kafka` tracing logs. |
`observability.tracings.jdbc.enabled` | true | Enable collection of `JDBC` tracing logs. |
`observability.tracings.redis.enabled` | true | Enable collection of tracing logs(`Jedis`、`Lettuce`). |
`observability.tracings.rabbit.enabled` | true | Enable collection of `RabbitMQ` tracing logs. |

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
