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

package com.megaease.easeagent.log4j2.log;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogDataImpl;
import com.megaease.easeagent.plugin.api.otlp.common.LogMapper;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.utils.SystemClock;
import io.opentelemetry.sdk.logs.data.Severity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;

import java.util.Map;

/**
 * reference to Opentelemetry instrumentation
 */
public class Log4jLogMapper implements LogMapper {
    private static final String SPECIAL_MAP_MESSAGE_ATTRIBUTE = "message";

    public AgentLogData mapLoggingEvent(MethodInfo logInfo, int levelInt, IPluginConfig config) {
        Object[] args = logInfo.getArgs();

        if (args == null) {
            return null;
        }

        AgentLogDataImpl.Builder builder = AgentLogDataImpl.builder();

        for (int i = 0; i < args.length; i++) {
            switch (i) {
                case 0:
                    // level
                    Level level = (Level)args[i];
                    if (level.intLevel() < levelInt) {
                        return null;
                    }
                    builder.severity(levelToSeverity(level));
                    builder.severityText(level.name());
                    break;
                case 4:
                    // message
                    Message message = (Message)args[i];
                    if (!(message instanceof MapMessage)) {
                        builder.body(message.getFormattedMessage());
                    } else {
                        MapMessage<?, ?> mapMessage = (MapMessage<?, ?>) message;

                        String body = mapMessage.getFormat();
                        boolean checkSpecialMapMessageAttribute = (body == null || body.isEmpty());
                        if (checkSpecialMapMessageAttribute) {
                            body = mapMessage.get(SPECIAL_MAP_MESSAGE_ATTRIBUTE);
                        }
                        if (body != null && !body.isEmpty()) {
                            builder.body(body);
                        }
                    }
                    break;
                case 5:
                    // throwable
                    Throwable throwable = (Throwable) args[5];
                    if (throwable != null) {
                        builder.throwable(throwable);
                    }
                    break;
                default:
                    break;
            }
        }

        // logger
        Logger logger = (Logger)logInfo.getInvoker();
        if (logger.getName() == null || logger.getName().isEmpty()) {
            builder.logger("ROOT");
        } else {
            builder.logger(logger.getName());
        }

        // thread
        builder.thread(Thread.currentThread());
        builder.epochMills(SystemClock.now());

        // MDC
        Map<String, String> contextData = ThreadContext.getImmutableContext();
        builder.contextData(config.getStringList(LogMapper.MDC_KEYS), contextData);

        // span context
        builder.spanContext();

        return builder.build();
    }

    private static Severity levelToSeverity(Level level) {
        switch (level.getStandardLevel()) {
            case ALL:
            case TRACE:
                return Severity.TRACE;
            case DEBUG:
                return Severity.DEBUG;
            case INFO:
                return Severity.INFO;
            case WARN:
                return Severity.WARN;
            case ERROR:
                return Severity.ERROR;
            case FATAL:
                return Severity.FATAL;
            case OFF:
                return Severity.UNDEFINED_SEVERITY_NUMBER;
        }
        return Severity.UNDEFINED_SEVERITY_NUMBER;
    }
}
