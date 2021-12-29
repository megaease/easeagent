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

package com.megaease.easeagent.plugin.healthy;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.health.AgentHealth;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;

@AdviceTo(value = SpringApplicationAdminMXBeanRegistrarAdvice.class)
public class OnApplicationEventInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {

    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        ApplicationEvent applicationEvent = (ApplicationEvent) methodInfo.getArgs()[0];
        if (applicationEvent instanceof ApplicationReadyEvent) {
            AgentHealth.INSTANCE.setReady(true);
        } else if (applicationEvent instanceof ApplicationFailedEvent) {
            AgentHealth.INSTANCE.setReady(false);
        }
    }

    @Override
    public String getType() {
        return "healthReady";
    }
}
