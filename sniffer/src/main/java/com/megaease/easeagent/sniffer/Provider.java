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
import brave.sampler.CountingSampler;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.utils.SQLCompression;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcConMetricInterceptor;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStatementMetricInterceptor;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import com.megaease.easeagent.metrics.redis.CommonRedisMetricInterceptor;
import com.megaease.easeagent.metrics.redis.RedisMetricInterceptor;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.CommonRedisClientConnectInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.RedisChannelWriterInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.StatefulRedisConnectionInterceptor;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterLogInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayServerTracingInterceptor;
import com.megaease.easeagent.zipkin.jdbc.JdbcStatementTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.CommonLettuceTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.JedisTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.SpringRedisTracingInterceptor;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

public abstract class Provider implements AgentReportAware, ConfigAware {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    private final AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();

    private final SQLCompression sqlCompression = SQLCompression.DEFAULT;

    private Tracer tracer;

    private Tracing tracing;
    private AgentReport agentReport;
    private Config config;

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.agentReport = report;
    }

    public void loadTracing() {
        if (tracer == null) {
            Tracing tracing = Tracing.newBuilder()
                    .localServiceName(config.getString(ConfigConst.SERVICE_NAME))
                    .traceId128Bit(false)
                    .sampler(CountingSampler.create(1))
                    .addSpanHandler(AsyncZipkinSpanHandler
                            .newBuilder(span -> agentReport.report(span))
                            .alwaysReportSpans(true)
                            .build()
                    ).build();
            Tracer tracer = tracing.tracer();
            this.tracing = tracing;
            this.tracer = tracer;
        }
    }

    @Injection.Bean
    public Tracing tracing() {
        loadTracing();
        return tracing;
    }

    @Injection.Bean
    public MetricRegistry metricRegistry() {
//        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
//                .outputTo(LoggerFactory.getLogger(JVMMemoryMetric.class))
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        reporter.start(10, 30, TimeUnit.SECONDS);
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
        return chainInvoker;
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
        builder.addInterceptor(new SpringGatewayInitGlobalFilterInterceptor(headersFilterChainBuilder, chainInvoker));
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
                .addInterceptor(new RedisMetricInterceptor(this.metricRegistry))
                .addInterceptor(new SpringRedisTracingInterceptor())
                ;
    }

    @Injection.Bean("builder4RedisClientConnectAsync")
    public AgentInterceptorChain.Builder builder4RedisClientConnectAsync() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RedisMetricInterceptor(this.metricRegistry))
                .addInterceptor(new CommonRedisClientConnectInterceptor())
                ;
    }

    @Injection.Bean("builder4StatefulRedisConnection")
    public AgentInterceptorChain.Builder builder4StatefulRedisConnection() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new StatefulRedisConnectionInterceptor())
                ;
    }

    @Injection.Bean("builder4LettuceDoWrite")
    public AgentInterceptorChain.Builder builder4LettuceDoWrite() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RedisChannelWriterInterceptor())
                .addInterceptor(new CommonRedisMetricInterceptor(this.metricRegistry))
                .addInterceptor(new CommonLettuceTracingInterceptor())
                ;
    }

    @Injection.Bean("builder4Jedis")
    public AgentInterceptorChain.Builder builder4Jedis() {
        loadTracing();
        return new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new CommonRedisMetricInterceptor(this.metricRegistry))
                .addInterceptor(new JedisTracingInterceptor())
                ;
    }

    @Injection.Bean("builder4KafkaDoSend")
    public AgentInterceptorChain.Builder builder4KafkaDoSend() {
        return new DefaultAgentInterceptorChain.Builder();
    }

    @Injection.Bean("commonInterceptorChainBuilder")
    public AgentInterceptorChain.Builder commonInterceptorChainBuilder() {
        return new DefaultAgentInterceptorChain.Builder();
    }
}
