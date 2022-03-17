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

import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

import java.util.Map;

public class LogWriter implements WriteBuffer.Writer<AccessLogInfo> {
    static final String TYPE_FIELD_NAME = "\"type\":\"";
    static final String TRACE_ID_FIELD_NAME = ",\"trace_id\":\"";
    static final String SPAN_ID_FIELD_NAME = ",\"span_id\":\"";
    static final String PARENT_ID_FIELD_NAME = ",\"pspan_id\":\"";
    static final String SERVICE_FIELD_NAME = ",\"service\":\"";
    static final String SYSTEM_FIELD_NAME = ",\"system\":\"";
    static final String CLIENT_IP_FIELD_NAME = ",\"client_ip\":\"";
    static final String USER_FIELD_NAME = ",\"user\":\"";
    static final String RESPONSE_SIZE_FIELD_NAME = ",\"response_size\":";
    static final String REQUEST_TIME_FIELD_NAME = ",\"request_time\":";
    static final String CPU_ELAPSED_TIME_FIELD_NAME = ",\"cpuElapsedTime\":";
    static final String URL_FIELD_NAME = ",\"url\":\"";
    static final String METHOD_FIELD_NAME = ",\"method\":\"";
    static final String STATUS_CODE_FIELD_NAME = ",\"status_code\":\"";
    static final String HOST_NAME_FIELD_NAME = ",\"host_name\":\"";
    static final String HOST_IPV4_FIELD_NAME = ",\"host_ipv4\":\"";
    static final String CATEGORY_FIELD_NAME = ",\"category\":\"";
    static final String MATCH_URL_FIELD_NAME = ",\"match_url\":\"";
    static final String TIMESTAMP_FIELD_NAME = ",\"timestamp\":";
    static final String HEADERS_FIELD_NAME = ",\"headers\":{";
    static final String QUERIES_FIELD_NAME = ",\"queries\":{";

    static final int STATIC_SIZE = 2
        + TYPE_FIELD_NAME.length() + 1
        + URL_FIELD_NAME.length() + 1
        + TRACE_ID_FIELD_NAME.length() + 1
        + SPAN_ID_FIELD_NAME.length() + 1
        // + PARENT_ID_FIELD_NAME.length() + 1
        + SERVICE_FIELD_NAME.length() + 1
        + SYSTEM_FIELD_NAME.length() + 1
        + METHOD_FIELD_NAME.length() + 1
        + CATEGORY_FIELD_NAME.length() + 1
        + HEADERS_FIELD_NAME.length()
        + QUERIES_FIELD_NAME.length()
        + STATUS_CODE_FIELD_NAME.length() + 1
        + CLIENT_IP_FIELD_NAME.length() + 1
        + USER_FIELD_NAME.length() + 1
        + RESPONSE_SIZE_FIELD_NAME.length()
        + REQUEST_TIME_FIELD_NAME.length()
        + CPU_ELAPSED_TIME_FIELD_NAME.length()
        + HOST_NAME_FIELD_NAME.length() + 1
        + HOST_IPV4_FIELD_NAME.length() + 1
        + MATCH_URL_FIELD_NAME.length() + 1
        + TIMESTAMP_FIELD_NAME.length();


    @Override
    public int sizeInBytes(AccessLogInfo value) {
        if (value.getEncodedData() != null) {
            return value.getEncodedData().size();
        }

        int size = STATIC_SIZE;
        size += value.getType().length();

        size += JsonEscaper.jsonEscapedSizeInBytes(value.getUrl());

        size += value.getTraceId().length();
        size += value.getSpanId().length();

        if (value.getParentSpanId() != null) {
            size += PARENT_ID_FIELD_NAME.length() + 1;
            size += stringSizeInBytes(value.getParentSpanId());
        }

        size += JsonEscaper.jsonEscapedSizeInBytes(value.getService());
        size += JsonEscaper.jsonEscapedSizeInBytes(value.getSystem());

        size +=value.getMethod().length();
        size +=value.getCategory().length();

        size += mapSizeInBytes(value.getHeaders());
        size += mapSizeInBytes(value.getQueries());

        size +=value.getStatusCode().length();

        size +=value.getClientIP().length();

        size += JsonEscaper.jsonEscapedSizeInBytes(value.getUser());

        size += WriteBuffer.asciiSizeInBytes(value.getResponseSize());
        size += WriteBuffer.asciiSizeInBytes(value.getRequestTime());
        size += WriteBuffer.asciiSizeInBytes(value.getCpuElapsedTime());
        size += JsonEscaper.jsonEscapedSizeInBytes(value.getHostName());

        size += value.getHostIpv4().length();
        size += JsonEscaper.jsonEscapedSizeInBytes(value.getMatchUrl());
        size += WriteBuffer.asciiSizeInBytes(value.getTimestamp());

        return size;
    }

