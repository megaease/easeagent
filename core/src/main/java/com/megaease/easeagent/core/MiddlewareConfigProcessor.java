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

package com.megaease.easeagent.core;

import com.megaease.easeagent.core.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class MiddlewareConfigProcessor {

    public static final String ENV_REDIS = "EASE_RESOURCE_REDIS";
    public static final String ENV_KAFKA = "EASE_RESOURCE_KAFKA";
    public static final String ENV_RABBITMQ = "EASE_RESOURCE_RABBITMQ";
    public static final String ENV_DATABASE = "EASE_RESOURCE_DATABASE";

    public static final MiddlewareConfigProcessor INSTANCE = new MiddlewareConfigProcessor();

    private final Map<String, Map<String, Object>> map = new HashMap<>();

    public void init() {
        this.initConfigItem(ENV_REDIS);
        this.initConfigItem(ENV_KAFKA);
        this.initConfigItem(ENV_RABBITMQ);
        this.initConfigItem(ENV_DATABASE);
    }

    private void initConfigItem(String envStr) {
        String str = System.getenv(envStr);
        if (str != null) {
            Map<String, Object> redisConfMap = JsonUtil.toMap(str);
            this.add(envStr, redisConfMap);
        }
    }

    public void add(String key, Map<String, Object> data) {
        map.put(key, data);
    }

    public Map<String, Object> getData(String key) {
        return map.get(key);
    }

}
