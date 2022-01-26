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

import com.megaease.easeagent.core.plugin.interceptor.InterceptorPluginDecorator;
import com.megaease.easeagent.core.plugin.interceptor.ProviderChain;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.Ordered;
import com.megaease.easeagent.plugin.interceptor.AgentInterceptorChain;
import lombok.Data;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
@SuppressWarnings("unused")
public class MethodTransformation {
    private static final Logger log = LoggerFactory.getLogger(MethodTransformation.class);

    private int index;
    private Junction<? super MethodDescription> matcher;
    private ProviderChain.Builder providerBuilder;

    public MethodTransformation(int index,
                                Junction<? super MethodDescription> matcher,
                                ProviderChain.Builder chain) {
        this.index = index;
        this.matcher = matcher;
        this.providerBuilder = chain;
    }

    public AgentInterceptorChain getAgentInterceptorChain(final int uniqueIndex,
                                                          final String type,
                                                          final String method,
                                                          final String methodDescription) {
        List<Supplier<Interceptor>> suppliers = this.providerBuilder.build()
            .getSupplierChain();

        List<Interceptor> interceptors = suppliers.stream()
            .map(Supplier::get)
            .sorted(Comparator.comparing(Ordered::order).reversed())
            .collect(Collectors.toList());

        interceptors.forEach(i -> {
            InterceptorPluginDecorator interceptor;
            if (i instanceof InterceptorPluginDecorator) {
                interceptor = (InterceptorPluginDecorator) i;
                try {
                    interceptor.init(interceptor.getConfig(), type, method, methodDescription);
                    interceptor.init(interceptor.getConfig(), uniqueIndex);
                } catch (Exception e) {
                    log.error("Interceptor init fail: {}::{}, {}", type, method, interceptor.getClass().getSimpleName());
                }
            }
        });

        return new AgentInterceptorChain(interceptors);
    }
}
