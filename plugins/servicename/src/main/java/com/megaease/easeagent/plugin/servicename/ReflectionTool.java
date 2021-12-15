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

package com.megaease.easeagent.plugin.servicename;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionTool {
    public static Object invokeMethod(Object own, String method, Object... args) throws ReflectiveOperationException {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getClass();
        }
        Method md = ReflectionUtils.findMethod(own.getClass(), method, types);
        ReflectionUtils.makeAccessible(md);
        return ReflectionUtils.invokeMethod(md, own, args);
    }

    public static Object extractField(Object own, String field) throws ReflectiveOperationException {
        Field fd = ReflectionUtils.findField(own.getClass(), field);
        ReflectionUtils.makeAccessible(fd);
        return ReflectionUtils.getField(fd, own);
    }


    public static boolean hasText(String val) {
        return val != null && val.trim().length() > 0;
    }
}
