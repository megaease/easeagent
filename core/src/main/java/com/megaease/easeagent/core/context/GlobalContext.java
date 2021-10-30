package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;

import javax.annotation.Nonnull;

public class GlobalContext {
    private final Configs conf;
    private final MetricRegistrySupplier metric;
    private final ILoggerFactory loggerFactory;
    private final Mdc mdc;

    public GlobalContext(@Nonnull Configs conf, @Nonnull MetricRegistrySupplier metric, @Nonnull ILoggerFactory loggerFactory, @Nonnull Mdc mdc) {
        this.conf = conf;
        this.metric = metric;
        this.loggerFactory = loggerFactory;
        this.mdc = mdc;
    }


    public Configs getConf() {
        return conf;
    }

    public Mdc getMdc() {
        return mdc;
    }


    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public MetricRegistrySupplier getMetric() {
        return metric;
    }
}
