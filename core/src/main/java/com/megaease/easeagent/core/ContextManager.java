package com.megaease.easeagent.core;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigContext;
import com.megaease.easeagent.core.log.LoggerFactoryImpl;
import com.megaease.easeagent.core.log.LoggerMdc;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.Objects;

public class ContextManager {
    private final Configs conf;
    private final PluginConfigContext pluginConfigContext;
    private final LoggerFactoryImpl loggerFactory;

    public ContextManager(Configs conf, PluginConfigContext pluginConfigContext, LoggerFactoryImpl loggerFactory) {
        this.conf = Objects.requireNonNull(conf, "conf must not be null.");
        this.pluginConfigContext = Objects.requireNonNull(pluginConfigContext, "pluginConfigContext must not be null.");
        this.loggerFactory = Objects.requireNonNull(loggerFactory, "loggerFactory must not be null.");
    }

    public static ContextManager build(Configs conf) {
        PluginConfigContext iConfigFactory = PluginConfigContext.builder(conf).build();
        EaseAgent.configFactory = iConfigFactory;
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        if (loggerFactory != null) {
            EaseAgent.loggerFactory = loggerFactory;
            EaseAgent.loggerMdc = new LoggerMdc(loggerFactory.facotry().mdc());
        } else {
        }
        return new ContextManager(conf, iConfigFactory, loggerFactory);
    }

}
