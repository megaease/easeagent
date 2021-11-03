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
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Response;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

public interface Context {
    boolean isNoop();

    Tracing currentTracing();

    <V> V get(Object key);

    <V> V remove(Object key);

    <V> V put(Object key, V value);

    Config getConfig();

    int enter(Object key);

    default boolean enter(Object key, int times) {
        return enter(key) == times;
    }

    int out(Object key);

    default boolean out(Object key, int times) {
        return out(key) == times;
    }

    AsyncContext exportAsync(Request request);

    Span importAsync(AsyncContext snapshot);

    ProgressContext nextProgress(Request request);

    ProgressContext importProgress(Request request);

    Map<Object, Object> clear();
}
