package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;

public class MotanConsumerRequest implements Request {
    private com.weibo.api.motan.rpc.Request request;

    public MotanConsumerRequest(com.weibo.api.motan.rpc.Request request) {
        this.request = request;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.CLIENT;
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
