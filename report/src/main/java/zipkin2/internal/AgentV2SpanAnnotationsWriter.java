package zipkin2.internal;

import zipkin2.Annotation;
import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.Nullable;
import zipkin2.internal.WriteBuffer;

public class AgentV2SpanAnnotationsWriter implements WriteBuffer.Writer<Span> {
    final String annotationFieldName = ",\"annotations\":[";
    final String timestampFieldName = "{\"timestamp\":";
    final String valueFieldName = ",\"value\":\"";
    final String endpointFieldName = ",\"endpoint\":";

    int annotationSizeInBytes(long timestamp, String value, int endpointSizeInBytes) {
        int sizeInBytes = 0;

        sizeInBytes += timestampFieldName.length();
        sizeInBytes = sizeInBytes + WriteBuffer.asciiSizeInBytes(timestamp);
        sizeInBytes += valueFieldName.length() + 1;
        sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value);
        if (endpointSizeInBytes != 0) {
            sizeInBytes += endpointFieldName.length() + 1;
            sizeInBytes += endpointSizeInBytes;
        }
        sizeInBytes++; // }
        return sizeInBytes;
    }

    void writeAnnotation(long timestamp, String value, @Nullable byte[] endpoint, WriteBuffer b) {
        b.writeAscii(timestampFieldName);
        b.writeAscii(timestamp);
        b.writeAscii(valueFieldName);
        b.writeUtf8(JsonEscaper.jsonEscape(value));
        b.writeByte(34); // " for value field
        if (endpoint != null) {
            b.writeAscii(endpointFieldName);
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
            sizeInBytes += annotationFieldName.length() + 1;
            tagCount = value.annotations().size();
            if (tagCount > 1) {
                sizeInBytes += tagCount - 1; // , for array item
            }

            for (int i = 0; i < tagCount; ++i) {
                Annotation a = (Annotation) value.annotations().get(i);
                sizeInBytes += annotationSizeInBytes(a.timestamp(), a.value(), 0);
            }
        }
        return sizeInBytes;
    }

    @Override
    public void write(Span value, WriteBuffer b) {
        if (!value.annotations().isEmpty()) {
            b.writeAscii(annotationFieldName);
            int i = 0;
            int length = value.annotations().size();

            while (i < length) {
                Annotation a = (Annotation) value.annotations().get(i++);
                writeAnnotation(a.timestamp(), a.value(), (byte[]) null, b);
                if (i < length) {
                    b.writeByte(44); //, for array item
                }
            }
            b.writeByte(93); // ] for annotation field
        }
    }
}
