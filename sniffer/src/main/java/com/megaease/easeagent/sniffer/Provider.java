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
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.CountingSampler;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.AdditionalAttributes;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.common.config.SwitchUtil;
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
import com.megaease.easeagent.core.MetricProvider;
import com.megaease.easeagent.core.TracingProvider;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.ChainBuilderFactory;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.utils.JsonUtil;
import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.AgentHttpHandlerProvider;
import com.megaease.easeagent.metrics.AutoRefreshReporter;
import com.megaease.easeagent.metrics.MetricRegistryService;
import com.megaease.easeagent.metrics.PrometheusAgentHttpHandler;
import com.megaease.easeagent.metrics.config.MetricsCollectorConfig;
import com.megaease.easeagent.metrics.config.MetricsConfig;
import com.megaease.easeagent.metrics.config.PluginMetricsConfig;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.converter.MetricsAdditionalAttributes;
import com.megaease.easeagent.metrics.impl.MetricRegistryImpl;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcDataSourceMetricInterceptor;
import com.megaease.easeagent.metrics.jdbc.interceptor.JdbcStmMetricInterceptor;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import com.megaease.easeagent.metrics.kafka.KafkaConsumerMetricInterceptor;
import com.megaease.easeagent.metrics.kafka.KafkaMessageListenerMetricInterceptor;
import com.megaease.easeagent.metrics.kafka.KafkaMetric;
import com.megaease.easeagent.metrics.kafka.KafkaProducerMetricInterceptor;
import com.megaease.easeagent.metrics.redis.JedisMetricInterceptor;
import com.megaease.easeagent.metrics.redis.LettuceMetricInterceptor;
import com.megaease.easeagent.metrics.servlet.GatewayMetricsInterceptor;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import com.megaease.easeagent.metrics.servlet.ServletMetric;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.report.PluginMetricReporter;
import com.megaease.easeagent.report.metric.MetricItem;
import com.megaease.easeagent.sniffer.healthy.AgentHealth;
import com.megaease.easeagent.sniffer.healthy.interceptor.OnApplicationEventInterceptor;
import com.megaease.easeagent.zipkin.impl.TracingImpl;
import com.megaease.easeagent.sniffer.jdbc.interceptor.JdbConPrepareOrCreateStmInterceptor;
import com.megaease.easeagent.sniffer.jdbc.interceptor.JdbcStmPrepareSqlInterceptor;
import com.megaease.easeagent.sniffer.kafka.spring.KafkaMessageListenerInterceptor;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaConsumerConstructInterceptor;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaConsumerPollInterceptor;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaProducerConstructInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.CommonRedisClientConnectInterceptor;
import com.megaease.easeagent.sniffer.lettuce.v5.interceptor.RedisChannelWriterInterceptor;
import com.megaease.easeagent.sniffer.thread.CrossThreadPropagationConfig;
import com.megaease.easeagent.sniffer.thread.HTTPHeaderExtractInterceptor;
import com.megaease.easeagent.sniffer.webclient.WebClientBuildInterceptor;
import com.megaease.easeagent.zipkin.CustomTagsSpanHandler;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import com.megaease.easeagent.zipkin.http.ServletHttpLogInterceptor;
import com.megaease.easeagent.zipkin.http.httpclient.HttpClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.httpclient5.HttpClient5AsyncTracingInterceptor;
import com.megaease.easeagent.zipkin.http.httpclient5.HttpClient5TracingInterceptor;
import com.megaease.easeagent.zipkin.http.okhttp.OkHttpAsyncTracingInterceptor;
import com.megaease.easeagent.zipkin.http.okhttp.OkHttpTracingInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayLogInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayServerTracingInterceptor;
import com.megaease.easeagent.zipkin.http.webclient.WebClientTracingInterceptor;
import com.megaease.easeagent.zipkin.jdbc.JdbcStmTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.spring.KafkaMessageListenerTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaConsumerTracingInterceptor;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaProducerTracingInterceptor;
import com.megaease.easeagent.zipkin.logging.AgentMDCScopeDecorator;
import com.megaease.easeagent.zipkin.redis.CommonLettuceTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.JedisTracingInterceptor;
import org.apache.commons.lang3.StringUtils;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.megaease.easeagent.config.ConfigConst.Observability.KEY_METRICS_MD5_DICTIONARY;

