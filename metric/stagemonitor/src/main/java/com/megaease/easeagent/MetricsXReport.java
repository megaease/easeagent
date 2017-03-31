package com.megaease.easeagent;

import com.codahale.metrics.Metric;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.megaease.easeagent.ConfigurationDecorator.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.DaemonExecutors.newScheduled;
import static com.megaease.easeagent.DaemonExecutors.shutdownAware;
import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@AutoService(Plugin.class)
public class MetricsXReport implements Plugin<MetricsXReport.Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger("metrics");

    @Override
    public void hook(final Configuration conf, Instrumentation inst, Subscription subs) {
        final int period = conf.period_seconds();
        final long duration = SECONDS.toMillis(period);
        final QuantizedTime quantizedTime = new QuantizedTime(duration);
        final Reporting reporting = new Reporting(conf, quantizedTime, jsonFactory(conf));
        shutdownAware(newScheduled("report-metrics", 1))
                .scheduleAtFixedRate(reporting, quantizedTime.offset(), duration, MILLISECONDS);
    }

    private JsonFactory jsonFactory(Configuration conf) {
        final String rateUnit = conf.rate_unit();
        final String durationUnit = conf.duration_unit();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MetricsXModule(TimeUnit.valueOf(rateUnit), TimeUnit.valueOf(durationUnit)));
        return new JsonFactory(mapper);
    }

    @Binding("metric.report")
    abstract static class Configuration {
        int period_seconds() {return 10;}

        String rate_unit() { return SECONDS.toString(); }

        String duration_unit() { return MILLISECONDS.toString(); }

        String system() { return "unknown"; }

        String application() {return "unknown";}

        String host_ipv4() {
            return LocalhostAddress.getLocalhostAddr().getHostAddress();
        }

        String hostname() {
            return LocalhostAddress.getLocalhostName();
        }

        // TODO remove stagemonitor's legacy
        String bulk_header_template() {
            return "";
        }
    }

    private static class Reporting implements Runnable {
        private final QuantizedTime       quantizedTime;
        private final JsonFactory         factory;
        private final Map<String, String> hostInfo;
        private final String              bulkTemplate;

        Reporting(Configuration conf, QuantizedTime quantizedTime, JsonFactory factory) {

            this.quantizedTime = quantizedTime;
            this.factory = factory;
            bulkTemplate = conf.bulk_header_template();
            hostInfo = hostInfo(conf);
        }

        @Override
        public void run() {
            final long timestamp = quantizedTime.timestamp();
            final String bulk = format(bulkTemplate, new Date(timestamp));
            SharedMetrics.singleton().iterate(new Consumer(factory, timestamp, hostInfo, bulk));
        }

        private Map<String, String> hostInfo(Configuration conf) {
            final long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
            Map<String, String> map = Maps.newHashMap();
            map.put("measurement_start", Long.toString(startTime));
            map.put("system", conf.system());
            map.put("hostname", conf.hostname());
            map.put("hostipv4", conf.host_ipv4());
            map.put("application", conf.application());
            return map;
        }

    }

    static class Consumer implements Metrics.Consumer {

        private final JsonFactory         factory;
        private final long                timestamp;
        private final Map<String, String> hostInfo;
        private final String              bulk;

        Consumer(JsonFactory factory, long timestamp, Map<String, String> hostInfo, String bulk) {
            this.factory = factory;
            this.timestamp = timestamp;
            this.hostInfo = hostInfo;
            this.bulk = bulk;
        }

        @Override
        public void accept(String name, Map<String, String> tags, Metric metric) {

            try {
                final String content = asJson(name, tags, metric);
                if (Strings.isNullOrEmpty(content)) return;

                // TODO batch out metrics
                LOGGER.info(content);
            } catch (IOException e) {
                throw new MayBeABug(e);
            }
        }

        String asJson(String name, Map<String, String> tags, Metric metric) throws IOException {
            final StringBuilder sb = new StringBuilder();
            final Writer writer = CharStreams.asWriter(sb);
            final JsonGenerator json = factory.createGenerator(writer);
            final boolean noBulk = Strings.isNullOrEmpty(bulk);

            if (!noBulk) json.writeRaw(bulk);

            json.writeStartObject();
            json.writeNumberField("@timestamp", timestamp);
            json.writeStringField("name", name);

            // https://github.com/hexdecteam/easeagent/wiki/message_protocol_for_gateway
            if (noBulk) json.writeStringField("type", name);

            json.writeObject(hostInfo);
            json.writeObject(tags);
            json.writeObject(metric);
            json.writeEndObject();
            json.writeRaw('\n'); // TODO remove stagemonitor's legacy
            json.flush();

            return sb.toString();
        }
    }

}
