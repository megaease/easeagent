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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import org.junit.Test;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import static org.junit.Assert.*;

@MockEaseAgent
public class LoadBalancerFeignClientInterceptorTest {

    @Test
    public void after() {
        LoadBalancerFeignClientInterceptor loadBalancerFeignClientInterceptor = new LoadBalancerFeignClientInterceptor();

        IClientConfig iClientConfig = new DefaultClientConfigImpl();
        String clientName = "testClientName";
        assertNull(iClientConfig.getClientName());
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, clientName}).retValue(iClientConfig).build();
        loadBalancerFeignClientInterceptor.after(methodInfo, EaseAgent.getContext());
        assertEquals(clientName, iClientConfig.getClientName());
    }

    @Test
    public void order() {
        LoadBalancerFeignClientInterceptor loadBalancerFeignClientInterceptor = new LoadBalancerFeignClientInterceptor();
        assertEquals(Order.HIGH.getOrder(), loadBalancerFeignClientInterceptor.order());
    }

    @Test
    public void getType() {
        LoadBalancerFeignClientInterceptor loadBalancerFeignClientInterceptor = new LoadBalancerFeignClientInterceptor();
        assertEquals("addServiceNameHead", loadBalancerFeignClientInterceptor.getType());
    }
}
