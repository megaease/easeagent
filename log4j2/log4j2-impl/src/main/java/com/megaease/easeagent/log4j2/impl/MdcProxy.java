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

package com.megaease.easeagent.log4j2.impl;

import org.slf4j.MDC;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MdcProxy {
    public static final MdcPut PUT_INSTANCE = new MdcPut();
    public static final MdcRemove REMOVE_INSTANCE = new MdcRemove();
    public static final MdcGet GET_INSTANCE = new MdcGet();

    private static class MdcPut implements BiFunction<String, String, Void> {

        @Override
        public Void apply(String key, String value) {
            MDC.put(key, value);
            return null;
        }
    }

    private static class MdcRemove implements Function<String, Void> {

        @Override
        public Void apply(String key) {
            MDC.remove(key);
            return null;
        }
    }

    private static class MdcGet implements Function<String, String> {

        @Override
        public String apply(String key) {
            return MDC.get(key);
        }
    }
}
