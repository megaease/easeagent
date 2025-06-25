/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.report.utils;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

public class UtilsTest {

    public static Map<String, String> readMap(String path) throws IOException {
        String config = UtilsTest.readFromResourcePath(path);
        return (Map<String, String>) (Map) JsonUtil.toMap(config);
    }

    public static Configs readConfigs(String path) throws IOException {
        return new Configs(readMap(path));
    }

    public static String readFromResourcePath(String resourcePath) throws IOException {
        try (InputStream in = ClassLoader.getSystemResource(resourcePath).openStream()) {
            int size = in.available();
            byte[] bytes = new byte[size];
            in.read(bytes, 0, size);
            return new String(bytes);
        }
    }

    public static <T> T getFiled(Object o, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object result = field.get(o);
        field.setAccessible(false);
        return (T) result;
    }
}
