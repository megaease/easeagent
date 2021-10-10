package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;

public class NoOpConfigFactory implements IConfigFactory {

    @Override
    public String getConfig(String property) {
        return null;
    }

    @Override
    public Config getConfig(String domain, String namespace, String id) {
        return new NoOpConfig(domain, namespace, id);
    }
}
