package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class WebClientPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return ConfigConst.Namespace.WEB_CLIENT;
    }

    @Override
    public String getDomain() {
        return ConfigConst.OBSERVABILITY;
    }

}
