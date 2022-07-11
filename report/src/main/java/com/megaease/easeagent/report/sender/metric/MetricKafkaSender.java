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
package com.megaease.easeagent.report.sender.metric;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.MetricProps;
import com.megaease.easeagent.report.plugin.NoOpCall;
import com.megaease.easeagent.report.sender.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.sender.metric.log4j.LoggerFactory;
import com.megaease.easeagent.report.sender.metric.log4j.RefreshableAppender;
import com.megaease.easeagent.report.util.Utils;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.util.Map;

@AutoService(Sender.class)
public class MetricKafkaSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.METRIC_KAFKA_SENDER_NAME;
    private static volatile AppenderManager appenderManager;

    private OutputProperties outputProperties;
    private MetricProps props;
    private Logger logger;

    private String prefix;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config, String prefix) {
        this.prefix = prefix;
        this.outputProperties = Utils.extractOutputProperties(config);
        this.props = MetricProps.newDefault(config, prefix);
        initAppenderManager();
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        lazyInitLogger();
        String msg = new String(encodedData.getData());
        logger.info(msg);
        return new NoOpCall<>();
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
        // check topic
        Map<String, String> cfg = this.props.asReportConfig().getConfigs();
        cfg.putAll(changes);
        MetricProps nProps = MetricProps.newDefault(new GlobalConfigs(cfg), this.prefix);
        if (!nProps.getTopic().equals(this.props.getTopic())) {
            try {
                this.close();
            } catch (IOException e) {
                // ignored
            }
            this.props = nProps;
            this.logger = null;
            lazyInitLogger();
        }
        // check enabled
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
