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

import fi.iki.elonen.router.RouterNanoHTTPD;
import lombok.SneakyThrows;

import java.util.List;

public class AgentHttpServer extends RouterNanoHTTPD {

    public static String JSON_TYPE = "application/json";

    public AgentHttpServer(int port) {
        super(port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void addHttpRoutes(List<AgentHttpHandler> agentHttpHandlers) {
        for (AgentHttpHandler agentHttpHandler : agentHttpHandlers) {
            this.addRoute(agentHttpHandler.getPath(), agentHttpHandler.getClass());
        }
    }

    @SneakyThrows
    public void startServer() {
        this.start(5000, true);
    }

//    public static void main(String[] args) {
//        AgentHttpServer agentHttpServer = new AgentHttpServer(9900);
//        agentHttpServer.startServer();
//        System.out.println("after start");
//        List<AgentHttpHandler> list = new ArrayList<>();
//        list.add(new TestHandler());
//        agentHttpServer.addHttpRoutes(list);
//    }
//
//    public static class TestHandler extends AgentHttpHandler {
//
//        @Override
//        public String getPath() {
//            return "/hello";
//        }
//
//        @Override
//        public Response process(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
//            String requestBody = this.buildRequestBody(session);
//            return NanoHTTPD.newFixedLengthResponse(requestBody);
//        }
//    }
}
