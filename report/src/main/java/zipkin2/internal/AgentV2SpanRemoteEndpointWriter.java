package zipkin2.internal;

import zipkin2.Span;

public class AgentV2SpanRemoteEndpointWriter extends AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {
    final String remoteEndpointFieldName = ",\"remoteEndpoint\":";

    @Override
    public int sizeInBytes(Span value) {
        if (value.remoteEndpoint() == null) {
            return 0;
        }
        int size = remoteEndpointFieldName.length();
        size += this.endpointSizeInBytes(value.remoteEndpoint(), false);
        return size;
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        if (value.remoteEndpoint() == null) {
            return;
        }
        buffer.writeAscii(remoteEndpointFieldName);
        this.writeEndpoint(value.remoteEndpoint(), buffer, false);
    }
}
