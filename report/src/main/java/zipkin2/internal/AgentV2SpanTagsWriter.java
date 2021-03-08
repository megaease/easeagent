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
