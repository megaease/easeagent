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
package com.megaease.easeagent.report.encoder.log;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.encoder.log.pattern.LogDataPatternFormatter;
import io.opentelemetry.api.trace.SpanContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PatternParser;
import zipkin2.internal.WriteBuffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
<providers>
    <timestamp>
        <fieldName>timestamp</fieldName>
        <pattern>[UNIX_TIMESTAMP_AS_NUMBER]</pattern>
        <timeZone>UTC</timeZone>
    </timestamp>
    <pattern>
        <pattern>
            {
                "service": "${APP_NAME}",
                "traceId": "%X{traceId}",
                "id": "%X{spanId}",
                "logLevel": "%-5level",
                "threadId": "%thread",
                "location": "%logger{36}",
                "message": "%msg%n",
                "type": "application-log"
            }
        </pattern>
    </pattern>
</providers>
**/
public class LogDataWriter implements WriteBuffer.Writer<AgentLogData> {
    static final String TYPE_FIELD_NAME = "\"type\":\"application-log\"";

    static final String TRACE_ID_FIELD_NAME = ",\"traceId\":\"";
    static final String SPAN_ID_FIELD_NAME = ",\"id\":\"";

    static final String SERVICE_FIELD_NAME = ",\"service\":\"";
    static final String SYSTEM_FIELD_NAME = ",\"system\":\"";
    static final String TIMESTAMP_FILED_NAME = ",\"timestamp\":\"";
    static final String TIMESTAMP_NUM_FILED_NAME = ",\"timestamp\":";
    static final String LOG_LEVEL_FIELD_NAME = ",\"logLevel\":\"";
    static final String THREAD_ID_FIELD_NAME = ",\"threadId\":\"";
    static final String LOCATION_FIELD_NAME = ",\"location\":\"";
    static final String MESSAGE_FIELD_NAME = ",\"message\":\"";

    static final String TIMESTAMP = "timestamp";
    static final String LOG_LEVEL = "logLevel";
    static final String THREAD_ID = "threadId";
    static final String LOCATION = "location";
    static final String MESSAGE = "message";

    static final int STATIC_SIZE = 2
        + TYPE_FIELD_NAME.length()
        + SERVICE_FIELD_NAME.length() + 1
        + SYSTEM_FIELD_NAME.length() + 1;

    private static final ThreadLocal<StringBuilder> threadLocal = new ThreadLocal<>();

    protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    protected static final int MAX_STRING_BUILDER_SIZE = 2048;

    Config config;

    PatternParser parser;

    boolean dateTypeIsNumber = false;
    List<LogDataPatternFormatter> dateFormats;
    List<LogDataPatternFormatter> threadIdFormats;
    List<LogDataPatternFormatter> levelFormats;
    List<LogDataPatternFormatter> locationFormats;
    List<LogDataPatternFormatter> messageFormats;

    Map<String, List<LogDataPatternFormatter>> customFields = new HashMap<>();

    public LogDataWriter(Config cfg) {
        this.config = cfg;
        this.parser = PatternLayout.createPatternParser(null);
        initFormatters();
    }

    @Override
    public int sizeInBytes(AgentLogData value) {
        int size = STATIC_SIZE;

        size += LogJsonEscaper.jsonEscapedSizeInBytes(value.getAgentResource().getService());
        size += LogJsonEscaper.jsonEscapedSizeInBytes(value.getAgentResource().getSystem());

        if (!value.getSpanContext().equals(SpanContext.getInvalid())) {
            size += TRACE_ID_FIELD_NAME.length() + value.getSpanContext().getTraceId().length() + 1;
            size += SPAN_ID_FIELD_NAME.length() + value.getSpanContext().getSpanId().length() + 1;
        }

        StringBuilder sb = getStringBuilder();
        if (this.dateTypeIsNumber) {
            size += TIMESTAMP_NUM_FILED_NAME.length();
            size += WriteBuffer.asciiSizeInBytes(value.getEpochMillis());
        } else {
            size += kvLength(TIMESTAMP_FILED_NAME, value, this.dateFormats, sb, false);
        }

        size += kvLength(LOG_LEVEL_FIELD_NAME, value, this.levelFormats, sb, false);
        size += kvLength(THREAD_ID_FIELD_NAME, value, this.threadIdFormats, sb, false);

        // instrumentInfo - loggerName
        size += kvLength(LOCATION_FIELD_NAME, value, this.locationFormats, sb, false);

        if (!this.customFields.isEmpty()) {
            for (Map.Entry<String, List<LogDataPatternFormatter>> c : this.customFields.entrySet()) {
                size += kvLength(c.getKey(), value, c.getValue(), sb, true);
            }
        }

        size += kvLength(MESSAGE_FIELD_NAME, value, this.messageFormats, sb, true);

        return size;
    }

