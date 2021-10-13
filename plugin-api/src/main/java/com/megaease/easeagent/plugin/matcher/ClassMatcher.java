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

import com.megaease.easeagent.plugin.asm.Modifier;
import com.megaease.easeagent.plugin.enums.ClassMatch;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class ClassMatcher implements IClassMatcher {
    private String name;
    private ClassMatch matchType;
    private int modifier = Modifier.ACC_NONE;
    private int notModifier = Modifier.ACC_NONE;
    private String classLoader;

    protected ClassMatcher() {
    }

    private ClassMatcher(String name, ClassMatch type, int modifier, int notModifier, String loaderName) {
        this.name = name;
        this.matchType = type;
        this.modifier = modifier;
        this.notModifier = notModifier;
        this.classLoader = loaderName;
    }

    public static ClassMatcherBuilder builder() {
        return new ClassMatcherBuilder();
    }


    public static class ClassMatcherBuilder {
        private String name;
        private ClassMatch matchType;
        private int modifier;
        private int notModifier;
        private String classLoader;

        ClassMatcherBuilder() {
        }

        public ClassMatcherBuilder hasSuperClass(String className) {
            return this.name(className).matchType(ClassMatch.SUPER_CLASS);
        }

        public ClassMatcherBuilder hasClassName(String className) {
            return this.name(className).matchType(ClassMatch.NAMED);
        }

        public ClassMatcherBuilder hasInterface(String className) {
            return this.name(className).matchType(ClassMatch.INTERFACE);
        }

        public ClassMatcherBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ClassMatcherBuilder matchType(ClassMatch matchType) {
            this.matchType = matchType;
            return this;
        }

        public ClassMatcherBuilder modifier(int modifier) {
            this.modifier = modifier;
            return this;
        }

        public ClassMatcherBuilder notModifier(int notModifier) {
            this.notModifier = notModifier;
            return this;
        }

        public ClassMatcherBuilder classLoader(String classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ClassMatcherBuilder isPublic() {
            this.modifier |= Modifier.ACC_PUBLIC;
            return this;
        }

        public ClassMatcherBuilder isPrivate() {
            this.modifier |= Modifier.ACC_PRIVATE;
            return this;
        }

        public ClassMatcherBuilder isAbstract() {
            this.modifier |= Modifier.ACC_ABSTRACT;
            return this;
        }

        public ClassMatcherBuilder isInterface() {
            this.modifier |= Modifier.ACC_INTERFACE;
            return this;
        }

        public ClassMatcherBuilder notPrivate() {
            this.notModifier |= Modifier.ACC_PRIVATE;
            return this;
        }

        public ClassMatcherBuilder notAbstract() {
            this.notModifier |= Modifier.ACC_ABSTRACT;
            return this;
        }

        public ClassMatcherBuilder notInterface() {
            this.notModifier |= Modifier.ACC_INTERFACE;
            return this;
        }

        public ClassMatcher build() {
            return new ClassMatcher(name, matchType, modifier, notModifier, classLoader);
        }

        public String toString() {
            return "ClassMatcher.ClassMatcherBuilder(name=" + this.name + ", matchType=" + this.matchType + ", modifier=" + this.modifier + ", notModifier=" + this.notModifier + ", classLoader=" + this.classLoader + ")";
        }
    }
}


