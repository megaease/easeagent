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

package com.megaease.easeagent.plugin.simple.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.AbstractMetric;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.simple.HttpServletPlugin;
import com.megaease.easeagent.plugin.simple.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.tools.metrics.ServerMetric;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default", plugin = HttpServletPlugin.class)
public class DoFilterMetricInterceptor extends BaseServletInterceptor {
    private static final String AFTER_MARK = DoFilterMetricInterceptor.class.getName() + "$AfterMark";
    private static volatile ServerMetric SERVER_METRIC = null;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        SERVER_METRIC = AbstractMetric.getInstance(config,
            new Tags("application", "http-request", "url"),
            (config1, tags) -> new ServerMetric(config1, tags));
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
    public String getName() {
        return Order.METRIC.getName();
    }
}
