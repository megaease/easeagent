package com.megaease.easeagent.metrics;

import com.alibaba.fastjson.serializer.AutowiredObjectSerializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class MetricEvent {
    final String name;
    final Metric metric;
    final Map<String, String> tags;
    final TimeUnit rate;
    final TimeUnit duration;
    final long timestamp;

    MetricEvent(Metric metric, String name, Map<String, String> tags, TimeUnit rate, TimeUnit duration) {
        this.metric = metric;
        this.name = name;
        this.tags = tags;
        this.rate = rate;
        this.duration = duration;
        timestamp = System.currentTimeMillis();
    }

    @AutoService(AutowiredObjectSerializer.class)
    public static class Serializer implements AutowiredObjectSerializer {

        static final char SEPARATOR = ',';

        @Override
        public Set<Type> getAutowiredFor() {
            return Collections.singleton((Type) MetricEvent.class);
        }

        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
            if (object instanceof MetricEvent) {
                MetricEvent event = (MetricEvent) object;

                final SerializeWriter out = serializer.out;
                out.write('{');

                out.writeFieldName("@timestamp");
                out.writeLong(event.timestamp);

                out.writeFieldValue(SEPARATOR, "name", event.name);
                out.writeFieldValue(SEPARATOR, "type", event.name);

                serialize(out, event.tags);

                if (event.metric instanceof Metered) {
                    serialize(out, (Metered) event.metric, (double) event.rate.toSeconds(1L));
                }

                if (event.metric instanceof Sampling) {
                    serialize(out, ((Sampling) event.metric).getSnapshot(), 1.0D / (double) event.duration.toNanos(1L));
                }

                out.write('}');
            }

        }

        private void serialize(SerializeWriter out, Map<String, String> tags) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                out.writeFieldValue(SEPARATOR, entry.getKey(), entry.getValue());
            }
        }

        private void serialize(SerializeWriter out, Metered metered, double factor) {
            out.writeFieldValue(SEPARATOR, "count", metered.getCount());
            out.writeFieldValue(SEPARATOR, "mean_rate", metered.getMeanRate() * factor);
            out.writeFieldValue(SEPARATOR, "m1_rate", metered.getOneMinuteRate() * factor);
            out.writeFieldValue(SEPARATOR, "m5_rate", metered.getFiveMinuteRate() * factor);
            out.writeFieldValue(SEPARATOR, "m15_rate", metered.getFifteenMinuteRate() * factor);
            // TODO remove blew and count by ES
            out.writeFieldValue(SEPARATOR, "m1_count", 0);
            out.writeFieldValue(SEPARATOR, "m5_count", 0);
            out.writeFieldValue(SEPARATOR, "m15_count", 0);

        }

        private void serialize(SerializeWriter out, Snapshot snapshot, double factor) {
            out.writeFieldValue(SEPARATOR, "min", snapshot.getMin() * factor);
            out.writeFieldValue(SEPARATOR, "max", snapshot.getMax() * factor);
            out.writeFieldValue(SEPARATOR, "mean", snapshot.getMean() * factor);
            out.writeFieldValue(SEPARATOR, "median", snapshot.getMedian() * factor);
            out.writeFieldValue(SEPARATOR, "std", snapshot.getStdDev() * factor);
            out.writeFieldValue(SEPARATOR, "p25", snapshot.getValue(0.25) * factor);
            out.writeFieldValue(SEPARATOR, "p75", snapshot.get75thPercentile() * factor);
            out.writeFieldValue(SEPARATOR, "p95", snapshot.get95thPercentile() * factor);
            out.writeFieldValue(SEPARATOR, "p98", snapshot.get98thPercentile() * factor);
            out.writeFieldValue(SEPARATOR, "p99", snapshot.get99thPercentile() * factor);
            out.writeFieldValue(SEPARATOR, "p999", snapshot.get999thPercentile() * factor);
        }
    }

}
