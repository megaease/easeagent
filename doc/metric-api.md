#  Metric API

In Easeagent, Metric is inspired by Dropwizard, but Dropwizard is just the basis of Metric.

The use of time series indicators needs to be very rigorous.
 
In order to prevent the random use of metrics and the wrong use of metrics as relational databases, we have a strict standard for business metrics.

In order to be applicable to the business, we additionally encapsulate and define a dedicated interface.

These interfaces are related to business.


### 1. [com.megaease.easeagent.plugin.api.metric.name.MetricValueFetcher](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/name/MetricValueFetcher.java)

Metric has different types, and each type can calculate different data.

For example: Counter can only get Count, Timer can get Max, Mean, Min, P95 ...

Regarding the value that the type can calculate, we have encapsulated it:

```java
public enum MetricValueFetcher {
    CountingCount(Counter::getCount, Counter.class),
    SnapshotMaxValue(Snapshot::getMax, Snapshot.class),
    SnapshotMeanValue(Snapshot::getMean, Snapshot.class),
    SnapshotMinValue(Snapshot::getMin, Snapshot.class),
    Snapshot25Percentile(s -> s.getValue(0.25), Snapshot.class),
    SnapshotMedianValue(Snapshot::getMedian, Snapshot.class),
    Snapshot50PercentileValue(Snapshot::getMedian, Snapshot.class),
    Snapshot75PercentileValue(Snapshot::get75thPercentile, Snapshot.class),
    Snapshot95PercentileValue(Snapshot::get95thPercentile, Snapshot.class),
    Snapshot98PercentileValue(Snapshot::get98thPercentile, Snapshot.class),
    Snapshot99PercentileValue(Snapshot::get99thPercentile, Snapshot.class),
    Snapshot999PercentileValue(Snapshot::get999thPercentile, Snapshot.class),
    MeteredM1Rate(Meter::getOneMinuteRate, Meter.class),
    MeteredM1RateIgnoreZero(Meter::getOneMinuteRate, Meter.class, aDouble -> aDouble),
    MeteredM5Rate(Meter::getFiveMinuteRate, Meter.class),
    MeteredM15Rate(Meter::getFifteenMinuteRate, Meter.class),
    MeteredMeanRate(Meter::getMeanRate, Meter.class),
    MeteredCount(Meter::getCount, Meter.class);
}
```

### 2. [com.megaease.easeagent.plugin.api.metric.name.MetricField](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/name/MetricField.java)

Different types will use different names depending on the business.

For example, the Count value of Counter and Timer can be named TotalCount or ErrorCount according to success or failure.

We have made certain specifications for the name:

