package com.megaease.easeagent.plugin.elasticsearch;

import com.megaease.easeagent.plugin.AgentPlugin;

public class ElasticsearchRedirectPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return "elasticsearch";
    }

    @Override
    public String getDomain() {
        return "integrability";
    }

}
