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

package com.megaease.easeagent.core.plugin.transformer.advice;

import lombok.Data;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaConstant;

@Data
public class MethodIdentityJavaConstant implements JavaConstant {
    private Integer identity;

    public MethodIdentityJavaConstant(int value) {
        this.identity = value;
    }

    @Override
    public Object toDescription() {
        return this.identity;
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.ForLoadedType.of(int.class);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return (T) this.identity;
    }
}
