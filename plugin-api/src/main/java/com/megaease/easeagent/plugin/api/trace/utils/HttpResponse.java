package com.megaease.easeagent.plugin.api.trace.utils;

import com.megaease.easeagent.plugin.api.trace.Response;

public interface HttpResponse extends Response {

    String method();

    String route();

    int statusCode();

    Throwable maybeError();
}
