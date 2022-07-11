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

package com.megaease.easeagent.metrics.converter;

import com.megaease.easeagent.metrics.TestConst;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class MetricsAdditionalAttributesTest {

    @Test
    public void get() {
        MetricsAdditionalAttributes metricsAdditionalAttributes = new MetricsAdditionalAttributes(MockConfig.getCONFIGS());
        assertEquals(TestConst.SERVICE_NAME, metricsAdditionalAttributes.get().get(TestConst.SERVICE_KEY_NAME));
        assertEquals(TestConst.SERVICE_SYSTEM, metricsAdditionalAttributes.get().get(ConfigConst.SYSTEM_NAME));
        String newServiceName = "newMetricServiceName";
        String newSystemName = "newMetricSystemName";
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(ConfigConst.SERVICE_NAME, newServiceName));
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(ConfigConst.SYSTEM_NAME, newSystemName));
        assertEquals(newServiceName, metricsAdditionalAttributes.get().get(TestConst.SERVICE_KEY_NAME));
        assertEquals(newSystemName, metricsAdditionalAttributes.get().get(ConfigConst.SYSTEM_NAME));

        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(ConfigConst.SERVICE_NAME, TestConst.SERVICE_NAME));
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(ConfigConst.SYSTEM_NAME, TestConst.SERVICE_SYSTEM));

        assertEquals(TestConst.SERVICE_NAME, metricsAdditionalAttributes.get().get(TestConst.SERVICE_KEY_NAME));
        assertEquals(TestConst.SERVICE_SYSTEM, metricsAdditionalAttributes.get().get(ConfigConst.SYSTEM_NAME));

    }
}
