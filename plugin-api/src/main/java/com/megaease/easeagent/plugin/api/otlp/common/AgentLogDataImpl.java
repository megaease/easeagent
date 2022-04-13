/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.megaease.easeagent.plugin.api.otlp.common;

import com.megaease.easeagent.plugin.report.EncodedData;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.EaseAgentResource;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
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
    private long threadId;
    private Throwable throwable;

    private Map<String, String> patternMap = null;
    private EncodedData encodedData;

    public AgentLogDataImpl(Builder builder) {
        this.epochMillis = builder.epochMills;
        this.spanContext = builder.spanContext == null ? SpanContext.getInvalid() : builder.spanContext;
        this.severity = builder.severity;
        this.severityText = builder.severityText != null ? builder.severityText : this.severity.name();
        this.body = builder.body;

        this.attributes = builder.attributesBuilder != null
            ? builder.attributesBuilder.build()
            : AgentAttributes.builder().build();

        this.instrumentationLibraryInfo = AgentInstrumentLibInfo.getInfo(builder.logger);
        this.threadName = builder.threadName;
        this.threadId = builder.threadId;
        this.throwable = builder.throwable;
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
    public void completeAttributes() {
        AttributesBuilder attrsBuilder = this.attributes.toBuilder();
        if (this.throwable != null) {
            attrsBuilder.put(SemanticKey.EXCEPTION_TYPE, throwable.getClass().getName());
            attrsBuilder.put(SemanticKey.EXCEPTION_MESSAGE, throwable.getMessage());

            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            attrsBuilder.put(SemanticKey.EXCEPTION_STACKTRACE, writer.toString());
        }

        attrsBuilder.put(SemanticKey.THREAD_NAME, threadName);
        attrsBuilder.put(SemanticKey.THREAD_ID, threadId);
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
        private Throwable throwable;

        private AttributesBuilder attributesBuilder = null;

        private String threadName;
        private long threadId;

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

        public Builder thread(Thread thread) {
            this.threadName = thread.getName();
            this.threadId = thread.getId();
            return this;
        }

        public Builder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public Builder contextData(Collection<String> keys, Map<String, String> data) {
            if (keys == null || keys.isEmpty()) {
                if (data.isEmpty()) {
                    return this;
                }
                keys = data.keySet();
            }

            AttributesBuilder ab = getAttributesBuilder();
            for (String key : keys) {
                ab.put(SemanticKey.stringKey(key), data.get(key));
            }
            return this;
        }

        public AttributesBuilder getAttributesBuilder() {
            if (attributesBuilder == null) {
                attributesBuilder = AgentAttributes.builder();
            }
            return attributesBuilder;
        }

        public AgentLogData build() {
            return new AgentLogDataImpl(this);
        }
    }
}
