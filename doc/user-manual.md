- [Metric Detail](#Metric-Detail)
- [Metric Field](#Metric-Field)
  - [Http Request](#Http-Request)
  - [JDBC Statement](#JDBC-Statement)
  - [JDBC Connection](#JDBC-Connection)
  - [JVM Memory](#JVM-Memory)
  - [JVM GC](#JVM-GC)
  - [Kafka Client](#Kafka-Client)
  - [RabbitMq Producer](#RabbitMq-Producer)
  - [RabbitMq Consumer](#RabbitMq-Consumer)

## Metric Detail

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

## Metric Field
Metric information output format is JSON

### Http Request
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

### JDBC Statement
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

### JDBC Connection
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

### JVM Memory
| Field           |  Type   | Description                                                  | Desc2 |
| :-------------- | :-----: | :----------------------------------------------------------- | ------- | 
| bytes-init      | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1). | JVM initial memory |
| bytes-used      | integer | The value represents the amount of memory currently used in bytes unit. | JVM used memory |
| bytes-committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used. | JVM commited memory |
| bytes-max       | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). | JVM max memory |

### JVM GC
| Field                 |  Type   | Description                                                  | Desc2 | 
| :-------------------- | :-----: | :----------------------------------------------------------- | ------- |
| total_collection_time | integer |The value represents the total time for garbage collection operation in millisecond unit. | JVM Gc time | 
| times                 | integer |  The value represents the total garbage collection times.     | JVM Gc collection times |
| times_rate            | integer |  The number of gc times per second.                           | JVM Gc times per second |


### Kafka Client
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

### RabbitMq Producer
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

### RabbitMq Consumer
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