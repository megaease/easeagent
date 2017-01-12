package com.hexdecteam.easeagent;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.stagemonitor.core.metrics.metrics2.MeterExt;
import org.stagemonitor.core.metrics.metrics2.TimerExt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class MetricsXModule extends Module {
    private static final Version VERSION = new Version(0, 1, 0, "", "com.hexdecteam.easeagent", "metrics-stagemonitor");

    private final TimeUnit rateUnit;
    private final TimeUnit durationUnit;

    MetricsXModule(TimeUnit rateUnit, TimeUnit durationUnit) {
        this.rateUnit = rateUnit;
        this.durationUnit = durationUnit;
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        JavaType type = setupContext.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        setupContext.addSerializers(new SimpleSerializers(
                Arrays.<JsonSerializer<?>>asList(
                        new MapSerializer(type),
                        new GaugeSerializer(),
                        new CounterSerializer(),
                        new SnapshotSerializer(durationUnit),
                        new HistogramSerializer(),
                        new MeterSerializer(rateUnit),
                        new TimerSerializer()
                )
        ));
    }

    @Override
    public String getModuleName() {
        return "metricX";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    private static class MapSerializer extends StdSerializer<Map<String, String>> {

        MapSerializer(JavaType type) {
            super(type);
        }

        @Override
        public void serialize(Map<String, String> map, JsonGenerator json, SerializerProvider provider)
                throws IOException {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                json.writeStringField(entry.getKey(), entry.getValue());
            }
        }
    }

    private static class GaugeSerializer extends StdSerializer<Gauge> {
        private GaugeSerializer() {
            super(Gauge.class);
        }

        @Override
        public void serialize(Gauge gauge,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            final Object value = gauge.getValue();
            if (value == null) return;

            if (value instanceof Number) {
                json.writeNumberField("value", ((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                json.writeBooleanField("value_boolean", (Boolean) value);
            } else {
                json.writeStringField("value_string", value.toString());
            }

        }
    }

    private static class CounterSerializer extends StdSerializer<Counter> {
        private CounterSerializer() {
            super(Counter.class);
        }

        @Override
        public void serialize(Counter counter,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeNumberField("count", counter.getCount());
        }
    }

    private static class SnapshotSerializer extends StdSerializer<Snapshot> {

        private final double durationFactor;

        SnapshotSerializer(TimeUnit durationUnit) {
            super(Snapshot.class);
            this.durationFactor = 1.0 / durationUnit.toNanos(1);
        }

        @Override
        public void serialize(Snapshot snapshot, JsonGenerator json, SerializerProvider provider) throws IOException {
            json.writeNumberField("max", snapshot.getMax() * durationFactor);
            json.writeNumberField("mean", snapshot.getMean() * durationFactor);
            json.writeNumberField("min", snapshot.getMin() * durationFactor);
            json.writeNumberField("std", snapshot.getStdDev() * durationFactor);
            json.writeNumberField("median", snapshot.getMedian() * durationFactor);
            json.writeNumberField("p25", snapshot.getValue(0.25) * durationFactor);
            json.writeNumberField("p75", snapshot.get75thPercentile() * durationFactor);
            json.writeNumberField("p95", snapshot.get95thPercentile() * durationFactor);
            json.writeNumberField("p98", snapshot.get98thPercentile() * durationFactor);
            json.writeNumberField("p99", snapshot.get99thPercentile() * durationFactor);
            json.writeNumberField("p999", snapshot.get999thPercentile() * durationFactor);
        }
    }

    private static class HistogramSerializer extends StdSerializer<Histogram> {

        private HistogramSerializer() {
            super(Histogram.class);
        }

        @Override
        public void serialize(Histogram histogram,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            final Snapshot snapshot = histogram.getSnapshot();
            json.writeNumberField("count", histogram.getCount());
            json.writeObject(snapshot);
        }
    }

    private static class MeterSerializer extends StdSerializer<MeterExt> {
        private final double rateFactor;

        MeterSerializer(TimeUnit rateUnit) {
            super(MeterExt.class);
            this.rateFactor = rateUnit.toSeconds(1);
        }

        @Override
        public void serialize(MeterExt meter,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeNumberField("count", meter.getCount());
            json.writeNumberField("m15_rate", meter.getFifteenMinuteRate() * rateFactor);
            json.writeNumberField("m1_rate", meter.getOneMinuteRate() * rateFactor);
            json.writeNumberField("m5_rate", meter.getFiveMinuteRate() * rateFactor);
            json.writeNumberField("mean_rate", meter.getMeanRate() * rateFactor);
            json.writeNumberField("m1_count", meter.getOneMinuteCount());
            json.writeNumberField("m5_count", meter.getFiveMinuteCount());
            json.writeNumberField("m15_count", meter.getFifteenMinuteCount());
        }
    }

    private static class TimerSerializer extends StdSerializer<TimerExt> {

        private TimerSerializer() {
            super(TimerExt.class);
        }

        @Override
        public void serialize(TimerExt timer,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeObject(timer.getSnapshot());
            json.writeObject(timer.getMeterExt());
        }

    }

}
