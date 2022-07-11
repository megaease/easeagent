/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.okhttp.interceptor;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import okhttp3.Request;

public class InternalRequest implements HttpRequest {

    private final Request originalRequest;
    private final Request.Builder requestBuilder;

    public InternalRequest(Request originalRequest, Request.Builder requestBuilder) {
        this.originalRequest = originalRequest;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public String method() {
        return originalRequest.method();
    }

    @Override
    public String path() {
        return originalRequest.url().uri().toString();
    }

    @Override
    public String route() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.CLIENT;
    }

    @Override
    public String header(String name) {
        return originalRequest.header(name);
    }

    @Override
    public boolean cacheScope() {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {
        requestBuilder.addHeader(name, value);
    }
}
