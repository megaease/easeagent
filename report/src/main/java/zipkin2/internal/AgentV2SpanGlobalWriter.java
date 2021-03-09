package zipkin2.internal;

import com.megaease.easeagent.report.trace.TraceProps;
import com.megaease.easeagent.report.util.TextUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.Span;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

import java.util.Optional;

public class AgentV2SpanGlobalWriter implements WriteBuffer.Writer<Span> {

    final String type;
    final String service;//= ApplicationUtils.getBean(Environment.class).getProperty(MetricNameBuilder
    // .SPRING_APPLICATION_NAME, "");
    final String system;// = ApplicationUtils.getBean(Environment.class).getProperty(MetricNameBuilder.SYSTEM_NAME, "");
    final TraceProps traceProperties;//= ApplicationUtils.getBean(TraceProperties.class);

    final String typeFieldName = ",\"type\":\"";
    final String serviceFieldName = ",\"service\":\"";
    final String systemFieldName = ",\"system\":\"";

    public AgentV2SpanGlobalWriter(String type, String service, String system, TraceProps tp) {
        this.type = type;
        this.service = service;
        this.system = system;
        this.traceProperties = tp;
    }

    @Override
    public int sizeInBytes(Span value) {
        final MutableInt mutableInt = new MutableInt(0);
        Optional.ofNullable(traceProperties).ifPresent(t -> {
            if (TextUtils.hasText(type)) {
                mutableInt.add(typeFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(type));
            }

            if (TextUtils.hasText(service)) {
                mutableInt.add(serviceFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(service));
            }

            if (TextUtils.hasText(system)) {
                mutableInt.add(systemFieldName.length() + 1);
                mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(system));
            }
        });
        return mutableInt.intValue();
    }

    @Override
    public void write(Span value, WriteBuffer buffer) {
        Optional.ofNullable(traceProperties).ifPresent(t -> {
            if (TextUtils.hasText(type)) {
                buffer.writeAscii(typeFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(type));
                buffer.writeByte(34);
            }

            if (TextUtils.hasText(service)) {
                buffer.writeAscii(serviceFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(service));
                buffer.writeByte(34);
            }

            if (TextUtils.hasText(system)) {
                buffer.writeAscii(systemFieldName);
                buffer.writeUtf8(JsonEscaper.jsonEscape(system));
                buffer.writeByte(34);
            }
        });
    }
}
