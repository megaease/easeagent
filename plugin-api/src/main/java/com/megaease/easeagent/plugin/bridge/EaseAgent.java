package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;

/**
 * the bridge api will be initiated when agent startup
 */
public final class EaseAgent {
    public static volatile ILoggerFactory loggerFactory = new NoOpLoggerFactory();
    public static volatile IConfigFactory configFactory = new NoOpConfigFactory();

    /*
     * api interface add here
     */
}
