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

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.report.OutputProperties;
import org.apache.commons.lang3.StringUtils;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.SDKAsyncReporter;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.kafka11.SDKKafkaSender;
import zipkin2.reporter.kafka11.SimpleSender;

import java.util.concurrent.TimeUnit;

/**
 * RefreshableReporter is a reporter wrapper, which enhances the AgentAsyncReporter with refreshable function
 *
 * @param <S> always zipkin2.reporter
 */
public class RefreshableReporter<S> implements Reporter<S> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RefreshableReporter.class);
    private final SDKAsyncReporter<S> asyncReporter;
    private final TraceProps traceProperties;
    private final OutputProperties agentOutputProperties;


    public RefreshableReporter(SDKAsyncReporter<S> reporter,
                               TraceProps traceProperties,
                               OutputProperties agentOutputProperties) {
        this.asyncReporter = reporter;
        this.traceProperties = traceProperties;
        this.agentOutputProperties = agentOutputProperties;
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


    public synchronized void refresh() {
        if (asyncReporter.getSender() != null) {
            try {
                asyncReporter.getSender().close();
                asyncReporter.closeFlushThread();
            } catch (Exception ignored) {
            }
        }

        if (traceProperties.getOutput().isEnabled() && traceProperties.isEnabled()
                && StringUtils.isNotEmpty(agentOutputProperties.getServers())) {
            final SDKKafkaSender sender = SDKKafkaSender.wrap(traceProperties,
                    KafkaSender.newBuilder()
                            .bootstrapServers(agentOutputProperties.getServers())
                            .topic(traceProperties.getOutput().getTopic())
                            .messageMaxBytes(traceProperties.getOutput().getMessageMaxBytes())
                            .encoding(Encoding.JSON)
                            .build());
            asyncReporter.setSender(sender);
            LOGGER.info("Set async reporter to Kafka sender");

        } else {
            asyncReporter.setSender(new SimpleSender());
        }
        asyncReporter.setPending(traceProperties.getOutput().getQueuedMaxSpans(), traceProperties.getOutput().getQueuedMaxSize());
        asyncReporter.setMessageTimeoutNanos(messageTimeout(traceProperties.getOutput().getMessageTimeout(), TimeUnit.MILLISECONDS));
        asyncReporter.startFlushThread(); // start thread
    }

    protected long messageTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            timeout = 1000L;
        }
        if (unit == null) {
            unit = TimeUnit.MILLISECONDS;
        }
        return unit.toNanos(timeout);
    }
}
