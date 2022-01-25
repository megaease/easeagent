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

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.Config;

import java.util.function.BiFunction;

public class AutoRefreshConfigItem<T> {
    private volatile T value;

    public AutoRefreshConfigItem(Config config, String name, BiFunction<Config, String, T> func) {
        ConfigUtils.bindProp(name, config, func, v -> this.value = v);
    }

    public T getValue() {
        return value;
    }
}
