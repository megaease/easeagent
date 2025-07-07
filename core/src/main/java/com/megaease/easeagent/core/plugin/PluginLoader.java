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

package com.megaease.easeagent.core.plugin;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.core.plugin.matcher.ClassTransformation;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.plugin.registry.PluginRegistry;
import com.megaease.easeagent.core.plugin.transformer.CompoundPluginTransformer;
import com.megaease.easeagent.core.plugin.transformer.DynamicFieldTransformer;
import com.megaease.easeagent.core.plugin.transformer.ForAdviceTransformer;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Ordered;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PluginLoader {

    private PluginLoader() {
    }

    static Logger log = LoggerFactory.getLogger(PluginLoader.class);

    public static AgentBuilder load(AgentBuilder ab, Configs conf) {
        pluginLoad();
        providerLoad();
        Set<ClassTransformation> sortedTransformations = pointsLoad(conf);

        for (ClassTransformation transformation : sortedTransformations) {
            ab = ab.type(transformation.getClassMatcher(), transformation.getClassloaderMatcher())
                .transform(compound(transformation.isHasDynamicField(), transformation.getMethodTransformations()));
        }
        return ab;
    }

    public static void providerLoad() {
        for (InterceptorProvider provider : BaseLoader.load(InterceptorProvider.class)) {
            log.debug("loading provider:{}", provider.getClass().getName());

            try {
                log.debug("provider for:{} at {}",
                    provider.getPluginClassName(), provider.getAdviceTo());
                PluginRegistry.register(provider);
            } catch (Exception | LinkageError e) {
                log.error(
                    "Unable to load provider in [class {}]",
                    provider.getClass().getName(),
                    e);
            }
        }
    }

    public static Set<ClassTransformation> pointsLoad(Configs conf) {
        List<Points> points = BaseLoader.load(Points.class);
        return points.stream().map(point -> {
                try {
                    return PluginRegistry.register(point, conf);
                } catch (Exception e) {
                    log.error(
                        "Unable to load points in [class {}]",
                        point.getClass().getName(),
                        e);
                    return null;
                }
            }).filter(Objects::nonNull)
            .sorted(Comparator.comparing(Ordered::order))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void pluginLoad() {
        for (AgentPlugin plugin : BaseLoader.loadOrdered(AgentPlugin.class)) {
            log.info(
                "Loading plugin {}:{} [class {}]",
                plugin.getDomain(),
                plugin.getNamespace(),
                plugin.getClass().getName());

            try {
                PluginRegistry.register(plugin);
            } catch (Exception | LinkageError e) {
                log.error(
                    "Unable to load extension {}:{} [class {}]",
                    plugin.getDomain(),
                    plugin.getNamespace(),
                    plugin.getClass().getName(),
                    e);
            }
        }
    }

    /**
     * @param methodTransformations method matchers under a special classMatcher
     * @return transform
     */
    public static AgentBuilder.Transformer compound(boolean hasDynamicField,
                                                    Iterable<MethodTransformation> methodTransformations) {
        List<AgentBuilder.Transformer> agentTransformers = StreamSupport
            .stream(methodTransformations.spliterator(), false)
            .map(ForAdviceTransformer::new)
            .collect(Collectors.toList());

        if (hasDynamicField) {
            agentTransformers.add(new DynamicFieldTransformer(AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME));
        }

        return new CompoundPluginTransformer(agentTransformers);
    }
}
