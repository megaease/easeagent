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

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import brave.sampler.CountingSampler;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.common.JsonUtil;
import com.megaease.easeagent.common.jdbc.MD5DictionaryItem;
import com.megaease.easeagent.common.jdbc.MD5SQLCompression;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.kafka.KafkaProducerDoSendInterceptor;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.core.IProvider;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.metrics.AutoRefreshReporter;
import com.megaease.easeagent.metrics.MetricsCollectorConfig;
import com.megaease.easeagent.metrics.converter.MetricsAdditionalAttributes;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcDataSourceMetricInterceptor;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStmMetricInterceptor;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import com.megaease.easeagent.metrics.kafka.KafkaConsumerMetricInterceptor;
import com.megaease.easeagent.metrics.kafka.KafkaMetric;
import com.megaease.easeagent.metrics.kafka.KafkaProducerMetricInterceptor;
import com.megaease.easeagent.metrics.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.metrics.rabbitmq.RabbitMqConsumerMetricInterceptor;
import com.megaease.easeagent.metrics.rabbitmq.RabbitMqProducerMetric;
import com.megaease.easeagent.metrics.rabbitmq.RabbitMqProducerMetricInterceptor;
import com.megaease.easeagent.metrics.redis.JedisMetricInterceptor;
import com.megaease.easeagent.metrics.redis.LettuceMetricInterceptor;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.report.metric.MetricItem;
import com.megaease.easeagent.sniffer.jdbc.interceptor.JdbConPrepareOrCreateStmInterceptor;
import com.megaease.easeagent.sniffer.jdbc.interceptor.JdbcStmPrepareSqlInterceptor;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaConsumerConstructInterceptor;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaProducerConstructInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.CommonRedisClientConnectInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.RedisChannelWriterInterceptor;
import com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor.RabbitMqChannelConsumeInterceptor;
import com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor.RabbitMqChannelPublishInterceptor;
import com.megaease.easeagent.sniffer.thread.CrossThreadPropagationConfig;
import com.megaease.easeagent.sniffer.thread.HTTPHeaderExtractInterceptor;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterLogInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayServerTracingInterceptor;
import com.megaease.easeagent.zipkin.jdbc.JdbcStmTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.spring.KafkaMessageListenerTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaConsumerTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaProducerTracingInterceptor;
import com.megaease.easeagent.zipkin.rabbitmq.v5.RabbitMqConsumerTracingInterceptor;
import com.megaease.easeagent.zipkin.rabbitmq.v5.RabbitMqProducerTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.CommonLettuceTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.JedisTracingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.megaease.easeagent.config.ConfigConst.Observability.KEY_METRICS_MD5_DICTIONARY;

public abstract class Provider implements AgentReportAware, ConfigAware, IProvider {

    private final AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(Provider.class);
    private Tracing tracing;
    private AgentReport agentReport;
    private Config config;
    private Supplier<Map<String, Object>> additionalAttributes;
    private AutoRefreshConfigItem<String> serviceName;

    @Override
    public void setConfig(Config config) {
        this.config = config;
        this.additionalAttributes = new MetricsAdditionalAttributes(config);
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.agentReport = report;
    }

