/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.async.trace.TraceAsyncProps;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import com.megaease.easeagent.report.async.trace.SDKAsyncReporter;
import com.megaease.easeagent.report.async.AsyncProps;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import zipkin2.reporter.Reporter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.config.report.ReportConfigConst.TRACE_SENDER;
import static com.megaease.easeagent.config.report.ReportConfigConst.TRACE_SENDER_NAME;

/**
 * RefreshableReporter is a reporter wrapper, which enhances the AgentAsyncReporter with refreshable function
 *
 * @param <S> always zipkin2.reporter
 */
public class RefreshableReporter<S> implements Reporter<S> {
    private final SDKAsyncReporter<S> asyncReporter;
    private AsyncProps traceProperties;
    private Config reportConfig;

    public RefreshableReporter(SDKAsyncReporter<S> reporter,
                               Config reportConfig) {
        this.asyncReporter = reporter;
        this.traceProperties = new TraceAsyncProps(reportConfig);
        this.reportConfig = reportConfig;
    }

    /**
     * report delegate span report to asyncReporter
     *
     * @param span a span need to be reported
     */
    @Override
    public void report(S span) {
        this.asyncReporter.report(span);
    }

    public synchronized void refresh(Map<String, String> cfg) {
        String name = cfg.get(TRACE_SENDER_NAME);
        SenderWithEncoder sender = asyncReporter.getSender();
        if (sender != null) {
            if (StringUtils.isNotEmpty(name) && !sender.name().equals(name)) {
                try {
                    sender.close();
                } catch (Exception ignored) {
                    // ignored
                }
                sender = ReporterRegistry.getSender(TRACE_SENDER, this.reportConfig);
                asyncReporter.setSender(sender);
            }
        } else {
            sender = ReporterRegistry.getSender(TRACE_SENDER, this.reportConfig);
            asyncReporter.setSender(sender);
        }

        traceProperties = new TraceAsyncProps(this.reportConfig);
        asyncReporter.closeFlushThread();
        asyncReporter.setPending(traceProperties.getQueuedMaxItems(), traceProperties.getQueuedMaxSize());
        asyncReporter.setMessageTimeoutNanos(messageTimeout(traceProperties.getMessageTimeout()));
        asyncReporter.startFlushThread(); // start thread
    }

    protected long messageTimeout(long timeout) {
        if (timeout < 0) {
            timeout = 1000L;
        }
        return TimeUnit.MILLISECONDS.toNanos(timeout);
    }
}
