/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.zipkin.logging;

import brave.baggage.CorrelationScopeDecorator;
import brave.internal.CorrelationContext;
import brave.internal.Nullable;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

public class AgentMDCScopeDecorator {
    static final CurrentTraceContext.ScopeDecorator INSTANCE = new AgentMDCScopeDecorator.Builder().build();
    static final CurrentTraceContext.ScopeDecorator INSTANCE_V2 = new AgentMDCScopeDecorator.BuilderV2().build();

    public static CurrentTraceContext.ScopeDecorator get() {
        return INSTANCE;
    }

    public static CurrentTraceContext.ScopeDecorator getV2() {
        return INSTANCE_V2;
    }

    public static CorrelationScopeDecorator.Builder newBuilder() {
        return new AgentMDCScopeDecorator.Builder();
    }

    static final class Builder extends CorrelationScopeDecorator.Builder {
        Builder() {
            super(AgentMDCScopeDecorator.MDCContext.INSTANCE);
        }
    }

    static final class BuilderV2 extends CorrelationScopeDecorator.Builder {
        BuilderV2() {
            super(AgentMDCScopeDecorator.MDCContextV2.INSTANCE);
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

    enum MDCContextV2 implements CorrelationContext {
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
