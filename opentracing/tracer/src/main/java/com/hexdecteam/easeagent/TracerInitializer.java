package com.hexdecteam.easeagent;

import brave.Tracer;
import brave.opentracing.BraveTracer;
import brave.sampler.CountingSampler;
import com.google.auto.service.AutoService;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.urlconnection.URLConnectionSender;

import java.lang.instrument.Instrumentation;

import static java.util.concurrent.TimeUnit.SECONDS;

@AutoService(Plugin.class)
public class TracerInitializer implements Plugin<TracerInitializer.Configuration> {

    @Override
    public void hook(Configuration conf, Instrumentation inst, Subscription subs) {
        final Sender sender = URLConnectionSender.builder()
                                                 .endpoint(conf.send_endpoint())
                                                 .encoding(Encoding.valueOf(conf.send_encoding()))
                                                 .compressionEnabled(conf.send_compression())
                                                 .build();
        final Reporter<Span> reporter = AsyncReporter.builder(sender)
                                                     .queuedMaxSpans(conf.reporter_queued_max_spans())
                                                     .messageTimeout(conf.reporter_message_timeout_seconds(), SECONDS)
                                                     .build();
        final Tracer tracer = Tracer.newBuilder()
                                    .localServiceName(conf.service_name())
                                    .traceId128Bit(conf.trace_id_128b())
                                    .sampler(CountingSampler.create(conf.sample_rate()))
                                    .reporter(reporter)
                                    .build();

        TraceContext.init(BraveTracer.wrap(tracer));
    }

    @ConfigurationDecorator.Binding("opentracing.tracer")
    static abstract class Configuration {

        public abstract String send_endpoint();

        public abstract String service_name();

        public float sample_rate() {
            return 1f;
        }

        public boolean send_compression() {
            return true;
        }

        public String send_encoding() {
            return Encoding.THRIFT.name();
        }

        public int reporter_queued_max_spans() {
            return 10000;
        }

        public long reporter_message_timeout_seconds() {
            return 1;
        }

        public boolean trace_id_128b() {
            return false;
        }
    }
}
