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

package com.megaease.easeagent.mock.plugin.api.demo;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricSupplier;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.ImmutableMap;

import javax.annotation.Nonnull;

public class M1MetricCollect {
    static final M1Metric m1Metric;

    static {
        IPluginConfig config = EaseAgent.getConfig("observability", "collectM1", ConfigConst.PluginID.METRIC);
        Tags tags = new Tags("application", "http-request", "url").put("city", "beijing");
        ServiceMetricSupplier<M1Metric> m1MetricSupplier = new ServiceMetricSupplier<M1Metric>() {
            @Override
            public NameFactory newNameFactory() {
                return NameFactory.createBuilder()
                    .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
                        .build())
                    .build();
            }

            @Override
            public M1Metric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                return new M1Metric(metricRegistry, nameFactory);
            }
        };

        m1Metric = EaseAgent.getOrCreateServiceMetric(config, tags, m1MetricSupplier);
    }

    public void collectM1() {
        String url = "GET /web_client";
        for (int i = 0; i < 100; i++) {
            m1Metric.collectMetric(url);
        }
    }

    public static class M1Metric extends ServiceMetric {

        public M1Metric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
            super(metricRegistry, nameFactory);
        }

        public void collectMetric(String key) {
            final Meter meter = meter(key, MetricSubType.DEFAULT);
            meter.mark();
        }
    }
}
