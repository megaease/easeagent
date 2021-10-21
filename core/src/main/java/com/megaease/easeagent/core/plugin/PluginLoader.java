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

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class PluginLoader {
    static Logger log = LoggerFactory.getLogger(PluginLoader.class);
    static ConcurrentHashMap<String, AgentPlugin> pluginMap = new ConcurrentHashMap<>();

    public static void load() {
        pluginLoad();
        providerLoad();
        pointsLoad();
    }

    public static void providerLoad() {
        for (Provider provider : BaseLoader.load(Provider.class)) {
            log.info("loading provider:{}", provider.getClass().getName());

            try {
                provider.getInterceptor();
                log.info("provider for:{} at {}",
                    provider.getPluginName(), provider.getAdviceTo());
            } catch (Exception | LinkageError e) {
                log.error(
                    "Unable to load provider in [class {}]",
                    provider.getClass().getName(),
                    e);
            }
        }
    }

    public static void pointsLoad() {
        for (Points points : BaseLoader.load(Points.class)) {
            log.info("loading pointcut:{}", points.getClass().getName());

            try {
                // xxx: what happens when there are two classes with a same name?
            } catch (Exception | LinkageError e) {
                log.error(
                    "Unable to load points in [class {}]",
                    points.getClass().getName(),
                    e);
            }
        }
    }

    public static void pluginLoad() {
        for (AgentPlugin plugin : BaseLoader.loadOrdered(AgentPlugin.class)) {
            log.info(
                "Loading extension {}:{} [class {}]",
                plugin.getDomain(),
                plugin.getName(),
                plugin.getClass().getName());

            try {
                pluginMap.putIfAbsent(plugin.getClass().getCanonicalName(), plugin);
            } catch (Exception | LinkageError e) {
                log.error(
                    "Unable to load extension {}:{} [class {}]",
                    plugin.getDomain(),
                    plugin.getName(),
                    plugin.getClass().getName(),
                    e);
            }
        }
    }

    public static AgentPlugin getPlugin(String name) {
        return pluginMap.get(name);
    }

    public static AgentPlugin getPlugin(Class<?> clazz) {
        return pluginMap.get(clazz.getCanonicalName());
    }
}
