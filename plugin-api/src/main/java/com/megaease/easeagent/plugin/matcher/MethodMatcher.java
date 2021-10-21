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
import com.megaease.easeagent.plugin.enums.StringMatch;
import lombok.Data;

import java.util.*;

@Data
@SuppressWarnings("unused")
public class MethodMatcher implements IMethodMatcher {
    private String name;
    private StringMatch nameMatchType;

    // ignored when with default value
    private String returnType = null;
    private String[] args;
    private int argsLength = -1;
    private int modifier = Modifier.ACC_NONE;
    private int notModifier = Modifier.ACC_NONE;

    private String qualifier;

    public static int MODIFIER_MASK = Modifier.ACC_ABSTRACT | Modifier.ACC_STATIC
        | Modifier.ACC_PRIVATE | Modifier.ACC_PUBLIC | Modifier.ACC_PROTECTED;

    protected MethodMatcher() {
    }

    private MethodMatcher(String name, StringMatch type, String returnType,
                            String[] args, int argLength, int modifier, int notModifier, String qualifier) {
        this.name = name;
        this.nameMatchType = type;
        this.returnType = returnType;
        this.args = args;
        this.argsLength = argLength;
        this.modifier = modifier;
        this.notModifier = notModifier;
        this.qualifier = qualifier;
    }

    public static MethodMatcherBuilder builder() {
        return new MethodMatcherBuilder();
    }

    public static class MethodMatcherBuilder {
        private String name;
        private StringMatch nameMatchType;
        private String returnType;
        private String[] args;
        private int argsLength;
        private int modifier;
        private int notModifier;
        private String qualifier = "default";

        MethodMatcherBuilder() {
        }

        public MethodMatcherBuilder named(String methodName) {
            return this.name(methodName).nameMatchType(StringMatch.EQUALS);
        }

        public MethodMatcherBuilder nameStartWith(String methodName) {
            return this.name(methodName).nameMatchType(StringMatch.START_WITH);
        }

        public MethodMatcherBuilder nameEndWith(String methodName) {
            return this.name(methodName).nameMatchType(StringMatch.END_WITH);
        }

        public MethodMatcherBuilder nameContains(String methodName) {
            return this.name(methodName).nameMatchType(StringMatch.CONTAINS);
        }

        public MethodMatcherBuilder isPublic() {
            this.modifier |= Modifier.ACC_PUBLIC;
            return this;
        }

        public MethodMatcherBuilder isPrivate() {
            this.modifier |= Modifier.ACC_PRIVATE;
            return this;
        }

        public MethodMatcherBuilder isAbstract() {
            this.modifier |= Modifier.ACC_ABSTRACT;
            return this;
        }

        public MethodMatcherBuilder isStatic() {
            this.modifier |= Modifier.ACC_STATIC;
            return this;
        }


        public MethodMatcherBuilder notPublic() {
            this.notModifier |= Modifier.ACC_PUBLIC;
            return this;
        }

        public MethodMatcherBuilder notPrivate() {
            this.notModifier |= Modifier.ACC_PRIVATE;
            return this;
        }

        public MethodMatcherBuilder notAbstract() {
            this.notModifier |= Modifier.ACC_ABSTRACT;
            return this;
        }

        public MethodMatcherBuilder notStatic() {
            this.notModifier|= Modifier.ACC_STATIC;
            return this;
        }

        public MethodMatcherBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MethodMatcherBuilder nameMatchType(StringMatch nameMatchType) {
            this.nameMatchType = nameMatchType;
            return this;
        }

        public MethodMatcherBuilder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public MethodMatcherBuilder args(String[] args) {
            this.args = args;
            return this;
        }

        public MethodMatcherBuilder arg(int idx, String argType) {
            if (args == null) {
                this.args = new String[idx + 1];
            } else if (this.args.length < idx + 1) {
                this.args = Arrays.copyOf(this.args, idx + 1);
            }

            return this;
        }

        public MethodMatcherBuilder argsLength(int length) {
            this.argsLength = length;

            if (length <= 0) {
                this.args = null;
            } else if (this.args == null) {
                this.args = new String[length];
            } else if (this.args.length < length) {
                this.args = Arrays.copyOf(this.args, length);
            }

            return this;
        }

        public MethodMatcherBuilder modifier(int modifier) {
            this.modifier = modifier;
            return this;
        }

        public MethodMatcherBuilder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public MethodMatcher build() {
            return new MethodMatcher(name, nameMatchType, returnType,
                args, argsLength, modifier, notModifier, qualifier);
        }

        public String toString() {
            return "MethodMatcher.MethodMatcherBuilder(name=" + this.name
                + ", nameMatchType=" + this.nameMatchType + ", returnType=" + this.returnType
                + ", args=" + Arrays.deepToString(this.args) + ", argsLength=" + this.argsLength
                + ", modifier=" + this.modifier
                + ", notModifier=" + this.notModifier + ")";
        }
    }

    public static MethodMatchersBuilder multiBuilder() {
        return new MethodMatchersBuilder();
    }

    public static class MethodMatchersBuilder {
        private Set<IMethodMatcher> methodMatchers;

        MethodMatchersBuilder() {
        }

        public MethodMatchersBuilder methodMatchers(Set<IMethodMatcher> methodMatchers) {
            this.methodMatchers = methodMatchers;
            return this;
        }

        public MethodMatchersBuilder match(MethodMatcher matcher) {
            if (matcher == null) {
                return this;
            }
            if (this.methodMatchers == null) {
                this.methodMatchers = new LinkedHashSet<>();
            }
            this.methodMatchers.add(matcher);
            return this;
        }

        public Set<IMethodMatcher> build() {
            return this.methodMatchers;
        }

        public String toString() {
            return "MethodMatchers.MethodMatchersBuilder(methodMatchers=" + this.methodMatchers + ")";
        }
    }
}
