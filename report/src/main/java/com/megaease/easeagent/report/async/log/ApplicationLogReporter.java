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
package com.megaease.easeagent.report.async.log;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.async.AsyncProps;
import com.megaease.easeagent.report.async.AsyncReporter;
import com.megaease.easeagent.report.async.DefaultAsyncReporter;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import com.megaease.easeagent.report.sender.SenderWithEncoder;

import io.opentelemetry.sdk.logs.data.LogData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@SuppressWarnings("unused")
public class ApplicationLogReporter implements ConfigChangeListener {
    Config config;
    AsyncReporter<LogData> asyncReporter;

    public ApplicationLogReporter(Config configs) {
        Map<String, String> cfg = ConfigUtils.extractByPrefix(configs.getConfigs(), LOGS);
        cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), OUTPUT_SERVER_V2));
        cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ASYNC));
        this.config = new Configs(cfg);
        configs.addChangeListener(this);

        SenderWithEncoder sender = ReporterRegistry.getSender(ReportConfigConst.LOG_SENDER, configs);
        AsyncProps asyncProperties = new LogAsyncProps(this.config, null);
        this.asyncReporter = DefaultAsyncReporter.builderAsyncReporter(sender, asyncProperties);
        this.asyncReporter.startFlushThread();
    }

    public void report(LogData log) {
        this.asyncReporter.report(log);
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        Map<String, String> changes = filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }
        this.config.updateConfigs(changes);
        this.refresh(this.config.getConfigs());
    }

    private Map<String, String> filterChanges(List<ChangeItem> list) {
        Map<String, String> cfg = new HashMap<>();
        list.stream()
            .filter(item -> item.getFullName().startsWith(LOGS)
                || item.getFullName().startsWith(OUTPUT_SERVER_V2))
            .forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));
        return cfg;
    }

    public synchronized void refresh(Map<String, String> cfg) {
        String name = cfg.get(LOG_ACCESS_SENDER_NAME);
        SenderWithEncoder sender = asyncReporter.getSender();
        if (sender != null) {
            if (StringUtils.isNotEmpty(name) && !sender.name().equals(name)) {
                try {
                    sender.close();
                } catch (Exception ignored) {
                    // ignored
                }
                sender = ReporterRegistry.getSender(LOG_SENDER, this.config);
                asyncReporter.setSender(sender);
            }
        } else {
            sender = ReporterRegistry.getSender(LOG_SENDER, this.config);
            asyncReporter.setSender(sender);
        }

        AsyncProps asyncProperties = new LogAsyncProps(this.config, null);
        asyncReporter.closeFlushThread();
        asyncReporter.setPending(asyncProperties.getQueuedMaxItems(), asyncProperties.getQueuedMaxSize());
        asyncReporter.setMessageTimeoutNanos(messageTimeout(asyncProperties.getMessageTimeout()));
        asyncReporter.startFlushThread(); // start thread
    }

    protected long messageTimeout(long timeout) {
        if (timeout < 0) {
            timeout = 1000L;
        }
        return TimeUnit.MILLISECONDS.toNanos(timeout);
    }
}
