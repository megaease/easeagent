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

package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.utils.common.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;

public class ProgressFields {
    public static final String EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG = "easeagent.progress.forwarded.headers";
    public static final String OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG = "observability.tracings.tag.response.headers";
    private static volatile Fields FORWARDED_HEADERS = build(Collections.emptyMap());
    private static volatile Fields RESPONSE_HOLD_TAG_FIELDS = build(Collections.emptyMap());

    public static BiFunction<String, Map<String, String>, String> changeListener() {
        return (key, values) -> {
            if (isForwardedHeader(key)) {
                setForwardedHeaders(values);
            } else if (isResponseHoldTagKey(key)) {
                setResponseHoldTagFields(values);
            }
            return null;
        };
    }

    public static boolean isForwardedHeader(String key) {
        return key.startsWith(EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG);
    }

    public static boolean isResponseHoldTagKey(String key) {
        return key.startsWith(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG);
    }

    private static void setForwardedHeaders(Map<String, String> headers) {
        FORWARDED_HEADERS = FORWARDED_HEADERS.rebuild(headers);
    }

    private static void setResponseHoldTagFields(Map<String, String> fields) {
        RESPONSE_HOLD_TAG_FIELDS = RESPONSE_HOLD_TAG_FIELDS.rebuild(fields);
    }

    public static boolean isEmpty(String[] fields) {
        return fields == null || fields.length == 0;
    }

    public static Set<String> getForwardedHeaders() {
        return FORWARDED_HEADERS.fieldSet;
    }

    public static String[] getResponseHoldTagFields() {
        return RESPONSE_HOLD_TAG_FIELDS.fields;
    }

    private static Fields build(@Nonnull Map<String, String> map) {
        if (map.isEmpty()) {
            return new Fields(Collections.emptySet(), Collections.emptyMap());
        }
        return new Fields(Collections.unmodifiableSet(new HashSet<>(map.values())), map);
    }


    public static class Fields {
        private final Set<String> fieldSet;
        private final String[] fields;
        private final Map<String, String> map;

        public Fields(@Nonnull Set<String> fieldSet, @Nonnull Map<String, String> map) {
            this.fieldSet = fieldSet;
            this.fields = fieldSet.toArray(new String[0]);
            this.map = map;
        }

        Fields rebuild(@Nonnull Map<String, String> map) {
            if (this.map.isEmpty()) {
                map.entrySet().removeIf(stringStringEntry -> StringUtils.isEmpty(stringStringEntry.getValue()));
                return build(map);
            }
            Map<String, String> newMap = new HashMap<>(this.map);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (StringUtils.isEmpty(entry.getValue())) {
                    newMap.remove(entry.getKey());
                } else {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            return build(Collections.unmodifiableMap(newMap));
        }
    }
}
