package com.megaease.easeagent.logback;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class LogbackPlugin implements AgentPlugin {
    @Override
    public String getNamespace() {
        return "logback";
    }

    @Override
    public String getDomain() {
        return ConfigConst.OBSERVABILITY;
    }
}
