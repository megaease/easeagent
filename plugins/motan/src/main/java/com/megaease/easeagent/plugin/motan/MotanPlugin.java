package com.megaease.easeagent.plugin.motan;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class MotanPlugin implements AgentPlugin {

    @Override
    public String getNamespace() {
        return ConfigConst.Namespace.MOTAN;
    }

    @Override
    public String getDomain() {
        return ConfigConst.OBSERVABILITY;
    }
}
