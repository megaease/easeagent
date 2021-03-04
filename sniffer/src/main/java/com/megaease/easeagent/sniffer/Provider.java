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

package com.megaease.easeagent.sniffer;

import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.sampler.CountingSampler;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.SQLCompression;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcConMetricInterceptor;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStatementMetricInterceptor;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import com.megaease.easeagent.zipkin.LogSender;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterLogInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import com.megaease.easeagent.zipkin.jdbc.JdbcStatementTracingInterceptor;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

import java.util.concurrent.TimeUnit;

@Configurable(bind = "sniffer.report")
public abstract class Provider {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    private final SQLCompression sqlCompression = SQLCompression.DEFAULT;

    private Tracer tracer;

    private Tracing tracing;

    public void loadTracing() {
        if (tracer == null) {
            Tracing tracing = Tracing.newBuilder()
                    .localServiceName(service_name())
                    .traceId128Bit(trace_id_128b())
                    .sampler(CountingSampler.create((float) sample_rate()))
                    .addSpanHandler(spanHandler())
                    .build();
            Tracer tracer = tracing.tracer();
            this.tracing = tracing;
            this.tracer = tracer;
        }
    }

    @Injection.Bean("agentInterceptor4Con")
    public AgentInterceptor agentInterceptor4Con() {
        return new JdbcConMetricInterceptor(metricRegistry);
    }

    @Injection.Bean("agentInterceptor4Stm")
    public AgentInterceptor agentInterceptor4Stm() {
        return new AgentInterceptor.Builder()
                .addInterceptor(new JdbcStatementMetricInterceptor(metricRegistry, sqlCompression))
                .addInterceptor(new JdbcStatementTracingInterceptor(sqlCompression))
                .build();
    }

//    @Injection.Bean("agentInterceptor4HttpServlet")
//    public AgentInterceptor agentInterceptor4HttpServlet() {
//        this.loadTracing();
//        return new HttpServletTracingInterceptor();
//    }

    @Injection.Bean("agentInterceptor4HttpFilter")
    public AgentInterceptor agentInterceptor4HttpFilter() {
        this.loadTracing();
        return new AgentInterceptor.Builder()
                .addInterceptor(new HttpFilterMetricsInterceptor(metricRegistry))
                .addInterceptor(new HttpFilterTracingInterceptor(this.tracing))
                .addInterceptor(new HttpFilterLogInterceptor())
                .build();
    }

    @Injection.Bean("agentInterceptor4RestTemplate")
    public AgentInterceptor agentInterceptor4RestTemplate() {
        this.loadTracing();
        return new RestTemplateTracingInterceptor(this.tracing);
    }

    @Injection.Bean("agentInterceptor4FeignClient")
    public AgentInterceptor agentInterceptor4FeignClient() {
        this.loadTracing();
        return new FeignClientTracingInterceptor(this.tracing);
    }

    private SpanHandler spanHandler() {
        return AsyncZipkinSpanHandler.create(new LogSender());
    }

    @Configurable.Item
    String reporter_name() {
        return "metrics";
    }

    @Configurable.Item
    String rate_unit() {
        return TimeUnit.SECONDS.toString();
    }

    @Configurable.Item
    String duration_unit() {
        return TimeUnit.MILLISECONDS.toString();
    }

    @Configurable.Item
    long period_seconds() {
        return 30;
    }

    @Configurable.Item
    String hostipv4() {
        return HostAddress.localaddr().getHostAddress();
    }

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
        return 1024 * 1024;
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
        return HostAddress.localaddr().getHostAddress();
    }

    @Configurable.Item
    String hostname() {
        return HostAddress.localhost();
    }
}
