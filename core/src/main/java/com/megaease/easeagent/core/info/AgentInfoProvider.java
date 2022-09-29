/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.core.info;

import com.megaease.easeagent.core.utils.JsonUtil;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandlerProvider;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.megaease.easeagent.plugin.bean.BeanProvider;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentInfoProvider implements AgentHttpHandlerProvider, BeanProvider {

    @Override
    public List<AgentHttpHandler> getAgentHttpHandlers() {
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new AgentInfoHttpHandler());
        return list;
    }

    public static class AgentInfoHttpHandler extends AgentHttpHandler {

        @Override
        public String getPath() {
            return "/agent-info";
        }

        @Override
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, JsonUtil.toJson(EaseAgent.getAgentInfo()));
        }
    }
}
