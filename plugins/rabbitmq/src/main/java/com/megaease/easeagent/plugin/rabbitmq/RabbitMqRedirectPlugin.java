package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class RabbitMqRedirectPlugin implements AgentPlugin {
    @Override
    public String getNamespace() {
        return ConfigConst.Namespace.RABBITMQ;
    }

    @Override
    public String getDomain() {
        return ConfigConst.INTEGRABILITY;
    }
}
