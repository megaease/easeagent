package com.megaease.easeagent.zipkin.servlet;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import java.util.Map;

public class HttpFilterLogInterceptor implements AgentInterceptor {
    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {

    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {

    }
}
