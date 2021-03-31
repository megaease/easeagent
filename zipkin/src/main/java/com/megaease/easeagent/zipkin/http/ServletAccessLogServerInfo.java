package com.megaease.easeagent.zipkin.http;

import com.megaease.easeagent.core.utils.ServletUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ServletAccessLogServerInfo implements AccessLogServerInfo {

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
        String matchURL = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
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
