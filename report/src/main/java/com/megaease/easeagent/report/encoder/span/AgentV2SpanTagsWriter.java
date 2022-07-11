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

package com.megaease.easeagent.report.encoder.span;

import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

import java.util.Iterator;
import java.util.Map;

public class AgentV2SpanTagsWriter implements WriteBuffer.Writer<ReportSpan> {
    @Override
    public int sizeInBytes(ReportSpan value) {
        int sizeInBytes = 0;
        if (!value.tags().isEmpty()) {
            sizeInBytes += 10;
            Iterator<Map.Entry<String, String>> i = value.tags().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> entry = i.next();
                sizeInBytes += 5;
                sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(entry.getKey());
                sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(entry.getValue());
                if (i.hasNext()) {
                    sizeInBytes += 1;
                }
            }
        }
        return sizeInBytes;
    }

    @Override
    public void write(ReportSpan value, WriteBuffer b) {
        if (!value.tags().isEmpty()) {
            b.writeAscii(",\"tags\":{");
            Iterator<Map.Entry<String, String>> i = value.tags().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> entry = i.next();

                b.writeByte('\"');
                b.writeUtf8(JsonEscaper.jsonEscape(entry.getKey()));
                b.writeAscii("\":\"");
                b.writeUtf8(JsonEscaper.jsonEscape(entry.getValue()));
                b.writeByte('\"');
                if (i.hasNext()) {
                    b.writeByte(',');
                }
            }
            b.writeByte('}');
        }
    }
}
