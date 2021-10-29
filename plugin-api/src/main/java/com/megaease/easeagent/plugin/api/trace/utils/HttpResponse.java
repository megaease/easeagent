package com.megaease.easeagent.plugin.api.trace.utils;

public interface HttpResponse {

    String method();

    String route();

    int statusCode();

    Throwable maybeError();
}
