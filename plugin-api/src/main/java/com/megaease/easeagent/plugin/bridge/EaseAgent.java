package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;

/**
 * the bridge api will be initiated when agent startup
 */
public final class EaseAgent {
    public static volatile ILoggerFactory loggerFactory = NoOpLoggerFactory.INSTANCE;
    public static volatile Mdc loggerMdc = NoOpLoggerFactory.NO_OP_MDC_INSTANCE;
    public static volatile IConfigFactory configFactory = new NoOpConfigFactory();

    /*
     * api interface add here
     */
}
