package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;
import com.megaease.easeagent.plugin.api.metric.MetricSupplier;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * the bridge api will be initiated when agent startup
 */
public final class EaseAgent {
    public static volatile MetricSupplier metricSupplier = NoOpMetrics.NO_OP_METRIC_SUPPLIER;
    public static volatile Supplier<Context> contextSupplier = () -> NoOpContext.NO_OP_CONTEXT;
    public static volatile ILoggerFactory loggerFactory = NoOpLoggerFactory.INSTANCE;
    public static volatile Mdc loggerMdc = NoOpLoggerFactory.NO_OP_MDC_INSTANCE;
    public static volatile IConfigFactory configFactory = new NoOpConfigFactory();

    /*
     * api interface add here
     */
}
