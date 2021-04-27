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

package com.megaease.easeagent.zipkin.logging;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.common.MethodUtils;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

import java.lang.reflect.Method;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class AgentLogMDC {

    public final Class<?> clazz;
    private final Method method4Get;
    private final Method method4Put;
    private final Method method4Remove;

    public static AgentLogMDC create(ClassLoader classLoader) {
        Class<?> aClass = LogUtils.checkLog4JMDC(classLoader);
        if (aClass == null) {
            aClass = LogUtils.checkLogBackMDC(classLoader);
        }
        if (aClass != null) {
            return new AgentLogMDC(aClass);
        }
        return null;
    }

    public AgentLogMDC(Class<?> aClass) {
        this.clazz = aClass;
        method4Get = MethodUtils.findMethod(clazz, "get", String.class);
        method4Put = MethodUtils.findMethod(clazz, "put", String.class, String.class);
        method4Remove = MethodUtils.findMethod(clazz, "remove", String.class);
    }

    public void put(String name, String value) {
        MethodUtils.invokeMethod(method4Put, null, name, value);
    }

    public String get(String name) {
        return (String) MethodUtils.invokeMethod(method4Get, null, name);
    }

    public void remove(String name) {
        MethodUtils.invokeMethod(method4Remove, null, name);
    }
}
