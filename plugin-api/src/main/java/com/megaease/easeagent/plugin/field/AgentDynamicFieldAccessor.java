/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.field;

import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.lang.reflect.Field;

public class AgentDynamicFieldAccessor {
    private static final Logger logger = EaseAgent.loggerFactory.getLogger(AgentDynamicFieldAccessor.class);

    public static final String DYNAMIC_FIELD_NAME = "ease_agent_dynamic_$$$_data";

    @SuppressWarnings("unchecked")
    public static <T> T getDynamicFieldValue(Object target) {
        if (!(target instanceof DynamicFieldAccessor)) {
            logger.warn(target.getClass().getName() + " must implements DynamicFieldAccessor");
            return null;
        }
        return (T) ((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data();
    }

    public static void setDynamicFieldValue(Object target, Object value) {
        if (!(target instanceof DynamicFieldAccessor)) {
            logger.warn(target.getClass().getName() + " must implements DynamicFieldAccessor");
            return;
        }
        ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(value);
    }

    public static Field getDynamicFieldFromClass(Class<?> clazz) {
        return AgentFieldReflectAccessor.getFieldFromClass(clazz, DYNAMIC_FIELD_NAME);
    }
}
