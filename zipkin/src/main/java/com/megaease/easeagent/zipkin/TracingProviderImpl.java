/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.CountingSampler;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.plugin.annotation.Injection;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.ITracing;
import com.megaease.easeagent.plugin.api.trace.TracingProvider;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import com.megaease.easeagent.plugin.bean.AgentInitializingBean;
import com.megaease.easeagent.plugin.bean.BeanProvider;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.AdditionalAttributes;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.zipkin.impl.TracingImpl;
import com.megaease.easeagent.zipkin.logging.AgentMDCScopeDecorator;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.brave.ConvertZipkinSpanHandler;

public class TracingProviderImpl implements BeanProvider, AgentReportAware, ConfigAware, AgentInitializingBean, TracingProvider {
    private static final String ENV_ZIPKIN_SERVER_URL = "ZIPKIN_SERVER_URL";
    private Tracing tracing;
    private ITracing iTracing;
    private AgentReport agentReport;
    private Config config;
    private AutoRefreshConfigItem<String> serviceName;


    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.agentReport = report;
    }

    @Override
    public void afterPropertiesSet() {
        ThreadLocalCurrentTraceContext traceContext = ThreadLocalCurrentTraceContext.newBuilder()
            .addScopeDecorator(AgentMDCScopeDecorator.get())
            .addScopeDecorator(AgentMDCScopeDecorator.getV2())
            .addScopeDecorator(AgentMDCScopeDecorator.getAgentDecorator())
            .build();

        serviceName = new AutoRefreshConfigItem<>(config, ConfigConst.SERVICE_NAME, Config::getString);

        /*
        String target = config.getString("observability.tracings.output.target");
        String zipkinUrl = config.getString("observability.tracings.output.target.zipkinUrl");
        Boolean compressionEnabled = config.getBoolean("observability.tracings.output.target.zipkin.compressionEnabled");

        boolean toZipkin = false;
        if (target != null && target.equalsIgnoreCase("zipkin") && StringUtils.isNotEmpty(zipkinUrl)) {
            toZipkin = true;
        }
        String zipkinUrlFromEnv = System.getenv(ENV_ZIPKIN_SERVER_URL);
        if (StringUtils.isNotEmpty(zipkinUrlFromEnv)) {
            toZipkin = true;
            zipkinUrl = zipkinUrlFromEnv;
        }
        Reporter<Span> reporter;
        if (toZipkin) {
            reporter = AsyncReporter.create(URLConnectionSender.newBuilder()
                .endpoint(zipkinUrl)
                .compressionEnabled(compressionEnabled == null || compressionEnabled).build());
        } else {
            reporter = span -> agentReport.report(span);
        }
        */

        Reporter<ReportSpan> reporter;
        reporter = span -> agentReport.report(span);
        this.tracing = Tracing.newBuilder()
            .localServiceName(getServiceName())
            .traceId128Bit(false)
            .sampler(CountingSampler.create(1))
            .addSpanHandler(new CustomTagsSpanHandler(this::getServiceName, AdditionalAttributes.getHostName()))
            .addSpanHandler(ConvertZipkinSpanHandler
                .builder(reporter)
                .alwaysReportSpans(true)
                .build()
            )
            .currentTraceContext(traceContext)
            .build();
    }

    @Injection.Bean
    public Tracing tracing() {
        return tracing;
    }

    @Override
    public TracingSupplier tracingSupplier() {
        return supplier -> {
            if (iTracing != null) {
                return iTracing;
            }
            synchronized (TracingProviderImpl.class) {
                if (iTracing != null) {
                    return iTracing;
                }
                iTracing = TracingImpl.build(supplier, tracing);
            }
            return iTracing;
        };
    }

    private String getServiceName() {
        return this.serviceName.getValue();
    }
}
