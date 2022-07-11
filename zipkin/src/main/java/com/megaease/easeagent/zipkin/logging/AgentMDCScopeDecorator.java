/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.megaease.easeagent.zipkin.logging;

import brave.baggage.CorrelationScopeDecorator;
import brave.internal.CorrelationContext;
import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

public class AgentMDCScopeDecorator {
    static final CurrentTraceContext.ScopeDecorator INSTANCE = new BuilderApp().build();
    static final CurrentTraceContext.ScopeDecorator INSTANCE_V2 = new BuilderEaseLogger().build();
    static final CurrentTraceContext.ScopeDecorator INSTANCE_EASEAGENT_LOADER = new BuilderAgentLoader().build();

    public static CurrentTraceContext.ScopeDecorator get() {
        return INSTANCE;
    }

    public static CurrentTraceContext.ScopeDecorator getV2() {
        return INSTANCE_V2;
    }

    public static CurrentTraceContext.ScopeDecorator getAgentDecorator() {
        return INSTANCE_EASEAGENT_LOADER;
    }

    static final class BuilderApp extends CorrelationScopeDecorator.Builder {
        BuilderApp() {
            super(MDCContextApp.INSTANCE);
        }
    }

    static final class BuilderEaseLogger extends CorrelationScopeDecorator.Builder {
        BuilderEaseLogger() {
            super(MDCContextEaseLogger.INSTANCE);
        }
    }

    static final class BuilderAgentLoader extends CorrelationScopeDecorator.Builder {
        BuilderAgentLoader() {
            super(MDCContextAgentLoader.INSTANCE);
        }
    }

    enum MDCContextAgentLoader implements CorrelationContext {
        INSTANCE;

        @Override
        public String getValue(String name) {
            return org.slf4j.MDC.get(name);
        }

        @Override
        public boolean update(String name, @Nullable String value) {
            if (value != null) {
                org.slf4j.MDC.put(name, value);
            } else {
                org.slf4j.MDC.remove(name);
            }
            return true;
        }
    }

    enum MDCContextApp implements CorrelationContext {
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
            return Thread.currentThread().getContextClassLoader();
        }
    }

    enum MDCContextEaseLogger implements CorrelationContext {
        INSTANCE;

        @Override
        public String getValue(String name) {
            return EaseAgent.loggerMdc.get(name);
        }

        @Override
        public boolean update(String name, @Nullable String value) {
            if (value != null) {
                EaseAgent.loggerMdc.put(name, value);
            } else {
                EaseAgent.loggerMdc.remove(name);
            }
            return true;
        }
    }
}
