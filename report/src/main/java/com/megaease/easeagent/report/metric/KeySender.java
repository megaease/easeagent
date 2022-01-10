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

package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.metric.log4j.LoggerFactory;
import com.megaease.easeagent.report.metric.log4j.RefreshableAppender;
import org.apache.logging.log4j.core.Logger;

public class KeySender {
    private static final String CONSOLE_APPEND = "console";
    private final String key;
    private final AppenderManager appenderManager;
    private final MetricProps metricProps;
    private Logger logger;
    private org.slf4j.Logger consoleLogger;
    private boolean isConsole = false;

    public KeySender(String key, AppenderManager appenderManager, MetricProps metricProps) {
        this.key = key;
        this.appenderManager = appenderManager;
        this.metricProps = metricProps;
    }

    public void send(String content) {
        this.lazyInitLogger();
        if (this.isConsole) {
            this.consoleLogger.info(content);
        } else {
            this.logger.info(content);
        }
    }

    private void lazyInitLogger() {
        if (logger != null) {
            return;
        }

        String loggerName = prepareAppenderAndLogger();
        if (metricProps.getAppendType().equals(CONSOLE_APPEND)) {
            this.isConsole = true;
            this.consoleLogger = org.slf4j.LoggerFactory.getLogger(loggerName);
        } else {
            logger = LoggerFactory.getLoggerContext().getLogger(loggerName);
        }
    }

    private String prepareAppenderAndLogger() {
        RefreshableAppender build = RefreshableAppender.builder()
                .names(this.key)
                .metricProps(this.metricProps)
                .appenderManager(this.appenderManager)
                .build();
        return build.getLogger();
    }
}
