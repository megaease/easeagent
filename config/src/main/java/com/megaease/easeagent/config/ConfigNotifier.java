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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConfigNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigNotifier.class);
    private final CopyOnWriteArrayList<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final String prefix;

    public ConfigNotifier(String prefix) {
        this.prefix = prefix;
    }

    public Runnable addChangeListener(ConfigChangeListener listener) {
        final boolean add = listeners.add(listener);
        return () -> {
            if (add) {
                listeners.remove(listener);
            }
        };
    }

    public void handleChanges(List<ChangeItem> list) {
        final List<ChangeItem> changes = this.prefix.isEmpty() ? list : filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }
        listeners.forEach(one -> {
            try {
                one.onChange(changes);
            } catch (Exception e) {
                LOGGER.warn("Notify config changes to listener failure: {}", one.toString());
            }
        });
    }

    private List<ChangeItem> filterChanges(List<ChangeItem> list) {
        return list.stream().filter(one -> one.getFullName().startsWith(prefix))
                .map(e -> new ChangeItem(e.getFullName().substring(prefix.length()), e.getFullName(), e.getOldValue(), e.getNewValue()))
                .collect(Collectors.toList());
    }
}
