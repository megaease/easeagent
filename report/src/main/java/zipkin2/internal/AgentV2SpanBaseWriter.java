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

package zipkin2.internal;

import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

public class AgentV2SpanBaseWriter implements WriteBuffer.Writer<Span> {

    final String traceIDFieldName = "\"traceId\":\"";
    final String parentIDFieldName = ",\"parentId\":\"";
    final String spanIDFieldName = ",\"id\":\"";
    final String kindFieldName = ",\"kind\":\"";
    final String nameFieldName = ",\"name\":\"";
    final String timestampFieldName = ",\"timestamp\":";
    final String durationFieldName = ",\"duration\":";
    final String debugFieldValue = ",\"debug\":true";
    final String sharedFieldValue = ",\"shared\":true";

    @Override
    public int sizeInBytes(Span value) {
        int sizeInBytes = 0;

        //traceId
        sizeInBytes += traceIDFieldName.length() + 1; // 1 represent the last quote sign
        sizeInBytes += value.traceId().length();

        //parentId
        if (value.parentId() != null) {
            sizeInBytes += parentIDFieldName.length() + 1;
            sizeInBytes += value.parentId().length();
        }

        // spanId
        sizeInBytes += spanIDFieldName.length() + 1;
        sizeInBytes += value.id().length();

        // kind
        if (value.kind() != null) {
            sizeInBytes += kindFieldName.length() + 1;
            sizeInBytes += value.kind().name().length();
        }

        // name
        if (value.name() != null) {
            sizeInBytes += nameFieldName.length() + 1;
            sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value.name());
        }

        // timestamp
        if (value.timestampAsLong() != 0L) {
            sizeInBytes += timestampFieldName.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes(value.timestampAsLong());
        }

        //duration
        if (value.durationAsLong() != 0L) {
            sizeInBytes += durationFieldName.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes(value.durationAsLong());
        }


        if (Boolean.TRUE.equals(value.debug())) {
            sizeInBytes += debugFieldValue.length();
        }

        if (Boolean.TRUE.equals(value.shared())) {
            sizeInBytes += sharedFieldValue.length();
        }
        return sizeInBytes;
    }

    @Override
    public void write(Span value, WriteBuffer b) {
        b.writeAscii(traceIDFieldName);
        b.writeAscii(value.traceId());
        b.writeByte('\"');

        if (value.parentId() != null) {
            b.writeAscii(parentIDFieldName);
            b.writeAscii(value.parentId());
            b.writeByte('\"');
        }

        b.writeAscii(spanIDFieldName);
        b.writeAscii(value.id());
        b.writeByte(34);
        if (value.kind() != null) {
            b.writeAscii(kindFieldName);
            b.writeAscii(value.kind().toString());
            b.writeByte('\"');
        }

        if (value.name() != null) {
            b.writeAscii(nameFieldName);
            b.writeUtf8(JsonEscaper.jsonEscape(value.name()));
            b.writeByte('\"');
        }

        if (value.timestampAsLong() != 0L) {
            b.writeAscii(timestampFieldName);
            b.writeAscii(value.timestampAsLong());
        }

        if (value.durationAsLong() != 0L) {
            b.writeAscii(durationFieldName);
            b.writeAscii(value.durationAsLong());
        }

        if (Boolean.TRUE.equals(value.debug())) {
            b.writeAscii(debugFieldValue);
        }

        if (Boolean.TRUE.equals(value.shared())) {
            b.writeAscii(sharedFieldValue);
        }
    }
}
