package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.annotation.Plugin;
import com.megaease.easeagent.plugin.api.config.Config;

@Plugin
public class SpringWebFilterPlugin implements AgentPlugin {
    private Config config;
    public boolean enable = true;

    @Override
    public void load(Config config) {
        this.config = config;
        this.enable = config.getBoolean("enable", false);
    }

    @Override
    public void unload(Config config) {
        this.config = config;
        this.enable = config.getBoolean("enable", false);
    }

    @Override
    public String getName() {
        return "springwebfilter";
    }

    @Override
    public String getDomain() {
        return "observability";
    }

    @Override
    public void onChange(Config oldConfig, Config newConfig) {

    }
}
