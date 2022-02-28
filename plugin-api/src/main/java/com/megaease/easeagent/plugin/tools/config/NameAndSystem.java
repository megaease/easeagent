/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.List;

public class NameAndSystem {
    public static final NameAndSystem INSTANCE;

    static {
        INSTANCE = new NameAndSystem();
        INSTANCE.name = EaseAgent.getConfig(ConfigConst.SERVICE_NAME);
        INSTANCE.system = EaseAgent.getConfig(ConfigConst.SYSTEM_NAME);
        EaseAgent.getConfig().addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(List<ChangeItem> list) {
                for (ChangeItem changeItem : list) {
                    if (ConfigConst.SERVICE_NAME.equals(changeItem.getFullName())) {
                        INSTANCE.name = changeItem.getNewValue();
                    }
                    if (ConfigConst.SYSTEM_NAME.equals(changeItem.getFullName())) {
                        INSTANCE.system = changeItem.getNewValue();
                    }
                }
            }
        });
    }

    public static String system() {
        return INSTANCE.system;
    }

    public static String name() {
        return INSTANCE.name;
    }

    private volatile String name;
    private volatile String system;

    private NameAndSystem() {
    }

    public String getName() {
        return name;
    }

    public String getSystem() {
        return system;
    }
}
