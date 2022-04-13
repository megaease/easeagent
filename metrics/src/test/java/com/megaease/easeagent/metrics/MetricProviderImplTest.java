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

import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetricProviderImplTest {
    public static MetricProviderImpl METRIC_PROVIDER;

    static {
        METRIC_PROVIDER = new MetricProviderImpl();
        METRIC_PROVIDER.setConfig(MockConfig.getCONFIGS());
        METRIC_PROVIDER.setAgentReport(new AgentReport() {
            @Override
            public void report(ReportSpan span) {
                // skip
            }

            @Override
            public void report(AccessLogInfo log) {
                // skip
            }

            @Override
            public void report(AgentLogData log) {
                // skip
            }

            @Override
            public MetricReporterFactory metricReporter() {
                return new MetricReporterFactory() {
                    @Override
                    public Reporter reporter(IPluginConfig config) {
                        return new Reporter() {
                            @Override
                            public void report(String msg) {

                            }

                            @Override
                            public void report(EncodedData msg) {

                            }
                        };
                    }
                };
            }
        });
    }

    @Test
    public void setConfig() {
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(METRIC_PROVIDER, "config"));
    }

    @Test
    public void setAgentReport() {
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(METRIC_PROVIDER, "agentReport"));
    }

    @Test
    public void metricSupplier() {
        MetricRegistrySupplier metricRegistrySupplier = METRIC_PROVIDER.metricSupplier();
        assertNotNull(metricRegistrySupplier);
    }
}
