/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.metrics;

import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class TestConst {
    public static final String SERVICE_KEY_NAME = "service";
    public static final String SERVICE_NAME = "test-metric-service";
    public static final String SERVICE_SYSTEM = "test-metric-system";
    public static final String NAMESPACE = "testMetric";
    public static final String NAMESPACE2 = "testMetric2";
    public static final String INTERVAL_CONFIG = ConfigConst.join(
        ConfigConst.PLUGIN,
        ConfigConst.OBSERVABILITY,
        NAMESPACE,
        ConfigConst.PluginID.METRIC,
        "interval"
    );
}
