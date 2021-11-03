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

package com.megaease.easeagent.plugin.api.metric.name;

import java.util.HashMap;
import java.util.Map;

public class Tags {
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";
    private final String category;
    private final String type;
    private final String keyFieldName;
    private final Map<String, String> tags;

    public Tags(String category, String type, String keyFieldName) {
        this.category = category;
        this.type = type;
        this.keyFieldName = keyFieldName;
        this.tags = new HashMap<>();
    }

    public Tags put(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
