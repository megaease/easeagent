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

package com.megaease.easeagent.plugin.tomcat.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.tomcat.TomcatPlugin;
import com.megaease.easeagent.plugin.tomcat.advice.FilterChainPoints;
import com.megaease.easeagent.plugin.tools.metrics.ServerMetric;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@AdviceTo(value = FilterChainPoints.class, qualifier = "default", plugin = TomcatPlugin.class)
public class FilterChainMetricInterceptor extends BaseServletInterceptor {
    private static final String AFTER_MARK = FilterChainMetricInterceptor.class.getName() + "$AfterMark";
    private static volatile ServerMetric SERVER_METRIC = null;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        SERVER_METRIC = ServiceMetricRegistry.getOrCreate(config, new Tags("application", "http-request", "url"), ServerMetric.SERVICE_METRIC_SUPPLIER);
    }

    @Override
    protected String getAfterMark() {
        return AFTER_MARK;
    }

    @Override
    public void internalAfter(Throwable throwable, String key, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long start) {
        long end = System.currentTimeMillis();
        SERVER_METRIC.collectMetric(key, httpServletResponse.getStatus(), throwable, start, end);
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
