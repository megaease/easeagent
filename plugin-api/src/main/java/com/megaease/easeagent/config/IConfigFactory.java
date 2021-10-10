package com.megaease.easeagent.config;

public interface IConfigFactory {
    /**
     * Returns the agent's all configuration.
     * @return The configuration of this Java agent.
     */
    Config getConfig();

    /**
     * Returns the agent's module configuration.
     * @return The configuration of a special module of Java agent.
     */
    Config getConfig(String module);
}
