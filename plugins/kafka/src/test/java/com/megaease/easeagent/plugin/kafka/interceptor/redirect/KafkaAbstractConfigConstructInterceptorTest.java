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

package com.megaease.easeagent.plugin.kafka.interceptor.redirect;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaTestUtils;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaAbstractConfigConstructInterceptorTest {

    @Test
    public void doBefore() {
        KafkaAbstractConfigConstructInterceptor interceptor = new KafkaAbstractConfigConstructInterceptor();
        KafkaTestUtils.mockRedirect(() -> {
            Map config = new HashMap();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);

            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{config}).build();
            interceptor.doBefore(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_URIS, config.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
            methodInfo = MethodInfo.builder().args(new Object[]{config}).build();
            interceptor.doBefore(methodInfo, EaseAgent.getContext());
            assertEquals(TestConst.REDIRECT_URIS, config.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        });
    }

    @Test
    public void getType() {
        KafkaAbstractConfigConstructInterceptor interceptor = new KafkaAbstractConfigConstructInterceptor();
        assertEquals(ConfigConst.PluginID.REDIRECT, interceptor.getType());
    }
}
