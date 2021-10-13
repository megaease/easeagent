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

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClassMethodMatcher {
    private ClassMatcher classMatcher;
    private MethodMatcher methodMatcher;

    public ClassMethodMatcher(ClassMatcher classMatcher, MethodMatcher methodMatcher) {
        this.classMatcher = classMatcher;
        this.methodMatcher = methodMatcher;
    }

    public static class Builder {
        private ClassMatcher classMatcher;
        private MethodMatcher methodMatcher = null;

        public Builder type(ClassMatcher matcher) {
            this.classMatcher = matcher;
            return this;
        }

        public Builder method(MethodMatcher matcher) {
            if (this.methodMatcher == null) {
                this.methodMatcher = matcher;
            } else {
                this.methodMatcher = this.methodMatcher.or(matcher);
            }
            return this;
        }

        public ClassMethodMatcher build() {
            return new ClassMethodMatcher(this.classMatcher, this.methodMatcher);
        }
    }
}
