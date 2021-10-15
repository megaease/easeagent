package com.megaease.easeagent.core.log;

import com.megaease.easeagent.plugin.api.logging.Mdc;

public class LoggerMdc implements Mdc {
    private final com.megaease.easeagent.log4j2.impl.Mdc mdc;

    public LoggerMdc(com.megaease.easeagent.log4j2.impl.Mdc mdc) {
        this.mdc = mdc;
    }

    @Override
    public void put(String key, String value) {
        mdc.put(key, value);
    }

    @Override
    public void remove(String key) {
        mdc.remove(key);
    }

    @Override
    public String get(String key) {
        return mdc.get(key);
    }
}
