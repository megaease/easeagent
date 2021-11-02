package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.HashMap;
import java.util.Map;

public class AsyncRequest implements Request {
    private final Request request;
    private final Map<String, String> header;

    public AsyncRequest(Request request) {
        this.request = request;
        this.header = new HashMap<>();
    }

    @Override
    public Span.Kind kind() {
        return request.kind();
    }

    @Override
    public String name() {
        return request.name();
    }

    @Override
    public String header(String name) {
        String value = request.header(name);
        return value == null ? header.get(name) : value;
    }

    @Override
    public boolean cacheScope() {
        return request.cacheScope();
    }

    @Override
    public void setHeader(String name, String value) {
        request.setHeader(name, value);
        header.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return header;
    }
}
