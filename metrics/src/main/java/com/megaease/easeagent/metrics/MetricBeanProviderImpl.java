/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.metrics;

import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandlerProvider;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.MetricProvider;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.bean.BeanProvider;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;

import java.util.ArrayList;
import java.util.List;

public class MetricBeanProviderImpl implements BeanProvider, AgentHttpHandlerProvider, ConfigAware,
    MetricProvider, AgentReportAware {
    private final MetricProviderImpl metricProvider = new MetricProviderImpl();

    @Override
    public List<AgentHttpHandler> getAgentHttpHandlers() {
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new PrometheusAgentHttpHandler());
        return list;

    }

    @Override
    public void setConfig(Config config) {
        this.metricProvider.setConfig(config);
    }

    @Override
    public MetricRegistrySupplier metricSupplier() {
        return metricProvider.metricSupplier();
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.metricProvider.setAgentReport(report);
    }

    public MetricProviderImpl getMetricProvider() {
        return metricProvider;
    }
}
