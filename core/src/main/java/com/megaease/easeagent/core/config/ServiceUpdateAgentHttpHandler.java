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
 *
 */
package com.megaease.easeagent.core.config;

import com.megaease.easeagent.config.CompatibilityConversion;
import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;

import java.util.Map;

public class ServiceUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
    public ServiceUpdateAgentHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
    }

    @Override
    public String getPath() {
        return "/config";
    }

    @Override
    public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
        this.mxBeanConfig.updateService2(CompatibilityConversion.transform(config), version);
        return null;
    }
}
