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

package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.utils.common.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class ProgressFields {
    public static final String EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG = "easeagent.progress.forwarded.headers";
    public static final String OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG = "observability.tracings.tag.response.headers.";
    public static final String OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG = "observability.tracings.service.tags.";
    private static volatile Fields responseHoldTagFields = build(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG, Collections.emptyMap());
    private static volatile Fields serviceTags = build(OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG, Collections.emptyMap());
    private static final Set<String> forwardHeaderSet = new HashSet<>();


    public static Consumer<Map<String, String>> changeListener() {
        return values -> new Change().putAll(values).flush();
    }

    public static boolean isProgressFields(String key) {
        return isForwardedHeader(key) || isResponseHoldTagKey(key) || isServerTags(key);
    }

    private static boolean isForwardedHeader(String key) {
        return key.equals(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG);
    }

    private static boolean isResponseHoldTagKey(String key) {
        return key.startsWith(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG);
    }

    private static boolean isServerTags(String key) {
        return key.startsWith(OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG);
    }

    private static void buildForwardedHeaderSet(String value) {
        String[] split = StringUtils.split(value, ",");
        forwardHeaderSet.clear();
        if (split == null || split.length == 0) {
            return;
        }
        forwardHeaderSet.addAll(Arrays.asList(split));
    }

    @SuppressWarnings("all")
    private static void setResponseHoldTagFields(Map<String, String> fields) {
        responseHoldTagFields = responseHoldTagFields.rebuild(fields);
    }

    @SuppressWarnings("all")
    private static void setServiceTags(Map<String, String> tags) {
        serviceTags = serviceTags.rebuild(tags);
    }

    public static boolean isEmpty(String[] fields) {
        return fields == null || fields.length == 0;
    }

    public static Set<String> getForwardedHeaders() {
        return forwardHeaderSet;
    }

    public static String[] getResponseHoldTagFields() {
        return responseHoldTagFields.values;
    }

    public static Map<String, String> getServiceTags() {
        return serviceTags.keyValues;
    }


    private static Fields build(@Nonnull String keyPrefix, @Nonnull Map<String, String> map) {
        if (map.isEmpty()) {
            return new Fields(keyPrefix, Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap());
        }
        Map<String, String> keyValues = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace(keyPrefix, "");
            keyValues.put(key, entry.getValue());
        }
        return new Fields(keyPrefix, Collections.unmodifiableSet(new HashSet<>(map.values())), keyValues, map);
    }

    public static class Fields {
        private final String keyPrefix;
        private final String[] values;
        private final Map<String, String> keyValues;
        private final Map<String, String> map;

        private Fields(@Nonnull String keyPrefix, @Nonnull Set<String> fieldSet, Map<String, String> keyValues, @Nonnull Map<String, String> map) {
            this.keyPrefix = keyPrefix;
            this.values = fieldSet.toArray(new String[0]);
            this.keyValues = keyValues;
            this.map = map;
        }

        Fields rebuild(@Nonnull Map<String, String> map) {
            if (this.map.isEmpty()) {
                map.entrySet().removeIf(stringStringEntry -> StringUtils.isEmpty(stringStringEntry.getValue()));
                return build(keyPrefix, map);
            }
            Map<String, String> newMap = new HashMap<>(this.map);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (StringUtils.isEmpty(entry.getValue())) {
                    newMap.remove(entry.getKey());
                } else {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            return build(keyPrefix, Collections.unmodifiableMap(newMap));
        }
    }

    static class Change {
        private final Map<String, String> responseHoldTags = new HashMap<>();
        private final Map<String, String> serverTags = new HashMap<>();

        public Change putAll(Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public void put(String key, String value) {
            if (ProgressFields.isForwardedHeader(key)) {
                buildForwardedHeaderSet(value);
            } else if (ProgressFields.isResponseHoldTagKey(key)) {
                responseHoldTags.put(key, value);
            } else if (ProgressFields.isServerTags(key)) {
                serverTags.put(key, value);
            }
        }

        private void flush() {
            if (!responseHoldTags.isEmpty()) {
                setResponseHoldTagFields(responseHoldTags);
            }
            if (!serverTags.isEmpty()) {
                setServiceTags(serverTags);
            }
        }
    }
}
