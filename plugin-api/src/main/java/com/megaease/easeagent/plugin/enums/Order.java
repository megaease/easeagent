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

package com.megaease.easeagent.plugin.enums;

import com.megaease.easeagent.plugin.api.config.ConfigConst;

/**
 * Priority definition, lower value with higher priority.
 * Higher priority interceptor run enter before lower ones
 * but exit after lower priority interceptors.
 */
public enum Order {
    FOUNDATION(0, "foundation"),
    HIGHEST(10, "highest"),
    REDIRECT(19, ConfigConst.PluginID.REDIRECT),
    HIGH(20, "high"),
    FORWARDED(30, ConfigConst.PluginID.FORWARDED),
    TRACING_INIT(90, ConfigConst.PluginID.TRACING_INIT),
    /**
     * if there is not tracing started, start a new tracing
     * if there is tracing started, append span to current tracing
     */
    TRACING(100, ConfigConst.PluginID.TRACING),
    /**
     * if there is not tracing started, don't generate root span
     * if there is tracing started, append span to current tracing
     */
    TRACING_APPEND(101, ConfigConst.PluginID.TRACING),
    METRIC(200, ConfigConst.PluginID.METRIC),
    LOG(201, ConfigConst.PluginID.LOG),
    LOW(210, "low"),
    LOWEST(255, "lowest");

    private final int order;
    private final String name;

    Order(int s, String name) {
        this.order = s;
        this.name = name;
    }

    public int getOrder() {
        return this.order;
    }

    public String getName() {
        return this.name;
    }
}
