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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.httpservlet.HttpServletPlugin;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterPoints;
import com.megaease.easeagent.plugin.tools.metrics.ServerMetric;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@AdviceTo(value = DoFilterPoints.class, qualifier = "default", plugin = HttpServletPlugin.class)
public class DoFilterMetricInterceptor extends BaseServletInterceptor {
    private static final String AFTER_MARK = DoFilterMetricInterceptor.class.getName() + "$AfterMark";
    private static volatile ServerMetric SERVER_METRIC = null;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        SERVER_METRIC = ServiceMetricRegistry.getOrCreate(config, new Tags("application", "http-request", "url"), ServerMetric.SERVICE_METRIC_SUPPLIER);
    }

    @Override
    String getAfterMark() {
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
