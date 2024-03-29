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
import com.megaease.easeagent.logback.log.LogbackLogMapper;
import com.megaease.easeagent.logback.points.LoggerPoints;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(LoggerPoints.class)
public class LogbackAppenderInterceptor implements NonReentrantInterceptor, PluginConfigChangeListener {
    int collectLevel = Level.INFO.levelInt;

    @Override
    public void init(IPluginConfig config, int uniqueIndex) {
        String lv = config.getString("level");
        if (StringUtils.isNotEmpty(lv)) {
            collectLevel = Level.toLevel(lv, Level.OFF).levelInt;
        }
        config.addChangeListener(this);
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        AgentLogData log = LogbackLogMapper.INSTANCE.mapLoggingEvent(methodInfo, collectLevel, context.getConfig());
        if (log == null) {
            return;
        }
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

    @Override
    public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
        String lv = newConfig.getString("level");
        if (StringUtils.isNotEmpty(lv)) {
            collectLevel = Level.toLevel(lv, Level.OFF).levelInt;
        }
    }
}
