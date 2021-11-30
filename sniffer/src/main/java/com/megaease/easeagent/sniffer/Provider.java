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
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.common.jdbc.MD5DictionaryItem;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.core.IProvider;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.MetricProvider;
import com.megaease.easeagent.core.TracingProvider;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.ChainBuilderFactory;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.AgentHttpHandlerProvider;
import com.megaease.easeagent.metrics.AutoRefreshReporter;
import com.megaease.easeagent.metrics.MetricProviderImpl;
import com.megaease.easeagent.metrics.MetricRegistryService;
import com.megaease.easeagent.metrics.PrometheusAgentHttpHandler;
import com.megaease.easeagent.metrics.config.MetricsCollectorConfig;
import com.megaease.easeagent.metrics.converter.MetricsAdditionalAttributes;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetric;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import com.megaease.easeagent.metrics.servlet.GatewayMetricsInterceptor;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.trace.ITracing;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.report.metric.MetricItem;
import com.megaease.easeagent.sniffer.healthy.AgentHealth;
import com.megaease.easeagent.sniffer.healthy.interceptor.OnApplicationEventInterceptor;
import com.megaease.easeagent.sniffer.thread.CrossThreadPropagationConfig;
import com.megaease.easeagent.sniffer.webclient.WebClientBuildInterceptor;
import com.megaease.easeagent.zipkin.CustomTagsSpanHandler;
import com.megaease.easeagent.zipkin.RootSpanFinishHandler;
import com.megaease.easeagent.zipkin.http.ServletHttpLogInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayLogInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayServerTracingInterceptor;
import com.megaease.easeagent.zipkin.http.webclient.WebClientTracingInterceptor;
import com.megaease.easeagent.zipkin.impl.TracingImpl;
import com.megaease.easeagent.zipkin.logging.AgentMDCScopeDecorator;
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
import java.util.function.Function;
import java.util.function.Supplier;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.KEY_METRICS_MD5_DICTIONARY;

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
    private ITracing iTracing;
    private AgentReport agentReport;
    private Config config;
    private Supplier<Map<String, Object>> additionalAttributes;
    private AutoRefreshConfigItem<String> serviceName;
    private Function rootSpanFinish;
    private MetricProviderImpl metricProvider = new MetricProviderImpl();


    @Override
    public void setConfig(Config config) {
        this.config = config;
        this.additionalAttributes = new MetricsAdditionalAttributes(config);
        this.metricProvider.setConfig(config);
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.agentReport = report;
        this.metricProvider.setAgentReport(report);
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
        Boolean compressionEnabled = config.getBoolean("observability.tracings.output.target.zipkin.compressionEnabled");
        boolean toZipkin = false;
        if (target.equalsIgnoreCase("zipkin") && StringUtils.isNotEmpty(zipkinUrl)) {
            toZipkin = true;
        }
        Reporter<Span> reporter;
        if (toZipkin) {
            reporter = AsyncReporter.create(URLConnectionSender.newBuilder().endpoint(zipkinUrl).compressionEnabled(compressionEnabled == null ? true : compressionEnabled).build());
        } else {
            reporter = span -> agentReport.report(span);
        }
        this.tracing = Tracing.newBuilder()
            .localServiceName(serviceName.getValue())
            .traceId128Bit(false)
            .sampler(CountingSampler.create(1))
            .addSpanHandler(new CustomTagsSpanHandler(serviceName::getValue, AdditionalAttributes.getHostName()))
            .addSpanHandler(new RootSpanFinishHandler((context) -> {
                if (rootSpanFinish != null) {
                    rootSpanFinish.apply(context);
                }
                return null;
            }))
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
    public TracingSupplier tracingSupplier() {
        return (supplier) -> {
            if (iTracing != null) {
                return iTracing;
            }
            synchronized (Provider.class) {
                if (iTracing != null) {
                    return iTracing;
                }
                iTracing = TracingImpl.build(supplier, tracing);
            }
            return iTracing;
        };
    }

    @Override
    public void setRootSpanFinishCall(Function rootSpanFinish) {
        this.rootSpanFinish = rootSpanFinish;
    }

    @Override
    public MetricRegistrySupplier metricSupplier() {
        return metricProvider.metricSupplier();
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

    @Injection.Bean("supplier4Filter")
    public Supplier<AgentInterceptorChain.Builder> supplier4Filter() {
        return () -> {
//            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
//            MetricsCollectorConfig collectorConfig = new MetricsCollectorConfig(this.config, ConfigConst.Observability.KEY_METRICS_REQUEST);
//            ServletMetric servletMetric = new ServletMetric(metricRegistry);
//            new AutoRefreshReporter(metricRegistry, collectorConfig, servletMetric.newConverter(this.additionalAttributes),
//                s -> this.agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_REQUEST, s))).run();
            return ChainBuilderFactory.DEFAULT.createBuilder()
//                .addInterceptor(new HTTPHeaderExtractInterceptor(new CrossThreadPropagationConfig(this.config)))
//                .addInterceptor(new HttpFilterMetricsInterceptor(servletMetric, config))
//                .addInterceptor(new HttpFilterTracingInterceptor(this.tracing, config))
                .addInterceptor(new ServletHttpLogInterceptor(config, s -> agentReport.report(new MetricItem(ConfigConst.Observability.KEY_METRICS_ACCESS, s))))
                ;
        };
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

    @Injection.Bean("supplier4OnApplicationEvent")
    public Supplier<AgentInterceptorChain.Builder> supplier4OnApplicationEvent() {
        return () -> ChainBuilderFactory.DEFAULT.createBuilder()
            .addInterceptor(new OnApplicationEventInterceptor());
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
}
