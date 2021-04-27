/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.*;
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

        AutoRefreshConfigItem<String> serviceName = new AutoRefreshConfigItem<>(configs, ConfigConst.SERVICE_NAME, Config::getString);
        SDKAsyncReporter reporter = SDKAsyncReporter.
                builderSDKAsyncReporter(AsyncReporter.builder(sender)
                                .queuedMaxSpans(traceProperties.getOutput().getQueuedMaxSpans())
                                .messageTimeout(traceProperties.getOutput().getMessageTimeout(), TimeUnit.MILLISECONDS)
                                .queuedMaxBytes(traceProperties.getOutput().getQueuedMaxSize()),
                        traceProperties,
                        serviceName::getValue);
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
