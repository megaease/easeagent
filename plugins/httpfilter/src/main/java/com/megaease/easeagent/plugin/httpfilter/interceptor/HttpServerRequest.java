package com.megaease.easeagent.plugin.httpfilter.interceptor;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.utils.HttpRequest;
import com.megaease.easeagent.plugin.api.trace.utils.TraceConst;

import javax.servlet.http.HttpServletRequest;

public class HttpServerRequest implements HttpRequest {
    protected final HttpServletRequest delegate;

    public HttpServerRequest(HttpServletRequest httpServletRequest) {
        this.delegate = httpServletRequest;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.SERVER;
    }

    @Override
    public String method() {
        return delegate.getMethod();
    }

    @Override
    public String path() {
        return delegate.getRequestURI();
    }

    @Override
    public String route() {
        Object maybeRoute = this.delegate.getAttribute(TraceConst.HTTP_ATTRIBUTE_ROUTE);
        return maybeRoute instanceof String ? (String) maybeRoute : null;
    }

    @Override
    public String getRemoteAddr() {
        return this.delegate.getRemoteAddr();
    }

    @Override
    public int getRemotePort() {
        return this.delegate.getRemotePort();
    }

    @Override
    public String getRemoteHost() {
        return this.delegate.getRemoteHost();
    }

    @Override
    public String header(String name) {
        return this.delegate.getHeader(name);
    }

    @Override
    public boolean cacheScope() {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {
//            this.delegate.setAttribute(name, value);
    }
}

