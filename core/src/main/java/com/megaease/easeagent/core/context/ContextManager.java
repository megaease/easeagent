package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigManager;
import com.megaease.easeagent.core.log.LoggerFactoryImpl;
import com.megaease.easeagent.core.log.LoggerMdc;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class ContextManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class.getName());
    private static final ThreadLocal<SessionContext> LOCAL_SESSION_CONTEXT = ThreadLocal.withInitial(() -> new SessionContext());
    private final PluginConfigManager pluginConfigManager;
    private final Function rootSpanFinish;
    private final Supplier<InitializeContext> sessionSupplier;
    //    private final Supplier<InitializeContext> initializeContextSupplier;
    private final GlobalContext globalContext;
    private volatile Function<Supplier<InitializeContext>, Tracing> tracing = (supplier) -> null;
    private volatile MetricRegistrySupplier metric = NoOpMetrics.NO_OP_METRIC_SUPPLIER;


    private ContextManager(@Nonnull Configs conf, @Nonnull PluginConfigManager pluginConfigManager, @Nonnull ILoggerFactory loggerFactory, @Nonnull Mdc mdc) {
        this.pluginConfigManager = pluginConfigManager;
        this.rootSpanFinish = new RootSpanFinish();
        this.sessionSupplier = new SessionContextSupplier();
//        this.initializeContextSupplier = new InitializeContextSupplier();
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

    public void setTracing(@Nonnull Function<Supplier<InitializeContext>, Tracing> tracing) {
        LOGGER.info("set tracing supplier function.");
        this.tracing = tracing;
    }

    public void setMetric(@Nonnull MetricRegistrySupplier metric) {
        LOGGER.info("set metric supplier function.");
        this.metric = metric;
    }

    public Function getRootSpanFinish() {
        return rootSpanFinish;
    }

    public class SessionContextSupplier implements Supplier<InitializeContext> {

        @Override
        public InitializeContext get() {
            SessionContext context = LOCAL_SESSION_CONTEXT.get();
            Tracing tracing = ContextManager.this.tracing.apply(this);
            context.setCurrentTracing(NoNull.of(tracing, NoOpTracer.NO_OP_TRACING));
            return context;
        }
    }

//    public class InitializeContextSupplier implements Supplier<InitializeContext> {
//
//        @Override
//        public InitializeContext get() {
//            SessionContext context = LOCAL_SESSION_CONTEXT.get();
//            Tracing tracing = ContextManager.this.tracing.apply((Supplier) this);
//            context.setCurrentTracing(NoNull.of(tracing, NoOpTracer.NO_OP_TRACING));
//            return context;
//        }
//    }

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
