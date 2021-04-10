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
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class AgentHttpServer {

    HttpServer httpServer;

    @SneakyThrows
    public AgentHttpServer(int port) {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void addHttpHandler(AgentHttpHandler agentHttpHandler) {
        this.httpServer.createContext(agentHttpHandler.getPath(), agentHttpHandler);
    }

    public void addHttpHandlers(List<AgentHttpHandler> agentHttpHandlers) {
        for (AgentHttpHandler agentHttpHandler : agentHttpHandlers) {
            this.httpServer.createContext(agentHttpHandler.getPath(), agentHttpHandler);
        }
    }

    public void start() {
        this.httpServer.start();
    }

    public void stop() {
        System.out.println("begin stop http server");
        this.httpServer.start();
    }

    @SneakyThrows
    public static void main(String[] args) {
        AgentHttpServer agentHttpServer = new AgentHttpServer(9900);
        agentHttpServer.addHttpHandler(new AgentHttpHandler() {
            @Override
            public String getPath() {
                return "/test";
            }

            @Override
            public HttpResponse process(HttpExchange exchange) throws IOException {
                String string = this.getRequestBodyString(exchange);
                System.out.println(string);
                return HttpResponse.builder().statusCode(200).data("ok-ok").build();
            }
        });
        agentHttpServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(agentHttpServer::stop));
        System.out.println("local http server started");
//        TimeUnit.HOURS.sleep(1);
    }
}
