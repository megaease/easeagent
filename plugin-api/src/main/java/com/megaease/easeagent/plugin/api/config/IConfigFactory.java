package com.megaease.easeagent.plugin.api.config;

public interface IConfigFactory {
    /**
     * Returns a configuration property from the agent's all configuration.
     *
     * @return The configuration of this Java agent.
     */
    String getConfig(String property);

    /**
     * Returns the agent's plugin configuration.
     *
     * @return The configuration of a special plugin of Java agent.
     */
    Config getConfig(String domain, String namespace, String name);
}
