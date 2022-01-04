/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.trace.TracingContext;
import com.megaease.easeagent.plugin.bridge.NoOpIPluginConfig;

/**
 * Subtype of {@link Context} and {@link TracingContext} which can push and pop Config.
 */
@SuppressWarnings("unused")
public interface InitializeContext extends Context, TracingContext {

    /**
     * Pushes a Config onto the top of session context config stack.
     *
     * @param config the config to be pushed onto this stack.
     */
    void pushConfig(IPluginConfig config);

    /**
     * Removes the Config at the top of this session context config stack
     * and returns that config as the value of this function.
     *
     * @return The config at the top of this stack (the last config of the <tt>Config</tt> object).
     * return {@link NoOpIPluginConfig#INSTANCE} if this stack is empty.
     */
    IPluginConfig popConfig();

    /**
     * Unlike get/put method transfer cross different interceptors and even cross the whole session,
     * putLocal/getLocal can only transfer data in current interceptor instance.
     * eg. when putLocal is called to put a Span in an interceptor's 'before' method,
     * it can only be accessed in current interceptor by 'getLocal', and can't accessed or modify by other interceptors.
     *
     * @param key   the key whose associated value is to be returned
     * @param value the value to which the specified key is mapped, or
     *              {@code null} if this context contains no mapping for the key
     * @return the value
     */
    <V> V putLocal(String key, V value);

    <V> V getLocal(String key);

    /**
     * Push/pop/peek a object onto the top of session context retStack.
     * usages: push an Span to context when an interceptor's 'before' called,
     * and pop the Span in 'after' procession
     */
    <T> void push(T obj);

    <T> T pop();

    <T> T peek();

    /**
     * called by framework to maintain stack
     */
    void pushRetBound();

    /**
     * called by framework to maintain stack
     */
    void popRetBound();

    /**
     * called by framework to maintain stack
     */
    void popToBound();

    /**
     * clear the context
     */
    void clear();
}
