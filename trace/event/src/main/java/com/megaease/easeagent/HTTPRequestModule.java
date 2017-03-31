package com.megaease.easeagent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Collections;

public class HTTPRequestModule extends Module {
    static final Version VERSION = new Version(0, 1, 0, "", "com.megaease.easeagent", "traces-servlet");

    static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getModuleName() {
        return "trace-servlet";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        setupContext.addSerializers(new SimpleSerializers(Collections.<JsonSerializer<?>>singletonList(
                new RequestSerializer()
        )));
    }

    static class RequestSerializer extends StdSerializer<HTTPTracedRequest> {
        protected RequestSerializer() {
            super(HTTPTracedRequest.class);
        }

        @Override
        public void serialize(HTTPTracedRequest req, JsonGenerator json, SerializerProvider provider) throws IOException {
            json.writeStringField("id", req.id());
            json.writeStringField("uniqueVisitorId", req.uniqueVisitorId());
            json.writeStringField("name", req.name());
            json.writeStringField("type", req.type());
            json.writeStringField("status", req.status());
            json.writeStringField("method", req.method());
            json.writeStringField("url", req.url());
            json.writeStringField("callStack", req.callStack());
            json.writeStringField("callStackJson", mapper.writeValueAsString(req.callStackJson()));
            json.writeObjectField("headers", req.headers());
            json.writeObjectField("parameters", req.parameters());
            json.writeObjectField("userAgent", req.userAgent());
            json.writeBooleanField("containsCallTree", req.containsCallTree());
            json.writeBooleanField("error", req.error());
            json.writeNumberField("statusCode", req.statusCode());
            json.writeNumberField("bytesWritten", req.bytesWritten());
            json.writeNumberField("executionCountDb", req.executionCountDb());
            json.writeNumberField("executionTimeDb", req.executionTimeDb());
            json.writeNumberField("executionTime", req.executionTime());
            json.writeNumberField("executionTimeCpu", req.executionTimeCpu());
        }
    }
}
