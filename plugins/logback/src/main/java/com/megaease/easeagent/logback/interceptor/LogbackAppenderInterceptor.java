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
package com.megaease.easeagent.logback.interceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.megaease.easeagent.logback.log.LoggingEventMapper;
import com.megaease.easeagent.logback.points.LoggerPoints;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(LoggerPoints.class)
public class LogbackAppenderInterceptor implements NonReentrantInterceptor {
    String level = Level.INFO.levelStr;
    Level collectLevel = Level.INFO;

    @Override
    public void init(IPluginConfig config, int uniqueIndex) {
        String lv = config.getString("level");
        if (StringUtils.isNotEmpty(lv)) {
            collectLevel = Level.toLevel(lv, Level.OFF);
            this.level = lv;
        }
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ILoggingEvent event = (ILoggingEvent)methodInfo.getArgs()[0];
        Level level = event.getLevel();
        if (level.levelInt < collectLevel.levelInt) {
            return;
        }
        AgentLogData log = LoggingEventMapper.INSTANCE.mapLoggingEvent(event);
        EaseAgent.getAgentReport().report(log);
    }

    @Override
    public String getType() {
        return Order.LOG.getName();
    }

    @Override
    public int order() {
        return Order.LOG.getOrder();
    }
}
