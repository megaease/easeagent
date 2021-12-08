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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.ProgressFields;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ProgressFieldsManager {
    public static void init(Configs configs) {
        BiFunction<String, Map<String, String>, String> changeListener = ProgressFields.changeListener();
        Change change = new Change(changeListener);
        for (Map.Entry<String, String> entry : configs.getConfigs().entrySet()) {
            change.put(entry.getKey(), entry.getValue());
        }
        change.flush();
        configs.addChangeListener(list -> {
            Change c = new Change(changeListener);
            for (ChangeItem changeItem : list) {
                c.put(changeItem.getFullName(), changeItem.getNewValue());
            }
            c.flush();
        });
    }

    static class Change {
        private final BiFunction<String, Map<String, String>, String> changeListener;
        Map<String, String> penetration = new HashMap<>();
        Map<String, String> responseHoldTag = new HashMap<>();

        public Change(BiFunction<String, Map<String, String>, String> changeListener) {
            this.changeListener = changeListener;
        }

        public void put(String key, String value) {
            if (ProgressFields.isPenetrationKey(key)) {
                penetration.put(key, value);
            } else if (ProgressFields.isResponseHoldTagKey(key)) {
                responseHoldTag.put(key, value);
            }
        }

        public void flush() {
            if (!penetration.isEmpty()) {
                changeListener.apply(ProgressFields.EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG, penetration);
            }
            if (!responseHoldTag.isEmpty()) {
                changeListener.apply(ProgressFields.EASEAGENT_PROGRESS_RESPONSE_HOLD_TAG_FIELDS_CONFIG, penetration);
            }
        }
    }
}
