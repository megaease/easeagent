package com.megaease.easeagent.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

class Metrics {
    static final Joiner.MapJoiner JOINER = Joiner.on(',').withKeyValueSeparator('=');

    private final MetricRegistry registry;

    Metrics(MetricRegistry registry) {this.registry = registry;}

    MeterName meter(String name) {
        return new MeterName(name, Collections.<String, String>emptyMap());
    }

    TimerName timer(String name) {
        return new TimerName(name, Collections.<String, String>emptyMap());
    }

    class MeterName extends FlatName<MeterName, Meter> {

        private MeterName(String name, Map<String, String> tags) {
            super(name, tags);
        }

        @Override
        Meter get() {
            return registry.meter(flat());
        }

        @Override
        MeterName tag(String key, String value) {
            return new MeterName(name, ImmutableMap.<String, String>builder().putAll(tags).put(key, value).build());
        }
    }

    class TimerName extends FlatName<TimerName, Timer> {

        private TimerName(String name, Map<String, String> build) {
            super(name, build);
        }

        @Override
        Timer get() {
            return registry.timer(flat());
        }

        @Override
        TimerName tag(String key, String value) {
            return new TimerName(name, ImmutableMap.<String, String>builder().putAll(tags).put(key, value).build());
        }
    }

    private static abstract class FlatName<N extends FlatName, M extends Metered> {
        final String name;
        final Map<String, String> tags;

        FlatName(String name, Map<String, String> tags) {
            this.name = name;
            this.tags = tags;
        }

        String flat() {
            return name + ":" + JOINER.join(tags);
        }

        abstract M get();

        abstract N tag(String key, String value);
    }
}