    @Override
    public void afterPropertiesSet() {
        serviceName = new AutoRefreshConfigItem<>(config, ConfigConst.SERVICE_NAME, Config::getString);
        this.tracing = Tracing.newBuilder()
                .localServiceName(config.getString(ConfigConst.SERVICE_NAME))
                .traceId128Bit(false)
                .sampler(CountingSampler.create(1))
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        logger.info(span.toString());
//                        return true;
//                    }
//                })
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        span.localServiceName(serviceName.getValue());
                        return true;
                    }
                })
                .addSpanHandler(AsyncZipkinSpanHandler
                        .newBuilder(span -> agentReport.report(span))
                        .alwaysReportSpans(true)
                        .build()
                ).build();
    }

    @Injection.Bean
    public Tracing tracing() {
        return tracing;
    }

    @Injection.Bean
    public CrossThreadPropagationConfig crossThreadPropagationConfig() {
        return new CrossThreadPropagationConfig(this.config);
    }

    @Injection.Bean
    public JVMMemoryMetric jvmMemoryMetric() {
        MetricRegistry metricRegistry = new MetricRegistry();
        JVMMemoryMetric jvmMemoryMetric = new JVMMemoryMetric(metricRegistry);
        MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JVM_MEMORY);
        new AutoRefreshReporter(metricRegistry, collectorConfig,
                jvmMemoryMetric.newConverter(this.additionalAttributes),
                s -> Provider.this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JVM_MEMORY, s))).run();
        return jvmMemoryMetric;
    }

    @Injection.Bean
    public JVMGCMetric jvmgcMetric() {
        MetricRegistry metricRegistry = new MetricRegistry();
        JVMGCMetric jvmgcMetric = new JVMGCMetric(metricRegistry);
        MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JVM_GC);
        new AutoRefreshReporter(metricRegistry, collectorConfig,
                jvmgcMetric.newConverter(this.additionalAttributes),
                s -> Provider.this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JVM_GC, s))).run();
        return jvmgcMetric;
    }

    @Injection.Bean
    public AgentInterceptorChainInvoker agentInterceptorChainInvoker() {
        return chainInvoker;
    }

    @Injection.Bean("supplier4DataSourceGetCon")
    public Supplier<AgentInterceptorChain.Builder> supplier4DataSourceGetCon() {
        return () -> {
            MetricRegistry metricRegistry = new MetricRegistry();
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JDBC_CONNECTION);
            final JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor(metricRegistry);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    interceptor.newConverter(this.additionalAttributes),
                    s -> Provider.this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JDBC_CONNECTION, s))).run();
            return new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(interceptor);
        };

    }

    @Injection.Bean("supplier4JdbcCon")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcCon() {
        return () -> new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new JdbConPrepareOrCreateStmInterceptor());

    }

    @Injection.Bean("supplier4JdbcStmPrepareSql")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcStmPrepareSql() {
        return () -> {
            DefaultAgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder();
            builder.addInterceptor(new JdbcStmPrepareSqlInterceptor());
            return builder;
        };
    }

    @Injection.Bean("supplier4JdbcStmExecute")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcStmExecute() {
        return () -> {
            MetricRegistry metricRegistry = new MetricRegistry();
            SQLCompression sqlCompression = new MD5SQLCompression(new Md5ReportConsumer());
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JDBC_STATEMENT);
            JdbcStmMetricInterceptor metricInterceptor = new JdbcStmMetricInterceptor(metricRegistry, sqlCompression);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metricInterceptor.newConverter(this.additionalAttributes),
                    s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JDBC_STATEMENT, s))).run();

            DefaultAgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder();
            builder.addInterceptor(new JdbcStmPrepareSqlInterceptor());
            builder.addInterceptor(metricInterceptor);
            builder.addInterceptor(new JdbcStmTracingInterceptor(sqlCompression));
            return builder;
        };
    }


    @Injection.Bean("supplier4Filter")
    public Supplier<AgentInterceptorChain.Builder> supplier4Filter() {
        return () -> {
            MetricRegistry metricRegistry = new MetricRegistry();
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_REQUEST);
            final HttpFilterMetricsInterceptor httpFilterMetricsInterceptor = new HttpFilterMetricsInterceptor(metricRegistry);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    httpFilterMetricsInterceptor.newConverter(this.additionalAttributes),
                    s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_REQUEST, s))).run();
            return new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(new HTTPHeaderExtractInterceptor(new CrossThreadPropagationConfig(this.config)))
                    .addInterceptor(httpFilterMetricsInterceptor)
                    .addInterceptor(new HttpFilterTracingInterceptor(this.tracing))
                    .addInterceptor(new HttpFilterLogInterceptor(serviceName, s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_ACCESS, s))))
                    ;
        };
    }

    @Injection.Bean("supplier4RestTemplate")
    public Supplier<AgentInterceptorChain.Builder> supplier4RestTemplate() {
        return () -> {
            return new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(new RestTemplateTracingInterceptor(tracing))
                    ;
        };
    }

    @Injection.Bean("supplier4FeignClient")
    public Supplier<AgentInterceptorChain.Builder> supplier4FeignClient() {
        return () -> {
            return new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(new FeignClientTracingInterceptor(tracing))
                    ;
        };
    }

    @Injection.Bean("supplier4Gateway")
    public Supplier<AgentInterceptorChain.Builder> supplier4Gateway() {
        return () -> {
            AgentInterceptorChain.Builder headersFilterChainBuilder = new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(new SpringGatewayServerTracingInterceptor(tracing));
            AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder();
            builder.addInterceptor(new SpringGatewayInitGlobalFilterInterceptor(headersFilterChainBuilder, chainInvoker));
            return builder;
        };
    }

    @Injection.Bean("supplier4GatewayHeaders")
    public Supplier<AgentInterceptorChain.Builder> supplier4GatewayHeaders() {
        return () -> {
            return new DefaultAgentInterceptorChain.Builder()
                    .addInterceptor(new SpringGatewayHttpHeadersInterceptor(this.tracing))
                    ;
        };
    }

