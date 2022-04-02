package com.megaease.easeagent.plugin.api.otlp.common;

import com.megaease.easeagent.plugin.report.EncodedData;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.EaseAgentResource;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@SuppressWarnings("unused")
public class AgentLogDataImpl implements AgentLogData {
    private EaseAgentResource resource = EaseAgentResource.getResource();
    private InstrumentationLibraryInfo instrumentationLibraryInfo;
    private long epochMillis;
    private SpanContext spanContext;
    private Severity severity;
    private String severityText;
    private String name = null;
    private Body body;
    private Attributes attributes;

    private String threadName;

    private Map<String, String> patternMap = null;
    private EncodedData encodedData;

    public AgentLogDataImpl(String logger, String threadName,
                            long epochMills, SpanContext sc, Severity level,
                            String levelText, Body body, Attributes attrs) {
        this.epochMillis = epochMills;
        this.spanContext = sc == null ? SpanContext.getInvalid() : sc;
        this.severity = level;
        this.severityText = levelText != null ? levelText : level.name();
        this.body = body;
        this.attributes = attrs;
        this.instrumentationLibraryInfo = AgentInstrumentLibInfo.getInfo(logger);
        this.threadName = threadName;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public String getLocation() {
        return this.instrumentationLibraryInfo.getName();
    }

    @Override
    public EaseAgentResource getAgentResource() {
        return this.resource;
    }

    @Override
    public Map<String, String> getPatternMap() {
        if (this.patternMap == null) {
            this.patternMap = new HashMap<>();
        }
        return this.patternMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public long getEpochNanos() {
        return TimeUnit.MILLISECONDS.toNanos(this.epochMillis);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String logger;
        private long epochMills;
        private SpanContext spanContext;
        private Severity severity;
        private String severityText;
        private Body body;
        private AttributesBuilder attributesBuilder = null;

        private String threadName;

        public Builder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public Builder epochMills(long timestamp) {
            this.epochMills = timestamp;
            return this;
        }

        public Builder spanContext() {
            this.spanContext = OtlpSpanContext.getLogSpanContext();
            return this;
        }

        public Builder severity(Severity level) {
            this.severity = level;
            return this;
        }

        public Builder severityText(String level) {
            this.severityText = level;
            return this;
        }

        public Builder body(String msg) {
            this.body = Body.string(msg);
            return this;
        }

        public Builder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder setThrowable(Throwable throwable) {
            AttributesBuilder attrsBuilder = getAttributesBuilder();
            attrsBuilder.put(SemanticAttributes.EXCEPTION_TYPE, throwable.getClass().getName());
            attrsBuilder.put(SemanticAttributes.EXCEPTION_MESSAGE, throwable.getMessage());
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            attrsBuilder.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
            return this;
        }

        public AttributesBuilder getAttributesBuilder() {
            if (attributesBuilder == null) {
                attributesBuilder = AgentAttributes.builder();
            }
            return attributesBuilder;
        }

        public AgentLogData build() {
            return new AgentLogDataImpl(logger, threadName, epochMills, spanContext,
                severity, severityText, body,
                getAttributesBuilder().build());
        }
    }
}
