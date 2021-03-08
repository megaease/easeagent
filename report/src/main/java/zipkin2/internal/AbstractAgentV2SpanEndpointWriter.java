package zipkin2.internal;

import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

public abstract class AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<Span> {

    final String serviceNameFieldName = "\"serviceName\":\"";
    final String ipv4FieldName = "\"ipv4\":\"";
    final String ipv6FieldName = "\"ipv6\":\"";
    final String portFieldName = "\"port\":";

    protected int endpointSizeInBytes(Endpoint value, boolean writeEmptyServiceName) {
        int sizeInBytes = 1; // one byte for {
        String serviceName = value.serviceName();
        if (serviceName == null && writeEmptyServiceName) {
            serviceName = "";
        }

        if (serviceName != null) {
            sizeInBytes += serviceNameFieldName.length() + 1;
            sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(serviceName);
        }

        if (value.ipv4() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += ipv4FieldName.length() + 1;
            sizeInBytes += value.ipv4().length();
        }

        if (value.ipv6() != null) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += ipv6FieldName.length() + 1;
            sizeInBytes += value.ipv6().length();
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (sizeInBytes != 1) {
                ++sizeInBytes;
            }

            sizeInBytes += portFieldName.length();
            sizeInBytes += WriteBuffer.asciiSizeInBytes((long) port);
        }

        sizeInBytes += 1; // one byte for }
        return sizeInBytes;
    }

    protected void writeEndpoint(Endpoint value, WriteBuffer b, boolean writeEmptyServiceName) {

        b.writeByte('{');
        boolean wroteField = false;
        String serviceName = value.serviceName();
        if (serviceName == null && writeEmptyServiceName) {
            serviceName = "";
        }

        if (serviceName != null) {
            b.writeAscii(serviceNameFieldName);
            b.writeUtf8(JsonEscaper.jsonEscape(serviceName));
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv4() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(ipv4FieldName);
            b.writeAscii(value.ipv4());
            b.writeByte('\"');
            wroteField = true;
        }

        if (value.ipv6() != null) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(ipv6FieldName);
            b.writeAscii(value.ipv6());
            b.writeByte('\"');
            wroteField = true;
        }

        int port = value.portAsInt();
        if (port != 0) {
            if (wroteField) {
                b.writeByte(',');
            }
            b.writeAscii(portFieldName);
            b.writeAscii((long) port);
        }

        b.writeByte('}');
    }
}
