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

package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.utils.SystemClock;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class HttpLog {

    public RequestInfo prepare(String system, String serviceName, Long beginTime, Span span, AccessLogServerInfo serverInfo) {
        RequestInfo requestInfo = prepare(system, serviceName, beginTime, serverInfo);
        if (span == null) {
            return requestInfo;
        }
        requestInfo.setTraceId(span.traceIdString());
        requestInfo.setSpanId(span.spanIdString());
        requestInfo.setParentSpanId(span.parentIdString());
        return requestInfo;
    }

    private RequestInfo prepare(String system, String serviceName, Long beginTime, AccessLogServerInfo serverInfo) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSystem(system);
        requestInfo.setService(serviceName);
        requestInfo.setHostName(HostAddress.localhost());
        requestInfo.setHostIpv4(HostAddress.getHostIpv4());
        requestInfo.setUrl(serverInfo.getMethod() + " " + serverInfo.getRequestURI());
        requestInfo.setMethod(serverInfo.getMethod());
        requestInfo.setHeaders(serverInfo.findHeaders());
        requestInfo.setBeginTime(beginTime);
        requestInfo.setQueries(serverInfo.findQueries());
        requestInfo.setClientIP(serverInfo.getClientIP());
        requestInfo.setBeginCpuTime(System.nanoTime());
        return requestInfo;
    }

    public String getLogString(RequestInfo requestInfo, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
        requestInfo.setStatusCode(serverInfo.getStatusCode());
        if (!success) {
            requestInfo.setStatusCode("500");
        }
        Long now = SystemClock.now();
        requestInfo.setTimestamp(now);
        requestInfo.setRequestTime(now - beginTime);
        requestInfo.setCpuElapsedTime(System.nanoTime() - requestInfo.getBeginCpuTime());
        requestInfo.setResponseSize(serverInfo.getResponseBufferSize());
        requestInfo.setMatchUrl(serverInfo.getMatchURL());
        List<RequestInfo> list = new ArrayList<>(1);
        list.add(requestInfo);
        String logString = JsonUtil.toJson(list);
        return logString;
    }
}
