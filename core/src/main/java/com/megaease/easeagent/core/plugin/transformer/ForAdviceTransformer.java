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

package com.megaease.easeagent.core.plugin.transformer;

import com.megaease.easeagent.core.plugin.CommonInlineAdvice;
import com.megaease.easeagent.core.plugin.Dispatcher;
import com.megaease.easeagent.core.plugin.annotation.Index;
import com.megaease.easeagent.core.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Ordered;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ForAdviceTransformer  implements AgentBuilder.Transformer {
    private final AgentBuilder.Transformer.ForAdvice transformer;
    private final MethodTransformation methodTransformInfo;

    public ForAdviceTransformer(MethodTransformation methodTransformInfo) {
        this.methodTransformInfo = methodTransformInfo;
        this.transformer = new AgentBuilder.Transformer
            .ForAdvice(Advice.withCustomMapping().bind(Index.class, methodTransformInfo.getIndex()))
            .include(getClass().getClassLoader())
            .advice(methodTransformInfo.getMatcher(), CommonInlineAdvice.class.getCanonicalName());

    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b, TypeDescription td, ClassLoader cl, JavaModule m) {
        // registry and generate interceptor
        SupplierChain<Interceptor> suppliersChain = this.methodTransformInfo.getSuppliersBuilder().build();
        ArrayList<Supplier<Interceptor>> suppliers = suppliersChain.getSuppliers();
        List<Interceptor> interceptors = suppliers.stream().map(Supplier::get)
            .sorted(Comparator.comparing(Ordered::order))
            .collect(Collectors.toList());
        AgentInterceptorChain chain = new AgentInterceptorChain(interceptors);
        Dispatcher.register(this.methodTransformInfo.getIndex(), chain);

        return transformer.transform(b, td, cl, m);
    }
}
