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
import lombok.Builder;
import lombok.Data;

import javax.lang.model.type.NullType;
import java.util.Arrays;

@Data
@Builder
@SuppressWarnings("unused")
public class MethodMatcher implements IMethodMatcher {
    private String name;
    private StringMatch nameMatchType;

    // ignored when with default value
    private Class<?> returnType = NullType.class;
    private String[] args;
    private int argsLength = -1;
    private int modifier = Modifier.ACC_NONE;

    protected MethodMatcher() {
    }

    private MethodMatcher(String name, StringMatch type, Class<?> returnType,
                            String[] args, int argLength, int modifier) {
        this.name = name;
        this.nameMatchType = type;
        this.returnType = returnType;
        this.args = args;
        this.argsLength = argLength;
        this.modifier = modifier;
    }

    public MethodMatcher isPublic() {
        this.modifier |= Modifier.ACC_PUBLIC;
        return this;
    }

    public MethodMatcher isPrivate() {
        this.modifier |= Modifier.ACC_PRIVATE;
        return this;
    }

    public MethodMatcher isAbstract() {
        this.modifier |= Modifier.ACC_ABSTRACT;
        return this;
    }

    public MethodMatcher arg(int idx, String argType) {
        if (args == null) {
            this.args = new String[idx + 1];
        } else if (this.args.length < idx + 1) {
            this.args = Arrays.copyOf(this.args, idx + 1);
        }

        return this;
    }

    public MethodMatcher argsLength(int length) {
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
}
