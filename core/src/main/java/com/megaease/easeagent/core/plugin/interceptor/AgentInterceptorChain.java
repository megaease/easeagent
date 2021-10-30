/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.plugin.interceptor;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;

import java.util.ArrayList;
import java.util.List;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class AgentInterceptorChain {
    public final ArrayList<Interceptor> interceptors;

    public AgentInterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
    }

    public AgentInterceptorChain(ArrayList<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void doBefore(MethodInfo methodInfo, int pos, Object context) {
        if (pos == this.interceptors.size()) {
            return;
        }
        Interceptor interceptor = interceptors.get(pos);
        try {
            interceptor.before(methodInfo, context);
        } catch (Throwable e) {
            // set error message to context;
        }
        this.doBefore(methodInfo, pos + 1, context);
    }

    public Object doAfter(MethodInfo methodInfo, int pos, Object context) {
        if (pos < 0) {
            return methodInfo.getRetValue();
        }
        Interceptor interceptor = interceptors.get(pos);
        try {
            interceptor.after(methodInfo, context);
        } catch (Throwable e) {
            // set error message to context;
        }
        return this.doAfter(methodInfo, pos - 1, context);
    }

    public int size() {
        return this.interceptors.size();
    }
}
