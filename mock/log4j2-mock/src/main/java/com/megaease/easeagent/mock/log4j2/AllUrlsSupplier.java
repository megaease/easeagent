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

package com.megaease.easeagent.mock.log4j2;

import com.megaease.easeagent.log4j2.ClassLoaderUtils;

import java.net.URL;

public class AllUrlsSupplier implements UrlSupplier {
    public static final String USE_ENV = "EASEAGENT-SLF4J2-USE-CURRENT";
    private static volatile boolean enabled = false;

    public static void setEnabled(boolean enabled) {
        AllUrlsSupplier.enabled = enabled;
    }

    @Override
    public URL[] get() {
        if (!enabled()) {
            return new URL[0];
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return ClassLoaderUtils.getAllUrls(classLoader);
    }

    private boolean enabled() {
        if (enabled) {
            return true;
        }
        String enabledStr = System.getProperty(USE_ENV);
        if (enabledStr == null) {
            return false;
        }
        try {
            return Boolean.parseBoolean(enabledStr);
        } catch (Exception e) {
            return false;
        }
    }
}
