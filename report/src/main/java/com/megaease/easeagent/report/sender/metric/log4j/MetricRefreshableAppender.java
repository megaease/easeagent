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

package com.megaease.easeagent.report.sender.metric.log4j;

import com.megaease.easeagent.report.metric.MetricProps;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@code MetricRefreshableAppender} is a lazy kafka appender  and can
 * be refreshed when kafka topic or bootstrap servers' address changed.
 * It has a {@link ConsoleAppender} and mock appender, when appender's
 * type is changed, it will not close kafka appender or create new
 * console appender. The default {@link ConsoleAppender} will be reused.
 * <p/>
 * We introduce this class for avoiding rebuild {@link KafkaAppender}
 * frequently when configuration changed the. KafkaAppender should be
 * rebuild only when the bootstrap server's address or topic changed.
 *
 * @author Kun Zhao
 * @version v1.0.1
 * @since v1.0.1
 */
public class MetricRefreshableAppender extends AbstractAppender implements TestableAppender {

    private final MetricProps metricProps;
    private final Configuration configuration;
    Consumer<LogEvent> logEventConsumer;
    private Appender console;
    private Appender mock;
    private final AppenderManager appenderManager;

    MetricRefreshableAppender(final String name,
                              final MetricProps metricProps,
                              final Configuration configuration,
                              final AppenderManager appenderManager
    ) {
        super(name, null, null, true, null);
        this.metricProps = metricProps;
        this.appenderManager = appenderManager;
        this.configuration = configuration;
        this.getConsoleAppender();
    }


    @Override
    public void append(LogEvent event) {
        if (!metricProps.isEnabled()) {
            return;
        }
        Optional.ofNullable(getAppender()).ifPresent(a -> a.append(event));
    }

    private Appender getAppender() {
        switch (metricProps.getSenderName()) {
            case "mock":
                return getMockAppender();
            default:
                return getKafkaAppender(metricProps.getTopic());
        }
    }

    private Appender getKafkaAppender(String topic) {
        return Optional.ofNullable(appenderManager.appender(topic)).orElse(getConsoleAppender());
    }

    private Appender getConsoleAppender() {
        if (console != null) {
            return console;
        }

        console = ConsoleAppender.newBuilder()
                .setConfiguration(configuration)
                .setLayout(this.getLayout())
                .setName(this.getName() + "_console")
                .build();
        console.start();
        return console;
    }


    @Override
    public void setTestAppender(Consumer<LogEvent> consumer) {
        this.logEventConsumer = consumer;
    }

    private Appender getMockAppender() {
        if (mock != null) {
            return mock;
        }
        mock = new AbstractAppender(this.getName() + "_mock", null, PatternLayout.createDefaultLayout(), true, null) {
            @Override
            public void append(LogEvent event) {
                Optional.ofNullable(logEventConsumer).ifPresent(l -> l.accept(event));
            }
        };
        return mock;
    }

}
