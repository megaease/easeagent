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

package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Response;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;
import java.util.function.Supplier;

public class RequestContextImpl implements RequestContext {
    private final Span span;
    private final Scope scope;
    private final AsyncRequest asyncRequest;
    private final Supplier<InitializeContext> supplier;

    public RequestContextImpl(Span span, Scope scope, AsyncRequest asyncRequest, Supplier<InitializeContext> supplier) {
        this.span = span;
        this.scope = scope;
        this.asyncRequest = asyncRequest;
        this.supplier = supplier;
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Span span() {
        return span;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public void setHeader(String name, String value) {
        asyncRequest.setHeader(name, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return asyncRequest.getHeaders();
    }

    @Override
    public Context getContext() {
        return supplier.get();
    }

    @Override
    public void finish(Response response) {
        String[] fields = ProgressFields.getResponseHoldTagFields();
        if (!ProgressFields.isEmpty(fields)) {
            for (String field : fields) {
                span.tag(field, response.header(field));
            }
        }
        span.finish();
    }
}
