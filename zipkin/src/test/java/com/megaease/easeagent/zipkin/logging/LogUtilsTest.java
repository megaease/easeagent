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

package com.megaease.easeagent.zipkin.logging;

import com.megaease.easeagent.log4j2.ClassLoaderUtils;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LogUtilsTest {

    public static ClassLoader getClassLoader(String[] matches) {
        URL[] urls = ClassLoaderUtils.getAllUrls(Thread.currentThread().getContextClassLoader(), url -> {
            String path = url.getPath();
            if (!path.endsWith(".jar")) {
                return false;
            }
            for (String match : matches) {
                if (path.indexOf(match) > 0) {
                    return true;
                }
            }
            return false;
        });
        return new URLClassLoader(urls, null);
    }

    private static void resetUtils() {
        AgentFieldReflectAccessor.setStaticFieldValue(LogUtils.class, "log4jLoaded", null);
        AgentFieldReflectAccessor.setStaticFieldValue(LogUtils.class, "logbackLoaded", null);
        AgentFieldReflectAccessor.setStaticFieldValue(LogUtils.class, "log4jMdcClass", null);
        AgentFieldReflectAccessor.setStaticFieldValue(LogUtils.class, "logbackMdcClass", null);
    }

    public static class Close implements Closeable {
        public Close() {
            resetUtils();
        }

        @Override
        public void close() {
            resetUtils();
        }
    }

    public static Close reset() {
        return new Close();
    }

    @Test
    public void checkLog4JMDC() {
        try (Close close = reset()) {
            ClassLoader classLoader = getClassLoader(new String[]{"log4j-slf4j-impl", "log4j-core", "log4j-api"});
            Class clzss = LogUtils.checkLog4JMDC(classLoader);
            assertNotNull(clzss);
            Class clzss2 = LogUtils.checkLogBackMDC(classLoader);
            assertNull(clzss2);
        }
    }

    @Test
    public void checkLogBackMDC() {
        try (Close close = reset()) {
            ClassLoader classLoader = getClassLoader(new String[]{"slf4j-api", "logback-core", "logback-access", "logback-classic"});
            Class clzss = LogUtils.checkLog4JMDC(classLoader);
            assertNull(clzss);
            Class clzss2 = LogUtils.checkLogBackMDC(classLoader);
            assertNotNull(clzss2);
        }

    }

    @Test
    public void loadClass() {
        Class clzss = LogUtils.loadClass(Thread.currentThread().getContextClassLoader(), "org.apache.logging.log4j.ThreadContext");
        assertNotNull(clzss);
        clzss = LogUtils.loadClass(Thread.currentThread().getContextClassLoader(), "org.slf4j.MDC");
        assertNotNull(clzss);
    }
}
