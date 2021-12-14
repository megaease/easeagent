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

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigImpl;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.NoOpConfig;
import com.megaease.easeagent.plugin.api.config.AutoRefreshRegistry;

import java.util.function.Supplier;

public class InterceptorPluginDecorator implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorPluginDecorator.class);
    private final Interceptor interceptor;
    private final AgentPlugin plugin;
    private final AutoRefreshConfigImpl config;

    public InterceptorPluginDecorator(Interceptor interceptor, AgentPlugin plugin) {
        this.interceptor = interceptor;
        this.plugin = plugin;
        this.config = AutoRefreshRegistry.getOrCreate(plugin.getDomain(), plugin.getNamespace(), interceptor.getType());
    }

    public Config getConfig() {
        return this.config.getConfig();
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Config cfg = this.config.getConfig();
        InitializeContext innerContext = (InitializeContext) context;
        innerContext.pushConfig(cfg);
        if (cfg == null || cfg.enabled() || cfg instanceof NoOpConfig) {
            innerContext.pushRetBound();
            this.interceptor.before(methodInfo, context);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("plugin.{}.{}.{} is not enabled", config.domain(), config.namespace(), config.id());
        }
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Config cfg = context.getConfig();
        InitializeContext innerContext = (InitializeContext) context;

        if (cfg == null || cfg.enabled() || cfg instanceof NoOpConfig) {
            try {
                this.interceptor.after(methodInfo, context);
            } finally {
                innerContext.popToBound();
                innerContext.popRetBound();
            }
        }
        innerContext.popConfig();
    }

    @Override
    public String getType() {
        return this.interceptor.getType();
    }

    @Override
    public void init(Config config, String type, String method, String methodDescriptor) {
        this.interceptor.init(config, type, method, methodDescriptor);
    }

    @Override
    public void init(Config config, int uniqueIndex) {
        this.interceptor.init(config, uniqueIndex);
    }

    @Override
    public int order() {
        int pluginOrder = this.plugin.order();
        int interceptorOrder = this.interceptor.order();
        return interceptorOrder << 8 + pluginOrder;
    }

    @SuppressWarnings("all")
    public static Supplier<Interceptor> getInterceptorSupplier(final AgentPlugin plugin, final Supplier<Interceptor> supplier) {
        return () -> {
            /*
            Interceptor interceptor = supplier.get();
            Field[] fs = interceptor.getClass().getDeclaredFields();
            for (Field f : fs) {
                // has non-static field
                if ((f.getModifiers() & Modifier.ACC_STATIC) == 0) {
                    interceptor = new StateInterceptor(supplier);
                    break;
                }
            }
             */
            return new InterceptorPluginDecorator(supplier.get(), plugin);
        };
    }
}
