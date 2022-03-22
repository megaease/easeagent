# Prometheus Metric Schedule

Prometheus Exports Rules: [Prometheus Exports](./metric-api.md#7prometheusexports)

## Common label

| label name    |    Value Example    | Description                                                                                                       |
|:--------------|:-------------------:|:------------------------------------------------------------------------------------------------------------------|
| MetricSubType |      `DEFAULT`      | The enum MetricSubType value: `DEFAULT, ERROR, CHANNEL, CONSUMER, PRODUCER, CONSUMER_ERROR, PRODUCER_ERROR, NONE` |
| MetricType    |    `CounterType`    | The Metric Type by metric calculate: `TimerType, HistogramType, MeterType, CounterType, GaugeType`                |
| host_ipv4     |   `10.127.48.163`   | The ipv4 by host: xxx.xxx.xxx.xx                                                                                  |
| host_name     | `MacBook-Pro.local` | host name.                                                                                                        |
| service       |     `demo-name`     | The `name` read from the configuration. for you service name.                                                     |
| system        |    `demo-system`    | The `system` read from the configuration. for you system name.                                                    |

## Metric Schedule

### HTTP Request
HTTP Request schema describes key metrics of service APIs, which include:
* Total execution count (cnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Error throughput percentage (m1errpct, m5errpct, m15errpct)
* Latency (p25, p50, p75, p95, p98, p99)
* Execution duration (min, mean, max)

| Metric Name                        |  Type   | Description                                                                                            |
|:-----------------------------------|:-------:|:-------------------------------------------------------------------------------------------------------|
| application_http_request_cnt       | integer | The total count of the request executed                                                                |
| application_http_request_errcnt    | integer | The total error count of the request executed                                                          |
| application_http_request_m1        | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 1 minute        |
| application_http_request_m5        | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| application_http_request_m15       | double  | The HTTP request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| application_http_request_m1err     | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| application_http_request_m5err     | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| application_http_request_m15err    | double  | The HTTP error request executions per second (exponentially-weighted moving average) in last 15 minute |
| application_http_request_m1errpct  | double  | error percentage in last 1 minute                                                                      |
| application_http_request_m5errpct  | double  | error percentage in last 5 minute                                                                      |
| application_http_request_m15errpct | double  | error percentage in last 15 minute                                                                     |
| application_http_request_min       | double  | The http-request minimal execution duration in milliseconds.                                           |
| application_http_request_max       | double  | The http-request maximal execution duration in milliseconds.                                           |
| application_http_request_mean      | double  | The http-request mean execution duration in milliseconds.                                              |
| application_http_request_p25       | double  | TP25: The http-request execution duration in milliseconds for 25% user.                                |
| application_http_request_p50       | double  | TP50: The http-request execution duration in milliseconds for 50% user.                                |
| application_http_request_p75       | double  | TP75: The http-request execution duration in milliseconds for 75% user.                                |
| application_http_request_p95       | double  | TP95: The http-request execution duration in milliseconds for 95% user.                                |
| application_http_request_p98       | double  | TP98: The http-request execution duration in milliseconds for 98% user.                                |
| application_http_request_p99       | double  | TP99: The http-request execution duration in milliseconds for 99% user.                                |

#### Dedicated label
| Label Name | Essential |   Value Example   | Description            |
|:-----------|:---------:|:-----------------:|:-----------------------|
| url        |   true    | `GET /web_client` | the URL of the request |

### JDBC Statement
JDBC Statement schema describes key metrics of JDBC SQL Statement, which include:
* Execution count (cnt, errcnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Metric Name                       |  Type   | Description                                                                                           |
|:----------------------------------|:-------:|:------------------------------------------------------------------------------------------------------|
| application_jdbc_statement_cnt    | integer | The total count of JDBC method executed                                                               |
| application_jdbc_statement_errcnt | integer | The total error count of JDBC method executed                                                         |
| application_jdbc_statement_m1     | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 1 minute.       |
| application_jdbc_statement_m5     | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 5 minutes.      |
| application_jdbc_statement_m15    | double  | The JDBC method executions per second (exponentially-weighted moving average) in last 15 minutes.     |
| application_jdbc_statement_m1err  | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 1 minute  |
| application_jdbc_statement_m5err  | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 5 minute. |
| application_jdbc_statement_m15err | double  | The JDBC method error executions per second (exponentially-weighted moving average) in last 15 minute |
| application_jdbc_statement_min    | double  | The JDBC method minimal execution duration in milliseconds.                                           |
| application_jdbc_statement_max    | double  | The JDBC method maximal execution duration in milliseconds.                                           |
| application_jdbc_statement_mean   | double  | The JDBC method mean execution duration in milliseconds.                                              |
| application_jdbc_statement_p25    | double  | TP25: The JDBC method execution duration in milliseconds for 25% user.                                |
| application_jdbc_statement_p50    | double  | TP50: The JDBC method execution duration in milliseconds for 50% user.                                |
| application_jdbc_statement_p75    | double  | TP75: The JDBC method execution duration in milliseconds for 75% user.                                |
| application_jdbc_statement_p95    | double  | TP95: The JDBC method execution duration in milliseconds for 95% user.                                |
| application_jdbc_statement_p98    | double  | TP98: The JDBC method execution duration in milliseconds for 98% user.                                |
| application_jdbc_statement_p99    | double  | TP99: The JDBC method execution duration in milliseconds for 99% user.                                |
| application_jdbc_statement_p999   | double  | TP99.9: The JDBC method execution duration in milliseconds for 99.9% user.                            |

#### Dedicated label
| Label Name | Essential |           Value Example            | Description                                                                                                                                                                                                                                           |
|:-----------|:---------:|:----------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| signature  |   true    | `440580e6c3215eceb4ef992d60adde9a` | Executed JDBC method signature. By default, It is an MD5 of SQL like `select * from data`. It can be SQL statement via turning off the switch: `plugin.observability.jdbc.sql.compress.enabled=false`. [Details](./user-manual.md#tracing-and-metric) |


### JDBC Connection
JDBC Connection schema describes key metrics of Getting Connection, which include:
* Execution count (cnt, errcnt)
* Throughput (m1, m5, m15)
* Error throughput (m1err, m5err, m15err)
* Latency (p25, p50, p75, p95, p98, p99, p999)
* Execution duration (min, mean, max)

| Metric Name                        |  Type   | Description                                                                                               |
|:-----------------------------------|:-------:|:----------------------------------------------------------------------------------------------------------|
| application_jdbc_connection_cnt    | integer | The total number of database connections                                                                  |
| application_jdbc_connection_errcnt | integer | The total error number of database connections                                                            |
| application_jdbc_connection_m1     | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 1 minute.    |
| application_jdbc_connection_m5     | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 5 minutes.   |
| application_jdbc_connection_m15    | double  | The JDBC connection establishment per second (exponentially-weighted moving average) in last 15 minutes.  |
| application_jdbc_connection_m1err  | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 1 minute  |
| application_jdbc_connection_m5err  | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 5 minute. |
| application_jdbc_connection_m15err | double  | The JDBC connection error executions per second (exponentially-weighted moving average) in last 15 minute |
| application_jdbc_connection_min    | double  | The JDBC connection minimal establishment duration in milliseconds.                                       |
| application_jdbc_connection_max    | double  | The JDBC connection maximal establishment duration in milliseconds.                                       |
| application_jdbc_connection_mean   | double  | The JDBC connection mean establishment duration in milliseconds.                                          |
| application_jdbc_connection_p25    | double  | TP25: The JDBC connection establishment duration in milliseconds for 25% user.                            |
| application_jdbc_connection_p50    | double  | TP50: The JDBC connection establishment duration in milliseconds for 50% user.                            |
| application_jdbc_connection_p75    | double  | TP75: The JDBC connection establishment duration in milliseconds for 75% user.                            |
| application_jdbc_connection_p95    | double  | TP95: The JDBC connection establishment duration in milliseconds for 95% user.                            |
| application_jdbc_connection_p98    | double  | TP98: The JDBC connection establishment duration in milliseconds for 98% user.                            |
| application_jdbc_connection_p99    | double  | TP99: The JDBC connection establishment duration in milliseconds for 99% user.                            |
| application_jdbc_connection_p999   | double  | TP99.9: The JDBC connection establishment duration in milliseconds for 99.9% user.                        |

#### Dedicated label
| Label Name | Essential |    Value Example     | Description                     |
|:-----------|:---------:|:--------------------:|:--------------------------------|
| url        |   true    | `jdbc:hsqldb:mem:7e` | The url of database connections |


### JVM Memory
JVM Memory schema describes key metrics of Java memory usage, which include:
* bytes-init
* bytes-used
* bytes-committed
* bytes-max


| Metric Name                            |  Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|:---------------------------------------|:-------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| application_jvm_memory_bytes_init      | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1).                                                                                                                                                                                            |
| application_jvm_memory_bytes_used      | integer | The value represents the amount of memory currently used in bytes unit.                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| application_jvm_memory_bytes_committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used.                                                                                                                                                                             |
| application_jvm_memory_bytes_max       | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). |

#### Dedicated label
| Label Name | Essential | Value Example      | Description                                                                                                                                                                                                    |
|:-----------|:---------:|:-------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| resource   |   true    | `pools.G1-Old-Gen` | Memory pool name. The Java virtual machine can have one or more memory pools. Reference list: [Platform](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.0/rest-reference/api-platform.html) |


### JVM GC
JVM GC schema describes key metrics of JVM garbage collection, which include:
* total_collection_time
* times
* times_rate

| Metric Name                              |  Type   | Description                                                                               |
|:-----------------------------------------|:-------:|:------------------------------------------------------------------------------------------|
| application_jvm_gc_total_collection_time | integer | The value represents the total time for garbage collection operation in millisecond unit. |
| application_jvm_gc_times                 | integer | The value represents the total garbage collection times.                                  |
| application_jvm_gc_times_rate            | integer | The number of gc times per second.                                                        |

#### Dedicated label
| Label Name | Essential |     Value Example     | Description                                                                                                                                                                                                                                                                                                                                          |
|:-----------|:---------:|:---------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| resource   |   true    | `G1 Young Generation` | GC name. Different JVM startup arguments will have different GC. Reference: [GC tuning](https://docs.oracle.com/en/java/javase/17/gctuning/introduction-garbage-collection-tuning.html#GUID-326EB4CF-8C8C-4267-8355-21AB04F0D304) , [Platform](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.0/rest-reference/api-platform.html) |


### Kafka Client
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

| Metric Name                  |  Type  | Description                                                                                          |
|:-----------------------------|:------:|:-----------------------------------------------------------------------------------------------------|
| application_kafka_prodrm1    | double | The executions per second (exponentially-weighted moving average) in last 1 minute (producer)        |
| application_kafka_prodrm5    | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| application_kafka_prodrm15   | double | The executions per second (exponentially-weighted moving average) in last 15 minute (producer)       |
| application_kafka_consrm1    | double | The executions per second (exponentially-weighted moving average) in last 1 minute (consumer)        |
| application_kafka_consrm5    | double | The executions per second (exponentially-weighted moving average) in last 5 minute (consumer)        |
| application_kafka_consrm15   | double | The executions per second (exponentially-weighted moving average) in last 15 minute (consumer)       |
| application_kafka_prodrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)  |
| application_kafka_prodrm5err | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| application_kafka_prodrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (producer) |
| application_kafka_consrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (consumer)  |
| application_kafka_consrm5err | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (consumer)  |
| application_kafka_consrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (consumer) |
| application_kafka_prodrmin   | double | The minimal execution duration in milliseconds.                                                      |
| application_kafka_prodrmax   | double | The maximal execution duration in milliseconds.                                                      |
| application_kafka_prodrmean  | double | The mean execution duration in milliseconds.                                                         |
| application_kafka_prodrp25   | double | TP25: The execution duration in milliseconds for 25% user.                                           |
| application_kafka_prodrp50   | double | TP50: The execution duration in milliseconds for 50% user.                                           |
| application_kafka_prodrp75   | double | TP75: The execution duration in milliseconds for 75% user.                                           |
| application_kafka_prodrp95   | double | TP95: The execution duration in milliseconds for 95% user.                                           |
| application_kafka_prodrp98   | double | TP98: The execution duration in milliseconds for 98% user.                                           |
| application_kafka_prodrp99   | double | TP99: The execution duration in milliseconds for 99% user.                                           |
| application_kafka_prodrp999  | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |
| application_kafka_consrmin   | double | The minimal execution duration in milliseconds.                                                      |
| application_kafka_consrmax   | double | The maximal execution duration in milliseconds.                                                      |
| application_kafka_consrmean  | double | The mean execution duration in milliseconds.                                                         |
| application_kafka_consrp25   | double | TP25: The execution duration in milliseconds for 25% user.                                           |
| application_kafka_consrp50   | double | TP50: The execution duration in milliseconds for 50% user.                                           |
| application_kafka_consrp75   | double | TP75: The execution duration in milliseconds for 75% user.                                           |
| application_kafka_consrp95   | double | TP95: The execution duration in milliseconds for 95% user.                                           |
| application_kafka_consrp98   | double | TP98: The execution duration in milliseconds for 98% user.                                           |
| application_kafka_consrp99   | double | TP99: The execution duration in milliseconds for 99% user.                                           |
| application_kafka_consrp999  | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |

#### Dedicated label
| Label Name | Essential | Value Example | Description |
|:-----------|:---------:|:-------------:|:------------|
| resource   |   true    |     `log`     | topic name  |


### RabbitMQ Producer
RabbitMQ Producer schema describes key metrics of RabbitMQ client publishing message, which include:
* Throughput (prodrm1, prodrm5, prodrm15)
* Error throughput (prodrm1err, prodrm5err, prodrm15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Metric Name                           |  Type  | Description                                                                                          |
|:--------------------------------------|:------:|:-----------------------------------------------------------------------------------------------------|
| application_rabbitmq_ex_ro_prodrm1    | double | The executions of producer per second (exponentially-weighted moving average) in last 1 minute       |
| application_rabbitmq_ex_ro_prodrm5    | double | The executions of producer per second (exponentially-weighted moving average) in last 5 minute       |
| application_rabbitmq_ex_ro_prodrm15   | double | The executionsof producer per second (exponentially-weighted moving average) in last 15 minute       |
| application_rabbitmq_ex_ro_prodrm1err | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (producer)  |
| application_rabbitmq_ex_ro_prodrm5err | double | The executions per second (exponentially-weighted moving average) in last 5 minute (producer)        |
| application_rabbitmq_ex_ro_prodrm5err | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (producer) |
| application_rabbitmq_ex_ro_min        | double | The producer minimal execution duration in milliseconds.                                             |
| application_rabbitmq_ex_ro_max        | double | The producer maximal execution duration in milliseconds.                                             |
| application_rabbitmq_ex_ro_mean       | double | The producer mean execution duration in milliseconds.                                                |
| application_rabbitmq_ex_ro_p25        | double | TP25: The producer execution duration in milliseconds for 25% user.                                  |
| application_rabbitmq_ex_ro_p50        | double | TP50: The producer execution duration in milliseconds for 50% user.                                  |
| application_rabbitmq_ex_ro_p75        | double | TP75: The producer execution duration in milliseconds for 75% user.                                  |
| application_rabbitmq_ex_ro_p95        | double | TP95: The producer execution duration in milliseconds for 95% user.                                  |
| application_rabbitmq_ex_ro_p98        | double | TP98: The producer execution duration in milliseconds for 98% user.                                  |
| application_rabbitmq_ex_ro_p99        | double | TP99: The producer execution duration in milliseconds for 99% user.                                  |
| application_rabbitmq_ex_ro_p999       | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                       |

#### Dedicated label
| Label Name | Essential |    Value Example     | Description                        |
|:-----------|:---------:|:--------------------:|:-----------------------------------|
| resource   |   true    | `myExchange-myQueue` | rabbitmq ${exchange}-${routingkey} |


### RabbitMQ Consumer
RabbitMQ Consumer schema describes key metrics of RabbitMQ client consuming message, which include:
* Throughput (queue_m1_rate, queue_m5_rate, queue_m15_rate)
* Error throughput (queue_m1_error_rate, queue_m5_error_rate, queue_m15_error_rate)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Metric Name                                        |  Type  | Description                                                                                       |
|:---------------------------------------------------|:------:|:--------------------------------------------------------------------------------------------------|
| application_rabbitmq_consumer_queue_m1_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 1 minute       |
| application_rabbitmq_consumer_queue_m5_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 5 minute       |
| application_rabbitmq_consumer_queue_m15_rate       | double | The executionsof queue per second (exponentially-weighted moving average) in last 15 minute       |
| application_rabbitmq_consumer_queue_m1_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)  |
| application_rabbitmq_consumer_queue_m5_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)  |
| application_rabbitmq_consumer_queue_m15_error_rate | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (queue) |
| application_rabbitmq_consumer_min                  | double | The consumer minimal execution duration in milliseconds.                                          |
| application_rabbitmq_consumer_max                  | double | The consumer maximal execution duration in milliseconds.                                          |
| application_rabbitmq_consumer_mean                 | double | The consumer mean execution duration in milliseconds.                                             |
| application_rabbitmq_consumer_p25                  | double | TP25: The consumer execution duration in milliseconds for 25% user.                               |
| application_rabbitmq_consumer_p50                  | double | TP50: The consumer execution duration in milliseconds for 50% user.                               |
| application_rabbitmq_consumer_p75                  | double | TP75: The consumer execution duration in milliseconds for 75% user.                               |
| application_rabbitmq_consumer_p95                  | double | TP95: The consumer execution duration in milliseconds for 95% user.                               |
| application_rabbitmq_consumer_p98                  | double | TP98: The consumer execution duration in milliseconds for 98% user.                               |
| application_rabbitmq_consumer_p99                  | double | TP99: The consumer execution duration in milliseconds for 99% user.                               |
| application_rabbitmq_consumer_p999                 | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                    |

#### Dedicated label
| Label Name | Essential | Value Example | Description         |
|:-----------|:---------:|:-------------:|:--------------------|
| resource   |   true    |   `myQueue`   | rabbitmq routingKey |


### Spring AMQP on Message Listener

Message Listener schema describes key metrics of Spring AMQP RabbitMQ Message Queue, which include:
* Throughput (queue_m1_rate, queue_m5_rate, queue_m15_rate)
* Error throughput (queue_m1_error_rate, queue_m5_error_rate, queue_m15_error_rate)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Metric Name                                     |  Type  | Description                                                                                       |
|:------------------------------------------------|:------:|:--------------------------------------------------------------------------------------------------|
| application_rabbitmq_queue_queue_m1_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 1 minute       |
| application_rabbitmq_queue_queue_m5_rate        | double | The executions of queue per second (exponentially-weighted moving average) in last 5 minute       |
| application_rabbitmq_queue_queue_m15_rate       | double | The executionsof queue per second (exponentially-weighted moving average) in last 15 minute       |
| application_rabbitmq_queue_queue_m1_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 1 minute (queue)  |
| application_rabbitmq_queue_queue_m5_error_rate  | double | The error executions per second (exponentially-weighted moving average) in last 5 minute (queue)  |
| application_rabbitmq_queue_queue_m15_error_rate | double | The error executions per second (exponentially-weighted moving average) in last 15 minute (queue) |
| application_rabbitmq_queue_min                  | double | The AMQP Message Listener minimal execution duration in milliseconds.                             |
| application_rabbitmq_queue_max                  | double | The AMQP Message Listener maximal execution duration in milliseconds.                             |
| application_rabbitmq_queue_mean                 | double | The AMQP Message Listener mean execution duration in milliseconds.                                |
| application_rabbitmq_queue_p25                  | double | TP25: The AMQP Message Listener execution duration in milliseconds for 25% user.                  |
| application_rabbitmq_queue_p50                  | double | TP50: The AMQP Message Listener execution duration in milliseconds for 50% user.                  |
| application_rabbitmq_queue_p75                  | double | TP75: The AMQP Message Listener execution duration in milliseconds for 75% user.                  |
| application_rabbitmq_queue_p95                  | double | TP95: The AMQP Message Listener execution duration in milliseconds for 95% user.                  |
| application_rabbitmq_queue_p98                  | double | TP98: The AMQP Message Listener execution duration in milliseconds for 98% user.                  |
| application_rabbitmq_queue_p99                  | double | TP99: The AMQP Message Listener execution duration in milliseconds for 99% user.                  |
| application_rabbitmq_queue_p999                 | double | TP99.9: The execution duration in milliseconds for 99.9% user.                                    |

#### Dedicated label
| Label Name | Essential | Value Example | Description    |
|:-----------|:---------:|:-------------:|:---------------|
| resource   |   true    |   `myQueue`   | rabbitmq queue |


### Elasticsearch
Elasticsearch schema describes key metrics of Elasticsearch client invoking, which include:
* Total execution count (cnt, errcnt, m1cnt, m5cnt, m15cnt)
* Throughput (m1, m5, m15, mean_rate)
* Error throughput (m1err, m5err, m15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Metric Name                         |  Type   | Description                                                                                                     |
|:------------------------------------|:-------:|:----------------------------------------------------------------------------------------------------------------|
| application_elasticsearch_cnt       | integer | The total count of the request executed                                                                         |
| application_elasticsearch_errcnt    | integer | The total error count of the request executed                                                                   |
| application_elasticsearch_m1cnt     | integer | The total count of the request executed in last 1 minute                                                        |
| application_elasticsearch_m5cnt     | integer | The total count of the request executed in last 5 minute                                                        |
| application_elasticsearch_m15cnt    | integer | The total count of the request executed in last 15 minute                                                       |
| application_elasticsearch_m1        | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 1 minute        |
| application_elasticsearch_m5        | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| application_elasticsearch_m15       | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| application_elasticsearch_mean_rate | double  | The Elasticsearch request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| application_elasticsearch_m1err     | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| application_elasticsearch_m5err     | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| application_elasticsearch_m15err    | double  | The Elasticsearch error request executions per second (exponentially-weighted moving average) in last 15 minute |
| application_elasticsearch_min       | double  | The Elasticsearch minimal execution duration in milliseconds.                                                   |
| application_elasticsearch_max       | double  | The Elasticsearch maximal execution duration in milliseconds.                                                   |
| application_elasticsearch_mean      | double  | The Elasticsearch mean execution duration in milliseconds.                                                      |
| application_elasticsearch_p25       | double  | TP25: The Elasticsearch execution duration in milliseconds for 25% user.                                        |
| application_elasticsearch_p50       | double  | TP50: The Elasticsearch execution duration in milliseconds for 50% user.                                        |
| application_elasticsearch_p75       | double  | TP75: The Elasticsearch execution duration in milliseconds for 75% user.                                        |
| application_elasticsearch_p95       | double  | TP95: The Elasticsearch execution duration in milliseconds for 95% user.                                        |
| application_elasticsearch_p98       | double  | TP98: The Elasticsearch execution duration in milliseconds for 98% user.                                        |
| application_elasticsearch_p99       | double  | TP99: The Elasticsearch execution duration in milliseconds for 99% user.                                        |

#### Dedicated label
| Label Name | Essential |      Value Example       | Description                  |
|:-----------|:---------:|:------------------------:|:-----------------------------|
| index      |   true    | `log-tracing-2022.01.11` | The Elasticsearch index name |


### MongoDB
MongoDB schema describes key metrics of MongoDB client invoking, which include:
* Total execution count (cnt, errcnt, m1cnt, m5cnt, m15cnt)
* Throughput (m1, m5, m15, mean_rate)
* Error throughput (m1err, m5err, m15err)
* Execution duration (min, mean, max)
* Latency (p25, p50, p75, p95, p98, p99)

| Metric Name                         |  Type   | Description                                                                                               |
|:------------------------------------|:-------:|:----------------------------------------------------------------------------------------------------------|
| application_mongodbclient_cnt       | integer | The total count of the request executed                                                                   |
| application_mongodbclient_errcnt    | integer | The total error count of the request executed                                                             |
| application_mongodbclient_m1cnt     | integer | The total count of the request executed in last 1 minute                                                  |
| application_mongodbclient_m5cnt     | integer | The total count of the request executed in last 5 minute                                                  |
| application_mongodbclient_m15cnt    | integer | The total count of the request executed in last 15 minute                                                 |
| application_mongodbclient_m1        | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 1 minute        |
| application_mongodbclient_m5        | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 5 minute.       |
| application_mongodbclient_m15       | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| application_mongodbclient_mean_rate | double  | The MongoDB request executions per second (exponentially-weighted moving average) in last 15 minute.      |
| application_mongodbclient_m1err     | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 1 minute  |
| application_mongodbclient_m5err     | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 5 minute. |
| application_mongodbclient_m15err    | double  | The MongoDB error request executions per second (exponentially-weighted moving average) in last 15 minute |
| application_mongodbclient_min       | double  | The MongoDB minimal execution duration in milliseconds.                                                   |
| application_mongodbclient_max       | double  | The MongoDB maximal execution duration in milliseconds.                                                   |
| application_mongodbclient_mean      | double  | The MongoDB mean execution duration in milliseconds.                                                      |
| application_mongodbclient_p25       | double  | TP25: The MongoDB execution duration in milliseconds for 25% user.                                        |
| application_mongodbclient_p50       | double  | TP50: The MongoDB execution duration in milliseconds for 50% user.                                        |
| application_mongodbclient_p75       | double  | TP75: The MongoDB execution duration in milliseconds for 75% user.                                        |
| application_mongodbclient_p95       | double  | TP95: The MongoDB execution duration in milliseconds for 95% user.                                        |
| application_mongodbclient_p98       | double  | TP98: The MongoDB execution duration in milliseconds for 98% user.                                        |
| application_mongodbclient_p99       | double  | TP99: The MongoDB execution duration in milliseconds for 99% user.                                        |

#### Dedicated label
| Label Name | Essential | Value Example | Description                                                                                                                            |
|:-----------|:---------:|:-------------:|:---------------------------------------------------------------------------------------------------------------------------------------|
| operation  |   true    |   `insert`    | The MongoDB request command name: insert, update or find etc. Reference: [Command](https://docs.mongodb.com/manual/reference/command/) |
