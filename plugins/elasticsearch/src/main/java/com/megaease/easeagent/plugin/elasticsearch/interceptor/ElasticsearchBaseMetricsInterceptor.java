/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricSupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.enums.Order;

public abstract class ElasticsearchBaseMetricsInterceptor extends ElasticsearchBaseInterceptor {

    protected ElasticsearchMetric elasticsearchMetric;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        super.init(config, className, methodName, methodDescriptor);
        Tags tags = new Tags("application", "elasticsearch", "index");
        RedirectProcessor.setTagsIfRedirected(Redirect.ELASTICSEARCH, tags);
        this.elasticsearchMetric = ServiceMetricRegistry.getOrCreate(config, tags, new ServiceMetricSupplier<ElasticsearchMetric>() {
            @Override
            public NameFactory newNameFactory() {
                return ElasticsearchMetric.nameFactory();
            }

            @Override
            public ElasticsearchMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                return new ElasticsearchMetric(metricRegistry, nameFactory);
            }
        });
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }

    public ElasticsearchMetric getElasticsearchMetric() {
        return elasticsearchMetric;
    }
}