    @Override
    public void write(AgentLogData value, WriteBuffer b) {
        StringBuilder sb = getStringBuilder();

        // fix items
        b.writeByte(123);
        b.writeAscii(TYPE_FIELD_NAME);

        if (!value.getSpanContext().equals(SpanContext.getInvalid())) {
            // traceId
            b.writeAscii(TRACE_ID_FIELD_NAME);
            b.writeAscii(value.getSpanContext().getTraceId());
            b.writeByte('\"');

            b.writeAscii(SPAN_ID_FIELD_NAME);
            b.writeAscii(value.getSpanContext().getSpanId());
            b.writeByte('\"');
        }

        // resource - system/service
        b.writeAscii(SERVICE_FIELD_NAME);
        b.writeUtf8(LogJsonEscaper.jsonEscape(value.getAgentResource().getService()));
        b.writeByte('\"');

        b.writeAscii(SYSTEM_FIELD_NAME);
        b.writeUtf8(LogJsonEscaper.jsonEscape(value.getAgentResource().getSystem()));
        b.writeByte('\"');

        if (this.dateTypeIsNumber) {
            b.writeAscii(TIMESTAMP_NUM_FILED_NAME);
            b.writeAscii(value.getEpochMillis());
        } else {
            writeKeyValue(b, TIMESTAMP_FILED_NAME, value, this.dateFormats, sb, false);
        }

        writeKeyValue(b, LOG_LEVEL_FIELD_NAME, value, this.levelFormats, sb, false);
        writeKeyValue(b, THREAD_ID_FIELD_NAME, value, this.threadIdFormats, sb, false);

        // instrumentInfo - loggerName
        writeKeyValue(b, LOCATION_FIELD_NAME, value, this.locationFormats, sb, false);

        // attribute and custom
        if (!this.customFields.isEmpty()) {
            for (Map.Entry<String, List<LogDataPatternFormatter>> c : this.customFields.entrySet()) {
                writeKeyValue(b, c.getKey(), value, c.getValue(), sb, true);
            }
        }

        writeKeyValue(b, MESSAGE_FIELD_NAME, value, this.messageFormats, sb, true);

        b.writeByte(125);

    }

    /**
     *  count size written by @{link writeKeyValue}
     * @return size
     */
    private int kvLength(String key, AgentLogData value,
                              List<LogDataPatternFormatter> formatters, StringBuilder sb, boolean escape) {
        String d = value.getPatternMap().get(key);
        if (d == null) {
            sb.setLength(0);
            d = toSerializable(value, formatters, sb);
            value.getPatternMap().put(key, d);
        }

        if (StringUtils.isEmpty(d)) {
            return 0;
        } else {
            if (escape) {
                return key.length() + LogJsonEscaper.jsonEscapedSizeInBytes(d) + 1;
            } else {
                return key.length() + d.length() + 1;
            }
        }
    }

    private void writeKeyValue(WriteBuffer b, String key, AgentLogData value,
                               List<LogDataPatternFormatter> formatters,
                               StringBuilder sb, boolean escape) {
        String d = value.getPatternMap().get(key);
        if (d == null) {
            sb.setLength(0);
            d = toSerializable(value, formatters, sb);
        }

        if (!StringUtils.isEmpty(d)) {
            b.writeAscii(key);
            if (escape) {
                b.writeUtf8(LogJsonEscaper.jsonEscape(d));
            } else {
                b.writeAscii(d);
            }
            b.writeByte('\"');
        }
    }

    private void initFormatters() {
        this.config.getConfigs().forEach((k, v) -> {
            List<LogDataPatternFormatter> logDataFormatters = LogDataPatternFormatter.transform(v, this.parser);

            switch (k) {
                case LOG_LEVEL:
                    this.levelFormats = logDataFormatters;
                    break;
                case THREAD_ID:
                    this.threadIdFormats = logDataFormatters;
                    break;
                case LOCATION:
                    this.locationFormats = logDataFormatters;
                    break;
                case TIMESTAMP:
                    this.dateFormats = logDataFormatters;
                    this.dateTypeIsNumber = v.equals("%d{UNIX_MILLIS}") || v.equals("%d{UNIX}")
                        || v.equals("%date{UNIX_MILLIS}") || v.equals("%date{UNIX}");
                    break;
                case MESSAGE:
                    this.messageFormats = logDataFormatters;
                    break;
                default:
                    // custom attribute encoder
                    String key = ",\"" + k + "\":\"";
                    this.customFields.put(key, logDataFormatters);
                    break;
            }
        });
    }

    /**
     * Returns a {@code StringBuilder} that this Layout implementation can use to write the formatted log event to.
     *
     * @return a {@code StringBuilder}
     */
    protected static StringBuilder getStringBuilder() {
        StringBuilder result = threadLocal.get();
        if (result == null) {
            result = new StringBuilder(DEFAULT_STRING_BUILDER_SIZE);
            threadLocal.set(result);
        }
        trimToMaxSize(result, MAX_STRING_BUILDER_SIZE);
        result.setLength(0);
        return result;
    }

    private String toSerializable(final AgentLogData value,
                                        List<LogDataPatternFormatter> formatters,
                                        final StringBuilder sb) {
        sb.setLength(0);
        for (LogDataPatternFormatter formatter : formatters) {
            formatter.format(value, sb);
        }
        return sb.toString();
    }

    public static void trimToMaxSize(final StringBuilder stringBuilder, final int maxSize) {
        if (stringBuilder != null && stringBuilder.capacity() > maxSize) {
            stringBuilder.setLength(maxSize);
            stringBuilder.trimToSize();
        }
    }
}
