package com.megaease.easeagent.plugin.api.trace.utils;

import com.megaease.easeagent.plugin.api.trace.Request;

public interface HttpRequest extends Request {
    @Override
    default String name() {
        return method();
    }

    String method();

    String path();

    String route();

    String getRemoteAddr();

    int getRemotePort();

    String getRemoteHost();
}
