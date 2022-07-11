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
package com.megaease.easeagent.core.config;

import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CanaryListUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanaryListUpdateAgentHttpHandler.class);
    public static final AtomicInteger LAST_COUNT = new AtomicInteger(0);

    public CanaryListUpdateAgentHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
    }

    @Override
    public String getPath() {
        return "/config-global-transmission";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
        LOGGER.info("call /config-global-transmission. configs: {}", map);
        synchronized (LAST_COUNT) {
            List<String> headers = (List<String>) map.get("headers");
            Map<String, String> config = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                config.put("easeagent.progress.forwarded.headers.global.transmission." + i, headers.get(i));
            }
            int last = LAST_COUNT.get();
            if (headers.size() < last) {
                for (int i = headers.size(); i < last; i++) {
                    config.put("easeagent.progress.forwarded.headers.global.transmission." + i, "");
                }
            }
            LAST_COUNT.set(headers.size());
            this.mxBeanConfig.updateConfigs(config);
        }
        return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
    }

    @Override
    public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
        this.mxBeanConfig.updateCanary2(config, version);
        return null;
    }
}
