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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.ConfigTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.Const;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.tools.config.NameAndSystem;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MD5ReportConsumerTest {

    private static void verifyMD5ReportConsumer(Map<String, Object> reportConsumer, String md5, String sql) {
        assertNotNull(reportConsumer.get("timestamp"));
        assertEquals(reportConsumer.get("category"), "application");
        if (reportConsumer.get("hostName") != null) {
            assertEquals(HostAddress.localhost(), reportConsumer.get("hostName"));
        } else {
            assertEquals(HostAddress.localhost(), reportConsumer.get("host_name"));
        }
        if (reportConsumer.get("hostIpv4") != null) {
            assertEquals(HostAddress.getHostIpv4(), reportConsumer.get("hostIpv4"));
        } else {
            assertEquals(HostAddress.getHostIpv4(), reportConsumer.get("host_ipv4"));
        }
        assertEquals("", reportConsumer.get("gid"));
        assertEquals(NameAndSystem.system(), reportConsumer.get("system"));
        assertEquals(NameAndSystem.name(), reportConsumer.get("service"));
        assertEquals("", reportConsumer.get("tags"));
        assertEquals("md5-dictionary", reportConsumer.get("type"));
        assertEquals(md5, reportConsumer.get("md5"));
        assertEquals(sql, reportConsumer.get("sql"));


    }

    @Test
    public void accept() {
        MD5ReportConsumer md5ReportConsumer = new MD5ReportConsumer();
        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "md5-dictionary");
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        String testMd5 = "testMd5";
        String testSql = "testSql";
        md5ReportConsumer.accept(Collections.singletonMap(testMd5, testSql));
        verifyMD5ReportConsumer(lastJsonReporter.getLastOnlyOne(), testMd5, testSql);
        IPluginConfig iPluginConfig = EaseAgent.getConfig(ConfigConst.OBSERVABILITY,
            ConfigConst.Namespace.MD5_DICTIONARY, ConfigConst.PluginID.METRIC);
        try (ConfigTestUtils.Reset ignored = ConfigTestUtils.changeBoolean(iPluginConfig, Const.ENABLED_CONFIG, false)) {
            lastJsonReporter.clean();
            md5ReportConsumer.accept(Collections.singletonMap(testMd5, testSql));
            try {
                lastJsonReporter.getLast();
                fail("must be throw error");
            } catch (Exception e) {
                //must be error.
            }
        }

    }
}
