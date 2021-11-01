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

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.StateInterceptor;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.asm.Modifier;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class InterceptorPluginDecorator implements Interceptor {
    private Interceptor interceptor;
    private AgentPlugin plugin;
    private Config config;

    public InterceptorPluginDecorator(Interceptor interceptor, AgentPlugin plugin) {
        this.interceptor = interceptor;
        this.plugin = plugin;
    }

    @Override
    public void before(MethodInfo methodInfo, Object context) {
        this.interceptor.before(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Object context) {
        return this.interceptor.after(methodInfo, context);
    }

    @Override
    public short order() {
        short pluginOrder = this.plugin.order();
        short interceptorOrder = this.interceptor.order();
        short current = (short) (pluginOrder << 8 + interceptorOrder);
        return current;
    }

    public static Supplier<Interceptor> getInterceptorSupplier(final AgentPlugin plugin, final Supplier<Interceptor> supplier) {
        Supplier<Interceptor> decoratorSupplier = new Supplier<Interceptor>() {
            @Override
            public Interceptor get() {
                Interceptor interceptor = supplier.get();
                Field[] fs = interceptor.getClass().getDeclaredFields();
                for (Field f : fs) {
                    // has non-static field
                    if ((f.getModifiers() & Modifier.ACC_STATIC) == 0) {
                        interceptor = new StateInterceptor(supplier);
                    }
                }
                return new InterceptorPluginDecorator(interceptor, plugin);
            }
        };
        return decoratorSupplier;
    }
}
