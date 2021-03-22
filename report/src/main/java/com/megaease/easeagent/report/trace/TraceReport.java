package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.util.Utils;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.SDKAsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.kafka11.SDKKafkaSender;
import zipkin2.reporter.kafka11.SimpleSender;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TraceReport {

    private final RefreshableReporter<Span> spanRefreshableReporter;

    public TraceReport(Configs configs) {
        spanRefreshableReporter = initSpanRefreshableReporter(configs);
        configs.addChangeListener(new InternalListener());
    }

    private RefreshableReporter<Span> initSpanRefreshableReporter(Configs configs) {
        final RefreshableReporter<Span> spanRefreshableReporter;
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        Sender sender = new SimpleSender();
        TraceProps traceProperties = Utils.extractTraceProps(configs);
        if (traceProperties.getOutput().isEnabled() && traceProperties.isEnabled()) {
            sender = SDKKafkaSender.wrap(traceProperties,
                    KafkaSender.newBuilder()
                            .bootstrapServers(outputProperties.getServers())
                            .topic(traceProperties.getOutput().getTopic())
                            .encoding(Encoding.JSON)
                            .messageMaxBytes(traceProperties.getOutput().getMessageMaxBytes())
                            .build());
        }

        String service = ConfigUtils.extractServiceName(configs);

        // We don't support change service and system name in runtime1
        SDKAsyncReporter reporter = SDKAsyncReporter.
                builderSDKAsyncReporter(AsyncReporter.builder(sender)
                                .queuedMaxSpans(traceProperties.getOutput().getQueuedMaxSpans())
                                .messageTimeout(traceProperties.getOutput().getMessageTimeout(), TimeUnit.MILLISECONDS)
                                .queuedMaxBytes(traceProperties.getOutput().getQueuedMaxSize()),
                        traceProperties,
                        service);
        reporter.startFlushThread();
        spanRefreshableReporter = new RefreshableReporter<Span>(reporter, traceProperties, outputProperties);
        return spanRefreshableReporter;
    }

    public void report(Span span) {
        this.spanRefreshableReporter.report(span);
    }

    private class InternalListener implements ConfigChangeListener {
        @Override
        public void onChange(List<ChangeItem> list) {
            if (Utils.isOutputPropertiesChange(list) || Utils.isTraceOutputPropertiesChange(list)) {
                spanRefreshableReporter.refresh();
            }
        }
    }
}
