# Plugin Development Guide
- [Overview](#Overview)
- [Plugin Structure](#Plugin-structure)
    - [The Simplest Plugin](#the-simplest-plugin)
- [Tracing API](#Tracing-API)
- [Metric API](#Metirc-API)
- [Redirect API](#Redirect-API)

## Overview
Most of easeagent's functionalities are supported by plugins.   
This document describes how to develop plugins for easeagent, and it will be divided into four sections to introduce plugin development.
1. Plugin structure, the plugin contains four components, which are the plugin definition, matcher, interceptor and the binding annotations between the three.
2. Tracing API, which helps users complete the transaction tracing task.
3. Metirc API, helps users to complete metrics data collection.
4. Redirect API

##  Plugin Structure
### The Simplest Plugin

##  Tracing API
##  Metric API
##  Redirect API


The main function of EaseAgent is to collect Java method call trace and metrics data.
Developer need to understand trace and metric before development.
* [metrics](https://github.com/dropwizard/metrics)
* [brave](https://github.com/openzipkin/brave)

Instrumenting the method base on [Byte buddy](https://github.com/raphw/byte-buddy) technology.

## Example For Enhancing Apache HttpClient4.5
User want to get tracing data from `Apache HttpClient`.

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
package ...
import ...

@Injection.Provider(Provider.class)
public abstract class HttpClientAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.apache.http.client.HttpClient"))) // enhanced client class
                .transform(adviceExecute(named("execute"))) // "execute" is the name of HttpClient
                .end();
    }

    @AdviceTo(Execute.class)
    protected abstract Definition.Transformer adviceExecute(ElementMatcher<? super MethodDescription> matcher);

    public static class Execute extends AbstractAdvice {

        @Injection.Autowire
        public Execute(
                @Injection.Qualifier("supplier4HttpClient") Supplier<AgentInterceptorChain.Builder> supplier,
                AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.Origin Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}

```

### Step 3
Create `AgentInterceptor` in module `zipkin`
```java
package ...
import ...

public class HttpClientTracingInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        // process tracing or metric for method before
        System.out.println("method before");
        // finally, user can invoke next interceptor
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        // process tracing or metric for method after
        System.out.println("method after");
        // finally, user can invoke next interceptor
        return AgentInterceptor.super.after(methodInfo, context, chain);
    }
}
```
Developer can refer to [FeignClientTracingInterceptor](https://github.com/megaease/easeagent/blob/master/zipkin/src/main/java/com/megaease/easeagent/zipkin/http/FeignClientTracingInterceptor.java) and
[brave-instrumentation-http](https://github.com/openzipkin/brave/tree/master/instrumentation/http) to learn how to write code with `brave`.

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

### Step 6
Build and run your application.
```
mvn clean package -am -pl build
```

### Example HttpClient

```java
CloseableHttpClient httpclient = HttpClients.createDefault();
HttpGet httpGet = new HttpGet("https://httpbin.org/get");
String str;
try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
    System.out.println(response.getStatusLine());
    HttpEntity entity = response.getEntity();
    str = EntityUtils.toString(entity);
    // do something useful with the response body
    // and ensure it is fully consumed
    EntityUtils.consume(entity);
}
```

## Metric 
EaseAgent use [io.dropwizard.metrics](https://github.com/dropwizard/metrics) to collect metric data.

### Metric Key
`MetricRegistry` need `metric key` parameter to create `Meter`、 `Counters`、 etc. In EaseAgent, `Metric key` should contain `SubType`、`Type`、`SubKey`.
Developer can refer to these files to learn how to write the code to collect metric data.
* [AbstractJdbcMetric](https://github.com/megaease/easeagent/blob/master/metrics/src/main/java/com/megaease/easeagent/metrics/jdbc/AbstractJdbcMetric.java)
 * [JdbcStmMetricInterceptor](https://github.com/megaease/easeagent/blob/master/metrics/src/main/java/com/megaease/easeagent/metrics/jdbc/interceptor/JdbcStmMetricInterceptor.java) 

#### For Example:

The application send message to Kafka topic `demo-topic`. EaseAgent will collect Counters(success and error) data.
```
SubKey = "demo-topic"
Type = "3"
SubType = "04" // For Producer success

The producer's Counters metric name for success is [04]+[3]+[demo-topic] = 043demo-topic
 
SubType = "06" // For Producer error
The producer's Counters metric key for error is [06]+[3]+[demo-topic] = 063demo-topic 
```
**The metric key is `SubType`+`Type`+`SubKey`**

#### Type And SubType Detail   
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
ERROR | 01 | Method call has error or HTTP response 500, etc
CHANNEL | 02 | RabbitMq publish message to channel or queue
CONSUMER | 03 | RabbitMq or Kafka consume message
PRODUCER | 04 | Kafka send message to topic
CONSUMER_ERROR | 05 | RabbitMq or Kafka consume message has error
PRODUCER_ERROR | 06 | Kafka send message has error

### Metric Field
Metric data output format is JSON.
* Example
```json
{
  "cnt" : 11,
  "url" : "jdbc:mysql:\/\/localhost:3306\/db_demo",
  "m5" : 2.16364,
  "max" : 2557,
  "mean" : 284.19,
  "p98" : 2557,
  "host_name" : "user-host-name",
  "m1cnt" : 120,
  "min" : 0,
  "category" : "application",
  "system" : "user-system",
  "type" : "jdbc-connection",
  "p99" : 2557,
  "m5cnt" : 600,
  "p95" : 2557,
  "m15" : 2.1878099999999998,
  "timestamp" : 1624966154443,
  "service" : "user-service",
  "m1" : 2.0240999999999998,
  "p25" : 43,
  "m15cnt" : 1800,
  "p75" : 87,
  "p50" : 55,
  "host_ipv4" : "192.168.2.2",
  "p999" : 2557
}
```

For different type, the JSON field will be different. The detail of metric field is as follows:

#### HTTP Request
| Field | Type | Description | Desc2 |
| ----- | ---- | ----------- | ------- |
| url                   |string|the URL of the request          | url |
| cnt       |integer| The total count of the request executed | Topn HTTP request total count|
| m1               |double| The HTTP request executions per second (exponentially-weighted moving average) in last 1 minute | TopN HTTP request M1 rate|
| m5               |double| The HTTP request executions per second (exponentially-weighted moving average) in last 5 minute. | TopN HTTP request M5 rate |
| m15              |double| The HTTP request executions per second (exponentially-weighted moving average) in last 15 minute. | TopN HTTP request M15 rate |
| errcnt |integer| The total error count of the request executed | Topn HTTP request total error count |
| m1err         |integer| The HTTP error request executions per second (exponentially-weighted moving average) in last 1 minute | TopN HTTP request M1 error rate |
| m5err         |integer|| The HTTP error request executions per second (exponentially-weighted moving average) in last 5 minute. | Topn HTTP request M5 error rate |
| m15err        |integer| The HTTP error request executions per second (exponentially-weighted moving average) in last 15 minute | TopN HTTP request M15 error rate |
| m1errpct      |double| error percentage in last 1 minute | Topn HTTP request M1 error percent |
| m5errpct      |double| error percentage in last 5 minute | Topn HTTP request M5 error percent |
| m15errpct     |double| error percentage in last 15 minute | Topn HTTP request M15 error percent |
|min|double|The http-request minimal execution duration in milliseconds.| TopN HTTP request min execution duration |
|max|double|The http-request maximal execution duration in milliseconds.| TopN HTTP request max execution duration |
|mean|double|The http-request mean execution duration in milliseconds.| TopN HTTP request mean execution duration |
|p25|double|TP25: The http-request execution duration in milliseconds for 25% user.| TopN HTTP request P25 execution duration |
|p50|double|TP50: The http-request execution duration in milliseconds for 50% user.| TopN HTTP request P50 execution duration |
|p75|double|TP75: The http-request execution duration in milliseconds for 75% user.| TopN HTTP request P75 execution duration |
|p95|double|TP95: The http-request execution duration in milliseconds for 95% user.| TopN HTTP request P95 execution duration |
|p98|double|TP98: The http-request execution duration in milliseconds for 98% user.| TopN HTTP request P98 execution duration |
|p99|double|TP99: The http-request execution duration in milliseconds for 99% user.| TopN HTTP request P99 execution duration |

#### JDBC Statement
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

#### JDBC Connection
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

#### JVM Memory
| Field           |  Type   | Description                                                  | Desc2 |
| :-------------- | :-----: | :----------------------------------------------------------- | ------- | 
| bytes-init      | integer | The value represents the initial amount of memory in bytes unit that the JVM requests from the operating system for memory management during startup. The JVM may request additional memory from the operating system and may also release memory to the system over time. The value of init may be undefined (value -1). | JVM initial memory |
| bytes-used      | integer | The value represents the amount of memory currently used in bytes unit. | JVM used memory |
| bytes-committed | integer | The value represents the amount of memory in bytes unit that is guaranteed to be available for use by the JVM. The amount of committed memory may change over time (increase or decrease). The JVM may release memory to the system and committed could be less than init. Value committed will always be greater than or equal to used. | JVM commited memory |
| bytes-max       | integer | The value represents the maximum amount of memory in bytes unit that can be used for memory management. Its value may be undefined (value -1). The maximum amount of memory may change over time if defined. The amount of used and committed memory will always be less than or equal to max if max is defined. A memory allocation may fail if it attempts to increase the used memory such that used > committed even if used <= max would still be true (for example, when the system is low on virtual memory). | JVM max memory |

#### JVM GC
| Field                 |  Type   | Description                                                  | Desc2 | 
| :-------------------- | :-----: | :----------------------------------------------------------- | ------- |
| total_collection_time | integer |The value represents the total time for garbage collection operation in millisecond unit. | JVM Gc time | 
| times                 | integer |  The value represents the total garbage collection times.     | JVM Gc collection times |
| times_rate            | integer |  The number of gc times per second.                           | JVM Gc times per second |

#### Kafka Client
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

#### RabbitMq Producer
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

#### RabbitMq Consumer
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

#### Redis Indicator 
| Field               |  Type   | Description                                                  | Desc2 |
| :------------------ | :-----: | :----------------------------------------------------------- | ------- |
|m1|double|The executions per second (exponentially-weighted moving average) in last 1 minute.| redis M1 rate |
|m5|double|The executions per second (exponentially-weighted moving average) in last 5 minutes.| redis M5 rate |
|m15|double|The executions per second (exponentially-weighted moving average) in last 15 minutes.| redis M15 rate |
|meanrate|double|The mean executions per second (exponentially-weighted moving average).| redis mean executions per second |
|m1err|double|The failure executions per second (exponentially-weighted moving average) in last 1 minutes.| redis M1 error rate |
|m5err|double|The failure executions per second (exponentially-weighted moving average) in last 5 minutes.| redis M5 error rate |
|m15err|double|The failure executions per second (exponentially-weighted moving average) in last 15 minutes.| redis M15 error rate |
|m1cnt|integer|The execution count in last 1 minute.| redis M1 count |
|m5cnt|integer|The execution count in last 5 minutes.| redis M5 count |
|m15cnt|integer|The execution count in last 15 minutes.| redis M15 count |
|min|double|The minimal execution duration in milliseconds.| redis min execution duration |
|max|double|The maximal execution duration in milliseconds.| redis max execution duration |
|mean|double|The mean execution duration in milliseconds.| redismean execution duration |
|p25|double|TP25: The execution duration in milliseconds for 25% user.| redis P25 execution duration |
|p50|double|TP50: The execution duration in milliseconds for 50% user.| redis P50 execution duration |
|p75|double|TP75: The execution duration in milliseconds for 75% user.| redis P75 execution duration |
|p95|double|TP95: The execution duration in milliseconds for 95% user.| redis P95 execution duration |
|p98|double|TP98: The execution duration in milliseconds for 98% user.| redis P98 execution duration |
|p99|double|TP99: The execution duration in milliseconds for 99% user.| redis P99 execution duration |
|p999|double|TP99.9: The execution duration in milliseconds for 99.9% user.| redis P999 execution duration |
