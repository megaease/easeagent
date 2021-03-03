package com.megaease.easeagent.core.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ServletUtils {

    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    public static void setHttpRouteAttribute(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        request.setAttribute("http.route", httpRoute != null ? httpRoute.toString() : "");
    }

    public static String getHttpRouteAttribute(HttpServletRequest request) {
        return (String) request.getAttribute("http.route");
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
        String[] pairs = httpServletRequest.getQueryString().split("&");
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
}
