package com.megaease.easeagent.report.sender.metric;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.MetricProps;
import com.megaease.easeagent.report.plugin.NoOpCallback;
import com.megaease.easeagent.report.sender.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.sender.metric.log4j.LoggerFactory;
import com.megaease.easeagent.report.sender.metric.log4j.RefreshableAppender;
import com.megaease.easeagent.report.util.Utils;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.util.Map;

@AutoService(Sender.class)
public class MetricKafkaSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.METRIC_SENDER_NAME;
    private static AppenderManager appenderManager;

    private OutputProperties outputProperties;
    private MetricProps props;
    private Logger logger;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        this.outputProperties = Utils.extractOutputProperties(config);
        this.props = MetricProps.newDefault(config);
        initAppenderManager();
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        lazyInitLogger();
        String msg = new String(encodedData);
        logger.info(msg);
        return new NoOpCallback<>();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        if (Utils.isOutputPropertiesChange(changes)
            && this.outputProperties.updateConfig(changes)) {
            appenderManager.refresh();
        }
    }

    @Override
    public void close() throws IOException {
        appenderManager.stop(this.props.getTopic());
    }

    private void initAppenderManager() {
        if (appenderManager != null) {
            return;
        }
        synchronized (MetricKafkaSender.class) {
            if (appenderManager != null) {
                return;
            }
            appenderManager = AppenderManager.create(this.outputProperties);
        }
    }

    private void lazyInitLogger() {
        if (logger != null) {
            return;
        }
        String loggerName = prepareAppenderAndLogger();
        logger = LoggerFactory.getLoggerContext().getLogger(loggerName);
    }

    private String prepareAppenderAndLogger() {
        RefreshableAppender build = RefreshableAppender.builder()
            .names(this.props.getName())
            .metricProps(this.props)
            .appenderManager(appenderManager)
            .build();
        return build.getLogger();
    }
}
