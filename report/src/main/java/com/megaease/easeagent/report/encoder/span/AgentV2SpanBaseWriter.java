/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.report.encoder.span;

import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

public class AgentV2SpanBaseWriter implements WriteBuffer.Writer<ReportSpan> {

    static final String TRACE_ID_FIELD_NAME = "\"traceId\":\"";
    static final String PARENT_ID_FIELD_NAME = ",\"parentId\":\"";
    static final String SPAN_ID_FIELD_NAME = ",\"id\":\"";
    static final String KIND_FIELD_NAME = ",\"kind\":\"";
    static final String NAME_FIELD_NAME = ",\"name\":\"";
    static final String TIMESTAMP_FIELD_NAME = ",\"timestamp\":";
    static final String DURATION_FIELD_NAME = ",\"duration\":";
    static final String DEBUG_FIELD_VALUE = ",\"debug\":true";
    static final String SHARED_FIELD_VALUE = ",\"shared\":true";

    @Override
    public int sizeInBytes(ReportSpan value) {
        int sizeInBytes = 0;

        //traceId
        sizeInBytes += TRACE_ID_FIELD_NAME.length() + 1; // 1 represent the last quote sign
        sizeInBytes += value.traceId().length();

        //parentId
        if (value.parentId() != null) {
            sizeInBytes += PARENT_ID_FIELD_NAME.length() + 1;
            sizeInBytes += value.parentId().length();
        }

        // spanId
        sizeInBytes += SPAN_ID_FIELD_NAME.length() + 1;
        sizeInBytes += value.id().length();

        // kind
        if (value.kind() != null) {
            sizeInBytes += KIND_FIELD_NAME.length() + 1;
            sizeInBytes += value.kind().length();
        }

        // name
        if (value.name() != null) {
            sizeInBytes += NAME_FIELD_NAME.length() + 1;
            sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value.name());
        }

        // timestamp
        if (value.timestamp() != 0L) {
            sizeInBytes += TIMESTAMP_FIELD_NAME.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes(value.timestamp());
        }

        //duration
        if (value.duration() != 0L) {
            sizeInBytes += DURATION_FIELD_NAME.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes(value.duration());
        }


        if (value.debug()) {
            sizeInBytes += DEBUG_FIELD_VALUE.length();
        }

        if (value.shared()) {
            sizeInBytes += SHARED_FIELD_VALUE.length();
        }
        return sizeInBytes;
    }

    @Override
    public void write(ReportSpan value, WriteBuffer b) {
        b.writeAscii(TRACE_ID_FIELD_NAME);
        b.writeAscii(value.traceId());
        b.writeByte('\"');

        if (value.parentId() != null) {
            b.writeAscii(PARENT_ID_FIELD_NAME);
            b.writeAscii(value.parentId());
            b.writeByte('\"');
        }

        b.writeAscii(SPAN_ID_FIELD_NAME);
        b.writeAscii(value.id());
        b.writeByte(34);
        if (value.kind() != null) {
            b.writeAscii(KIND_FIELD_NAME);
            b.writeAscii(value.kind());
            b.writeByte('\"');
        }

        if (value.name() != null) {
            b.writeAscii(NAME_FIELD_NAME);
            b.writeUtf8(JsonEscaper.jsonEscape(value.name()));
            b.writeByte('\"');
        }

        if (value.timestamp() != 0L) {
            b.writeAscii(TIMESTAMP_FIELD_NAME);
            b.writeAscii(value.timestamp());
        }

        if (value.duration() != 0L) {
            b.writeAscii(DURATION_FIELD_NAME);
            b.writeAscii(value.duration());
        }

        if (Boolean.TRUE.equals(value.debug())) {
            b.writeAscii(DEBUG_FIELD_VALUE);
        }

        if (Boolean.TRUE.equals(value.shared())) {
            b.writeAscii(SHARED_FIELD_VALUE);
        }
    }
}
