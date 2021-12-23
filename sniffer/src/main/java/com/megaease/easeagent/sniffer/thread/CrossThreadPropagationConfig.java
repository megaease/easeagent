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

package com.megaease.easeagent.sniffer.thread;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CrossThreadPropagationConfig {
    private volatile String[] canaryHeaders = new String[0];
    private final Predicate<String> prefixPredicate = e -> e.startsWith(ConfigConst.GlobalCanaryLabels.SERVICE_HEADERS + ConfigConst.DELIMITER);

    public CrossThreadPropagationConfig(Config config) {
        this.resetData(config);
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(List<ChangeItem> list) {
                boolean anyMatch = list.stream()
                        .map(ChangeItem::getFullName)
                        .anyMatch(prefixPredicate);
                if (anyMatch) {
                    resetData(config);
                }
            }
        });
    }

    private void resetData(Config config) {
        this.canaryHeaders = config.keySet().stream()
                .filter(prefixPredicate)
                .map(config::getString)
                .filter(Objects::nonNull).distinct().toArray(String[]::new);
    }

    public String[] getCanaryHeaders() {
        return canaryHeaders;
    }
}
