package zipkin2.reporter.kafka11.codec;

import zipkin2.internal.AgentV2SpanWriter;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.Encoding;
import zipkin2.internal.*;

import java.util.List;

public enum AgentSpanBytesEncoder implements BytesEncoder<Span> {
    JSON_V1 {
        public Encoding encoding() {
            return Encoding.JSON;
        }

        public int sizeInBytes(Span input) {
            return (new V1JsonSpanWriter()).sizeInBytes(input);
        }

        public byte[] encode(Span span) {
            return JsonCodec.write(new V1JsonSpanWriter(), span);
        }

        public byte[] encodeList(List<Span> spans) {
            return JsonCodec.writeList(new V1JsonSpanWriter(), spans);
        }

        public int encodeList(List<Span> spans, byte[] out, int pos) {
            return JsonCodec.writeList(new V1JsonSpanWriter(), spans, out, pos);
        }
    },
    THRIFT {
        public Encoding encoding() {
            return Encoding.THRIFT;
        }

        public int sizeInBytes(Span input) {
            return (new V1ThriftSpanWriter()).sizeInBytes(input);
        }

        public byte[] encode(Span span) {
            return (new V1ThriftSpanWriter()).write(span);
        }

        public byte[] encodeList(List<Span> spans) {
            return (new V1ThriftSpanWriter()).writeList(spans);
        }

        public int encodeList(List<Span> spans, byte[] out, int pos) {
            return (new V1ThriftSpanWriter()).writeList(spans, out, pos);
        }
    },
    JSON_V2 {
        final AgentV2SpanWriter writer = new AgentV2SpanWriter();

        public Encoding encoding() {
            return Encoding.JSON;
        }

        public int sizeInBytes(Span input) {
            return this.writer.sizeInBytes(input);
        }

        public byte[] encode(Span span) {
            return JsonCodec.write(this.writer, span);
        }

        public byte[] encodeList(List<Span> spans) {
            return JsonCodec.writeList(this.writer, spans);
        }

        public int encodeList(List<Span> spans, byte[] out, int pos) {
            return JsonCodec.writeList(this.writer, spans, out, pos);
        }
    },
    PROTO3 {
        final Proto3Codec codec = new Proto3Codec();

        public Encoding encoding() {
            return Encoding.PROTO3;
        }

        public int sizeInBytes(Span input) {
            return this.codec.sizeInBytes(input);
        }

        public byte[] encode(Span span) {
            return this.codec.write(span);
        }

        public byte[] encodeList(List<Span> spans) {
            return this.codec.writeList(spans);
        }

        public int encodeList(List<Span> spans, byte[] out, int pos) {
            return this.codec.writeList(spans, out, pos);
        }
    };

    private AgentSpanBytesEncoder() {
    }

    public abstract int encodeList(List<Span> var1, byte[] var2, int var3);
}
