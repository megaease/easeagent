package com.megaease.easeagent.plugin.elasticsearch;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class ElasticsearchRedirectPlugin implements AgentPlugin {
    @Override
    public String getNamespace() {
        return ConfigConst.Namespace.ELASTICSEARCH;
    }

    @Override
    public String getDomain() {
        return ConfigConst.INTEGRABILITY;
    }

}
