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

package com.megaease.easeagent.core.plugin.transformer;

import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.NullObject;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicFieldAdvice {
    static Logger log = LoggerFactory.getLogger(DynamicFieldAdvice.class);

    public static class DynamicInstanceInit {
        @Advice.OnMethodEnter
        public static void enter(@Advice.This Object target, @Advice.Origin("#m") String method) {
            if (((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data() == null) {
                ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(NullObject.NULL);
            }
        }
    }

    public static class DynamicClassInit {
        @Advice.OnMethodExit(onThrowable = Exception.class)
        public static void exit(@Advice.Origin("#t") String type, @Advice.Origin("#m") String method) {
            log.info("Add dynamic field to {} at {}", type, method);
        }
    }
}
