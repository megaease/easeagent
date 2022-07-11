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

package com.megaease.easeagent.core.plugin.matcher;

import com.megaease.easeagent.plugin.Ordered;
import lombok.Data;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.any;

@Data
public class ClassTransformation implements Ordered {
    private int order;
    private Junction<TypeDescription> classMatcher;
    private ElementMatcher<ClassLoader> classloaderMatcher;
    private Set<MethodTransformation>  methodTransformations;
    private boolean hasDynamicField;

    public ClassTransformation(int order,
                               ElementMatcher<ClassLoader> classloaderMatcher,
                               Junction<TypeDescription> classMatcher,
                               Set<MethodTransformation> methodTransformations,
                               boolean hasDynamicField) {
        this.order = order;
        if (classloaderMatcher == null) {
            this.classloaderMatcher = any();
        } else {
            this.classloaderMatcher = classloaderMatcher;
        }
        this.classMatcher = classMatcher;
        this.methodTransformations = methodTransformations;
        this.hasDynamicField = hasDynamicField;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int order() {
        return this.order;
    }

    public static class Builder {
        private int order;
        private Junction<TypeDescription> classMatcher;
        private ElementMatcher<ClassLoader> classloaderMatcher = null;
        private Set<MethodTransformation> methodTransformations;
        private boolean hasDynamicField;

        Builder() {
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder classloaderMatcher(ElementMatcher<ClassLoader> clmMatcher) {
            this.classloaderMatcher = clmMatcher;
            return this;
        }

        public Builder classMatcher(Junction<TypeDescription> classMatcher) {
            this.classMatcher = classMatcher;
            return this;
        }

        public Builder methodTransformations(Set<MethodTransformation> methodTransformations) {
            this.methodTransformations = methodTransformations;
            return this;
        }

        public Builder hasDynamicField(boolean hasDynamicField) {
            this.hasDynamicField = hasDynamicField;
            return this;
        }

        public ClassTransformation build() {
            return new ClassTransformation(order, classloaderMatcher, classMatcher,
                methodTransformations, hasDynamicField);
        }

        public String toString() {
            return "ClassTransformation.Builder(order=" + this.order + ", classMatcher=" + this.classMatcher + ", methodTransformations=" + this.methodTransformations + ", hasDynamicField=" + this.hasDynamicField + ")";
        }
    }
}
