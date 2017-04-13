package com.megaease.easeagent.zipkin;

import brave.Tracer;
import brave.internal.Platform;
import brave.sampler.CountingSampler;
import com.google.common.base.Strings;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.LocalhostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import zipkin.Codec;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoder;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;

import static java.util.concurrent.TimeUnit.SECONDS;
import static zipkin.BinaryAnnotation.create;

@Configurable(bind = "zipkin.tracer")
abstract class Provider {

    @Injection.Bean
    public CallTrace callTrace() {
        return new CallTrace();
    }

    @Injection.Bean
    public Tracer tracer() {
        return Tracer.newBuilder()
                     .localServiceName(service_name())
                     .traceId128Bit(trace_id_128b())
                     .sampler(CountingSampler.create((float) sample_rate()))
                     .reporter(reporter(sender()))
                     .build();

    }

    private Sender sender() {
        return Strings.isNullOrEmpty(send_endpoint())
                ? new LogSender()
                : new GatewaySender(message_max_bytes(), send_endpoint(), connect_timeout(), read_timeout(),
                                    send_compression(), user_agent());
    }

    private AsyncReporter<Span> reporter(Sender sender) {
        return AsyncReporter.builder(sender)
                            .queuedMaxSpans(reporter_queued_max_spans())
                            .messageTimeout(reporter_message_timeout_seconds(), SECONDS)
                            .build(encoder());
    }

    private Encoder<Span> encoder() {
        final Endpoint endpoint = Platform.get().localEndpoint().toBuilder().serviceName(service_name()).build();
        return new Encoder<Span>() {
            @Override
            public Encoding encoding() {
                return Encoding.JSON;
            }

            @Override
            public byte[] encode(Span span) {
                return Codec.JSON.writeSpan(span.toBuilder()
                                                .addBinaryAnnotation(create("system", system(), endpoint))
                                                .addBinaryAnnotation(create("application", application(), endpoint))
                                                .addBinaryAnnotation(create("hostipv4", host_ipv4(), endpoint))
                                                .addBinaryAnnotation(create("hostname", hostname(), endpoint))
                                                .build());
            }
        };
    }

    @Configurable.Item
    abstract String send_endpoint();

    @Configurable.Item
    abstract String system();

    @Configurable.Item
    abstract String application();

    @Configurable.Item
    String service_name() {
        return system() + "-" + application() + "-" + hostname();
    }

    @Configurable.Item
    double sample_rate() {
        return 1f;
    }

    @Configurable.Item
    boolean send_compression() {
        return false;
    }

    @Configurable.Item
    int reporter_queued_max_spans() {
        return 10000;
    }

    @Configurable.Item
    long reporter_message_timeout_seconds() {
        return 1;
    }

    @Configurable.Item
    boolean trace_id_128b() {
        return false;
    }

    @Configurable.Item
    int message_max_bytes() {
        return 5 * 1024 * 10240;
    }

    @Configurable.Item
    int connect_timeout() {
        return 10 * 1000;
    }

    @Configurable.Item
    int read_timeout() {
        return 60 * 1000;
    }

    @Configurable.Item
    String user_agent() {
        return "easeagent/0.1.0";
    }

    @Configurable.Item
    String host_ipv4() {
        return LocalhostAddress.getLocalhostAddr().getHostAddress();
    }

    @Configurable.Item
    String hostname() {
        return LocalhostAddress.getLocalhostName();
    }

}
