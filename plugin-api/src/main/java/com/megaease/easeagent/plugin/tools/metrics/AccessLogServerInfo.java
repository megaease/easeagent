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

import java.util.Map;

public interface AccessLogServerInfo {
    String getMethod();

    String getHeader(String key);

    String getRemoteAddr();

    String getRequestURI();

    int getResponseBufferSize();

    String getMatchURL();

    Map<String, String> findHeaders();

    Map<String, String> findQueries();

    String getStatusCode();

    default String getClientIP() {
        return AccessLogServerInfo.getRemoteHost(this);
    }

    static String getRemoteHost(AccessLogServerInfo serverInfo) {
        if (serverInfo == null) {
            return "unknown";
        }
        String ip = serverInfo.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = serverInfo.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = serverInfo.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = serverInfo.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
