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

package com.megaease.easeagent.core.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultAgentInterceptorChain implements AgentInterceptorChain {

    private final List<AgentInterceptor> agentInterceptors;

    private int pos = 0;

    public DefaultAgentInterceptorChain(List<AgentInterceptor> agentInterceptors) {
        this.agentInterceptors = agentInterceptors;
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Map<Object, Object> context) {
        if (pos == this.agentInterceptors.size()) {
            return;
        }
        AgentInterceptor interceptor = this.agentInterceptors.get(pos++);
        interceptor.before(methodInfo, context, this);
    }

    @Override
    public Object doAfter(MethodInfo methodInfo, Map<Object, Object> context) {
        pos--;
        if (pos < 0) {
            return methodInfo.getRetValue();
        }
        AgentInterceptor interceptor = this.agentInterceptors.get(pos);
        return interceptor.after(methodInfo, context, this);
    }

    @Override
    public void skipBegin() {
        this.pos = this.agentInterceptors.size();
    }

    public static class Builder implements AgentInterceptorChain.Builder {

        private final List<AgentInterceptor> list = new ArrayList<>();

        @Override
        public AgentInterceptorChain.Builder addInterceptor(AgentInterceptor agentInterceptor) {
            list.add(agentInterceptor);
            return this;
        }

        @Override
        public DefaultAgentInterceptorChain build() {
            return new DefaultAgentInterceptorChain(this.list);
        }
    }

}
