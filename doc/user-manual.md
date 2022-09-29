
# User Manual

- [User Manual](#user-manual)
  - [Configuration](#configuration)
    - [Getting the configuration file](#getting-the-configuration-file)
    - [Global Configuration](#global-configuration)
      - [Internal HTTP Server](#internal-http-server)
      - [Output Data Server: Kafka and HTTP/Zipkin Server](#output-data-server-kafka-and-httpzipkin-server)
      - [Progress Configuration](#progress-configuration)
        - [Forwarded headers config](#forwarded-headers-config)
        - [Tracing config](#tracing-config)
    - [Plugin Configuration](#plugin-configuration)
      - [Tracing and Metric](#tracing-and-metric)
      - [Application Log](#application-log)
      - [Redirect](#redirect)
      - [Forwarded headers plugin enabled](#forwarded-headers-plugin-enabled)
      - [Service Name Head](#service-name-head)
      - [Plugin Http configuration modification api](#plugin-http-configuration-modification-api)
  - [Logging](#logging)
  - [Prometheus Support](#prometheus-support)
  - [Health Check and Readiness Check Endpoint](#health-check-and-readiness-check-endpoint)
  - [Agent info Endpoint](#agent-info-endpoint)
  - [Tracing](#tracing)
    - [Tracing Component](#tracing-component)
    - [Custom Span Tag](#custom-span-tag)
      - [JDBC](#jdbc)
      - [Cache](#cache)
      - [RabbitMQ Producer And Consumer](#rabbitmq-producer-and-consumer)
      - [Kafka Producer And Consumer](#kafka-producer-and-consumer)
  - [Metric](#metric)
    - [Metric Field](#metric-field)
      - [HTTP Request](#http-request)
      - [JDBC Statement](#jdbc-statement)
      - [JDBC Connection](#jdbc-connection)
      - [JVM Memory](#jvm-memory)
      - [JVM GC](#jvm-gc)
      - [Kafka Client](#kafka-client)
      - [RabbitMQ Producer](#rabbitmq-producer)
      - [RabbitMQ Consumer](#rabbitmq-consumer)
      - [Spring AMQP on Message Listener](#spring-amqp-on-message-listener)
      - [Elasticsearch](#elasticsearch)
      - [MongoDB](#mongodb)
  - [Application Log](#application-log-1)

## Configuration
The EaseAgent configuration information can be divided into two categories, one is the **global configuration** and the other is the **plugin configuration**.  
Global configuration include dedicated parameters for controlling metrics and tracing collection behavior via **agent.properties**. These parameters include:

* Data reporting frequency
* Data reporting output type
* Kafka topic of data reporting
* Data collecting and reporting switch
* Queue depth in process for high throughput

Plugin level configuration provides more granular control and customizable configuration.

### Getting the configuration file
You may extract default configuration from the JAR file or create new properties from a blank file.
```
$ jar xf easeagent.jar agent.properties easeagent-log4j2.xml
```
Run the user application with EaseAgent
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ java "-javaagent:${EASE_AGENT_PATH}/easeagent.jar" -Deaseagent.config.path=${EASE_AGENT_PATH}/agent.properties -jar user-app.jar
```


### Global Configuration
#### Internal HTTP Server
EaseAgent opens port `9900` by default to receive configuration change notifications and Prometheus requests.

| Key                        | Default Value | Description                                                                                                                                                                                                                                                                             |
| -------------------------- | ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `easeagent.server.enabled` | true          | Enable Internal HTTP Server. `false` can disable it. EaseAgent will no longer accept any HTTP requests (`Prometheus`、`Health Check`、`Readiness Check`、`Agent Info`) when the Internal HTTP Server is disabled. User can add VM parameter:`-Deaseagent.server.enabled=[true or false]` to override. |
| `easeagent.server.port`    | 9900          | Internal HTTP Server port. User can add VM parameter:`-Deaseagent.server.port=[new port]` to override.                                                                                                                                                                                  |

#### Output Data Server: Kafka and HTTP/Zipkin Server
Tracing and metric data can be output to kafka server.

| Key                                     | Default Value  | Description                                                                  |
| --------------------------------------- | -------------- | ---------------------------------------------------------------------------- |
| `reporter.outputServer.bootstrapServer` | 127.0.0.1:9092 | Kafka server host and port. Tracing and metric data will be output to kafka. |
| `reporter.outputServer.timeout`         | 10000          | Connect timeout. Time Unit: millisecond.                                     |

Global configuration for tracing output

| Key                                  | Default Value                          | Description                                                                                                                             |
| ------------------------------------ | -------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `reporter.tracing.sender.appendType` | console                                | `console` : output tracing to console; `kafka` : output tracing to kafka output server; `http`: send data to http server(zipkin) server |
| `reporter.tracing.sender.url`        | [http://localhost:9411/api/v2/spans]() | Zipkin(HTTP) server url, only available when `reporter.tracing.sender.appendType=http`                                                  |
| `reporter.tracing.sender.topic`      | log-tracing                            | kafka topic, only available when `reporter.tracing.sender.appendType=kafka`                                                             |


Following tracing sender configuration items:

| Key                                        | Default Value | Description                                                                                                                                                                            |
| ------------------------------------------ | ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reporter.tracings.output.enabled`         | true          | `true`: enable output tracing data;<br /> `false`: disable all tracing data output                                                                                                     |
| `reporter.tracings.output.messageMaxBytes` | 999900        | Maximum bytes sendable per message including encoding overhead.                                                                                                                        |
| `reporter.tracings.output.queuedMaxSpans`  | 1000          | Maximum backlog of spans reported before sent.                                                                                                                                         |
| `reporter.tracings.output.queuedMaxSize`   | 1000000       | Maximum backlog of span bytes reported before sent.                                                                                                                                    |
| `reporter.tracings.output.messageTimeout`  | 1000          | Spans are bundled into messages, up to `messageMaxBytes`. This timeout starts when the first unsent span is reported, which ensures that spans are not stuck in an incomplete message. |


Configuration for access log output are similar to tracing:

| Key                              | Default Value    | Description                                                                                                                     |
| -------------------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `reporter.log.sender.appendType` | console          | `console` : output log to console; `kafka` : output log to kafka output server; `http`: send data to http server(zipkin) server |
| `reporter.log.sender.url`        | /application-log | HTTP server url, only available when `reporter.log.sender.appendType=http`                                              |
| `reporter.log.sender.topic`      | applicaton-log   | kafka topic, only available when `reporter.log.sender.appendType=kafka`                                                         |


| Key                                   | Default Value | Description                                                                                                                                                                         |
| ------------------------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reporter.log.output.enabled`         | true          | `true`: enable output log data;<br /> `false`: disable all log data output                                                                                                          |
| `reporter.log.output.messageMaxBytes` | 999900        | Maximum bytes sendable per message including encoding overhead.                                                                                                                     |
| `reporter.log.output.queuedMaxLogs`   | 1000          | Maximum backlog of logs reported before sent.                                                                                                                                       |
| `reporter.log.output.queuedMaxSize`   | 1000000       | Maximum backlog of log bytes reported before sent.                                                                                                                                  |
| `reporter.log.output.messageTimeout`  | 1000          | Logs are bundled into messages, up to `messageMaxBytes`. This timeout starts when the first unsent log is reported, which ensures that logs are not stuck in an incomplete message. |



#### Progress Configuration

##### Forwarded headers config

Easeagent provides a header pass-through plugin.

Config format: 

`easeagent.progress.forwarded.headers.{key}={headerName}`

1. {key} indicates the unique key of the header configuration, used to identify the configuration modification
2. {headerName} is the Header Name you need to pass through

Example:
```properties
easeagent.progress.forwarded.headers.canary.0=X-Mesh-Canary
```
In the process of supporting easemesh traffic coloring, the request header `X-Mesh-Canary` needs to be deeply passed through.

```
(add header: X-Mesh-Canary=lv1) -> serviceA(X-Mesh-Canary=lv1) -> mesh(check  X-Mesh-Canary) --> servcieB
                                                                                         |
                                                                                         |_____> servcieB-canary(X-Mesh-Canary=lv1)
```

plugin enabled config: [Enabled](#Forwarded headers plugin enabled)


##### Tracing config

Easeagent will grab the header from the response of the process, and put the name and value of the header as a tag in the Span of Tracing.

Config format:
`observability.tracings.tag.response.headers.{key}={value}`

1. {key} indicates the unique key of the header configuration, used to identify the configuration modification
2. {headerName} is the Header Name you need to tag

Example:
```properties
observability.tracings.tag.response.headers.eg.0=X-EG-Circuit-Breaker
```

In the process of supporting sidecars (such as easemesh), the sidecars will hijack or color traffic according to the situation.

In order to facilitate observation and drawing, sidecars should add header information in the response header and record the tag in Tracing.

example:
easemesh adds the following header information: `X-EG-Circuit-Breaker`, `X-EG-Retryer`, `X-EG-Rate-Limiter`, `X-EG-Time-Limiter`

The tag will be added to the Tracing Span of the request client:

```json
{"kind": "CLIENT", "tags": {"X-EG-Circuit-Breaker":"aaaa", "X-EG-Retryer":"bbbb", "X-EG-Rate-Limiter":"cccc", "X-EG-Time-Limiter":"dddd"}}
```

### Plugin Configuration
Most capabilities of Easeagent, such as tracing and metric, are provided through plugins.
The format of the plugin configuration is defined as follows.
```
plugin.[domain].[namespace].[function].[key] = [value]
```
Take the tracing switch of `httpclient` as an example.
```
plugin.observability.httpclient.tracing.enabled=true

domain          : observability
namespace       : httpclient
function        : tracing
key             : enabled
value           : true
```

`[domain]` and `[namespace]` and `[function]` are defined by plugins, and further details can be found in the [plugin development guide](./development-guide.md).

For plugin level configuration, EaseAgent defines a spacial **namespace** of `global` in which user can define default configuration for any `function`, like `metric`, and each namespace plugin of this `function` will uses the default configuration when it does not create configuration with its own namespace.

For example, Metric have a set of default plugin configuration as follows:
```
plugin.observability.global.metric.enabled=true
plugin.observability.global.metric.interval=30
plugin.observability.global.metric.topic=application-meter
plugin.observability.global.metric.appendType=kafka
```

All metric plugins will inherit default configuration, unless they have configured a configuration item with the same [key] and replaced the `global` namespace with its own namespace to override.

```
# this configuration item of rabbitmq indicate that rabbitmq's metirc data is printed to console instead of send to kafka server as configured by the default.

plugin.observability.rabbitmq.metric.appendType=console
```
But the switch configuration item using `enabled` as key cannot be overridden, for `boolean` type configuration is determined by a "logical AND" operation between the global and its own namespace configuration.

The following sections describe the metric and tracing configuration items,  as well as the currently supported plugins and their corresponding namespaces

#### Tracing and Metric

| Key                                             | Default Value     | Description                                                                                                                                                                                                       |
| ----------------------------------------------- | ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `plugin.observability.global.tracing.enabled`   | true              | Enable all tracing collection. `false`: Disable all tracing collection.                                                                                                                                           |
| `plugin.observability.global.metric.enabled`    | true              | Enable all metrics collection. `false`: Disable all metrics collection.                                                                                                                                           |
| `plugin.observability.global.metric.interval`   | 30                | Time interval between two outputs. Time Unit: second.                                                                                                                                                             |
| `plugin.observability.global.metric.topic`      | application-meter | Send metric data to the specified kafka topic, only avaliable when `appendType` is `kafka`.                                                                                                                       |
| `plugin.observability.global.metric.url`        | /metrics          | Send metric data to the specified http URI, which will be appended to `reporter.outputServer.bootstrapServer`, to form a full url, only avaliable when `appendType` is `http`.                                    |
| `plugin.observability.global.metric.appendType` | kafka             | The value should be `kafka`, `console` or `http`. `kafka`: EaseAgent will output metric data to kafka server. `console`: EaseAgent will output metric data to console; `http`: output metric data to http server. |

Supported components and corresponding namespaces:

| Plugin/Components | Namespace        | Description                                                                                                                                                                                                                                                                                                                                                                |
| ----------------- | ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| httpservlet       | `httpServlet`    | Http Request Metric                                                                                                                                                                                                                                                                                                                                                        |
| spring-gateway    | `springGateway`  | Http Request Metric                                                                                                                                                                                                                                                                                                                                                        |
| jdbcConnection    | `jdbcConnection` | JDBC Connection Metric                                                                                                                                                                                                                                                                                                                                                     |
| jdbcStatement     | `jdbcStatement`  | JDBC SQL Metric. When using SQL as a tag, the string length of SQL is often very long, which will consume network bandwidth and CPU to a great extent. Our solution is to use SQL's MD5 as an indicator, which is associated with the storage and front-end.Closed configuration: `plugin.observability.jdbc.sql.compress.enabled=false`                                   |
| md5Dictionary     | `md5Dictionary`  | SQL-MD5Dictionary. `When EaseAgent is used with EaseMesh, tracing and metric data will be stored in Elasticsearch. In order to reduce the space occupied by SQL in Elasticsearch, EaseAgent uses md5 to reduce the length of SQL, and then periodically stores it in Kafka, and finally stores it in Elasticsearch. Only one copy of sql will be stored in Elasticsearch.` |
| redis             | `redis`          | Redis Metric                                                                                                                                                                                                                                                                                                                                                               |
| kafka             | `kafka`          | Kafka Metric                                                                                                                                                                                                                                                                                                                                                               |
| rabbitmq          | `rabbitmq`       | RabbitMQ Metric                                                                                                                                                                                                                                                                                                                                                            |
| jvmGc             | `jvmGc`          | JVM GC Metric                                                                                                                                                                                                                                                                                                                                                              |
| JVM Memory        | `jvmMemory`      | JVM Memory Metric                                                                                                                                                                                                                                                                                                                                                          |

#### Application Log
Application log modules collecting application logs printed by the application.

Supported components/plugins and corresponding namespaces:

| Plugin/Components | Namespace       | Description               |
| ----------------- | --------------- | ------------------------- |
| logback           | `logback`       | Support logback library   |
| log4j2            | `log4j2 `       | Support log4j2 library         |
| access            | `access`        | Access log module         |

The default configuration is as follows:

```
plugin.observability.global.log.enabled=true
plugin.observability.global.log.appendType=console
plugin.observability.global.log.topic=application-log
plugin.observability.global.log.url=/application-log
plugin.observability.global.log.level=INFO


plugin.observability.global.log.encoder=LogDataJsonEncoder
plugin.observability.global.log.encoder.timestamp=%d{UNIX_MILLIS}
plugin.observability.global.log.encoder.logLevel=%-5level
plugin.observability.global.log.encoder.threadId=%thread
plugin.observability.global.log.encoder.location=%logger{36}
plugin.observability.global.log.encoder.message=%msg%n

plugin.observability.access.log.encoder=AccessLogJsonEncoder

plugin.observability.logback.log.enabled=false
plugin.observability.log4j2.log.enabled=false
```

The `logback` and `log4j2` modules are disabled by default, and they can be `enabled` in the user configuration file to enable collecting logs printed by the application.
The `LogDataJsonEncoder` supports `log4j2` style pattern configuration for each field.

To send logs data to `Opentelemetry` compatible backend, the corresponding `Encoder` need to be developed.

#### Redirect
Redirection feature combined with `EaseMesh` to direct traffic to shadow services to simulate real traffic for the whole site performance test in the production environment in an effective and safe way.
For more detail, please reference [EaseMesh](https://megaease.com/easemesh/) documents.

The default configuration has only one item:

```
plugin.integrability.global.redirect.enabled=true
```

Supported components/plugins and corresponding namespaces:

| Plugin/Components | Namespace       | Description               |
| ----------------- | --------------- | ------------------------- |
| jdbc              | `jdbc`          | Database Redirection      |
| redis             | `redis`         | Redis Redirection         |
| kafka             | `kafka`         | Kafka Redirection         |
| rabbitmq          | `rabbitmq`      | RabbitMQ Redirection      |
| elasticsearch     | `elasticsearch` | Elasticsearch Redirection |

#### Forwarded headers plugin enabled

Easeagent provides a header pass-through plugin.

```
plugin.integrability.global.forwarded.enabled=true
```


#### Service Name Head

To support easemesh, we have added a new plugin called "servicename".

It will get the service name in advance, and then put the service name in the HTTP request header.

header name config:
```properties
plugin.integrability.serviceName.addServiceNameHead.propagate.head=X-Mesh-RPC-Service
```

The current way to obtain ServiceName only supports service discovery using Spring Cloud.

#### Plugin Http configuration modification api

After the EaseAgent enabled the http port, the http api can be used to modify the configuration of the plugin.

1. The plugin configuration items can be modified directly from the configuration information mapping:
    ```
    GET /plugins/domains/{domain}/namespaces/{namespace}/{id}/properties/{property}/{value}/{version}
    ```

2. API supports passing json one-time "modification/addition" content instead of setting them one by one. For example:
    ```
    POST /plugins/domains/{domain}/namespaces/{namespace}/{id}/properties
    
    {
         "version": "1",
         "property1": "value1",
         "property2": "value2"
    }
    ```

the {version} can be any information



## Logging
EaseAgent use `Log4j2` for all internal logging, the default log level is `INFO`, and the logs will be outputted to the `Console`. User can modify the log level and appender in the `easeagent-log4j2.xml` file.

After modification, User can run the application with EaseAgent.
```
$ export EASE_AGENT_PATH=[Replace with agent path]
$ java "-javaagent:${EASE_AGENT_PATH}/easeagent.jar -Deaseagent.log.conf=${EASE_AGENT_PATH}/easeagent-log4j2.xml" -jar user-app.jar
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

## Agent info Endpoint
EaseAgent supply the `agent info` http api.
```
[GET] http://[ip]:[easeagent.server.port]/agent-info
```
The response status will be 200(OK)
Response Body:
```json
{
    "type": "EaseAgent",
    "version": "x.x.x"
}
```

## Tracing
EaseAgent use [brave](https://github.com/openzipkin/brave) to collect tracing logs.The data format stored in `Kafka`  is [Zipkin Data Model](https://zipkin.io/pages/data_model.html). User can send tracing logs to [Zipkin server](https://zipkin.io/pages/quickstart.html).

### Tracing Component
| Component Type | Component                                    | Reference                                                                                                                                                                                                                   |
| -------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| HTTP Client    | `RestTemplate`、 `WebClient`、 `FeignClient` | [brave-instrumentation-http](https://github.com/openzipkin/brave/tree/master/instrumentation/http)                                                                                                                          |
| HTTP Server    | `Servlet`、`Filter`                          | [brave-instrumentation-http](https://github.com/openzipkin/brave/tree/master/instrumentation/http)                                                                                                                          |
| DataBase       | `JDBC`                                       | [Brave](https://github.com/openzipkin/brave/tree/master/brave)                                                                                                                                                              |
| Cache          | `Jedis`、`Lettuce`                           | [Brave](https://github.com/openzipkin/brave/tree/master/brave)                                                                                                                                                              |
| Message        | `RabbitMQ`、`Kafka`                          | [brave-instrumentation-messaging](https://github.com/openzipkin/brave/tree/master/instrumentation/messaging) 、[Brave Kafka instrumentation](https://github.com/openzipkin/brave/tree/master/instrumentation/kafka-clients) |
| Logging        | `Log4j2`、`Logback`                          | [brave-context-log4j2](https://github.com/openzipkin/brave/tree/master/context/log4j2) 、[brave-context-slf4j](https://github.com/openzipkin/brave/tree/master/context/slf4j)                                               |

### Custom Span Tag

#### JDBC
| Tag             | Description                                                            |
| --------------- | ---------------------------------------------------------------------- |
| sql             | Sql text in user application                                           |
| local-component | Default value = 'database'                                             |
| url             | Connection information. Example: `jdbc:mysql://localhost:3306/db_demo` |
| error           | SQLException information                                               |

#### Cache
| Tag          | Description                           |
| ------------ | ------------------------------------- |
| redis.method | Redis command. Example: `MGET`、`GET` |

#### RabbitMQ Producer And Consumer
| Tag                | Description         |
| ------------------ | ------------------- |
| rabbit.exchange    | RabbitMQ exchange   |
| rabbit.routing_key | RabbitMQ routingKey |
| rabbit.queue       | RabbitMQ routingKey |

#### Kafka Producer And Consumer
| Tag          | Description               |
| ------------ | ------------------------- |
| kafka.key    | Kafka consumer record Key |
| kafka.topic  | Kafka topic               |
| kafka.broker | Kafka url                 |

## Metric
EaseAgent use [io.dropwizard.metrics](https://github.com/dropwizard/metrics) to collect metric information.

Prometheus Metric Schedule: [Prometheus Metric](./prometheus-metric-schedule.md)

Prometheus Exports Rules: [Prometheus Exports](./metric-api.md#7prometheusexports)


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
* Total execution count (cnt, errcnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Error throughput percentage (m1errpct, m5errpct, m15errpct)
* Latency (p25, p50, p75, p95, p98, p99)
* Execution duration (min, mean, max)

| Field     |  Type   | Description                                                                                            |
| :-------- | :-----: | :----------------------------------------------------------------------------------------------------- |
| url       | string  | the URL of the request                                                                                 |
| cnt       | integer | The total count of the request executed                                                                |
| errcnt    | integer | The total error count of the request executed                                                          |
| m1        | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 1 minute        |
| m5        | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| m15       | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| m1err     | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| m5err     | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err    | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 15 minute |
| m1errpct  | double  | error percentage in last 1 minute                                                                      |
| m5errpct  | double  | error percentage in last 5 minute                                                                      |
| m15errpct | double  | error percentage in last 15 minute                                                                     |
| min       | double  | The http-request minimal execution duration in milliseconds.                                           |
| max       | double  | The http-request maximal execution duration in milliseconds.                                           |
| mean      | double  | The http-request mean execution duration in milliseconds.                                              |
| p25       | double  | TP25: The http-request execution duration in milliseconds for 25% user.                                |
| p50       | double  | TP50: The http-request execution duration in milliseconds for 50% user.                                |
| p75       | double  | TP75: The http-request execution duration in milliseconds for 75% user.                                |
| p95       | double  | TP95: The http-request execution duration in milliseconds for 95% user.                                |
| p98       | double  | TP98: The http-request execution duration in milliseconds for 98% user.                                |
| p99       | double  | TP99: The http-request execution duration in milliseconds for 99% user.                                |

#### JDBC Statement
JDBC Statement schema describes key metrics of JDBC SQL Statement, which include:
* Execution count (cnt, errcnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Field     |  Type   | Description                                                                                           |
| :-------- | :-----: | :---------------------------------------------------------------------------------------------------- |
| signature | string  | Executed JDBC method signature.                                                                       |
| cnt       | integer | The total count of JDBC method executed                                                               |
| errcnt    | integer | The total error count of JDBC method executed                                                         |
| m1        | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 1 minute.       |
| m5        | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 5 minutes.      |
| m15       | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 15 minutes.     |
| m1err     | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 1 minute  |
| m5err     | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err    | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 15 minute |
| min       | double  | The JDBC method minimal execution duration in milliseconds.                                           |
| max       | double  | The JDBC method maximal execution duration in milliseconds.                                           |
| mean      | double  | The JDBC method mean execution duration in milliseconds.                                              |
| p25       | double  | TP25: The JDBC method execution duration in milliseconds for 25% user.                                |
| p50       | double  | TP50: The JDBC method execution duration in milliseconds for 50% user.                                |
| p75       | double  | TP75: The JDBC method execution duration in milliseconds for 75% user.                                |
| p95       | double  | TP95: The JDBC method execution duration in milliseconds for 95% user.                                |
| p98       | double  | TP98: The JDBC method execution duration in milliseconds for 98% user.                                |
| p99       | double  | TP99: The JDBC method execution duration in milliseconds for 99% user.                                |
| p999      | double  | TP99.9: The JDBC method execution duration in milliseconds for 99.9% user.                            |

#### JDBC Connection
JDBC Connection schema describes key metrics of Getting Connection, which include:
* Execution count (cnt, errcnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Field  |  Type   | Description                                                                                               |
| :----- | :-----: | :-------------------------------------------------------------------------------------------------------- |
| url    | string  | The url of database connections                                                                           |
| cnt    | integer | The total number of database connections                                                                  |
| errcnt | integer | The total error number of database connections                                                            |
| m1     | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 1 minute.    |
| m5     | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 5 minutes.   |
| m15    | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 15 minutes.  |
| m1err  | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 1 minute  |
| m5err  | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 15 minute |
| min    | double  | The JDBC connection minimal establishment duration in milliseconds.                                       |
| max    | double  | The JDBC connection maximal establishment duration in milliseconds.                                       |
| mean   | double  | The JDBC connection mean establishment duration in milliseconds.                                          |
| p25    | double  | TP25: The JDBC connection establishment duration in milliseconds for 25% user.                            |
| p50    | double  | TP50: The JDBC connection establishment duration in milliseconds for 50% user.                            |
| p75    | double  | TP75: The JDBC connection establishment duration in milliseconds for 75% user.                            |
| p95    | double  | TP95: The JDBC connection establishment duration in milliseconds for 95% user.                            |
| p98    | double  | TP98: The JDBC connection establishment duration in milliseconds for 98% user.                            |
| p99    | double  | TP99: The JDBC connection establishment duration in milliseconds for 99% user.                            |
| p999   | double  | TP99.9: The JDBC connection establishment duration in milliseconds for 99.9% user.                        |

#### JVM Memory
JVM Memory schema describes key metrics of Java memory usage, which include:
* bytes-init
* bytes-used
* bytes-committed
* bytes-max


| Field           |  Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| :-------------- | :-----: | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| resource        | String  | memory pool name                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| bytes-init      | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1).                                                                                                                                                                                            |
| bytes-used      | integer | The value represents the amount of memory currently used in bytes unit.                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| bytes-committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used.                                                                                                                                                                             |
| bytes-max       | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). |

#### JVM GC
JVM GC schema describes key metrics of JVM garbage collection, which include:
* total_collection_time
* times
* times_rate

| Field                 |  Type   | Description                                                                               |
| :-------------------- | :-----: | :---------------------------------------------------------------------------------------- |
| resource              | string  | gc name                                                                                   |
| total_collection_time | integer | The value represents the total time for garbage collection operation in millisecond unit. |
| times                 | integer | The value represents the total garbage collection times.                                  |
| times_rate            | integer | The number of gc times per second.                                                        |

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

| Field      |  Type  | Description                                                                                          |
| :--------- | :----: | :--------------------------------------------------------------------------------------------------- |
| resource   | string | topic name                                                                                           |
| prodrm1    | double | The executions per second (exponentially-weighted moving average) in last 1 minute (producer)        |
| prodrm5    | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| prodrm15   | double | The executions per second (exponentially-weighted moving average) in last 15 minute (producer)       |
| consrm1    | double | The executions per second (exponentially-weighted moving average) in last 1 minute (consumer)        |
| consrm5    | double | The executions per second (exponentially-weighted moving average) in last 5 minute (consumer)        |
| consrm15   | double | The executions per second (exponentially-weighted moving average) in last 15 minute (consumer)       |
| prodrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)  |
| prodrm5err | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| prodrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (producer) |
| consrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (consumer)  |
| consrm5err | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (consumer)  |
| consrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (consumer) |
| prodrmin   | double | The minimal execution duration in milliseconds.                                                      |
| prodrmax   | double | The maximal execution duration in milliseconds.                                                      |
| prodrmean  | double | The mean execution duration in milliseconds.                                                         |
| prodrp25   | double | TP25: The execution duration in milliseconds for 25% user.                                           |
| prodrp50   | double | TP50: The execution duration in milliseconds for 50% user.                                           |
| prodrp75   | double | TP75: The execution duration in milliseconds for 75% user.                                           |
| prodrp95   | double | TP95: The execution duration in milliseconds for 95% user.                                           |
| prodrp98   | double | TP98: The execution duration in milliseconds for 98% user.                                           |
| prodrp99   | double | TP99: The execution duration in milliseconds for 99% user.                                           |
| prodrp999  | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |
| consrmin   | double | The minimal execution duration in milliseconds.                                                      |
| consrmax   | double | The maximal execution duration in milliseconds.                                                      |
| consrmean  | double | The mean execution duration in milliseconds.                                                         |
| consrp25   | double | TP25: The execution duration in milliseconds for 25% user.                                           |
| consrp50   | double | TP50: The execution duration in milliseconds for 50% user.                                           |
| consrp75   | double | TP75: The execution duration in milliseconds for 75% user.                                           |
| consrp95   | double | TP95: The execution duration in milliseconds for 95% user.                                           |
| consrp98   | double | TP98: The execution duration in milliseconds for 98% user.                                           |
| consrp99   | double | TP99: The execution duration in milliseconds for 99% user.                                           |
| consrp999  | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |

#### RabbitMQ Producer
RabbitMQ Producer schema describes key metrics of RabbitMQ client publishing message, which include:
* Throughput (prodrm1, prodrm5, prodrm15)
* Error throughput (prodrm1err, prodrm5err, prodrm15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field      |  Type  | Description                                                                                          |
| :--------- | :----: | :--------------------------------------------------------------------------------------------------- |
| resource   | string | rabbitmq exchange or routingkey                                                                      |
| prodrm1    | double | The executions of producer per second (exponentially-weighted moving average) in last 1 minute       |
| prodrm5    | double | The executions of producer per second (exponentially-weighted moving average) in last 5 minute       |
| prodrm15   | double | The executionsof producer per second (exponentially-weighted moving average) in last 15 minute       |
| prodrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)  |
| prodrm5err | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| prodrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (producer) |
| min        | double | The producer minimal execution duration in milliseconds.                                             |
| max        | double | The producer maximal execution duration in milliseconds.                                             |
| mean       | double | The producer mean execution duration in milliseconds.                                                |
| p25        | double | TP25: The producer execution duration in milliseconds for 25% user.                                  |
| p50        | double | TP50: The producer execution duration in milliseconds for 50% user.                                  |
| p75        | double | TP75: The producer execution duration in milliseconds for 75% user.                                  |
| p95        | double | TP95: The producer execution duration in milliseconds for 95% user.                                  |
| p98        | double | TP98: The producer execution duration in milliseconds for 98% user.                                  |
| p99        | double | TP99: The producer execution duration in milliseconds for 99% user.                                  |
| p999       | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |

#### RabbitMQ Consumer
RabbitMQ Consumer schema describes key metrics of RabbitMQ client consuming message, which include:
* Throughput (queue_m1_rate, queue_m5_rate, queue_m15_rate)
* Error throughput (queue_m1_error_rate, queue_m5_error_rate, queue_m15_error_rate)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field                |  Type  | Description                                                                                       |
| :------------------- | :----: | :------------------------------------------------------------------------------------------------ |
| resource             | string | rabbitmq routingKey                                                                               |
| queue_m1_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 1 minute       |
| queue_m5_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 5 minute       |
| queue_m15_rate       | double | The executionsof queue per second (exponentially-weighted moving average) in last 15 minute       |
| queue_m1_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)  |
| queue_m5_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)  |
| queue_m15_error_rate | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (queue) |
| min                  | double | The consumer minimal execution duration in milliseconds.                                          |
| max                  | double | The consumer maximal execution duration in milliseconds.                                          |
| mean                 | double | The consumer mean execution duration in milliseconds.                                             |
| p25                  | double | TP25: The consumer execution duration in milliseconds for 25% user.                               |
| p50                  | double | TP50: The consumer execution duration in milliseconds for 50% user.                               |
| p75                  | double | TP75: The consumer execution duration in milliseconds for 75% user.                               |
| p95                  | double | TP95: The consumer execution duration in milliseconds for 95% user.                               |
| p98                  | double | TP98: The consumer execution duration in milliseconds for 98% user.                               |
| p99                  | double | TP99: The consumer execution duration in milliseconds for 99% user.                               |
| p999                 | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                    |

#### Spring AMQP on Message Listener
Message Listener schema describes key metrics of Spring AMQP RabbitMQ Message Queue, which include:
* Throughput (queue_m1_rate, queue_m5_rate, queue_m15_rate)
* Error throughput (queue_m1_error_rate, queue_m5_error_rate, queue_m15_error_rate)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field                |  Type  | Description                                                                                       |
| :------------------- | :----: | :------------------------------------------------------------------------------------------------ |
| resource             | string | rabbitmq queue                                                                                    |
| queue_m1_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 1 minute       |
| queue_m5_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 5 minute       |
| queue_m15_rate       | double | The executionsof queue per second (exponentially-weighted moving average) in last 15 minute       |
| queue_m1_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)  |
| queue_m5_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)  |
| queue_m15_error_rate | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (queue) |
| min                  | double | The AMQP Message Listener minimal execution duration in milliseconds.                             |
| max                  | double | The AMQP Message Listener maximal execution duration in milliseconds.                             |
| mean                 | double | The AMQP Message Listener mean execution duration in milliseconds.                                |
| p25                  | double | TP25: The AMQP Message Listener execution duration in milliseconds for 25% user.                  |
| p50                  | double | TP50: The AMQP Message Listener execution duration in milliseconds for 50% user.                  |
| p75                  | double | TP75: The AMQP Message Listener execution duration in milliseconds for 75% user.                  |
| p95                  | double | TP95: The AMQP Message Listener execution duration in milliseconds for 95% user.                  |
| p98                  | double | TP98: The AMQP Message Listener execution duration in milliseconds for 98% user.                  |
| p99                  | double | TP99: The AMQP Message Listener execution duration in milliseconds for 99% user.                  |
| p999                 | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                    |

#### Elasticsearch
Elasticsearch schema describes key metrics of Elasticsearch client invoking, which include:
* Total execution count (cnt, errcnt, m1cnt, m5cnt, m15cnt)
* Throughput (m1, m5, m15, mean_rate)
* Error throughput (m1err, m5err, m15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field     |  Type   | Description                                                                                                     |
| :-------- | :-----: | :-------------------------------------------------------------------------------------------------------------- |
| index     | string  | The Elasticsearch index name                                                                                    |
| cnt       | integer | The total count of the request executed                                                                         |
| errcnt    | integer | The total error count of the request executed                                                                   |
| m1cnt     | integer | The total count of the request executed in last 1 minute                                                        |
| m5cnt     | integer | The total count of the request executed in last 5 minute                                                        |
| m15cnt    | integer | The total count of the request executed in last 15 minute                                                       |
| m1        | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 1 minute        |
| m5        | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| m15       | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| mean_rate | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| m1err     | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| m5err     | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err    | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 15 minute |
| min       | double  | The Elasticsearch minimal execution duration in milliseconds.                                                   |
| max       | double  | The Elasticsearch maximal execution duration in milliseconds.                                                   |
| mean      | double  | The Elasticsearch mean execution duration in milliseconds.                                                      |
| p25       | double  | TP25: The Elasticsearch execution duration in milliseconds for 25% user.                                        |
| p50       | double  | TP50: The Elasticsearch execution duration in milliseconds for 50% user.                                        |
| p75       | double  | TP75: The Elasticsearch execution duration in milliseconds for 75% user.                                        |
| p95       | double  | TP95: The Elasticsearch execution duration in milliseconds for 95% user.                                        |
| p98       | double  | TP98: The Elasticsearch execution duration in milliseconds for 98% user.                                        |
| p99       | double  | TP99: The Elasticsearch execution duration in milliseconds for 99% user.                                        |

#### MongoDB
MongoDB schema describes key metrics of MongoDB client invoking, which include:
* Total execution count (cnt, errcnt, m1cnt, m5cnt, m15cnt)
* Throughput (m1, m5, m15, mean_rate)
* Error throughput (m1err, m5err, m15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Field     |  Type   | Description                                                                                               |
| :-------- | :-----: | :-------------------------------------------------------------------------------------------------------- |
| operation | string  | The MongoDB request command name                                                                          |
| cnt       | integer | The total count of the request executed                                                                   |
| errcnt    | integer | The total error count of the request executed                                                             |
| m1cnt     | integer | The total count of the request executed in last 1 minute                                                  |
| m5cnt     | integer | The total count of the request executed in last 5 minute                                                  |
| m15cnt    | integer | The total count of the request executed in last 15 minute                                                 |
| m1        | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 1 minute        |
| m5        | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| m15       | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| mean_rate | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| m1err     | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| m5err     | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| m15err    | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 15 minute |
| min       | double  | The MongoDB minimal execution duration in milliseconds.                                                   |
| max       | double  | The MongoDB maximal execution duration in milliseconds.                                                   |
| mean      | double  | The MongoDB mean execution duration in milliseconds.                                                      |
| p25       | double  | TP25: The MongoDB execution duration in milliseconds for 25% user.                                        |
| p50       | double  | TP50: The MongoDB execution duration in milliseconds for 50% user.                                        |
| p75       | double  | TP75: The MongoDB execution duration in milliseconds for 75% user.                                        |
| p95       | double  | TP95: The MongoDB execution duration in milliseconds for 95% user.                                        |
| p98       | double  | TP98: The MongoDB execution duration in milliseconds for 98% user.                                        |
| p99       | double  | TP99: The MongoDB execution duration in milliseconds for 99% user.                                        |

## Application Log

