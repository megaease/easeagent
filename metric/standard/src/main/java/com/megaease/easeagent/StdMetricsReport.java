package com.megaease.easeagent;

import com.codahale.metrics.Metric;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.DaemonExecutors.newScheduled;
import static com.megaease.easeagent.DaemonExecutors.shutdownAware;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@AutoService(Plugin.class)
public class StdMetricsReport implements Plugin<StdMetricsReport.Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdMetricsReport.class);

    @Override
    public void hook(Configuration conf, Instrumentation inst, Subscription subs) {
        final int period = conf.period_seconds();
        final ObjectMapper mapper = new ObjectMapper();
        final TimeUnit rateUnit = TimeUnit.valueOf(conf.rate_unit());
        final TimeUnit durationUnit = TimeUnit.valueOf(conf.duration_unit());
        final boolean showSamples = conf.show_samples();

        mapper.registerModule(new MetricsModule(rateUnit, durationUnit, showSamples));

        shutdownAware(newScheduled("report-metrics", 1)).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SharedMetrics.singleton().iterate(new StdMetrics.Consumer() {
                    @Override
                    public void accept(String name, Map<String, String> tags, Metric metric) {
                        final String type = metric.getClass().getSimpleName();
                        final ImmutableMap<String, Object> map = ImmutableMap.of("name", name, type, metric);
                        try {
                            LOGGER.info(mapper.writeValueAsString(map));
                        } catch (JsonProcessingException e) {
                            throw new MayBeABug(e);
                        }
                    }

                });
            }
        }, period, period, SECONDS);
    }

    @ConfigurationDecorator.Binding("metric.report")
    static abstract class Configuration {
        int period_seconds() {return 10;}

        String rate_unit() {return SECONDS.toString();}

        String duration_unit() { return MILLISECONDS.toString(); }

        boolean show_samples() { return false; }
    }
}
