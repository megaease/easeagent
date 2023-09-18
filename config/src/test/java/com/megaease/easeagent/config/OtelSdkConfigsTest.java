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
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.utils.SystemEnv;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


public class OtelSdkConfigsTest {

    @Test
    public void updateEnvCfg() {
        //value from system env "OTEL_RESOURCE_ATTRIBUTES
        String attributes = "service.name=service1,service.namespace=namespace1";
        SystemEnv.set("OTEL_RESOURCE_ATTRIBUTES", attributes);
        Map<String, String> envCfg = OtelSdkConfigs.updateEnvCfg();
        Assert.assertEquals("service1", envCfg.get("name"));
        Assert.assertEquals("namespace1", envCfg.get("system"));

        // override by system env
        SystemEnv.set("OTEL_SERVICE_NAME", "service2");
        SystemEnv.set("OTEL_SERVICE_NAMESPACE", "namespace2");
        envCfg = OtelSdkConfigs.updateEnvCfg();
        Assert.assertEquals("service2", envCfg.get("name"));
        Assert.assertEquals("namespace2", envCfg.get("system"));

        // override by system property
        System.setProperty("otel.service.name", "service3");
        System.setProperty("otel.service.namespace", "namespace3");
        envCfg = OtelSdkConfigs.updateEnvCfg();
        Assert.assertEquals("service3", envCfg.get("name"));
        Assert.assertEquals("namespace3", envCfg.get("system"));

    }
}
