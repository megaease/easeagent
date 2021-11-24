/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.TraceConst;

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

