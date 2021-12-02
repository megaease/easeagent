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

package com.megaease.easeagent.plugin.api.middleware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareConfigProcessor {

    public static final String ENV_REDIS = "EASE_RESOURCE_REDIS";
    public static final String ENV_ES = "EASE_RESOURCE_ELASTICSEARCH";
    public static final String ENV_KAFKA = "EASE_RESOURCE_KAFKA";
    public static final String ENV_RABBITMQ = "EASE_RESOURCE_RABBITMQ";
    public static final String ENV_DATABASE = "EASE_RESOURCE_DATABASE";

    public static final MiddlewareConfigProcessor INSTANCE = new MiddlewareConfigProcessor();

    private final Map<String, ResourceConfig> map = new HashMap<>();

    public void init() {
        this.initConfigItem(ENV_REDIS, true);
        this.initConfigItem(ENV_KAFKA, true);
        this.initConfigItem(ENV_RABBITMQ, true);
        this.initConfigItem(ENV_DATABASE, false);
        this.initConfigItem(ENV_ES, true);
    }

    private void initConfigItem(String envStr, boolean needParse) {
        String str = System.getenv(envStr);
        if (str == null) {
            return;
        }
        ResourceConfig resourceConfig = JsonUtil.toObject(str, new TypeReference<ResourceConfig>() {
        });
        resourceConfig.parseHostAndPorts(needParse);
        if (resourceConfig.hasUrl()) {
            this.add(envStr, resourceConfig);
        }
    }

    public void add(String key, ResourceConfig resourceConfig) {
        map.put(key, resourceConfig);
    }

    public ResourceConfig getData(String key) {
        return map.get(key);
    }

}
