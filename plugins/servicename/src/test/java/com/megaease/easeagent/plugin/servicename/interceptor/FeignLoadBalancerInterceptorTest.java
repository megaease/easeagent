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
import com.megaease.easeagent.plugin.api.trace.Getter;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.servicename.Const;
import com.netflix.client.config.DefaultClientConfigImpl;
import feign.Request;
import org.junit.Test;
import org.springframework.cloud.openfeign.ribbon.MockRibbonRequest;

import java.net.URISyntaxException;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@MockEaseAgent
public class FeignLoadBalancerInterceptorTest {

    @Test
    public void before() throws URISyntaxException {
        FeignLoadBalancerInterceptor interceptor = new FeignLoadBalancerInterceptor();
        BaseServiceNameInterceptorTest.initInterceptor(interceptor);
        EaseAgent.getContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);

        Object ribbonRequest = MockRibbonRequest.createRibbonRequest();
        DefaultClientConfigImpl defaultClientConfig = new DefaultClientConfigImpl();
        String serviceName = "testServiceName";
        defaultClientConfig.setClientName(serviceName);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{ribbonRequest, defaultClientConfig}).build();
        interceptor.before(methodInfo, EaseAgent.getContext());

        Request request = MockRibbonRequest.getRequest(methodInfo.getArgs()[0]);
        CheckUtils.check(name -> {
            Collection<String> head = request.headers().get(name);
            assertNotNull(head);
            return head.iterator().next();
        }, serviceName);
    }
}
