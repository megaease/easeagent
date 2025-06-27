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
import brave.sampler.BoundarySampler;
import brave.sampler.CountingSampler;
import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.annotation.Injection;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.ITracing;
import com.megaease.easeagent.plugin.api.trace.TracingProvider;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import com.megaease.easeagent.plugin.bean.AgentInitializingBean;
import com.megaease.easeagent.plugin.bean.BeanProvider;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.AdditionalAttributes;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.zipkin.impl.TracingImpl;
import com.megaease.easeagent.zipkin.logging.AgentMDCScopeDecorator;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.brave.ConvertZipkinSpanHandler;

public class TracingProviderImpl implements BeanProvider, AgentReportAware, ConfigAware, AgentInitializingBean, TracingProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TracingProviderImpl.class);
    private static final String ENV_ZIPKIN_SERVER_URL = "ZIPKIN_SERVER_URL";

    public static final String SAMPLER_TYPE_COUNTING = "counting";
    public static final String SAMPLER_TYPE_RATE_LIMITING = "rate_limiting";
    public static final String SAMPLER_TYPE_BOUNDARY = "boundary";
    private Tracing tracing;
    private volatile ITracing iTracing;
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

        Reporter<ReportSpan> reporter;
        reporter = span -> agentReport.report(span);
        this.tracing = Tracing.newBuilder()
            .localServiceName(getServiceName())
            .traceId128Bit(false)
            .sampler(getSampler())
            .addSpanHandler(new CustomTagsSpanHandler(this::getServiceName, AdditionalAttributes.getHostName()))
            .addSpanHandler(ConvertZipkinSpanHandler
                .builder(reporter)
                .alwaysReportSpans(true)
                .build()
            )
            .currentTraceContext(traceContext)
            .build();
    }


    protected Sampler getSampler() {
        String sampledType = this.config.getString(ConfigConst.Observability.TRACE_SAMPLED_TYPE);
        if (sampledType == null) {
            return Sampler.ALWAYS_SAMPLE;
        }
        Double probability = this.config.getDouble(ConfigConst.Observability.TRACE_SAMPLED);
        if (probability == null) {
            return Sampler.ALWAYS_SAMPLE;
        }
        try {
            switch (sampledType) {
                case SAMPLER_TYPE_COUNTING:
                    return CountingSampler.create(probability.floatValue());
                case SAMPLER_TYPE_RATE_LIMITING:
                    return RateLimitingSampler.create(probability.intValue());
                case SAMPLER_TYPE_BOUNDARY:
                    return BoundarySampler.create(probability.floatValue());
                default:
                    return Sampler.ALWAYS_SAMPLE;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("observability.tracings.sampled error, use Sampler.ALWAYS_SAMPLE for.", e.getMessage());
            return Sampler.ALWAYS_SAMPLE;
        }
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
