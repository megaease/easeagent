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

import com.megaease.easeagent.plugin.api.trace.Setter;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import okhttp3.Request;

public class ForwardedRequest implements Setter {
    private final Object realCall;
    private Request.Builder requestBuilder;

    public ForwardedRequest(Object realCall) {
        this.realCall = realCall;
    }

    private Request.Builder builder() {
        if (requestBuilder == null) {
            Request originalRequest = AgentFieldReflectAccessor.getFieldValue(realCall, "originalRequest");
            if (originalRequest == null) {
                return null;
            }
            requestBuilder = originalRequest.newBuilder();
        }
        return requestBuilder;
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public void setHeader(String name, String value) {
        Request.Builder builder = builder();
        if (builder == null) {
            return;
        }
        builder.addHeader(name, value);
    }
}
