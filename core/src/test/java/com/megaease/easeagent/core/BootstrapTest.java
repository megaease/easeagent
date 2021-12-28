/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.core;


import com.j256.simplejmx.client.JmxClient;
import com.j256.simplejmx.common.IoUtils;
import com.j256.simplejmx.server.JmxServer;
import com.megaease.easeagent.config.Configs;
import lombok.SneakyThrows;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BootstrapTest {
    private final static Integer DEFAULT_PORT = 8778;
    private static JmxServer server;
    private static InetAddress serverAddress;

    private static ObjectName objectName;

    @BeforeClass
    public static void beforeClass() throws Exception {
        serverAddress = InetAddress.getByName("127.0.0.1");
        objectName = new ObjectName(Bootstrap.MX_BEAN_OBJECT_NAME);
    }

    @AfterClass
    public static void afterClass() {
        IoUtils.closeQuietly(server);
        System.gc();
    }

    @Test
    public void should_work() throws Exception {
        HashMap<String, String> source = new HashMap<>();

        String text = UUID.randomUUID().toString();
        String text2 = UUID.randomUUID().toString();
        source.put("key", text);
        source.put("key2", text2);
        Configs configs = new Configs(source);

        Bootstrap.registerMBeans(configs);
        server = new JmxServer(serverAddress, DEFAULT_PORT);
        server.start();
        JmxClient jmxClient = new JmxClient(serverAddress, DEFAULT_PORT);

        Map<String, String> cfg = mxBeanGetConfigs(jmxClient);
        MatcherAssert.assertThat(cfg.get("key"), CoreMatchers.equalTo(text));

        source = new HashMap<>();
        String helloValue = "helloValue";
        source.put("hello", helloValue);
        mxBeanSetConfigs(jmxClient, source);
        cfg = mxBeanGetConfigs(jmxClient);

        MatcherAssert.assertThat(cfg.get("key"), CoreMatchers.equalTo(text));
        MatcherAssert.assertThat(cfg.get("hello"), CoreMatchers.equalTo(helloValue));

        jmxClient.close();
    }

    @SneakyThrows
    private TabularData getUpdateConfigsOperationInfo(MBeanOperationInfo operationInfo,
                                                             Map<String, String> source) {
        OpenMBeanParameterInfoSupport p = (OpenMBeanParameterInfoSupport)operationInfo.getSignature()[0];

        TabularType pType = (TabularType)p.getOpenType();
        CompositeType rowType = pType.getRowType();

        TabularDataSupport tabularData = new TabularDataSupport(pType);
        for(Map.Entry<String, String> entry : source.entrySet()) {
            Map<String, Object> imap = new HashMap<>();
            imap.put("key",  entry.getKey());
            imap.put("value", entry.getValue());
            try {
                CompositeData compositeData = new CompositeDataSupport(rowType, imap);
                tabularData.put(compositeData);
            } catch (OpenDataException e) {
                throw new IllegalArgumentException(e.getMessage(),e);
            }
        }
        return tabularData;
    }

    @SneakyThrows
    private void mxBeanSetConfigs(JmxClient client, Map<String, String> source) {
        MBeanOperationInfo operationInfo = client.getOperationInfo(objectName, "updateConfigs");
        TabularData data = getUpdateConfigsOperationInfo(operationInfo, source);
        client.invokeOperation(objectName, "updateConfigs", data);
    }

    @SneakyThrows
    private Map<String, String> mxBeanGetConfigs(JmxClient client) {
        Object a = client.getAttribute(objectName, "Configs");

        TabularDataSupport d = (TabularDataSupport)  a;
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : d.entrySet()) {
            CompositeDataSupport dd = (CompositeDataSupport)entry.getValue();
            map.put(dd.get("key").toString(), dd.get("value").toString());
        }

        return map;
    }
}
