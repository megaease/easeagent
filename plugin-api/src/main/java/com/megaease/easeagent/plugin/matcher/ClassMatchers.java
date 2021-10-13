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

import com.megaease.easeagent.plugin.enums.ClassMatch;

public class ClassMatchers {
    public static ClassMatcher hasSuperClass(String className) {
        return ClassMatcher.builder()
            .name(className)
            .matchType(ClassMatch.BASE)
            .build();
    }

    public static ClassMatcher hasClassName(String className) {
        return ClassMatcher.builder()
            .name(className)
            .matchType(ClassMatch.NAMED)
            .build();
    }

    public static ClassMatcher hasInterface(String className) {
        return ClassMatcher.builder()
            .name(className)
            .matchType(ClassMatch.INTERFACE)
            .build();
    }
}
