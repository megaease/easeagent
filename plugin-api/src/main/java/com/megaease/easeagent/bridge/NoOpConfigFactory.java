package com.megaease.easeagent.bridge;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.IConfigFactory;

public class NoOpConfigFactory implements IConfigFactory {
    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public Config getConfig(String module) {
        return null;
    }
}
