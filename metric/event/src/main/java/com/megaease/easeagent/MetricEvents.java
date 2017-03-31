package com.megaease.easeagent;

import com.codahale.metrics.Gauge;
import com.google.auto.service.AutoService;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@AutoService(Plugin.class)
public class MetricEvents implements Plugin<Plugin.Noop> {

    @Override
    public void hook(Transformation.Noop conf, Instrumentation inst, Subscription subs) {
        subs.register(this);
    }


    @Subscription.Consume
    public void receive(final Register register) {
        try {
            SharedMetrics.singleton().registerIfAbsent(register.name, new Callable<Gauge<Object>>() {
                @Override
                public Gauge<Object> call() throws Exception {
                    return new Gauge<Object>() {
                        @Override
                        public Object getValue() {
                            try {
                                return register.method.invoke(register.host);
                            } catch (Exception e) {
                                throw new MayBeABug(e);
                            }
                        }
                    };
                }
            });
        } catch (Exception e) {
            throw new MayBeABug(e);
        }
    }

    @Subscription.Consume
    public void receive(Inc inc) {
        SharedMetrics.singleton()
                     .counter(inc.name, inc.tags)
                     .inc(inc.n);
    }

    @Subscription.Consume
    public void receive(Dec dec) {
        SharedMetrics.singleton()
                     .counter(dec.name, dec.tags)
                     .dec(dec.n);
    }

    @Subscription.Consume
    public void receive(Mark mark) {
        SharedMetrics.singleton()
                     .meter(mark.name, mark.tags)
                     .mark(mark.n);
    }

    @Subscription.Consume
    public void receive(Update update) {
        SharedMetrics.singleton()
                     .timer(update.name, update.tags)
                     .update(update.duration, update.unit);
    }

    @ToString
    @EqualsAndHashCode
    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Register {
        public final String name;
        public final Method method;
        public final Object host;

        public Register(String name, Method method, Object host) {
            this.name = name;
            this.method = method;
            this.host = host;
        }
    }

    @ToString
    @EqualsAndHashCode
    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Inc {
        public final String              name;
        public final Map<String, String> tags;
        public final long                n;

        public Inc(String name) {
            this(name, 1);
        }

        public Inc(String name, int n) {
            this.name = name;
            this.tags = new HashMap<String, String>();
            this.n = n;
        }

        public Inc tag(String key, String value) {
            tags.put(key, value);
            return this;
        }
    }

    @ToString
    @EqualsAndHashCode
    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Dec {
        public final String              name;
        public final Map<String, String> tags;
        public final long                n;

        public Dec(String name) {
            this(name, 1);
        }

        public Dec(String name, int n) {
            this.name = name;
            this.tags = new HashMap<String, String>();
            this.n = n;
        }

        public Dec tag(String key, String value) {
            tags.put(key, value);
            return this;
        }
    }

    @ToString
    @EqualsAndHashCode
    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Mark {
        public final String              name;
        public final Map<String, String> tags;
        public final long                n;

        public Mark(String name) {
            this(name, 1);
        }

        public Mark(String name, long n) {
            this.name = name;
            this.tags = new HashMap<String, String>();
            this.n = n;
        }

        public Mark tag(String key, String value) {
            tags.put(key, value);
            return this;
        }
    }

    @ToString
    @EqualsAndHashCode
    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Update {
        public final String              name;
        public final Map<String, String> tags;
        public final long                duration;
        public final TimeUnit            unit;

        public Update(String name, long duration, TimeUnit unit) {
            this.name = name;
            this.unit = unit;
            this.tags = new HashMap<String, String>();
            this.duration = duration;
        }

        public Update tag(String key, String value) {
            tags.put(key, value);
            return this;
        }
    }
}
