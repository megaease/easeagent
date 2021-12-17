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

package com.megaease.easeagent.plugin.httpservlet.utils;

import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.httpservlet.interceptor.DoFilterTraceInterceptor;
import com.megaease.easeagent.plugin.utils.ClassUtils;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServletUtils {
    public static final Logger LOGGER = EaseAgent.getLogger(ServletUtils.class);
    public static final String START_TIME = ServletUtils.class.getName() + "$StartTime";
    public static final String PROGRESS_CONTEXT = DoFilterTraceInterceptor.class.getName() + ".RequestContext";
    public static final String HANDLER_MAPPING_CLASS = "org.springframework.web.servlet.HandlerMapping";
    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE;

    static {
        String pattern = null;
        if (ClassUtils.hasClass(HANDLER_MAPPING_CLASS)) {
            pattern = SpringWebUtils.getBestMatchingPatternAttribute();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("class<{}> not found ", HANDLER_MAPPING_CLASS);
            }
            pattern = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
        }
        BEST_MATCHING_PATTERN_ATTRIBUTE = pattern;

    }

    public static String matchUrlBySpringWeb(HttpServletRequest request) {
        return (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    public static String getHttpRouteAttributeFromRequest(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return httpRoute != null ? httpRoute.toString() : "";
    }

    public static boolean markProcessed(HttpServletRequest request, String mark) {
        if (request.getAttribute(mark) != null) {
            return true;
        }
        request.setAttribute(mark, "m");
        return false;
    }

    public static long startTime(HttpServletRequest httpServletRequest) {
        Object startObj = httpServletRequest.getAttribute(START_TIME);
        Long start = null;
        if (startObj == null) {
            start = System.currentTimeMillis();
            httpServletRequest.setAttribute(START_TIME, start);
        } else {
            start = (Long) startObj;
        }
        return start;
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
