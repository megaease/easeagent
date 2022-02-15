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

import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.report.async.SDKAsyncReporter;
import com.megaease.easeagent.report.async.TraceAsyncProps;
import com.megaease.easeagent.report.encoder.span.GlobalExtrasSupplier;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import zipkin2.reporter.Reporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class TraceReport {
    private final RefreshableReporter<ReportSpan> spanRefreshableReporter;

    public TraceReport(Config configs) {
        spanRefreshableReporter = initSpanRefreshableReporter(configs);
        configs.addChangeListener(new InternalListener());
    }

    private RefreshableReporter<ReportSpan> initSpanRefreshableReporter(Config configs) {
        SenderWithEncoder sender = ReporterRegistry.getSender(ReportConfigConst.TRACE_SENDER, configs);

        TraceAsyncProps traceProperties = TraceAsyncProps.newDefault(configs);

        GlobalExtrasSupplier extrasSupplier = new GlobalExtrasSupplier() {
            final AutoRefreshConfigItem<String> serviceName = new AutoRefreshConfigItem<>(configs, ConfigConst.SERVICE_NAME, Config::getString);
            final AutoRefreshConfigItem<String> systemName = new AutoRefreshConfigItem<>(configs, ConfigConst.SYSTEM_NAME, Config::getString);

            @Override
            public String service() {
                return serviceName.getValue();
            }

            @Override
            public String system() {
                return systemName.getValue();
            }
        };

        SDKAsyncReporter<ReportSpan> reporter = SDKAsyncReporter.
            builderSDKAsyncReporter(sender, traceProperties, extrasSupplier);

        reporter.startFlushThread();

        return new RefreshableReporter<>(reporter, traceProperties);
    }

    public void report(ReportSpan span) {
        this.spanRefreshableReporter.report(span);
    }

    private class InternalListener implements ConfigChangeListener {
        @Override
        public void onChange(List<ChangeItem> list) {
            Map<String, String> cfg = filterChanges(list);

            if (cfg.isEmpty()) {
                return;
            }
            cfg = ConfigUtils.extractAndConvertPrefix(cfg, TRACE_OUTPUT_V1, TRACE_ASYNC);
            spanRefreshableReporter.refresh(cfg);
        }

        private Map<String, String> filterChanges(List<ChangeItem> list) {
            Map<String, String> cfg = new HashMap<>();
            list.stream()
                .filter(one -> {
                    String name = one.getFullName();
                    return name.startsWith(TRACE_OUTPUT_V1)
                        || name.startsWith(TRACE_SENDER_NAME)
                        || name.startsWith(TRACE_ASYNC);
                }).forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

            return cfg;
        }
    }

    static Reporter<ReportSpan> NOOP = new Reporter<ReportSpan>() {
        @Override
        public void report(ReportSpan span) {
        }

        @Override public String toString() {
            return "NoopReporter{}";
        }
    };
}
