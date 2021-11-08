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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.httpservlet.utils.InternalAsyncListener;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.utils.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.utils.metrics.ServerMetric;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterMetricInterceptor implements FirstEnterInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(DoFilterMetricInterceptor.class);
    private static final String AFTER_MARK = DoFilterMetricInterceptor.class.getName() + "$AfterMark";
    private static final Object START = new Object();
    private static final NameFactory NAME_FACTORY = ServerMetric.buildNameFactory();
    private static volatile ServerMetric SERVER_METRIC = null;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        if (SERVER_METRIC != null) {
            return;
        }
        synchronized (DoFilterMetricInterceptor.class) {
            if (SERVER_METRIC != null) {
                return;
            }
            Tags tags = new Tags("application", "http-request", "url");
            MetricRegistry metricRegistry = EaseAgent.newMetricRegistry(config, NAME_FACTORY, tags);
            SERVER_METRIC = new ServerMetric(metricRegistry, NAME_FACTORY);
        }

    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        final long start = context.remove(START);
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        if (ServletUtils.markProcessedAfter(httpServletRequest, AFTER_MARK)) {
            return;
        }
        String httpRoute = ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        final String key = httpServletRequest.getMethod() + " " + httpRoute;
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        if (methodInfo.getThrowable() != null) {
            internalAfter(methodInfo.getThrowable(), key, httpServletResponse, start);
        } else if (httpServletRequest.isAsyncStarted()) {
            httpServletRequest.getAsyncContext().addListener(new InternalAsyncListener(
                    asyncEvent -> {
                        HttpServletResponse suppliedResponse = (HttpServletResponse) asyncEvent.getSuppliedResponse();
                        internalAfter(asyncEvent.getThrowable(), key, suppliedResponse, start);
                    }

                )
            );
        } else {
            internalAfter(null, key, httpServletResponse, start);
        }
    }

    private void internalAfter(Throwable throwable, String key, HttpServletResponse httpServletResponse, long start) {
        long end = System.currentTimeMillis();
        SERVER_METRIC.collectMetric(key, httpServletResponse.getStatus(), throwable, start, end);
    }

    @Override
    public String getName() {
        return Order.METRIC.getName();
    }
}
