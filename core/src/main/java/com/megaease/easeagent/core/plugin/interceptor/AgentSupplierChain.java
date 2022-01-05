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
import com.megaease.easeagent.plugin.Ordered;
import com.megaease.easeagent.plugin.interceptor.AgentInterceptorChain;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class AgentSupplierChain {
    public final List<Supplier<Interceptor>> suppliers;

    public AgentSupplierChain(List<Supplier<Interceptor>> suppliers) {
        this.suppliers = suppliers;
    }

    public AgentInterceptorChain getInterceptorChain() {
        List<Interceptor> interceptors = suppliers.stream().map(Supplier::get)
            .sorted(Comparator.comparing(Ordered::order))
            .collect(Collectors.toList());

        return new AgentInterceptorChain(interceptors);
    }

    public int size() {
        return this.suppliers.size();
    }
}
