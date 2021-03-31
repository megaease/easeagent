package com.megaease.easeagent.zipkin.http;

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
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
