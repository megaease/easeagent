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

package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.mock.utils.MockSystemEnv;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    public static final String REDIRECT_HOST = "192.186.0.12";
    public static final int REDIRECT_PORT = 5672;
    public static final String REDIRECT_USERNAME = "testUserName";
    public static final String REDIRECT_PASSWORD = "testPassword";

    public static IPluginConfig getMqMetricConfig() {
        RabbitMqPlugin rabbitMqPlugin = new RabbitMqPlugin();
        return EaseAgent.getConfig(rabbitMqPlugin.getDomain(), rabbitMqPlugin.getNamespace(), ConfigConst.PluginID.METRIC);
    }

    public static String getRedirectUri() {
        return String.format("%s:%s", REDIRECT_HOST, REDIRECT_PORT);
    }

    public static void setRedirect() {
        MockSystemEnv.set(MiddlewareConstants.ENV_RABBITMQ, String.format("{\"uris\":\"%s\", \"userName\":\"%s\",\"password\":\"%s\"}", getRedirectUri(), REDIRECT_USERNAME, REDIRECT_PASSWORD));
        AgentFieldReflectAccessor.setFieldValue(Redirect.RABBITMQ, "config", ResourceConfig.getResourceConfig(Redirect.RABBITMQ.getEnv(), Redirect.RABBITMQ.isNeedParse()));
    }

    public static String getRedirectedUri() {
        Map<Redirect, String> redirectedUris = AgentFieldReflectAccessor.getFieldValue(RedirectProcessor.INSTANCE, "redirectedUris");
        return redirectedUris.get(Redirect.RABBITMQ);
    }

    public static void cleanRedirectedUri() {
        AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "redirectedUris", new HashMap<>());
    }
}
