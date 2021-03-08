package zipkin2.internal;

import zipkin2.Span;

public class AgentV2SpanLocalEndpointWriter extends AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {
    final String localEndpointFieldName = ",\"localEndpoint\":";

    @Override
    public int sizeInBytes(Span value) {
        if (value.localEndpoint() == null) {
            return 0;
        }
        int size = localEndpointFieldName.length();
        size += this.endpointSizeInBytes(value.localEndpoint(), true);
        return size;
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        if (value.localEndpoint() == null) {
            return;
        }
        buffer.writeAscii(localEndpointFieldName);
        this.writeEndpoint(value.localEndpoint(), buffer, true);
    }
}
