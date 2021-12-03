package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.plugin.AgentPlugin;

public class RabbitMqRedirectPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return "rabbitmq";
    }

    @Override
    public String getDomain() {
        return "integrability";
    }
}
