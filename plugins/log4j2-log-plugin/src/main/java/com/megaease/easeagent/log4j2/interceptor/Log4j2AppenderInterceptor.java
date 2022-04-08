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
package com.megaease.easeagent.log4j2.interceptor;

import com.megaease.easeagent.log4j2.points.AbstractLoggerPoints;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.api.otlp.common.LogMapper;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.tools.loader.AgentHelperClassLoader;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.plugin.utils.common.WeakConcurrentMap;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;

@AdviceTo(AbstractLoggerPoints.class)
public class Log4j2AppenderInterceptor implements NonReentrantInterceptor {
    static WeakConcurrentMap<ClassLoader, LogMapper> logMappers = new WeakConcurrentMap<>();

    int collectLevel = Level.INFO.intLevel();

    @Override
    public void init(IPluginConfig config, int uniqueIndex) {
        String lv = config.getString("level");
        if (StringUtils.isNotEmpty(lv)) {
            collectLevel = Level.toLevel(lv, Level.OFF).intLevel();
        }
        AgentHelperClassLoader.registryUrls(this.getClass());
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ClassLoader appLoader = methodInfo.getInvoker().getClass().getClassLoader();
        LogMapper mapper = logMappers.getIfPresent(appLoader);

        if (mapper == null) {
            ClassLoader help = AgentHelperClassLoader.getClassLoader(appLoader, EaseAgent.getAgentClassLoader());
            try {
                Class<?> cls = help.loadClass("com.megaease.easeagent.log4j2.log.Log4jLogMapper");
                mapper = (LogMapper) cls.getConstructor().newInstance();
                logMappers.putIfProbablyAbsent(appLoader, mapper);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
                return;
            }
        }

        AgentLogData log = mapper.mapLoggingEvent(methodInfo, this.collectLevel);
        if (log != null) {
            EaseAgent.getAgentReport().report(log);
        }
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
