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

package com.megaease.easeagent.mock.plugin.api.utils;

import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * a tag verifier for metric filter
 *
 * @see com.megaease.easeagent.mock.plugin.api.MockEaseAgent#lastMetricJsonReporter(Predicate)
 */
public class TagVerifier {
    List<Pair<String, String>> tags = new ArrayList<>();

    public TagVerifier add(String name, String value) {
        tags.add(new Pair<>(name, value));
        return this;
    }

    public boolean verifyAnd(Map<String, Object> map) {
        for (Pair<String, String> tag : tags) {
            if (!contains(map, tag.getKey(), tag.getValue())) {
                return false;
            }
        }
        return true;
    }


    private boolean contains(Map<String, Object> map, String key, String value) {
        Object v = map.get(key);
        if (!(v instanceof String)) {
            return false;
        }
        return v.equals(value);
    }

    /**
     * build a TagVerifier by Tags and key
     *
     * @param tags ${@link Tags}
     * @param key  tags.getKeyFieldName()'s value
     * @return
     */
    public static TagVerifier build(Tags tags, String key) {
        return new TagVerifier()
            .add(Tags.CATEGORY, tags.getCategory())
            .add(Tags.TYPE, tags.getType())
            .add(tags.getKeyFieldName(), key);
    }
}