```java
public enum MetricField {
    MIN_EXECUTION_TIME("min", ConverterType.DURATION, 2),
    MAX_EXECUTION_TIME("max", ConverterType.DURATION, 2),
    MEAN_EXECUTION_TIME("mean", ConverterType.DURATION, 2),
    P25_EXECUTION_TIME("p25", ConverterType.DURATION, 2),
    P50_EXECUTION_TIME("p50", ConverterType.DURATION, 2),
    P75_EXECUTION_TIME("p75", ConverterType.DURATION, 2),
    P95_EXECUTION_TIME("p95", ConverterType.DURATION, 2),
    P98_EXECUTION_TIME("p98", ConverterType.DURATION, 2),
    P99_EXECUTION_TIME("p99", ConverterType.DURATION, 2),
    P999_EXECUTION_TIME("p999", ConverterType.DURATION, 2),
    STD("std"),
    EXECUTION_COUNT("cnt"),
    EXECUTION_ERROR_COUNT("errcnt"),
    M1_RATE("m1", ConverterType.RATE, 5),
    M5_RATE("m5", ConverterType.RATE, 5),
    M15_RATE("m15", ConverterType.RATE, 5),
    RETRY_M1_RATE("retrym1", ConverterType.RATE, 5),
    RETRY_M5_RATE("retrym5", ConverterType.RATE, 5),
    RETRY_M15_RATE("retrym15", ConverterType.RATE, 5),
    RATELIMITER_M1_RATE("rlm1", ConverterType.RATE, 5),
    RATELIMITER_M5_RATE("rlm5", ConverterType.RATE, 5),
    RATELIMITER_M15_RATE("rlm15", ConverterType.RATE, 5),
    CIRCUITBREAKER_M1_RATE("cbm1", ConverterType.RATE, 5),
    CIRCUITBREAKER_M5_RATE("cbm5", ConverterType.RATE, 5),
    CIRCUITBREAKER_M15_RATE("cbm15", ConverterType.RATE, 5),
    MEAN_RATE("mean_rate", ConverterType.RATE, 5),
    M1_ERROR_RATE("m1err", ConverterType.RATE, 5),
    M5_ERROR_RATE("m5err", ConverterType.RATE, 5),
    M15_ERROR_RATE("m15err", ConverterType.RATE, 5),
    M1_COUNT("m1cnt", ConverterType.RATE, 0),
    M5_COUNT("m5cnt", ConverterType.RATE, 0),
    M15_COUNT("m15cnt", ConverterType.RATE, 0),
    TIMES_RATE("time_rate", ConverterType.RATE, 5),
    TOTAL_COLLECTION_TIME("total_collection_time", ConverterType.RATE, 0),
    TIMES("times", ConverterType.RATE, 0),
    /* channel is for rabbitmq */
    CHANNEL_M1_RATE("channel_m1_rate", ConverterType.RATE, 5),
    CHANNEL_M5_RATE("channel_m5_rate", ConverterType.RATE, 5),
    CHANNEL_M15_RATE("channel_m15_rate", ConverterType.RATE, 5),
    QUEUE_M1_RATE("queue_m1_rate", ConverterType.RATE, 5),
    QUEUE_M5_RATE("queue_m5_rate", ConverterType.RATE, 5),
    QUEUE_M15_RATE("queue_m15_rate", ConverterType.RATE, 5),
    QUEUE_M1_ERROR_RATE("queue_m1_error_rate", ConverterType.RATE, 5),
    QUEUE_M5_ERROR_RATE("queue_m5_error_rate", ConverterType.RATE, 5),
    QUEUE_M15_ERROR_RATE("queue_m15_error_rate", ConverterType.RATE, 5),
    /*producer and consumer is for message kafka rabbitmq service*/
    PRODUCER_M1_RATE("prodrm1", ConverterType.RATE, 5),
    PRODUCER_M5_RATE("prodrm5", ConverterType.RATE, 5),
    PRODUCER_M15_RATE("prodrm15", ConverterType.RATE, 5),
    PRODUCER_M1_ERROR_RATE("prodrm1err", ConverterType.RATE, 5),
    PRODUCER_M5_ERROR_RATE("prodrm5err", ConverterType.RATE, 5),
    PRODUCER_M15_ERROR_RATE("prodrm15err", ConverterType.RATE, 5),
    CONSUMER_M1_RATE("consrm1", ConverterType.RATE, 5),
    CONSUMER_M5_RATE("consrm5", ConverterType.RATE, 5),
    CONSUMER_M15_RATE("consrm15", ConverterType.RATE, 5),
    CONSUMER_M1_ERROR_RATE("consrm1err", ConverterType.RATE, 5),
    CONSUMER_M5_ERROR_RATE("consrm5err", ConverterType.RATE, 5),
    CONSUMER_M15_ERROR_RATE("consrm15err", ConverterType.RATE, 5),
    EXECUTION_PRODUCER_ERROR_COUNT("prodrerrcnt"),
    EXECUTION_CONSUMER_ERROR_COUNT("consrerrcnt"),
    EXECUTION_PRODUCER_COUNT("prodrcnt"),
    EXECUTION_CONSUMER_COUNT("consrcnt"),
    PRODUCER_MIN_EXECUTION_TIME("prodrmin", ConverterType.DURATION, 2),
    PRODUCER_MAX_EXECUTION_TIME("prodrmax", ConverterType.DURATION, 2),
    PRODUCER_MEAN_EXECUTION_TIME("prodrmean", ConverterType.DURATION, 2),
    PRODUCER_P25_EXECUTION_TIME("prodrp25", ConverterType.DURATION, 2),
    PRODUCER_P50_EXECUTION_TIME("prodrp50", ConverterType.DURATION, 2),
    PRODUCER_P75_EXECUTION_TIME("prodrp75", ConverterType.DURATION, 2),
    PRODUCER_P95_EXECUTION_TIME("prodrp95", ConverterType.DURATION, 2),
    PRODUCER_P98_EXECUTION_TIME("prodrp98", ConverterType.DURATION, 2),
    PRODUCER_P99_EXECUTION_TIME("prodrp99", ConverterType.DURATION, 2),
    PRODUCER_P999_EXECUTION_TIME("prodrp999", ConverterType.DURATION, 2),
    CONSUMER_MIN_EXECUTION_TIME("consrmin", ConverterType.DURATION, 2),
    CONSUMER_MAX_EXECUTION_TIME("consrmax", ConverterType.DURATION, 2),
    CONSUMER_MEAN_EXECUTION_TIME("consrmean", ConverterType.DURATION, 2),
    CONSUMER_P25_EXECUTION_TIME("consrp25", ConverterType.DURATION, 2),
    CONSUMER_P50_EXECUTION_TIME("consrp50", ConverterType.DURATION, 2),
    CONSUMER_P75_EXECUTION_TIME("consrp75", ConverterType.DURATION, 2),
    CONSUMER_P95_EXECUTION_TIME("consrp95", ConverterType.DURATION, 2),
    CONSUMER_P98_EXECUTION_TIME("consrp98", ConverterType.DURATION, 2),
    CONSUMER_P99_EXECUTION_TIME("consrp99", ConverterType.DURATION, 2),
    CONSUMER_P999_EXECUTION_TIME("consrp999", ConverterType.DURATION, 2),
    NONE("", ConverterType.RATE, 0);
}
```

