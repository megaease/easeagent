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

package com.megaease.easeagent.log4j2.api;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Mdc {
    private final BiFunction<String, String, Void> putFunction;
    private final Function<String, Void> removeFunction;
    private final Function<String, String> getFunction;

    public Mdc(@Nonnull BiFunction<String, String, Void> putFunction, @Nonnull Function<String, Void> removeFunction, @Nonnull Function<String, String> getFunction) {
        this.putFunction = putFunction;
        this.removeFunction = removeFunction;
        this.getFunction = getFunction;
    }

    public void put(String key, String value) {
        putFunction.apply(key, value);
    }

    public void remove(String key) {
        removeFunction.apply(key);
    }

    public String get(String key) {
        return getFunction.apply(key);
    }
}
