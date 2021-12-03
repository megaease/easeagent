package com.megaease.easeagent.plugin.kafka;

import com.megaease.easeagent.plugin.AgentPlugin;

public class KafkaRedirectPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return "kafka";
    }

    @Override
    public String getDomain() {
        return "integrability";
    }
}
