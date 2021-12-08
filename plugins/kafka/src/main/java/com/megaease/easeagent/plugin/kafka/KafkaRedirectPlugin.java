package com.megaease.easeagent.plugin.kafka;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class KafkaRedirectPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return ConfigConst.Namespace.KAFKA;
    }

    @Override
    public String getDomain() {
        return ConfigConst.INTEGRABILITY;
    }
}
