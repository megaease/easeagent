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

package com.megaease.easeagent.mock.utils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class JdkHttpServer {
    public static final JdkHttpServer INSTANCE;

    static {
        try {
            INSTANCE = JdkHttpServer.builder().build();
        } catch (IOException e) {
            throw new RuntimeException("build JdkHttpServer fail.", e);
        }
    }

    private final int port;
    private final HttpServer server;
    private final String path;
    private final String url;
    private AtomicReference<Headers> lastHeaders = new AtomicReference<>();
    private AtomicReference<HttpExchange> lastHttpExchange = new AtomicReference<>();
    private Consumer<Headers> headersConsumer;
    private Consumer<HttpExchange> exchangeConsumer;

    public JdkHttpServer(int port, HttpServer server, String path) {
        this.port = port;
        this.server = server;
        this.path = path;
        this.url = String.format("http://127.0.0.1:%s%s", port, path);
    }

    public JdkHttpServer start() {
        HttpContext context = server.createContext(path);
        context.setHandler(JdkHttpServer.this::handleRequest);
        server.start();
        return this;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public void stop() {
        server.stop(0);
    }

    public void setHeadersConsumer(Consumer<Headers> headersConsumer) {
        this.headersConsumer = headersConsumer;
    }

    public void setExchangeConsumer(Consumer<HttpExchange> exchangeConsumer) {
        this.exchangeConsumer = exchangeConsumer;
    }

    public Headers getLastHeaders() {
        return lastHeaders.get();
    }

    public HttpExchange getLastHttpExchange() {
        return lastHttpExchange.get();
    }

    public void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        lastHeaders.set(exchange.getRequestHeaders());
        lastHttpExchange.set(exchange);
        if (this.headersConsumer != null) {
            this.headersConsumer.accept(exchange.getRequestHeaders());
        }
        if (this.exchangeConsumer != null) {
            this.exchangeConsumer.accept(exchange);
        }
        String response = String.format("This is the response at %s port: %s", requestURI, port);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int tryPortNum = 4;
        private int port = 0;
        private String path;
        private Consumer<Headers> headersConsumer;
        private Consumer<HttpExchange> exchangeConsumer;

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setHeadersConsumer(Consumer<Headers> headersConsumer) {
            this.headersConsumer = headersConsumer;
            return this;
        }

        public Builder setExchangeConsumer(Consumer<HttpExchange> exchangeConsumer) {
            this.exchangeConsumer = exchangeConsumer;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public void setTryPortNum(int tryPortNum) {
            this.tryPortNum = tryPortNum;
        }

        private HttpServer buildHttpServer() throws IOException {
            if (0 < port) {
                return HttpServer.create(new InetSocketAddress(port), 0);
            }
            IOException ioException = null;
            for (int i = 0; i < tryPortNum; i++) {
                try {
                    DatagramSocket s = new DatagramSocket(0);
                    int p = s.getLocalPort();
                    return HttpServer.create(new InetSocketAddress(p), 0);
                } catch (IOException e) {
                    ioException = e;
                }
            }
            throw ioException;
        }

        public JdkHttpServer build() throws IOException {
            HttpServer httpServer = buildHttpServer();
            int p = httpServer.getAddress().getPort();
            String httpPath = path == null ? "/example" : path;
            JdkHttpServer jdkHttpServer = new JdkHttpServer(p, httpServer, httpPath);
            jdkHttpServer.setHeadersConsumer(headersConsumer);
            jdkHttpServer.setExchangeConsumer(exchangeConsumer);
            return jdkHttpServer;
        }

    }
}
