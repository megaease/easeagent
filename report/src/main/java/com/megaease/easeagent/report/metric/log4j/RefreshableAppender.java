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

package com.megaease.easeagent.report.metric.log4j;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.report.metric.MetricProps;
import com.megaease.easeagent.report.util.TextUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDisruptor;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.function.Consumer;

/**
 * The {@link org.apache.logging.log4j.core.Appender} is log4j object which is
 * responsible for delivering LogEvents to their destination.
 * RefreshableAppender is dedicated to maintaining the
 * {@link org.apache.logging.log4j.core.Appender} for hot updates.
 * RefreshableAppender provides implementations that include update
 * {@link org.apache.logging.log4j.core.Appender} Appender's type, Kafka
 * bootstrap server address, Kafka topic, etc dynamically.
 *
 * @author wanglei
 * @version v1.0.1
 * @since 2020/2/23 09:38
 */
public interface RefreshableAppender extends TestableAppender {

    static Builder builder() {
        return new Builder();
    }

    /**
     * Fetching logger name corresponded to current
     * {@code org.apache.logging.log4j.core.Appender}
     *
     * @return a name of logger
     */
    String getLogger();


    class DefaultRefreshableAppender implements RefreshableAppender {

        private static final Logger LOGGER = com.megaease.easeagent.log4j2.LoggerFactory.getLogger(DefaultRefreshableAppender.class);

        private final String loggerName;
        private final String appenderName;
        protected final MetricRefreshableAppender delegate;
        private final AppenderManager appenderManager;

        DefaultRefreshableAppender(
            String appender,
            String loggerName,
            AppenderManager appenderManager,
            MetricProps metricProps) {
            this.loggerName = loggerName;
            this.appenderName = appender;
            this.appenderManager = appenderManager;
            LoggerContext context = LoggerFactory.getLoggerContext();
            // start disruptor synchronized thread
            startAsyncDisruptor(context);
            AppenderRef[] appenderRefs = forAppenderRefs();
            LoggerConfig logger = createLogger(loggerName, context, appenderRefs);
            delegate = newDelegate(context, metricProps);
            if (delegate != null) {
                //shouldn't be null always
                logger.addAppender(delegate, Level.INFO, null);
            }
            context.getConfiguration().addLogger(loggerName, logger);
            context.updateLoggers();
        }

        private void startAsyncDisruptor(LoggerContext context) {
            AsyncLoggerConfigDisruptor asyncLoggerConfigDelegate = (AsyncLoggerConfigDisruptor) context.getConfiguration().getAsyncLoggerConfigDelegate();
            if (!asyncLoggerConfigDelegate.isStarted() && !asyncLoggerConfigDelegate.isStarting()) {
                asyncLoggerConfigDelegate.start();
            }
        }

        private AppenderRef[] forAppenderRefs() {
            return new AppenderRef[]{AppenderRef.createAppenderRef(appenderName, Level.INFO, null)};
        }

        private LoggerConfig createLogger(String loggerName, LoggerContext ctx, AppenderRef[] refs) {
            return AsyncLoggerConfig.createLogger(false, Level.INFO, loggerName,
                "true", refs, null, ctx.getConfiguration(), null);
        }

        private MetricRefreshableAppender newDelegate(LoggerContext context, MetricProps metricProps) {
            try {
                MetricRefreshableAppender metricRefreshableAppender = new MetricRefreshableAppender(this.appenderName,
                    metricProps,
                    context.getConfiguration(),
                    appenderManager);
                metricRefreshableAppender.start();
                return metricRefreshableAppender;
            } catch (Exception e) {
                LOGGER.warn("new refreshable appender failed + [" + e.getMessage() + "]");
            }
            return null;
        }

        @Override
        public String getLogger() {
            return loggerName;
        }


        @Override
        public void setTestAppender(Consumer<LogEvent> logEventConsumer) {
            this.delegate.setTestAppender(logEventConsumer);
        }
    }

    class Builder {
        private String appender;
        private String loggerName;
        private AppenderManager appenderManager;
        private MetricProps metricProps;

        public Builder names(String prefix) {
            if (TextUtils.isEmpty(prefix)) {
                prefix = "DefaultPrefix";
            }
            this.appender = prefix.concat("MetricAppender");
            this.loggerName = prefix;
            return this;
        }


        public Builder appenderManager(AppenderManager manager) {
            this.appenderManager = manager;
            return this;
        }

        public Builder metricProps(MetricProps metricProps) {
            this.metricProps = metricProps;
            return this;
        }

        public RefreshableAppender build() {
            if (TextUtils.isEmpty(appender) || TextUtils.isEmpty(loggerName) || appenderManager == null) {
                throw new IllegalArgumentException("appender, loggerName must be a unique name, kafkaAppenderManager can't be null");
            }
            return new DefaultRefreshableAppender(
                appender,
                loggerName,
                appenderManager, metricProps);
        }
    }

}
