package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigContext;
import com.megaease.easeagent.core.log.LoggerFactoryImpl;
import com.megaease.easeagent.core.log.LoggerMdc;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.bridge.NoOpLoggerFactory;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.utils.NoNull;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ContextManager {
    private static final ThreadLocal<SessionContext> LOCAL_SESSION_CONTEXT = ThreadLocal.withInitial(() -> new SessionContext());
    private final Configs conf;
    private final PluginConfigContext pluginConfigContext;
    private final ILoggerFactory loggerFactory;
    private volatile Supplier<Tracing> tracing = () -> null;
    private volatile MetricRegistrySupplier metric = NoOpMetrics.NO_OP_METRIC_SUPPLIER;
    private final Function rootSpanFinish;

    public ContextManager(Configs conf, PluginConfigContext pluginConfigContext, ILoggerFactory loggerFactory) {
        this.conf = Objects.requireNonNull(conf, "conf must not be null.");
        this.pluginConfigContext = Objects.requireNonNull(pluginConfigContext, "pluginConfigContext must not be null.");
        if (loggerFactory == null) {
            loggerFactory = new NoOpLoggerFactory();
        }
        this.loggerFactory = Objects.requireNonNull(loggerFactory, "loggerFactory must not be null.");
        this.rootSpanFinish = new RootSpanFinish();
    }

    public static ContextManager build(Configs conf) {
        TransparentTransmission.init(conf);
        PluginConfigContext iConfigFactory = PluginConfigContext.builder(conf).build();
        EaseAgent.configFactory = iConfigFactory;
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        ILoggerFactory iLoggerFactory = NoOpLoggerFactory.INSTANCE;
        if (loggerFactory != null) {
            EaseAgent.loggerFactory = loggerFactory;
            EaseAgent.loggerMdc = new LoggerMdc(loggerFactory.facotry().mdc());
            iLoggerFactory = loggerFactory;
        }
        ContextManager contextManager = new ContextManager(conf, iConfigFactory, iLoggerFactory);
        EaseAgent.contextSupplier = contextManager.new SessionContextSupplier();
        EaseAgent.metricRegistrySupplier = contextManager.new MetricRegistrySupplierImpl();
        return contextManager;
    }

    public void setTracing(@Nonnull Supplier<Tracing> tracing) {
        this.tracing = tracing;
    }

    public void setMetric(@Nonnull MetricRegistrySupplier metric) {
        this.metric = metric;
    }

    public Function getRootSpanFinish() {
        return rootSpanFinish;
    }

    public class SessionContextSupplier implements Supplier<Context> {

        @Override
        public Context get() {
            SessionContext context = LOCAL_SESSION_CONTEXT.get();
            context.setCurrentTracing(NoNull.of(ContextManager.this.tracing.get(), NoOpTracer.NO_OP_TRACING));
            return context;
        }
    }

    public class MetricRegistrySupplierImpl implements MetricRegistrySupplier {

        @Override
        public MetricRegistry newMetricRegistry(Config config, NameFactory nameFactory, Tags tags) {
            return NoNull.of(metric.newMetricRegistry(config, nameFactory, tags), NoOpMetrics.NO_OP_METRIC);
        }
    }

    public class RootSpanFinish implements Function {

        @Override
        public Object apply(Object o) {
            LOCAL_SESSION_CONTEXT.get().clear();
            return null;
        }
    }
}
