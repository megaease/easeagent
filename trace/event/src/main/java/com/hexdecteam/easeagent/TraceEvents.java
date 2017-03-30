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
import java.text.MessageFormat;
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

        private final Map<String, Object> hostInfo;
        private final JsonFactory         jsonFactory;
        private final String              bulkTemplate;

        Reporter(Map<String, Object> hostInfo, JsonFactory jsonFactory, String bulkTemplate) {
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
                json.writeStringField("@timestamp", stringFormat(timestamp));
                json.writeStringField("type", "http_request");
                write(json, this.hostInfo);
                json.writeObject(request);
                json.writeEndObject();
                json.writeRaw('\n'); // logstash need a '\n' as delimiter
                json.flush();
                LOGGER.info(sb.toString());
            } catch (IOException e) {
                throw new MayBeABug(e);
            }

        }

        // TODO fix remaining problem of stagemonitor
        private String stringFormat(long timestamp) {
            return MessageFormat.format("{0,date,yyyy-MM-dd'T'HH:mm:ss.SSSZ}", new Date(timestamp));
        }

        private void write(JsonGenerator json, Map<String, Object> map) throws IOException {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if(value instanceof Long) {
                    json.writeNumberField(entry.getKey(), (Long) value);
                } else {
                    json.writeStringField(entry.getKey(), value.toString());
                }
            }
        }

    }

    private Map<String, Object> hostInfo(Configuration conf) {
        final long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        Map<String, Object> map = Maps.newHashMap();
        map.put("measurement_start", startTime);
        map.put("system", conf.system());
        map.put("hostname", conf.hostname());
        map.put("hostipv4", conf.host_ipv4());
        map.put("application", conf.application());
        return map;
    }


    @ConfigurationDecorator.Binding("trace.report")
    static abstract class Configuration {
        String system() { return UUID.randomUUID().toString(); }

        String application() {return null;}

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

}
