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

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.TracingContext;

/**
 * Subtype of {@link Context} and {@link TracingContext} which can push and pop Config.
 */
public interface InitializeContext extends Context, TracingContext {

    /**
     * Pushes a Config onto the top of session context config stack.
     *
     * @param config the config to be pushed onto this stack.
     */
    void pushConfig(Config config);

    /**
     * Removes the Config at the top of this session context config stack
     * and returns that config as the value of this function.
     *
     * @return The config at the top of this stack (the last config of the <tt>Config</tt> object).
     * return {@link com.megaease.easeagent.plugin.bridge.NoOpConfig#INSTANCE} if this stack is empty.
     */
    Config popConfig();

    /**
     * Increments by one the current sequence value.
     *
     * @return the updated value
     */
    long inc();

    /**
     * Decrements by one the current sequence value.
     *
     * @return the updated value
     */
    long dec();
}