//    @Injection.Bean("agentInterceptorChainBuilder4SpringRedis")
//    public AgentInterceptorChain.Builder agentInterceptorChainBuilder4SpringRedis() {
//        loadTracing();
//        return new DefaultAgentInterceptorChain.Builder()
//                .addInterceptor(new RedisMetricInterceptor(new MetricRegistry()))
//                .addInterceptor(new SpringRedisTracingInterceptor())
//                ;
//    }

    @Injection.Bean("supplier4RedisClientConnectAsync")
    public Supplier<AgentInterceptorChain.Builder> supplier4RedisClientConnectAsync() {
        return () -> new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new CommonRedisClientConnectInterceptor());
    }

    @Injection.Bean("supplier4LettuceDoWrite")
    public Supplier<AgentInterceptorChain.Builder> supplier4LettuceDoWrite() {
        return () -> new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new RedisChannelWriterInterceptor())
                .addInterceptor(new LettuceMetricInterceptor(new MetricRegistry()))
                .addInterceptor(new CommonLettuceTracingInterceptor());
    }

    @Injection.Bean("supplier4Jedis")
    public Supplier<AgentInterceptorChain.Builder> supplier4Jedis() {
        return () -> new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new JedisMetricInterceptor(new MetricRegistry()))
                .addInterceptor(new JedisTracingInterceptor());
    }

    @Injection.Bean("supplier4KafkaProducerDoSend")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaProducerDoSend() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            MetricRegistry metricRegistry = new MetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    kafkaMetric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            KafkaProducerMetricInterceptor metricInterceptor = new KafkaProducerMetricInterceptor(kafkaMetric);
            KafkaProducerTracingInterceptor tracingInterceptor = new KafkaProducerTracingInterceptor(tracing);

            DefaultAgentInterceptorChain.Builder builder4Async = new DefaultAgentInterceptorChain.Builder();
            builder4Async.addInterceptor(metricInterceptor)
                    .addInterceptor(tracingInterceptor);

            chainBuilder.addInterceptor(new KafkaProducerDoSendInterceptor(chainInvoker, builder4Async))
                    .addInterceptor(metricInterceptor)
                    .addInterceptor(tracingInterceptor);

            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4KafkaProducerConstructor")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaProducerConstructor() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            chainBuilder.addInterceptor(new KafkaProducerConstructInterceptor());
            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4KafkaConsumerConstructor")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaConsumerConstructor() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            chainBuilder.addInterceptor(new KafkaConsumerConstructInterceptor());
            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4KafkaConsumerDoPoll")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaConsumerDoPoll() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            MetricRegistry metricRegistry = new MetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    kafkaMetric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            chainBuilder.addInterceptor(new KafkaConsumerTracingInterceptor(tracing))
                    .addInterceptor(new KafkaConsumerMetricInterceptor(kafkaMetric));

            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4SpringKafkaMessageListenerOnMessage")
    public Supplier<AgentInterceptorChain.Builder> supplier4SpringKafkaMessageListenerOnMessage() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            MetricRegistry metricRegistry = new MetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    kafkaMetric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            chainBuilder.addInterceptor(new KafkaMessageListenerTracingInterceptor(tracing));

            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4RabbitMqBasicPublish")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqBasicPublish() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            MetricRegistry metricRegistry = new MetricRegistry();
            RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
            RabbitMqProducerMetricInterceptor metricInterceptor = new RabbitMqProducerMetricInterceptor(metric);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_RABBIT);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_RABBIT, s))).run();

            chainBuilder.addInterceptor(new RabbitMqChannelPublishInterceptor())
                    .addInterceptor(metricInterceptor)
                    .addInterceptor(new RabbitMqProducerTracingInterceptor(tracing))
            ;

            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4RabbitMqBasicConsume")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqBasicConsume() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();
            chainBuilder.addInterceptor(new RabbitMqChannelConsumeInterceptor());
            return chainBuilder;
        };
    }

    @Injection.Bean("supplier4RabbitMqHandleDelivery")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqHandleDelivery() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = new DefaultAgentInterceptorChain.Builder();

            MetricRegistry metricRegistry = new MetricRegistry();
            RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
            RabbitMqConsumerMetricInterceptor metricInterceptor = new RabbitMqConsumerMetricInterceptor(metric);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_RABBIT);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_RABBIT, s))).run();

            chainBuilder
                    .addInterceptor(metricInterceptor)
                    .addInterceptor(new RabbitMqConsumerTracingInterceptor(tracing))
            ;
            return chainBuilder;
        };
    }

    class Md5ReportConsumer implements Consumer<Map<String, String>> {

        @Override
        public void accept(Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                MD5DictionaryItem item = MD5DictionaryItem.builder()
                        .timestamp(System.currentTimeMillis())
                        .category("application")
                        .hostName(HostAddress.localhost())
                        .hostIpv4(HostAddress.localaddr().getHostAddress())
                        .gid("")
                        .service(serviceName.getValue())
                        .tags("")
                        .type(KEY_METRICS_MD5_DICTIONARY)
                        .id("")
                        .md5(entry.getKey())
                        .sql(entry.getValue())
                        .build();
                String json = JsonUtil.toJson(item);
                agentReport.report(new MetricItem(KEY_METRICS_MD5_DICTIONARY, json));
            }
        }
    }
}
