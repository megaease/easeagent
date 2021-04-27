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

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetricReport {
    private final Configs configs;
    private final ConcurrentHashMap<String, KeySender> typeLoggers = new ConcurrentHashMap<>();
    private final AppenderManager appenderManager;

    public MetricReport(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(new InternalListener());
    }

    public void report(MetricItem item) {
        KeySender sender = typeLoggers.computeIfAbsent(item.getKey(), this::newKeyLogger);
        sender.send(item.getContent());
    }

    private KeySender newKeyLogger(String key) {
        return new KeySender(key, this.appenderManager, Utils.extractMetricProps(configs,key));
    }


    private class InternalListener implements ConfigChangeListener {
        @Override
        public void onChange(List<ChangeItem> list) {
            this.tryRefreshAppenders(list);
        }

        private void tryRefreshAppenders(List<ChangeItem> list) {
            if (Utils.isOutputPropertiesChange(list)) {
                appenderManager.refresh();
            }
        }
    }
}
