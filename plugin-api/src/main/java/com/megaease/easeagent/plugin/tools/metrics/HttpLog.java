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
        AccessLogInfo accessLogInfo = prepare(system, serviceName, beginTime, serverInfo);
        if (span == null) {
            return accessLogInfo;
        }
        accessLogInfo.setTraceId(span.traceIdString());
        accessLogInfo.setSpanId(span.spanIdString());
        accessLogInfo.setParentSpanId(span.parentIdString());
        return accessLogInfo;
    }

    private AccessLogInfo prepare(String system, String serviceName, Long beginTime, AccessLogServerInfo serverInfo) {
        AccessLogInfo accessLogInfo = new AccessLogInfo();
        accessLogInfo.setSystem(system);
        accessLogInfo.setService(serviceName);
        accessLogInfo.setHostName(HostAddress.localhost());
        accessLogInfo.setHostIpv4(HostAddress.getHostIpv4());
        accessLogInfo.setUrl(serverInfo.getMethod() + " " + serverInfo.getRequestURI());
        accessLogInfo.setMethod(serverInfo.getMethod());
        accessLogInfo.setHeaders(serverInfo.findHeaders());
        accessLogInfo.setBeginTime(beginTime);
        accessLogInfo.setQueries(getQueries(serverInfo));
        accessLogInfo.setClientIP(serverInfo.getClientIP());
        accessLogInfo.setBeginCpuTime(System.nanoTime());
        return accessLogInfo;
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

    public String getLogString(AccessLogInfo accessLogInfo, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
        this.finish(accessLogInfo, success, beginTime, serverInfo);

        List<AccessLogInfo> list = new ArrayList<>(1);
        list.add(accessLogInfo);

        return JsonUtil.toJson(list);
    }


    public void finish(AccessLogInfo accessLogInfo, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
        accessLogInfo.setStatusCode(serverInfo.getStatusCode());
        if (!success) {
            accessLogInfo.setStatusCode("500");
        }
        long now = SystemClock.now();
        accessLogInfo.setTimestamp(now);
        accessLogInfo.setRequestTime(now - beginTime);
        accessLogInfo.setCpuElapsedTime(System.nanoTime() - accessLogInfo.getBeginCpuTime());
        accessLogInfo.setResponseSize(serverInfo.getResponseBufferSize());
        accessLogInfo.setMatchUrl(serverInfo.getMatchURL());
    }
}
