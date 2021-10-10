package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;

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
