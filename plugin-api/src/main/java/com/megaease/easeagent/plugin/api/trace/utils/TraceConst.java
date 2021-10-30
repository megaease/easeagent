package com.megaease.easeagent.plugin.api.trace.utils;

public interface TraceConst {
    String HTTP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    String HTTP_ATTRIBUTE_ROUTE = "http.route";
    String HTTP_TAG_ROUTE = HTTP_ATTRIBUTE_ROUTE;
    String HTTP_TAG_METHOD = "http.method";
    String HTTP_TAG_PATH = "http.path";
    String HTTP_TAG_STATUS_CODE = "http.status_code";
    String HTTP_TAG_ERROR = "error";
}
