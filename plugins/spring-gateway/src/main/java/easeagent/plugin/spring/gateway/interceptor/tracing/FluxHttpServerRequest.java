/*
 * Copyright (c) 2021, MegaEase
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

package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class FluxHttpServerRequest implements HttpRequest {
    private final ServerHttpRequest request;

    public FluxHttpServerRequest(ServerHttpRequest request) {
        this.request = request;
    }

    @Override
    public Span.Kind kind() {
        return Span.Kind.CLIENT;
    }

    @Override
    public String header(String name) {
        HttpHeaders headers = this.request.getHeaders();
        return headers.getFirst(name);
    }

    @Override
    public boolean cacheScope() {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {
    }

    @Override
    public String method() {
        return this.request.getMethodValue();
    }

    @Override
    public String path() {
        return this.request.getPath().value();
    }

    @Override
    public String route() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        if (this.request != null && this.request.getRemoteAddress() != null) {
            return this.request.getRemoteAddress().getAddress().getHostAddress();
        } else {
            return "Unknown";
        }
    }

    @Override
    public int getRemotePort() {
        if (this.request != null && this.request.getRemoteAddress() != null) {
            return this.request.getRemoteAddress().getPort();
        } else {
            return 0;
        }
    }
}
