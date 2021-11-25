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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigManager;
import com.megaease.easeagent.core.MetricProvider;
import com.megaease.easeagent.core.TracingProvider;
import com.megaease.easeagent.core.log.LoggerFactoryImpl;
import com.megaease.easeagent.core.log.LoggerMdc;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.trace.ITracing;
import com.megaease.easeagent.plugin.api.trace.TracingSupplier;
import com.megaease.easeagent.plugin.bridge.*;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.report.AgentReport;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class ContextManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class.getName());
    private static final ThreadLocal<SessionContext> LOCAL_SESSION_CONTEXT = ThreadLocal.withInitial(SessionContext::new);
    private final PluginConfigManager pluginConfigManager;
    private final Function rootSpanFinish;
    private final Supplier<InitializeContext> sessionSupplier;
    private final GlobalContext globalContext;
    private volatile TracingSupplier tracingSupplier = (supplier) -> null;
    private volatile MetricRegistrySupplier metric = NoOpMetrics.NO_OP_METRIC_SUPPLIER;


    private ContextManager(@Nonnull Configs conf, @Nonnull PluginConfigManager pluginConfigManager, @Nonnull ILoggerFactory loggerFactory, @Nonnull Mdc mdc) {
        this.pluginConfigManager = pluginConfigManager;
        this.rootSpanFinish = new RootSpanFinish();
        this.sessionSupplier = new SessionContextSupplier();
        this.globalContext = new GlobalContext(conf, new MetricRegistrySupplierImpl(), loggerFactory, mdc);
    }

    public static ContextManager build(Configs conf) {
        LOGGER.info("build context manager.");
        ProgressFieldsManager.init(conf);
        PluginConfigManager pluginConfigManager = PluginConfigManager.builder(conf).build();
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        ILoggerFactory iLoggerFactory = NoOpLoggerFactory.INSTANCE;
        Mdc mdc = NoOpLoggerFactory.NO_OP_MDC_INSTANCE;
        if (loggerFactory != null) {
            iLoggerFactory = loggerFactory;
            mdc = new LoggerMdc(loggerFactory.facotry().mdc());
        }
        ContextManager contextManager = new ContextManager(conf, pluginConfigManager, iLoggerFactory, mdc);
        EaseAgent.loggerFactory = contextManager.globalContext.getLoggerFactory();
        EaseAgent.loggerMdc = contextManager.globalContext.getMdc();
        EaseAgent.contextSupplier = (Supplier) contextManager.sessionSupplier;
        EaseAgent.initializeContextSupplier = contextManager.sessionSupplier;
        EaseAgent.metricRegistrySupplier = contextManager.globalContext.getMetric();
        EaseAgent.configFactory = contextManager.pluginConfigManager;
        return contextManager;
    }

    public void setTracing(@Nonnull TracingProvider tracing) {
        LOGGER.info("set tracing supplier function.");
        this.tracingSupplier = tracing.tracingSupplier();
        tracing.setRootSpanFinishCall(rootSpanFinish);
    }

    public void setMetric(@Nonnull MetricProvider metricProvider) {
        LOGGER.info("set metric supplier function.");
        this.metric = metricProvider.metricSupplier();
    }

    public class SessionContextSupplier implements Supplier<InitializeContext> {

        @Override
        public InitializeContext get() {
            SessionContext context = LOCAL_SESSION_CONTEXT.get();
            ITracing tracing = context.getTracing();
            if (tracing == null || tracing.isNoop()) {
                context.setCurrentTracing(NoNull.of(tracingSupplier.get(this), NoOpTracer.NO_OP_TRACING));
            }
            return context;
        }
    }

    public class MetricRegistrySupplierImpl implements MetricRegistrySupplier {

        @Override
        public MetricRegistry newMetricRegistry(Config config, NameFactory nameFactory, Tags tags) {
            return NoNull.of(metric.newMetricRegistry(config, nameFactory, tags), NoOpMetrics.NO_OP_METRIC);
        }

        @Override
        public Reporter reporter(Config config) {
            return NoNull.of(metric.reporter(config), NoOpReporter.NO_OP_REPORTER);
        }
    }

    public class RootSpanFinish implements Function {

        @Override
        public Object apply(Object o) {
//            LOCAL_SESSION_CONTEXT.get().clear();
            return null;
        }
    }
}
