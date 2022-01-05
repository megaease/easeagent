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

import zipkin2.Annotation;
import zipkin2.Span;

public class AgentV2SpanAnnotationsWriter implements WriteBuffer.Writer<Span> {
    static final String ANNOTATION_FIELD_NAME = ",\"annotations\":[";
    static final String TIMESTAMP_FIELD_NAME = "{\"timestamp\":";
    static final String VALUE_FIELD_NAME = ",\"value\":\"";
    static final String ENDPOINT_FIELD_NAME = ",\"endpoint\":";

    int annotationSizeInBytes(long timestamp, String value, int endpointSizeInBytes) {
        int sizeInBytes = 0;

        sizeInBytes += TIMESTAMP_FIELD_NAME.length();
        sizeInBytes = sizeInBytes + WriteBuffer.asciiSizeInBytes(timestamp);
        sizeInBytes += VALUE_FIELD_NAME.length() + 1;
        sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value);
        if (endpointSizeInBytes != 0) {
            sizeInBytes += ENDPOINT_FIELD_NAME.length() + 1;
            sizeInBytes += endpointSizeInBytes;
        }
        sizeInBytes++;
        return sizeInBytes;
    }

    void writeAnnotation(long timestamp, String value, @Nullable byte[] endpoint, WriteBuffer b) {
        b.writeAscii(TIMESTAMP_FIELD_NAME);
        b.writeAscii(timestamp);
        b.writeAscii(VALUE_FIELD_NAME);
        b.writeUtf8(JsonEscaper.jsonEscape(value));
        b.writeByte(34); // " for value field
        if (endpoint != null) {
            b.writeAscii(ENDPOINT_FIELD_NAME);
            b.write(endpoint);
            b.writeByte(34); // " for value field
        }

        b.writeByte(125); // } for timestamp
    }

    @Override
    public int sizeInBytes(Span value) {
        int tagCount;
        int sizeInBytes = 0;
        if (!value.annotations().isEmpty()) {
            sizeInBytes += ANNOTATION_FIELD_NAME.length() + 1;
            tagCount = value.annotations().size();
            if (tagCount > 1) {
                sizeInBytes += tagCount - 1; // , for array item
            }

            for (int i = 0; i < tagCount; ++i) {
                Annotation a = value.annotations().get(i);
                sizeInBytes += annotationSizeInBytes(a.timestamp(), a.value(), 0);
            }
        }
        return sizeInBytes;
    }

    @Override
    public void write(Span value, WriteBuffer b) {
        if (!value.annotations().isEmpty()) {
            b.writeAscii(ANNOTATION_FIELD_NAME);
            int i = 0;
            int length = value.annotations().size();

            while (i < length) {
                Annotation a = value.annotations().get(i++);
                writeAnnotation(a.timestamp(), a.value(), (byte[]) null, b);
                if (i < length) {
                    b.writeByte(44); //, for array item
                }
            }
            b.writeByte(93); // ] for annotation field
        }
    }
}
