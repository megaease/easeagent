/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.api.Context;

import java.util.HashMap;
import java.util.Map;

public class MongoCtx {

    private static final String MONGO_CTX_KEY = MongoCtx.class.getName();

    private final Map<String, Object> map = new HashMap<>();

    private MongoCtx() {
    }

    public static MongoCtx create() {
        return new MongoCtx();
    }

    public static MongoCtx getOrCreate(Context context) {
        MongoCtx o = context.get(MONGO_CTX_KEY);
        if (o == null) {
            o = MongoCtx.create();
            o.addToContext(context);
        }
        return o;
    }

    public void addToContext(Context context) {
        context.put(MONGO_CTX_KEY, this);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map.get(key);
    }
}
