package com.megaease.easeagent.zipkin.logging;

import brave.baggage.CorrelationScopeDecorator;
import brave.internal.CorrelationContext;
import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext;

public class AgentMDCScopeDecorator {
    static final CurrentTraceContext.ScopeDecorator INSTANCE = new AgentMDCScopeDecorator.Builder().build();

    public static CurrentTraceContext.ScopeDecorator get() {
        return INSTANCE;
    }

    public static CorrelationScopeDecorator.Builder newBuilder() {
        return new AgentMDCScopeDecorator.Builder();
    }

    static final class Builder extends CorrelationScopeDecorator.Builder {
        Builder() {
            super(AgentMDCScopeDecorator.MDCContext.INSTANCE);
        }
    }

    enum MDCContext implements CorrelationContext {
        INSTANCE;

        @Override
        public String getValue(String name) {
            ClassLoader classLoader = getUserClassLoader();
            AgentLogMDC agentLogMDC = AgentLogMDC.create(classLoader);
            if (agentLogMDC == null) {
                return null;
            }
            return agentLogMDC.get(name);
        }

        @Override
        public boolean update(String name, @Nullable String value) {
            ClassLoader classLoader = getUserClassLoader();
            AgentLogMDC agentLogMDC = AgentLogMDC.create(classLoader);
            if (agentLogMDC == null) {
                return true;
            }
            if (value != null) {
                agentLogMDC.put(name, value);
            } else {
                agentLogMDC.remove(name);
            }
            return true;
        }

        private ClassLoader getUserClassLoader() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader;
//            if (classLoader==null){
//            }
        }
    }
}
