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

package com.megaease.easeagent.common.config;

import com.megaease.easeagent.config.Config;

public class SwitchUtil {

    public static final String GLOBAL_METRICS_ENABLE_KEY = "observability.metrics.enabled";
    public static final String GLOBAL_TRACING_ENABLE_KEY = "observability.tracings.enabled";

    public static boolean enableMetric(Config config, String key) {
        Boolean globalEnabled = config.getBoolean(GLOBAL_METRICS_ENABLE_KEY);
        if (!globalEnabled) {
            return false;
        }
        return config.getBoolean(key);
    }

    public static boolean enableTracing(Config config, String key) {
        Boolean globalEnabled = config.getBoolean(GLOBAL_TRACING_ENABLE_KEY);
        if (!globalEnabled) {
            return false;
        }
        return config.getBoolean(key);
    }
}
