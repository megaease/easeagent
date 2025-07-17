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

import com.megaease.easeagent.plugin.tomcat.utils.ServletUtils;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TomcatAccessLogServerInfo implements AccessLogServerInfo {

    private HttpServletRequest request;
    private HttpServletResponse response;

    public void load(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getHeader(String key) {
        return request.getHeader(key);
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public int getResponseBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public String getMatchURL() {
        String matchURL = ServletUtils.matchUrlBySpringWeb(request);
        if (StringUtils.isEmpty(matchURL)) {
            return "";
        }
        return request.getMethod() + " " + matchURL;
    }

    @Override
    public Map<String, String> findHeaders() {
        Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String key = headerNames.nextElement();
            headers.put(key, request.getHeader(key));
        }
        return headers;
    }

    @Override
    public Map<String, String> findQueries() {
        return ServletUtils.getQueries4SingleValue(request);
    }

    @Override
    public String getStatusCode() {
        return String.valueOf(response.getStatus());
    }

}
