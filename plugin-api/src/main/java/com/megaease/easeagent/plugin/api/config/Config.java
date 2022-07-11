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

package com.megaease.easeagent.plugin.api.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface Config {
    boolean hasPath(String path);

    String getString(String name);

    String getString(String name, String defVal);

    Integer getInt(String name);

    Integer getInt(String name, int defValue);

    Boolean getBoolean(String name);

    Boolean getBoolean(String name, boolean defValue);

    Boolean getBooleanNullForUnset(String name);

    Double getDouble(String name);

    Double getDouble(String name, double defValue);

    Long getLong(String name);

    Long getLong(String name, long defValue);

    List<String> getStringList(String name);

    Runnable addChangeListener(ConfigChangeListener listener);

    Set<String> keySet();

    Map<String, String> getConfigs();

    void updateConfigs(Map<String, String> changes);

    void updateConfigsNotNotify(Map<String, String> changes);
}
