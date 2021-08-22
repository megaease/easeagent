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

import com.fasterxml.jackson.core.type.TypeReference;
import com.megaease.easeagent.core.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiddlewareConfigProcessor {

    public static final String ENV_REDIS = "EASE_RESOURCE_REDIS";
    public static final String ENV_KAFKA = "EASE_RESOURCE_KAFKA";
    public static final String ENV_RABBITMQ = "EASE_RESOURCE_RABBITMQ";
    public static final String ENV_DATABASE = "EASE_RESOURCE_DATABASE";
    public static final String EASE_RESOURCE_URL = "EASE_RESOURCE_URL";

    public static final MiddlewareConfigProcessor INSTANCE = new MiddlewareConfigProcessor();

    private final Map<String, List<Map<String, Object>>> map = new HashMap<>();

    public void init() {
        this.initConfigItem(ENV_REDIS);
        this.initConfigItem(ENV_KAFKA);
        this.initConfigItem(ENV_RABBITMQ);
        this.initConfigItem(ENV_DATABASE);
    }

    private void initConfigItem(String envStr) {
        String str = System.getenv(envStr);
        if (str != null) {
            List<String> list = JsonUtil.toObject(str, new TypeReference<List<String>>() {
            });

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (String s : list) {
                int begin = s.indexOf(":");
                int end = s.lastIndexOf(":");
                Map<String, Object> dataMap = new HashMap<>();
                if (begin == end) {
                    String[] arr = s.split(":");
                    dataMap.put("host", arr[0]);
                    dataMap.put("port", Integer.parseInt(arr[1]));
                    mapList.add(dataMap);
                } else {
                    //process url
                    dataMap.put(EASE_RESOURCE_URL, s);
                }
            }
            this.add(envStr, mapList);
        }
    }

    public void add(String key, List<Map<String, Object>> list) {
        map.put(key, list);
    }

    public Map<String, Object> getFirstData(String key) {
        List<Map<String, Object>> data = getData(key);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return data.get(0);
    }

    public List<Map<String, Object>> getData(String key) {
        return map.get(key);
    }

}
