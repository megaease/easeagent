package com.megaease.easeagent.httpserver;

import java.util.Map;

public interface IHttpHandler {
    String getPath();

    HttpResponse process(HttpRequest request, Map<String, String> uriParams);

    default int priority() {
        return 100;
    }
}
