/*
 * Copyright (c) 2023, Inspireso and/or its affiliates. All rights reserved.
 */

package io.opentelemetry.sdk.resources;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.tools.config.AutoRefreshConfigSupplierTest;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static io.opentelemetry.sdk.resources.OtelSdkConfigs.OTEL_RESOURCE_ATTRIBUTES;
import static org.junit.Assert.*;

public class OtelSdkConfigsTest {

    @Test
    public void updateEnvCfg() {
        //value from system env "OTEL_RESOURCE_ATTRIBUTES
        String attributes="service.name=service1,service.namespace=namespace1";
        SystemEnv.set(OTEL_RESOURCE_ATTRIBUTES, attributes);
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
