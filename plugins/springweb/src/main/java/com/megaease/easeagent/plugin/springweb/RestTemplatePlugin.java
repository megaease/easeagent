package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class RestTemplatePlugin implements AgentPlugin {
    @Override
    public String getNamespace() {
        return ConfigConst.Namespace.REST_TEMPLATE;
    }

    @Override
    public String getDomain() {
        return ConfigConst.OBSERVABILITY;
    }

}
