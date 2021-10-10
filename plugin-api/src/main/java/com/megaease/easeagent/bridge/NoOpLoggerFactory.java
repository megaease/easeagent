package com.megaease.easeagent.bridge;

import com.megaease.easeagent.api.logging.ILoggerFactory;
import com.megaease.easeagent.api.logging.Logger;

public class NoOpLoggerFactory implements ILoggerFactory {
    public Logger getLogger(String name) {
        return null;
    }
}
