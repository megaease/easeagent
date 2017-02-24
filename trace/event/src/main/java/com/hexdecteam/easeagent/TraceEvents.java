package com.hexdecteam.easeagent;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static java.text.MessageFormat.format;

@AutoService(Plugin.class)
public class TraceEvents implements Plugin<TraceEvents.Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger("requests");

    @Override
    public void hook(Configuration conf, Instrumentation inst, Subscription subs) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new HTTPRequestModule());
        subs.register(new Reporter(hostInfo(conf), new JsonFactory(mapper), conf.bulk_header_template()));
    }

    static class Reporter {

        private final Map<String, String> hostInfo;
        private final JsonFactory         jsonFactory;
        private final String              bulkTemplate;

        Reporter(Map<String, String> hostInfo, JsonFactory jsonFactory, String bulkTemplate) {
            this.hostInfo = hostInfo;
            this.jsonFactory = jsonFactory;
            this.bulkTemplate = bulkTemplate;
        }

        @Subscription.Consume
        public void receive(HTTPTracedRequest request) {
            final StringBuilder sb = new StringBuilder();
            final Writer writer = CharStreams.asWriter(sb);
            try {
                final long timestamp = System.currentTimeMillis();
                final JsonGenerator json = jsonFactory.createGenerator(writer);
                if (!Strings.isNullOrEmpty(bulkTemplate)) {
                    // https://github.com/hexdecteam/easeagent/wiki/message_protocol_for_gateway
                    final String bulk = format(bulkTemplate, new Date(timestamp));
                    json.writeRaw(bulk);
                }
                json.writeStartObject();
                json.writeNumberField("@timestamp", timestamp);
                write(json, this.hostInfo);
                json.writeObject(request);
                json.writeEndObject();
                json.flush();
                LOGGER.info(sb.toString());
            } catch (IOException e) {
                throw new MayBeABug(e);
            }

        }

        private void write(JsonGenerator json, Map<String, String> map) throws IOException {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                json.writeStringField(entry.getKey(), entry.getValue());
            }
        }

    }

    private Map<String, String> hostInfo(Configuration conf) {
        final long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        Map<String, String> map = Maps.newHashMap();
        map.put("measurement_start", Long.toString(startTime));
        map.put("gid", conf.gid());
        map.put("host", conf.host());
        map.put("host_ipv4", conf.host_ipv4());
        map.put("instance", conf.instance());
        map.put("application", conf.application());
        return map;
    }


    @ConfigurationDecorator.Binding("trace.report")
    static abstract class Configuration {
        String gid() { return UUID.randomUUID().toString(); }

        String host() {
            return LocalhostAddress.getLocalhostName();
        }

        String host_ipv4() {
            try {
                return InetAddress.getByName(host()).getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        String application() {return null;}

        String instance() {return null;}

        // TODO remove stagemonitor's legacy
        String bulk_header_template() {
            return "";
        }
    }

}
