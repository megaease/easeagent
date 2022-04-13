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

import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.utils.SystemClock;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpLog {

    public AccessLogInfo prepare(String system, String serviceName, Long beginTime, Span span, AccessLogServerInfo serverInfo) {
        AccessLogInfo accessLog = prepare(system, serviceName, beginTime, serverInfo);
        if (span == null) {
            return accessLog;
        }
        accessLog.setTraceId(span.traceIdString());
        accessLog.setSpanId(span.spanIdString());
        accessLog.setParentSpanId(span.parentIdString());
        return accessLog;
    }

    private AccessLogInfo prepare(String system, String serviceName, Long beginTime, AccessLogServerInfo serverInfo) {
        AccessLogInfo accessLog = new AccessLogInfo();
        accessLog.setSystem(system);
        accessLog.setService(serviceName);
        accessLog.setHostName(HostAddress.localhost());
        accessLog.setHostIpv4(HostAddress.getHostIpv4());
        accessLog.setUrl(serverInfo.getMethod() + " " + serverInfo.getRequestURI());
        accessLog.setMethod(serverInfo.getMethod());
        accessLog.setHeaders(serverInfo.findHeaders());
        accessLog.setBeginTime(beginTime);
        accessLog.setQueries(getQueries(serverInfo));
        accessLog.setClientIP(serverInfo.getClientIP());
        accessLog.setBeginCpuTime(System.nanoTime());
        return accessLog;
    }

    private Map<String, String> getQueries(AccessLogServerInfo serverInfo) {
        Map<String, String> serviceTags = ProgressFields.getServiceTags();
        Map<String, String> meshTags = RedirectProcessor.tags();
        if (serviceTags.isEmpty() && meshTags.isEmpty()) {
            return serverInfo.findQueries();
        }
        Map<String, String> queries = new HashMap<>(meshTags);
        queries.putAll(serviceTags);
        queries.putAll(serverInfo.findQueries());
        return queries;
    }

    public String getLogString(AccessLogInfo accessLog, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
        this.finish(accessLog, success, beginTime, serverInfo);

        List<AccessLogInfo> list = new ArrayList<>(1);
        list.add(accessLog);

        return JsonUtil.toJson(list);
    }


    public void finish(AccessLogInfo accessLog, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
        accessLog.setStatusCode(serverInfo.getStatusCode());
        if (!success) {
            accessLog.setStatusCode("500");
        }
        long now = SystemClock.now();
        accessLog.setTimestamp(now);
        accessLog.setRequestTime(now - beginTime);
        accessLog.setCpuElapsedTime(System.nanoTime() - accessLog.getBeginCpuTime());
        accessLog.setResponseSize(serverInfo.getResponseBufferSize());
        accessLog.setMatchUrl(serverInfo.getMatchURL());
    }
}