// import com.megaease.easeagent.metrics.rabbitmq.*;
/*
import com.megaease.easeagent.sniffer.rabbitmq.spring.RabbitMqMessageListenerOnMessageInterceptor;
import com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor.RabbitMqChannelConsumeInterceptor;
import com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor.RabbitMqChannelPublishInterceptor;
import com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor.RabbitMqConsumerHandleDeliveryInterceptor;
*/
/*
import com.megaease.easeagent.zipkin.rabbitmq.spring.RabbitMqMessageListenerTracingInterceptor;
import com.megaease.easeagent.zipkin.rabbitmq.v5.RabbitMqConsumerTracingInterceptor;
import com.megaease.easeagent.zipkin.rabbitmq.v5.RabbitMqProducerTracingInterceptor;
*/

public abstract class Provider implements AgentReportAware, ConfigAware, IProvider, AgentHttpHandlerProvider, TracingProvider, MetricProvider {

    private static final String EASEAGENT_HEALTH_READINESS_ENABLED = "easeagent.health.readiness.enabled";
    private final AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance().setLogElapsedTime(false);
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
    public List<AgentHttpHandler> getAgentHttpHandlers() {
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new AgentHealth.HealthAgentHttpHandler());
        list.add(new AgentHealth.LivenessAgentHttpHandler());
        list.add(new AgentHealth.ReadinessAgentHttpHandler());
        list.add(new PrometheusAgentHttpHandler());
        return list;
    }

    @Override
    public void afterPropertiesSet() {
        AgentHealth.instance.setReadinessEnabled(this.config.getBoolean(EASEAGENT_HEALTH_READINESS_ENABLED));
        ThreadLocalCurrentTraceContext traceContext = ThreadLocalCurrentTraceContext.newBuilder()
            .addScopeDecorator(AgentMDCScopeDecorator.get())
            .build();
        serviceName = new AutoRefreshConfigItem<>(config, ConfigConst.SERVICE_NAME, Config::getString);
        String target = config.getString("observability.tracings.output.target");
        String zipkinUrl = config.getString("observability.tracings.output.target.zipkinUrl");
        boolean toZipkin = false;
        if (target.equalsIgnoreCase("zipkin") && StringUtils.isNotEmpty(zipkinUrl)) {
            toZipkin = true;
        }
        Reporter<Span> reporter;
        if (toZipkin) {
            reporter = AsyncReporter.create(URLConnectionSender.create(zipkinUrl));
        } else {
            reporter = span -> agentReport.report(span);
        }
        this.tracing = Tracing.newBuilder()
            .localServiceName(serviceName.getValue())
            .traceId128Bit(false)
            .sampler(CountingSampler.create(1))
            .addSpanHandler(new CustomTagsSpanHandler(serviceName::getValue, AdditionalAttributes.getHostName()))
            .addSpanHandler(AsyncZipkinSpanHandler
                .newBuilder(reporter)
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
    public Supplier<com.megaease.easeagent.plugin.api.trace.Tracing> tracingSupplier() {
        return () -> TracingImpl.build(tracing);
    }

    @Override
    public MetricRegistrySupplier metricSupplier() {
        return new ApplicationMetricRegistrySupplier();
    }

    @Injection.Bean
    public CrossThreadPropagationConfig crossThreadPropagationConfig() {
        return new CrossThreadPropagationConfig(this.config);
    }

    @Injection.Bean
    public JVMMemoryMetric jvmMemoryMetric() {
        MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
        JVMMemoryMetric jvmMemoryMetric = new JVMMemoryMetric(metricRegistry, config);
        MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JVM_MEMORY);
        new AutoRefreshReporter(metricRegistry, collectorConfig,
            jvmMemoryMetric.newConverter(this.additionalAttributes),
            s -> Provider.this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JVM_MEMORY, s))).run();
        return jvmMemoryMetric;
    }

    @Injection.Bean
    public JVMGCMetric jvmgcMetric() {
        MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
        JVMGCMetric jvmgcMetric = new JVMGCMetric(metricRegistry, config);
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
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JDBC_CONNECTION);
            final JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor(metricRegistry, config);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                interceptor.newConverter(this.additionalAttributes),
                s -> Provider.this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JDBC_CONNECTION, s))).run();
            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(interceptor);
        };

    }

    @Injection.Bean("supplier4JdbcCon")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcCon() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new JdbConPrepareOrCreateStmInterceptor());

    }

    @Injection.Bean("supplier4JdbcStmPrepareSql")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcStmPrepareSql() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new JdbcStmPrepareSqlInterceptor());
    }

    @Injection.Bean("supplier4JdbcStmExecute")
    public Supplier<AgentInterceptorChain.Builder> supplier4JdbcStmExecute() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            SQLCompression sqlCompression = new MD5SQLCompression(new Md5ReportConsumer(config));
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_JDBC_STATEMENT);
            JdbcStmMetricInterceptor metricInterceptor = new JdbcStmMetricInterceptor(metricRegistry, sqlCompression, config);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                metricInterceptor.newConverter(this.additionalAttributes),
                s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_JDBC_STATEMENT, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new JdbcStmPrepareSqlInterceptor())
                .addInterceptor(metricInterceptor)
                .addInterceptor(new JdbcStmTracingInterceptor(sqlCompression, config));
        };
    }


    @Injection.Bean("supplier4Filter")
    public Supplier<AgentInterceptorChain.Builder> supplier4Filter() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_REQUEST);
            ServletMetric servletMetric = new ServletMetric(metricRegistry);
            new AutoRefreshReporter(metricRegistry, collectorConfig, servletMetric.newConverter(this.additionalAttributes),
                s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_REQUEST, s))).run();
            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new HTTPHeaderExtractInterceptor(new CrossThreadPropagationConfig(this.config)))
                .addInterceptor(new HttpFilterMetricsInterceptor(servletMetric, config))
                .addInterceptor(new HttpFilterTracingInterceptor(this.tracing, config))
                .addInterceptor(new ServletHttpLogInterceptor(config, s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_ACCESS, s))))
                ;
        };
    }

    @Injection.Bean("supplier4RestTemplate")
    public Supplier<AgentInterceptorChain.Builder> supplier4RestTemplate() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new RestTemplateTracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4FeignClient")
    public Supplier<AgentInterceptorChain.Builder> supplier4FeignClient() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new FeignClientTracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4Gateway")
    public Supplier<AgentInterceptorChain.Builder> supplier4Gateway() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_REQUEST);
            GatewayMetricsInterceptor gatewayMetricsInterceptor = new GatewayMetricsInterceptor(metricRegistry, config);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                gatewayMetricsInterceptor.newConverter(this.additionalAttributes),
                s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_REQUEST, s))).run();
            AgentInterceptorChain.Builder headersFilterChainBuilder = ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(gatewayMetricsInterceptor)
                .addInterceptor(new SpringGatewayServerTracingInterceptor(tracing, config))
                .addInterceptor(new SpringGatewayLogInterceptor(config, s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_ACCESS, s))));
            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new SpringGatewayInitGlobalFilterInterceptor(headersFilterChainBuilder, chainInvoker));
        };
    }

    @Injection.Bean("supplier4GatewayHeaders")
    public Supplier<AgentInterceptorChain.Builder> supplier4GatewayHeaders() {
        return () -> new DefaultAgentInterceptorChain.Builder()
            .addInterceptor(new SpringGatewayHttpHeadersInterceptor(this.tracing));
    }

    @Injection.Bean("supplier4RedisClientConnectAsync")
    public Supplier<AgentInterceptorChain.Builder> supplier4RedisClientConnectAsync() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new CommonRedisClientConnectInterceptor());
    }

    @Injection.Bean("supplier4RedisClusterConnectAsync")
    public Supplier<AgentInterceptorChain.Builder> supplier4RedisClusterConnectAsync() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new CommonRedisClientConnectInterceptor());
    }

    @Injection.Bean("supplier4LettuceDoWrite")
    public Supplier<AgentInterceptorChain.Builder> supplier4LettuceDoWrite() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            LettuceMetricInterceptor metricInterceptor = new LettuceMetricInterceptor(metricRegistry, config);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_CACHE);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                metricInterceptor.newConverter(additionalAttributes),
                s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_CACHE, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new RedisChannelWriterInterceptor())
                .addInterceptor(metricInterceptor)
                .addInterceptor(new CommonLettuceTracingInterceptor(this.tracing, config));
        };
    }

    @Injection.Bean("supplier4Jedis")
    public Supplier<AgentInterceptorChain.Builder> supplier4Jedis() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            JedisMetricInterceptor metricInterceptor = new JedisMetricInterceptor(metricRegistry, config);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_CACHE);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                metricInterceptor.newConverter(additionalAttributes),
                s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_CACHE, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(metricInterceptor)
                .addInterceptor(new JedisTracingInterceptor(this.tracing, config));
        };
    }

    @Injection.Bean("supplier4KafkaProducerDoSend")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaProducerDoSend() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                kafkaMetric.newConverter(additionalAttributes),
                s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            KafkaProducerMetricInterceptor metricInterceptor = new KafkaProducerMetricInterceptor(kafkaMetric, config);
            KafkaProducerTracingInterceptor tracingInterceptor = new KafkaProducerTracingInterceptor(tracing, config);

            AgentInterceptorChain.Builder builder4Async = ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(metricInterceptor)
                .addInterceptor(tracingInterceptor);

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new KafkaProducerDoSendInterceptor(chainInvoker, builder4Async))
                .addInterceptor(metricInterceptor)
                .addInterceptor(tracingInterceptor);
        };
    }

    @Injection.Bean("supplier4KafkaProducerConstructor")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaProducerConstructor() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new KafkaProducerConstructInterceptor());
    }

    @Injection.Bean("supplier4KafkaConsumerConstructor")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaConsumerConstructor() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new KafkaConsumerConstructInterceptor());
    }

    @Injection.Bean("supplier4KafkaConsumerDoPoll")
    public Supplier<AgentInterceptorChain.Builder> supplier4KafkaConsumerDoPoll() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                kafkaMetric.newConverter(additionalAttributes),
                s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new KafkaConsumerPollInterceptor())
                .addInterceptor(new KafkaConsumerTracingInterceptor(tracing, config))
                .addInterceptor(new KafkaConsumerMetricInterceptor(kafkaMetric, config))
                ;
        };
    }

    @Injection.Bean("supplier4SpringKafkaMessageListenerOnMessage")
    public Supplier<AgentInterceptorChain.Builder> supplier4SpringKafkaMessageListenerOnMessage() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            KafkaMetric kafkaMetric = new KafkaMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_KAFKA);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                kafkaMetric.newConverter(additionalAttributes),
                s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_KAFKA, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new KafkaMessageListenerInterceptor())
                .addInterceptor(new KafkaMessageListenerMetricInterceptor(kafkaMetric, config))
                .addInterceptor(new KafkaMessageListenerTracingInterceptor(tracing, config));
        };
    }

    /*
    @Injection.Bean("supplier4RabbitMqBasicPublish")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqBasicPublish() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
            RabbitMqProducerMetricInterceptor metricInterceptor = new RabbitMqProducerMetricInterceptor(metric, config);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_RABBIT);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_RABBIT, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                    .addInterceptor(new RabbitMqChannelPublishInterceptor())
                    .addInterceptor(metricInterceptor)
                    .addInterceptor(new RabbitMqProducerTracingInterceptor(tracing, config))
                    ;
        };
    }

    @Injection.Bean("supplier4RabbitMqBasicConsume")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqBasicConsume() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new RabbitMqChannelConsumeInterceptor());
    }

    @Injection.Bean("supplier4RabbitMqHandleDelivery")
    public Supplier<AgentInterceptorChain.Builder> supplier4RabbitMqHandleDelivery() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
            RabbitMqConsumerMetricInterceptor metricInterceptor = new RabbitMqConsumerMetricInterceptor(metric, config);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_RABBIT);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_RABBIT, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                    .addInterceptor(new RabbitMqConsumerHandleDeliveryInterceptor())
                    .addInterceptor(metricInterceptor)
                    .addInterceptor(new RabbitMqConsumerTracingInterceptor(tracing, config))
                    ;
        };
    }

    @Injection.Bean("supplier4SpringRabbitMqMessageListenerOnMessage")
    public Supplier<AgentInterceptorChain.Builder> supplier4SpringRabbitMqMessageListenerOnMessage() {
        return () -> {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);

            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(config, ConfigConst.Observability.KEY_METRICS_RABBIT);
            new AutoRefreshReporter(metricRegistry, collectorConfig,
                    metric.newConverter(additionalAttributes),
                    s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_RABBIT, s))).run();

            return ChainBuilderFactory.DEFAULT.createBuilder()
                    .addInterceptor(new RabbitMqMessageListenerOnMessageInterceptor())
                    .addInterceptor(new RabbitMqMessageListenerMetricInterceptor(metric, config))
                    .addInterceptor(new RabbitMqMessageListenerTracingInterceptor(tracing, config))
                    ;
        };
    }
    */

    @Injection.Bean("supplier4WebClientBuild")
    public Supplier<AgentInterceptorChain.Builder> supplier4WebClientBuild() {
        return () -> {
            AgentInterceptorChain.Builder chainBuilder = ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new WebClientTracingInterceptor(tracing, config));
            return ChainBuilderFactory.DEFAULT.createBuilder()
                .addInterceptor(new WebClientBuildInterceptor(chainBuilder, chainInvoker))
                ;
        };
    }

    @Injection.Bean("supplier4HttpClient")
    public Supplier<AgentInterceptorChain.Builder> getSupplier4HttpClient() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new HttpClientTracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4HttpClient5")
    public Supplier<AgentInterceptorChain.Builder> getSupplier4HttpClient5() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new HttpClient5TracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4HttpClient5Async")
    public Supplier<AgentInterceptorChain.Builder> getSupplier4HttpClient5Async() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new HttpClient5AsyncTracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4OnApplicationEvent")
    public Supplier<AgentInterceptorChain.Builder> supplier4OnApplicationEvent() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new OnApplicationEventInterceptor());
    }

    @Injection.Bean("supplier4OkHttp")
    public Supplier<AgentInterceptorChain.Builder> supplier4OkHttp() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new OkHttpTracingInterceptor(tracing, config));
    }

    @Injection.Bean("supplier4OkHttpAsync")
    public Supplier<AgentInterceptorChain.Builder> supplier4OkHttpAsync() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new OkHttpAsyncTracingInterceptor(tracing, config));
    }

    class Md5ReportConsumer implements Consumer<Map<String, String>> {
        private final Config config;
        private static final String ENABLE_KEY = "observability.metrics.md5Dictionary.enabled";

        public Md5ReportConsumer(Config config) {
            this.config = config;
        }

        @Override
        public void accept(Map<String, String> map) {
            if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
                return;
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                MD5DictionaryItem item = MD5DictionaryItem.builder()
                    .timestamp(System.currentTimeMillis())
                    .category("application")
                    .hostName(HostAddress.localhost())
                    .hostIpv4(HostAddress.getHostIpv4())
                    .gid("")
                    .system(config.getString("system"))
                    .service(serviceName.getValue())
                    .tags("")
                    .type("md5-dictionary")
                    .id("")
                    .md5(entry.getKey())
                    .sql(entry.getValue())
                    .build();
                String json = JsonUtil.toJson(item);
                agentReport.report(new MetricItem(KEY_METRICS_MD5_DICTIONARY, json));
            }
        }
    }

    class ApplicationMetricRegistrySupplier implements MetricRegistrySupplier {

        @Override
        public com.megaease.easeagent.plugin.api.metric.MetricRegistry newMetricRegistry(com.megaease.easeagent.plugin.api.config.Config config, NameFactory nameFactory, Tags tags) {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            MetricsConfig metricsConfig = new PluginMetricsConfig(config);
            ConverterAdapter converterAdapter = new ConverterAdapter(nameFactory, KeyType.Timer, Provider.this.additionalAttributes, tags);
            PluginMetricReporter.Reporter reporter = agentReport.pluginMetricReporter().reporter(config);
            new AutoRefreshReporter(metricRegistry, metricsConfig,
                converterAdapter,
                s -> reporter.report(s)).run();
            return MetricRegistryImpl.build(metricRegistry);
        }
    }
}
