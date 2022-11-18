package com.megaease.easeagent.plugin.motan.interceptor.trace.provider;

import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;

public class MotanProviderRequest implements Request {

    private final com.weibo.api.motan.rpc.Request request;

    public MotanProviderRequest(com.weibo.api.motan.rpc.Request request) {
        this.request = request;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.SERVER;
    }

    @Override
    public String header(String name) {
        return request.getAttachments().get(name);
    }

    @Override
    public String name() {
        return MotanCtxUtils.name(request);
    }

    @Override
    public boolean cacheScope() {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {
        request.getAttachments().put(name, value);
    }
}