    @Override
    public void write(AccessLogInfo value, WriteBuffer b) {
        b.writeByte(123);
        b.writeAscii(TYPE_FIELD_NAME);
        b.writeAscii(value.getType());
        b.writeByte('\"');

        b.writeAscii(URL_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getUrl()));
        b.writeByte('\"');

        b.writeAscii(TRACE_ID_FIELD_NAME);
        b.writeAscii(value.getTraceId());
        b.writeByte('\"');

        b.writeAscii(SPAN_ID_FIELD_NAME);
        b.writeAscii(value.getSpanId());
        b.writeByte('\"');

        if (value.getParentSpanId() != null) {
            b.writeAscii(PARENT_ID_FIELD_NAME);
            writeAscii(value.getParentSpanId(), b);
            b.writeByte('\"');
        }
        b.writeAscii(SERVICE_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getService()));
        b.writeByte('\"');

        b.writeAscii(SYSTEM_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getSystem()));
        b.writeByte('\"');

        b.writeAscii(METHOD_FIELD_NAME);
        b.writeAscii(value.getMethod());
        b.writeByte('\"');

        b.writeAscii(CATEGORY_FIELD_NAME);
        b.writeAscii(value.getCategory());
        b.writeByte('\"');

        b.writeAscii(HEADERS_FIELD_NAME);
        writeMap(value.getHeaders(), b);

        b.writeAscii(QUERIES_FIELD_NAME);
        writeMap(value.getQueries(), b);

        b.writeAscii(STATUS_CODE_FIELD_NAME);
        b.writeAscii(value.getStatusCode());
        b.writeByte('\"');

        b.writeAscii(CLIENT_IP_FIELD_NAME);
        b.writeAscii(value.getClientIP());
        b.writeByte('\"');

        b.writeAscii(USER_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getUser()));
        b.writeByte('\"');

        b.writeAscii(RESPONSE_SIZE_FIELD_NAME);
        b.writeAscii(value.getResponseSize());

        b.writeAscii(REQUEST_TIME_FIELD_NAME);
        b.writeAscii(value.getRequestTime());

        b.writeAscii(CPU_ELAPSED_TIME_FIELD_NAME);
        b.writeAscii(value.getCpuElapsedTime());

        b.writeAscii(HOST_NAME_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getHostName()));
        b.writeByte('\"');

        b.writeAscii(HOST_IPV4_FIELD_NAME);
        b.writeAscii(value.getHostIpv4());
        b.writeByte('\"');

        b.writeAscii(MATCH_URL_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value.getMatchUrl()));
        b.writeByte('\"');

        b.writeAscii(TIMESTAMP_FIELD_NAME);
        b.writeAscii(value.getTimestamp());
        b.writeByte(125);
    }


    private int mapSizeInBytes(Map<String, String> vs) {
        int s = 1;
        if (vs.isEmpty()) {
            return s;
        }
        for (Map.Entry<String, String> kv : vs.entrySet()) {
            if (s > 1)  {
                s += 6;
            } else {
                s += 5;
            }
            s += JsonEscaper.jsonEscapedSizeInBytes(kv.getKey());
            s += JsonEscaper.jsonEscapedSizeInBytes(kv.getValue());
        }
        return s;
    }

    private void writeMap(Map<String, String> vs, WriteBuffer b) {
        int idx = 0;
        for (Map.Entry<String, String> kv : vs.entrySet()) {
            if (idx++ > 0)  {
                b.writeByte(',');
            }
            b.writeByte('\"');
            b.writeUtf8(JsonEscaper.jsonEscape(kv.getKey()));
            b.writeByte('\"');
            b.writeByte(':');
            b.writeByte('\"');
            b.writeUtf8(JsonEscaper.jsonEscape(kv.getValue()));
            b.writeByte('\"');
        }
        b.writeByte('}');
    }

    private int stringSizeInBytes(String v) {
        if (v == null) {
            return "null".length() + 2;
            // return 0;
        } else {
            return v.length();
        }
    }

    private void writeAscii(String v, WriteBuffer b) {
        if (v == null) {
            b.writeAscii("null");
        } else {
            b.writeAscii(v);
        }
    }
}
