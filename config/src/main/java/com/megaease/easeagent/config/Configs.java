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

package com.megaease.easeagent.config;


import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import java.util.*;
import java.util.stream.Collectors;

public class Configs implements Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configs.class);
    protected Map<String, String> source;
    protected ConfigNotifier notifier;

    protected Configs() {
    }

    public Configs(Map<String, String> source) {
        this.source = new TreeMap<>(source);
        notifier = new ConfigNotifier("");
    }

    public void updateConfigsNotNotify(Map<String, String> changes) {
        this.source.putAll(changes);
    }

    public void updateConfigs(Map<String, String> changes) {
        Map<String, String> dump = new TreeMap<>(this.source);
        List<ChangeItem> items = new LinkedList<>();
        changes.forEach((name, value) -> {
            String old = dump.get(name);
            if (!Objects.equals(old, value)) {
                dump.put(name, value);
                items.add(new ChangeItem(name, name, old, value));
            }
        });
        if (!items.isEmpty()) {
            LOGGER.info("change items: {}", items);
            this.source = dump;
            this.notifier.handleChanges(items);
        }
    }

    protected boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    @Override
    public Map<String, String> getConfigs() {
        return new TreeMap<>(this.source);
    }

    public String toPrettyDisplay() {
        return this.source.toString();
    }

    public boolean hasPath(String path) {
        return this.source.containsKey(path);
    }


    public String getString(String name) {
        return this.source.get(name);
    }

    public Integer getInt(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Integer getInt(String name, int defValue) {
        Integer anInt = getInt(name);
        if (anInt == null) {
            return defValue;
        }
        return anInt;
    }

    public Boolean getBooleanNullForUnset(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    public Boolean getBoolean(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    @Override
    public Boolean getBoolean(String name, boolean defValue) {
        Boolean aBoolean = getBoolean(name);
        if (aBoolean == null) {
            return defValue;
        }
        return aBoolean;
    }

    public Double getDouble(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Double getDouble(String name, double defValue) {
        Double aDouble = getDouble(name);
        if (aDouble == null) {
            return defValue;
        }
        return aDouble;
    }

    public Long getLong(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Long getLong(String name, long defValue) {
        Long aLong = getLong(name);
        if (aLong == null) {
            return defValue;
        }
        return aLong;
    }

    public List<String> getStringList(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Runnable addChangeListener(ConfigChangeListener listener) {
        return notifier.addChangeListener(listener);
    }

    @Override
    public Set<String> keySet() {
        return this.source.keySet();
    }
}