### 3. [com.megaease.easeagent.plugin.api.metric.name.NameFactory](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/name/NameFactory.java)

According to the value type and naming combination, we standardize a NameFactory.

#### Example:

```java
class ServerMetric{
    @Nonnull
    public static NameFactory nameFactory() {
        return NameFactory.createBuilder()
            .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
                .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
                .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
                .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                .build())
            .counterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .meterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                .build())
            .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
            .timerType(MetricSubType.DEFAULT,
                ImmutableMap.<MetricField, MetricValueFetcher>builder()
                    .put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
                    .put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
                    .put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
                    .put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
                    .put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
                    .put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
                    .put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
                    .put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
                    .put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
                    .put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
                    .build())
            .build();
    }
}
```

### 4. Registry
 
Inherit the ServiceMetric class in order to obtain the MetricRegistry.
[com.megaease.easeagent.plugin.api.metric.ServiceMetric](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/ServiceMetric.java)

Use the ServiceMetricRegistry interface to register and create a singleton.

[com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/ServiceMetricRegistry.java)

##### Config

Metric configuration follows plugin configuration rules [metric config](user-manual.md#metric)

##### Tags

Metrics have values as well as tags. EaseAgent supports custom tags, but these tags must be predictable and given in advance.

To better support the business, three tags must be given: category，type and key

So it is fixed in Tags and must provide three pieces of information: category，type and keyFieldName

[com.megaease.easeagent.plugin.api.metric.name.Tags](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/name/Tags.java)

Its tag is copied as follows:
```
output.put("category", tags.category)
output.put("type", tags.type)
output.put(tags.keyFieldName, {@link NameFactory}.key[?])
tags.tags.forEach((k,v)->{
    output.put(k,v)
})
```

##### Singleton

The Key of the singleton is: `domain`, `namespace`, `id`, `tags` and the `type` of class.


#### Example:
```java
public class ServerMetric extends ServiceMetric {

    public ServerMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
        super(metricRegistry, nameFactory);
    }

    public void collectMetric(String key, int statusCode, Throwable throwable, long startMillis, long endMillis) {
        Timer timer = metricRegistry.timer(nameFactory.timerName(key, MetricSubType.DEFAULT));
        timer.update(Duration.ofMillis(endMillis - startMillis));
        final Meter errorMeter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.ERROR));
        final Meter meter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.DEFAULT));
        Counter errorCounter = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.ERROR));
        Counter counter = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT));
        boolean hasException = throwable != null;
        if (statusCode >= 400 || hasException) {
            errorMeter.mark();
            errorCounter.inc();
        }
        counter.inc();
        meter.mark();

        metricRegistry.gauge(nameFactory.gaugeName(key, MetricSubType.DEFAULT), () -> () -> {
            BigDecimal m1ErrorPercent = BigDecimal.ZERO;
            BigDecimal m5ErrorPercent = BigDecimal.ZERO;
            BigDecimal m15ErrorPercent = BigDecimal.ZERO;
            BigDecimal error = BigDecimal.valueOf(errorMeter.getOneMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal n = BigDecimal.valueOf(meter.getOneMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m1ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            error = BigDecimal.valueOf(errorMeter.getFiveMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFiveMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m5ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }

            error = BigDecimal.valueOf(errorMeter.getFifteenMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFifteenMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m15ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            return new ErrorPercentModelGauge(m1ErrorPercent, m5ErrorPercent, m15ErrorPercent);
        });
    }

    @Nonnull
    public static NameFactory nameFactory() {
        return NameFactory.createBuilder()
            .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
                .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
                .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
                .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                .build())
            .counterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .meterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                .build())
            .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
            .timerType(MetricSubType.DEFAULT,
                ImmutableMap.<MetricField, MetricValueFetcher>builder()
                    .put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
                    .put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
                    .put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
                    .put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
                    .put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
                    .put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
                    .put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
                    .put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
                    .put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
                    .put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
                    .build())
            .build();
    }
}

public class DoFilterMetricInterceptor extends Interceptor {
    private static volatile ServerMetric SERVER_METRIC = null;
    private static final Object START_MACK = new Object();
    
    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        SERVER_METRIC = ServiceMetricRegistry.getOrCreate(config, new Tags("application", "http-request", "url"),
            new ServiceMetricSupplier<ServerMetric>() {
                @Override
                public NameFactory newNameFactory() {
                    return ServerMetric.nameFactory();
                }
        
                @Override
                public ServerMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                    return new ServerMetric(metricRegistry, nameFactory);
                }
            });
    }
    
    @Override
    public void before(MethodInfo methodInfo, Context context){
        context.put(START_MACK, System.currentTimeMillis());
    }
    
    @Override
    public void after(MethodInfo methodInfo, Context context){
        long start = context.remove(START_MACK);
        long end = System.currentTimeMillis();
        String key = "GET /demo";
        int statusCode = 200;
        SERVER_METRIC.collectMetric(key, statusCode, throwable, start, end);
    }
}
```

### 5. Metric

Even though EaseAgent's Metric is inspired by Dropwizard, it still hopes to decouple its implementation from Dropwizard.

So EaseAgent has its own API, although the implementation scheme currently used is Dropwizard.

[com.megaease.easeagent.plugin.api.metric.MetricRegistry](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/MetricRegistry.java)

[com.megaease.easeagent.plugin.api.metric.Counter](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/Counter.java)

[com.megaease.easeagent.plugin.api.metric.Histogram](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/Histogram.java)

[com.megaease.easeagent.plugin.api.metric.Meter](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/Meter.java)

[com.megaease.easeagent.plugin.api.metric.Snapshot](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/Snapshot.java)

[com.megaease.easeagent.plugin.api.metric.Timer](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/metric/Timer.java)


### 6. customize

When you are sure that you want to implement your own metric, then you only need a metric output class, and finally use this output class to output to Kafka or backend. 

We provide such an output interface, this interface accepts string output.

The standard of EaseMonitor is to use json format, if unnecessary, please use json as output.

[com.megaease.easeagent.plugin.api.Reporter](../plugin-api/src/main/java/com/megaease/easeagent/plugin/api/Reporter.java)

[com.megaease.easeagent.plugin.bridge.EaseAgent.metricReporter](../plugin-api/src/main/java/com/megaease/easeagent/plugin/bridge/EaseAgent.java)

The obtained Reporter is a singleton, and the key of the singleton is `namespace`.

Its output configuration complies with metric configuration rules: [metric config](user-manual.md#metric)

#### Example:
```java
public class MD5ReportConsumer {
    private final Config config;
    private final Reporter reporter;
    public MD5ReportConsumer() {
        this.config = AutoRefreshRegistry.getOrCreate("observability", "md5Dictionary", "metric");
        this.reporter = EaseAgent.metricReporter(config);
    }

    
    @Override
    public void accept(Map<String, String> map) {
        if (!this.config.enabled()) {
            return;
        }
        map.put("host_name", HostAddress.localhost());
        map.put("host_ipv4", HostAddress.getHostIpv4());
        map.put("category", "application");
        String json = JsonUtil.toJson(item);
        this.reporter.report(json);
    }
}
```
