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

package com.megaease.easeagent.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.ChangeItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProgressFieldsManager {

    private ProgressFieldsManager() {
    }

    public static void init(Configs configs) {
        Consumer<Map<String, String>> changeListener = ProgressFields.changeListener();
        changeListener.accept(configs.getConfigs());
        configs.addChangeListener(list -> {
            Map<String, String> map = new HashMap<>();
            for (ChangeItem changeItem : list) {
                String key = changeItem.getFullName();
                if (ProgressFields.isProgressFields(key)) {
                    map.put(key, changeItem.getNewValue());
                }
                changeListener.accept(map);
            }
        });
    }
}
