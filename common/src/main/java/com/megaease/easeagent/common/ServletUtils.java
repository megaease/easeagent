package com.megaease.easeagent.common;

import javax.servlet.http.HttpServletRequest;

public class ServletUtils {

    public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    public static void setHttpRouteAttribute(HttpServletRequest request) {
        Object httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        request.setAttribute("http.route", httpRoute != null ? httpRoute.toString() : "");
    }

    public static String getHttpRouteAttribute(HttpServletRequest request) {
        return (String) request.getAttribute("http.route");
    }
}
