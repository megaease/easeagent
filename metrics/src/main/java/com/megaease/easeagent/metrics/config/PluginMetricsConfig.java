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

package com.megaease.easeagent.metrics.config;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import com.megaease.easeagent.plugin.utils.NoNull;

import static com.megaease.easeagent.plugin.api.config.Const.METRIC_DEFAULT_INTERVAL;
import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.KEY_COMM_INTERVAL;

public class PluginMetricsConfig implements MetricsConfig {
    private volatile boolean enabled;
    private volatile int interval;
    private Runnable callback;

    public PluginMetricsConfig(IPluginConfig config) {
        set(config);
        config.addChangeListener(new PluginConfigChange());
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public void setIntervalChangeCallback(Runnable runnable) {
        this.callback = runnable;
    }

    private void set(IPluginConfig config) {
        this.enabled = config.enabled();
        this.interval = NoNull.of(config.getInt(KEY_COMM_INTERVAL), METRIC_DEFAULT_INTERVAL);
    }

    class PluginConfigChange implements PluginConfigChangeListener {

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
            int oldInterval = PluginMetricsConfig.this.interval;
            set(newConfig);
            if (oldInterval != PluginMetricsConfig.this.interval) {
                callback.run();
            }
        }
    }
}
