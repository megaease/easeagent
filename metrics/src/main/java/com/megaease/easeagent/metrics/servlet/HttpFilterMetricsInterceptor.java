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

package com.megaease.easeagent.metrics.servlet;

import com.megaease.easeagent.common.http.HttpServletInterceptor;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpFilterMetricsInterceptor extends HttpServletInterceptor {

    private final static String PROCESSED_BEFORE_KEY = HttpFilterMetricsInterceptor.class.getName() + ".processedBefore";

    private final static String PROCESSED_AFTER_KEY = HttpFilterMetricsInterceptor.class.getName() + ".processedAfter";

    private final ServletMetric servletMetric;

    public HttpFilterMetricsInterceptor(ServletMetric servletMetric) {
        this.servletMetric = servletMetric;
    }

    @Override
    public void internalBefore(MethodInfo methodInfo, Map<Object, Object> context, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    }

    @Override
    public void internalAfter(MethodInfo methodInfo, Map<Object, Object> context, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String httpRoute = ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        String key = httpServletRequest.getMethod() + " " + httpRoute;
        this.servletMetric.collectMetric(key, httpServletResponse.getStatus(), methodInfo.getThrowable(), context);
    }

    @Override
    public String processedBeforeKey() {
        return PROCESSED_BEFORE_KEY;
    }

    @Override
    public String processedAfterKey() {
        return PROCESSED_AFTER_KEY;
    }

}
