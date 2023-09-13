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
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.utils.SystemEnv;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Get config from system properties or environment variables.
 */
final class ConfigPropertiesUtils {

    public static boolean getBoolean(String propertyName, boolean defaultValue) {
        String strValue = getString(propertyName);
        return strValue == null ? defaultValue : Boolean.parseBoolean(strValue);
    }

    public static int getInt(String propertyName, int defaultValue) {
        String strValue = getString(propertyName);
        if (strValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    @Nullable
    public static String getString(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value != null) {
            return value;
        }
        return SystemEnv.get(toEnvVarName(propertyName));
    }

    /**
     * dot.case -> UPPER_UNDERSCORE
     */
    public static String toEnvVarName(String propertyName) {
        return propertyName.toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_');
    }

    private ConfigPropertiesUtils() {
    }
}
