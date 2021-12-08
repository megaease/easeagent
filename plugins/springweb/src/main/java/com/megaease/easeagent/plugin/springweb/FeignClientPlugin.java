package com.megaease.easeagent.plugin.springweb;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class FeignClientPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return ConfigConst.Namespace.FEIGN_CLIENT;
    }

    @Override
    public String getDomain() {
        return ConfigConst.OBSERVABILITY;
    }

}
