package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.metric.log4j.RefreshableAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeySender {
    private final String key;
    private final AppenderManager appenderManager;
    private final MetricProps metricProps;
    private Logger logger;

    public KeySender(String key, AppenderManager appenderManager, MetricProps metricProps) {
        this.key = key;
        this.appenderManager = appenderManager;
        this.metricProps = metricProps;
    }

    public void send(String content) {
        this.lazyInitLogger();
        this.logger.info(content);
    }

    private void lazyInitLogger() {
        if (logger == null) {
            String loggerName = prepareAppenderAndLogger();
            logger = LoggerFactory.getLogger(loggerName);
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
