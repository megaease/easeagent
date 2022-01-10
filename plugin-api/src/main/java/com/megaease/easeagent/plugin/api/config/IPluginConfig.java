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

package com.megaease.easeagent.plugin.api.config;

import com.megaease.easeagent.plugin.Const;

import java.util.List;
import java.util.Set;

public interface IPluginConfig {
    String domain();

    String namespace();

    String id();

    boolean hasProperty(String property);

    String getString(String property);

    Integer getInt(String property);

    Boolean getBoolean(String property);

    default boolean enabled() {
        Boolean b = getBoolean(Const.ENABLED_CONFIG);
        if (b == null) {
            return false;
        }
        return b;
    }

    default Boolean getBoolean(String property, Boolean defaultValue) {
        Boolean ret;
        if (!hasProperty(property)) {
            return defaultValue;
        } else {
            ret = getBoolean(property);
            return ret != null ? ret : defaultValue;
        }
    }

    Double getDouble(String property);

    Long getLong(String property);

    List<String> getStringList(String property);

    IPluginConfig getGlobal();

    Set<String> keySet();

    void addChangeListener(PluginConfigChangeListener listener);
}
