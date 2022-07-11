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

import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import okhttp3.Response;

public class InternalResponse implements HttpResponse {
    private final Throwable caught;
    private final String method;
    private final Response response;

    public InternalResponse(Throwable caught, String method, Response response) {
        this.caught = caught;
        this.method = method;
        this.response = response;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String route() {
        return null;
    }

    @Override
    public int statusCode() {
        return this.response.code();
    }

    @Override
    public Throwable maybeError() {
        return caught;
    }

    @Override
    public String header(String name) {
        return response.header(name);
    }
}
