/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public abstract class AgentHttpHandler implements HttpHandler {

    public abstract String getPath();

    public abstract HttpResponse process(HttpExchange exchange) throws IOException;

    protected String getRequestBodyString(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }

    @SneakyThrows
    @Override
    public void handle(HttpExchange exchange) {
        try {
            HttpResponse httpResponse = this.process(exchange);
            int len = 0;
            byte[] bytes = null;
            if (httpResponse.getData() != null) {
                bytes = httpResponse.getData().getBytes(StandardCharsets.UTF_8);
                len = bytes.length;
            }
            exchange.sendResponseHeaders(httpResponse.getStatusCode(), len);
            if (bytes != null) {
                exchange.getResponseBody().write(bytes);
            }
        } finally {
            exchange.close();
        }
    }
}
