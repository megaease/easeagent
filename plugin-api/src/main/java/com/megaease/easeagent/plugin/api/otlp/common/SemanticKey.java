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
package com.megaease.easeagent.plugin.api.otlp.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.checkerframework.checker.units.qual.C;

import java.util.concurrent.ConcurrentHashMap;

public class SemanticKey {
    public static final String SCHEMA_URL = SemanticAttributes.SCHEMA_URL;
    public static final AttributeKey<String> THREAD_NAME = SemanticAttributes.THREAD_NAME;
    public static final AttributeKey<Long> THREAD_ID = SemanticAttributes.THREAD_ID;

    public static final AttributeKey<String> EXCEPTION_TYPE = SemanticAttributes.EXCEPTION_TYPE;
    public static final AttributeKey<String> EXCEPTION_MESSAGE = SemanticAttributes.EXCEPTION_MESSAGE;
    public static final AttributeKey<String> EXCEPTION_STACKTRACE = SemanticAttributes.EXCEPTION_STACKTRACE;

    private static final ConcurrentHashMap<String, AttributeKey<String>> keysMap = new ConcurrentHashMap<>();

    public static AttributeKey<String> stringKey(String key) {
        AttributeKey<String> vk = keysMap.get(key);
        return vk != null ? vk : keysMap.computeIfAbsent(key, AttributeKey::stringKey);
    }
}
