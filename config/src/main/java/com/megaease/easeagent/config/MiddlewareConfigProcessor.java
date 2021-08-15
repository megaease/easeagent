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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiddlewareConfigProcessor {

    public static final MiddlewareConfigProcessor INSTANCE = new MiddlewareConfigProcessor();

    private final Map<String, Object> map = new HashMap<>();

    public void add(String key, Object data) {
        map.put(key, data);
    }

    public void addAll(Map<String, Object> dataMap) {
        map.putAll(dataMap);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) map.get(key);
    }

}
