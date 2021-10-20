package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigContext;
import com.megaease.easeagent.core.log.LoggerFactoryImpl;
import com.megaease.easeagent.core.log.LoggerMdc;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.bridge.NoOpLoggerFactory;

import java.util.Objects;

public class ContextManager {
    private final Configs conf;
    private final PluginConfigContext pluginConfigContext;
    private final ILoggerFactory loggerFactory;

    public ContextManager(Configs conf, PluginConfigContext pluginConfigContext, ILoggerFactory loggerFactory) {
        this.conf = Objects.requireNonNull(conf, "conf must not be null.");
        this.pluginConfigContext = Objects.requireNonNull(pluginConfigContext, "pluginConfigContext must not be null.");
        if (loggerFactory == null) {
            loggerFactory = new NoOpLoggerFactory();
        }
        this.loggerFactory = Objects.requireNonNull(loggerFactory, "loggerFactory must not be null.");
    }

    public static ContextManager build(Configs conf) {
        PluginConfigContext iConfigFactory = PluginConfigContext.builder(conf).build();
        EaseAgent.configFactory = iConfigFactory;
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        ILoggerFactory iLoggerFactory = NoOpLoggerFactory.INSTANCE;
        if (loggerFactory != null) {
            EaseAgent.loggerFactory = loggerFactory;
            EaseAgent.loggerMdc = new LoggerMdc(loggerFactory.facotry().mdc());
            iLoggerFactory = loggerFactory;
        }
        return new ContextManager(conf, iConfigFactory, iLoggerFactory);
    }
}
