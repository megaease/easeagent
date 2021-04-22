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

package com.megaease.easeagent.core.utils;

import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.*;

public class ServletUtils {

    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    public static String getHttpRouteAttributeFromRequest(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return httpRoute != null ? httpRoute.toString() : "";
    }

    public static String getRemoteHost(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }

    public static Map<String, String> getHeaders(HttpServletRequest httpServletRequest) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        Map<String, String> map = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = httpServletRequest.getHeader(name);
            map.put(name, value);
        }
        return map;
    }

    @SneakyThrows
    public static Map<String, List<String>> getQueries(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> map = new HashMap<>();
        String queryString = httpServletRequest.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return map;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!map.containsKey(key)) {
                map.put(key, new LinkedList<>());
            }
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            map.get(key).add(value);
        }
        return map;
    }

    public static Map<String, String> getQueries4SingleValue(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> map = getQueries(httpServletRequest);
        Map<String, String> singleValueMap = new HashMap<>();
        map.forEach((key, values) -> {
            if (values != null && values.size() > 0) {
                singleValueMap.put(key, values.get(0));
            }
        });
        return singleValueMap;
    }
}
