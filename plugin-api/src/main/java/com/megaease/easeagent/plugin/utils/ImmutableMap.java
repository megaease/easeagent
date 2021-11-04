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

package com.megaease.easeagent.plugin.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMap {

    public static <K, V> Builder builder() {
        return new Builder();
    }

    public static class Builder<K, V> {
        Map<K, V> result = new HashMap<>();

        public Builder put(K k, V v) {
            result.put(k, v);
            return this;
        }

        public Map<K, V> build() {
            return Collections.unmodifiableMap(result);
        }
    }
}
