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

package com.megaease.easeagent.core.plugin.registry;

import com.google.common.base.Strings;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain.Builder;
import com.megaease.easeagent.core.plugin.matcher.ClassMatcherConvert;
import com.megaease.easeagent.core.plugin.matcher.ClassTransformation;
import com.megaease.easeagent.core.plugin.matcher.MethodMatcherConvert;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.utils.AgentArray;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QualifierRegistry {
    static ConcurrentHashMap<String, AgentPlugin> qualifierToPlugin = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, AgentPlugin> pointsToPlugin = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, AgentPlugin> pluginClassnameToPlugin = new ConcurrentHashMap<>();

    static final ConcurrentHashMap<String, Integer> qualifierToIndex = new ConcurrentHashMap<>();
    static final AgentArray<Builder<Interceptor>> interceptorSuppliers = new AgentArray<>();

    public static void register(AgentPlugin plugin) {
        pluginClassnameToPlugin.putIfAbsent(plugin.getClass().getCanonicalName(), plugin);
    }

    private static String getMethodQualifier(String classname, String qualifier) {
        return classname + ":" + qualifier;
    }

    public static ClassTransformation register(Points points) {
        String pointsClassName = points.getClass().getCanonicalName();
        IClassMatcher classMatcher = points.getClassMatcher();
        boolean hasDynamicField = points.isAddDynamicField();
        Junction<TypeDescription> innerClassMatcher = ClassMatcherConvert.INSTANCE.convert(classMatcher);

        Set<IMethodMatcher> methodMatchers = points.getMethodMatcher();

        Set<MethodTransformation> mInfo = methodMatchers.stream().map(matcher -> {
            Junction<MethodDescription> bMethodMatcher = MethodMatcherConvert.INSTANCE.convert(matcher);
            String qualifier = getMethodQualifier(pointsClassName, matcher.getQualifier());
            int index = qualifierToIndex.get(qualifier);
            Builder<Interceptor> chainBuilder = interceptorSuppliers.get(index);
            return new MethodTransformation(index, bMethodMatcher, chainBuilder);
        }).collect(Collectors.toSet());
        AgentPlugin plugin = pointsToPlugin.get(pointsClassName);
        int order = plugin.order();

        return ClassTransformation.builder().classMatcher(innerClassMatcher)
            .hasDynamicField(hasDynamicField)
            .methodTransformations(mInfo)
            .order(order).build();
    }

    public static int register(Provider provider) {
        String qualifier = provider.getAdviceTo();
        // map interceptor/pointcut to plugin

        AgentPlugin plugin = pluginClassnameToPlugin.get(provider.getPluginClassName());
        if (plugin == null) {
            // code autogenerate issues that are unlikely to occur!
            throw new RuntimeException();
        }

        qualifierToPlugin.putIfAbsent(qualifier, plugin);
        pointsToPlugin.putIfAbsent(getPointsClassName(qualifier), plugin);

        // generate index and supplier chain
        Integer index = qualifierToIndex.get(provider.getAdviceTo());
        if (index != null) {
            interceptorSuppliers.get(index).addSupplier(provider.getInterceptorProvider());
            return index;
        } else {
            synchronized (qualifierToIndex) {
                index = qualifierToIndex.get(provider.getAdviceTo());
                if (index == null) {
                    index = interceptorSuppliers.add(SupplierChain.builder());
                    qualifierToIndex.putIfAbsent(provider.getAdviceTo(), index);
                    interceptorSuppliers.get(index).addSupplier(provider.getInterceptorProvider());
                }
            }
        }

        return index;
    }

    static String getPointsClassName(String name) {
        int index;
        if (Strings.isNullOrEmpty(name)) {
            return "unknown";
        }
        index = name.indexOf(':');
        if (index < 0) {
            return name;
        }
        return name.substring(0, index);
    }

    public int getInstrumentIndex() {
        return 100;
    }
}
