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
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.plugin.interceptor.ProviderChain;
import com.megaease.easeagent.core.plugin.interceptor.ProviderChain.Builder;
import com.megaease.easeagent.core.plugin.interceptor.ProviderPluginDecorator;
import com.megaease.easeagent.core.plugin.matcher.*;
import com.megaease.easeagent.core.utils.AgentArray;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PluginRegistry {
    static Logger log = EaseAgent.getLogger(PluginRegistry.class);

    static final ConcurrentHashMap<String, AgentPlugin> QUALIFIER_TO_PLUGIN = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, AgentPlugin> POINTS_TO_PLUGIN = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, AgentPlugin> PLUGIN_CLASSNAME_TO_PLUGIN = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Points> POINTS_CLASSNAME_TO_POINTS = new ConcurrentHashMap<>();

    static final ConcurrentHashMap<String, Integer> QUALIFIER_TO_INDEX = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<Integer, MethodTransformation> INDEX_TO_METHOD_TRANSFORMATION = new ConcurrentHashMap<>();
    static final AgentArray<Builder> INTERCEPTOR_PROVIDERS = new AgentArray<>();

    private PluginRegistry() {
    }

    public static void register(AgentPlugin plugin) {
        PLUGIN_CLASSNAME_TO_PLUGIN.putIfAbsent(plugin.getClass().getCanonicalName(), plugin);
    }

    public static void register(Points points) {
        POINTS_CLASSNAME_TO_POINTS.putIfAbsent(points.getClass().getCanonicalName(), points);
    }

    public static Collection<Points> getPoints() {
        return POINTS_CLASSNAME_TO_POINTS.values();
    }

    public static Points getPoints(String pointsClassName) {
        return POINTS_CLASSNAME_TO_POINTS.get(pointsClassName);
    }

    private static String getMethodQualifier(String classname, String qualifier) {
        return classname + ":" + qualifier;
    }

    public static ClassTransformation registerClassTransformation(Points points) {
        String pointsClassName = points.getClass().getCanonicalName();
        IClassMatcher classMatcher = points.getClassMatcher();
        boolean hasDynamicField = points.isAddDynamicField();
        Junction<TypeDescription> innerClassMatcher = ClassMatcherConvert.INSTANCE.convert(classMatcher);
        ElementMatcher<ClassLoader> loaderMatcher = ClassLoaderMatcherConvert.INSTANCE
            .convert(points.getClassLoaderMatcher());

        Set<IMethodMatcher> methodMatchers = points.getMethodMatcher();

        Set<MethodTransformation> mInfo = methodMatchers.stream().map(matcher -> {
            Junction<MethodDescription> bMethodMatcher = MethodMatcherConvert.INSTANCE.convert(matcher);
            String qualifier = getMethodQualifier(pointsClassName, matcher.getQualifier());
            Integer index = QUALIFIER_TO_INDEX.get(qualifier);
            if (index == null) {
                // it is unusual for this is a pointcut without interceptor.
                // maybe there is some error in plugin providers configuration
                return null;
            }
            Builder providerBuilder = INTERCEPTOR_PROVIDERS.get(index);
            if (providerBuilder == null) {
                return null;
            }
            MethodTransformation mt = new MethodTransformation(index, bMethodMatcher, providerBuilder);
            if (INDEX_TO_METHOD_TRANSFORMATION.putIfAbsent(index, mt) != null) {
                log.error("There are duplicate qualifier in Points:{}!", qualifier);
            }
            return mt;
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        AgentPlugin plugin = POINTS_TO_PLUGIN.get(pointsClassName);
        int order = plugin.order();
        return ClassTransformation.builder().classMatcher(innerClassMatcher)
            .hasDynamicField(hasDynamicField)
            .methodTransformations(mInfo)
            .classloaderMatcher(loaderMatcher)
            .order(order).build();
    }

    public static int register(InterceptorProvider provider) {
        String qualifier = provider.getAdviceTo();
        // map interceptor/pointcut to plugin

        AgentPlugin plugin = PLUGIN_CLASSNAME_TO_PLUGIN.get(provider.getPluginClassName());
        if (plugin == null) {
            // code autogenerate issues that are unlikely to occur!
            throw new RuntimeException();
        }
        QUALIFIER_TO_PLUGIN.putIfAbsent(qualifier, plugin);
        POINTS_TO_PLUGIN.putIfAbsent(getPointsClassName(qualifier), plugin);

        // generate index and supplier chain
        Integer index = QUALIFIER_TO_INDEX.get(provider.getAdviceTo());
        if (index == null) {
            synchronized (QUALIFIER_TO_INDEX) {
                index = QUALIFIER_TO_INDEX.get(provider.getAdviceTo());
                if (index == null) {
                    index = INTERCEPTOR_PROVIDERS.add(ProviderChain.builder());
                    QUALIFIER_TO_INDEX.putIfAbsent(provider.getAdviceTo(), index);
                }
            }
        }
        INTERCEPTOR_PROVIDERS.get(index)
            .addProvider(new ProviderPluginDecorator(plugin, provider));

        return index;
    }

    public static String getPointsClassName(String name) {
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

    public static MethodTransformation getMethodTransformation(int pointcutIndex) {
        return INDEX_TO_METHOD_TRANSFORMATION.get(pointcutIndex);
    }

    public static void addMethodTransformation(int pointcutIndex, MethodTransformation info) {
        INDEX_TO_METHOD_TRANSFORMATION.putIfAbsent(pointcutIndex, info);
    }

}
