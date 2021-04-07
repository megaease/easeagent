package com.megaease.easeagent.core.interceptor;

public class ChainBuilderFactory {

    public static final ChainBuilderFactory DEFAULT = new ChainBuilderFactory();

    public AgentInterceptorChain.Builder createBuilder() {
        return new DefaultAgentInterceptorChain.Builder();
    }
}
