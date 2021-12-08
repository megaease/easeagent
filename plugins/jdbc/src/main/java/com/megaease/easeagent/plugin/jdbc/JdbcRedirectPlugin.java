package com.megaease.easeagent.plugin.jdbc;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.enums.Order;

public class JdbcRedirectPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return ConfigConst.Namespace.JDBC;
    }

    @Override
    public String getDomain() {
        return ConfigConst.INTEGRABILITY;
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
