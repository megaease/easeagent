package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Logger;

public class NoOpLoggerFactory implements ILoggerFactory {
    public Logger getLogger(String name) {
        return null;
    }
}
