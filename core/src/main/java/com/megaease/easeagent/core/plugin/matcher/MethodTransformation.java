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

package com.megaease.easeagent.core.plugin.matcher;

import com.megaease.easeagent.core.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.interceptor.AgentSupplierChain;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Ordered;
import lombok.Data;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
@SuppressWarnings("unused")
public class MethodTransformation {
    private final static Logger log = LoggerFactory.getLogger(MethodTransformation.class);

    private int index;
    private Junction<? super MethodDescription> matcher;
    private SupplierChain.Builder<Interceptor> suppliersBuilder;

    public MethodTransformation(int index,
                                Junction<? super MethodDescription> matcher,
                                SupplierChain.Builder<Interceptor> chain) {
        this.index = index;
        this.matcher = matcher;
        this.suppliersBuilder = chain;
    }

    public AgentSupplierChain getSupplierChain(ClassLoader classLoader) {
        SupplierChain<Interceptor> suppliersChain = getSuppliersBuilder().build();
        ArrayList<Supplier<Interceptor>> suppliers = suppliersChain.getSuppliers();

        return new AgentSupplierChain(suppliers);
    }

    public AgentInterceptorChain getAgentInterceptorChain(ClassLoader classLoader) {
        SupplierChain<Interceptor> suppliersChain = getSuppliersBuilder().build();
        ArrayList<Supplier<Interceptor>> suppliers = suppliersChain.getSuppliers();

        List<Interceptor> interceptors = suppliers.stream().map(supplier -> {
                return supplier.get();
            })
            .sorted(Comparator.comparing(Ordered::order))
            .collect(Collectors.toList());

        return new AgentInterceptorChain(interceptors);
    }
}
