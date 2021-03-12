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
import com.codahale.metrics.Slf4jReporter;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.utils.SQLCompression;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcConMetricInterceptor;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStatementMetricInterceptor;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import com.megaease.easeagent.metrics.redis.SpringRedisMetricInterceptor;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.RedisClientCreateInterceptor;
import com.megaease.easeagent.zipkin.LogSender;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterLogInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayServerTracingInterceptor;
import com.megaease.easeagent.zipkin.jdbc.JdbcStatementTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.SpringRedisTracingInterceptor;
import org.slf4j.LoggerFactory;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

import java.util.concurrent.TimeUnit;

@Configurable(bind = "sniffer.report")
public abstract class Provider {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    private final AgentInterceptorChainInvoker agentInterceptorChainInvoker = AgentInterceptorChainInvoker.getInstance();

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

    @Injection.Bean
    public MetricRegistry metricRegistry() {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(JVMMemoryMetric.class))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, 30, TimeUnit.SECONDS);
        return metricRegistry;
    }

    @Injection.Bean
    public JVMMemoryMetric jvmMemoryMetric() {
        return new JVMMemoryMetric(this.metricRegistry);
    }

    @Injection.Bean
    public JVMGCMetric jvmgcMetric() {
        return new JVMGCMetric(this.metricRegistry);
    }

    @Injection.Bean
    public AgentInterceptorChainInvoker agentInterceptorChainInvoker() {
        return agentInterceptorChainInvoker;
    }

    @Injection.Bean("agentInterceptorChainBuilder4Con")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4Con() {
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new JdbcConMetricInterceptor(metricRegistry));
    }

    @Injection.Bean("agentInterceptorChainBuilder4Stm")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4Stm() {
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new JdbcStatementMetricInterceptor(metricRegistry, sqlCompression))
                .addInterceptor(new JdbcStatementTracingInterceptor(sqlCompression))
                ;
    }

    @Injection.Bean("agentInterceptorChainBuilder4Filter")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4Filter() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new HttpFilterMetricsInterceptor(metricRegistry))
                .addInterceptor(new HttpFilterTracingInterceptor(this.tracing))
                .addInterceptor(new HttpFilterLogInterceptor())
                ;
    }

    @Injection.Bean("agentInterceptorChainBuilder4RestTemplate")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4RestTemplate() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RestTemplateTracingInterceptor(tracing))
                ;
    }

    @Injection.Bean("agentInterceptorChainBuilder4FeignClient")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4FeignClient() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new FeignClientTracingInterceptor(tracing))
                ;
    }

    @Injection.Bean("agentInterceptorChainBuilder4Gateway")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4Gateway() {
        AgentInterceptorChain.Builder headersFilterChainBuilder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayServerTracingInterceptor(tracing));
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder();
        builder.addInterceptor(new SpringGatewayInitGlobalFilterInterceptor(headersFilterChainBuilder, agentInterceptorChainInvoker));
        return builder;
    }

    @Injection.Bean("agentInterceptorChainBuilder4GatewayHeaders")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4GatewayHeaders() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(this.tracing))
                ;
    }

    @Injection.Bean("agentInterceptorChainBuilder4SpringRedis")
    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4SpringRedis() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringRedisMetricInterceptor(this.metricRegistry))
                .addInterceptor(new SpringRedisTracingInterceptor())
                ;
    }

    @Injection.Bean("builder4RedisClientCreate")
    public AgentInterceptorChain.Builder builder4RedisClientCreate() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RedisClientCreateInterceptor())
//                .addInterceptor(new SpringRedisMetricInterceptor(this.metricRegistry))
//                .addInterceptor(new SpringRedisTracingInterceptor())
                ;
    }
    @Injection.Bean("builder4RedisClientConnect")
    public AgentInterceptorChain.Builder builder4RedisClientConnect() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RedisClientCreateInterceptor())
//                .addInterceptor(new SpringRedisMetricInterceptor(this.metricRegistry))
//                .addInterceptor(new SpringRedisTracingInterceptor())
                ;
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
