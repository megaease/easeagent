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

import java.util.Iterator;
import java.util.Map;

public class AgentV2SpanTagsWriter implements WriteBuffer.Writer<Span> {
    @Override
    public int sizeInBytes(Span value) {
        int sizeInBytes = 0;
        if (!value.tags().isEmpty()) {
            sizeInBytes += 10;
            Iterator i = value.tags().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) i.next();
                sizeInBytes += 5;
                sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes((CharSequence) entry.getKey());
                sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes((CharSequence) entry.getValue());
                if (i.hasNext()) {
                    sizeInBytes += 1;
                }
            }
        }
        return sizeInBytes;
    }

    @Override
    public void write(Span value, WriteBuffer b) {
        if (!value.tags().isEmpty()) {
            b.writeAscii(",\"tags\":{");
            Iterator i = value.tags().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) i.next();

                b.writeByte('\"');
                b.writeUtf8(JsonEscaper.jsonEscape((CharSequence) entry.getKey()));
                b.writeAscii("\":\"");
                b.writeUtf8(JsonEscaper.jsonEscape((CharSequence) entry.getValue()));
                b.writeByte('\"');
                if (i.hasNext()) {
                    b.writeByte(',');
                }
            }
            b.writeByte('}');
        }
    }
}
