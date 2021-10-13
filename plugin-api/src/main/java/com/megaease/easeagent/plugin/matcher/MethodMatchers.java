/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.matcher;

import com.megaease.easeagent.plugin.enums.StringMatch;

@SuppressWarnings("unused")
public class MethodMatchers {
    public static MethodMatcher named(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.EQUALS)
            .build();
    }

    public static MethodMatcher nameStartWith(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.START_WITH)
            .build();
    }

    public static MethodMatcher nameEndWith(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.END_WITH)
            .build();
    }

    public static MethodMatcher nameContains(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.CONTAINS)
            .build();
    }
}
