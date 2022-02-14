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
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.trace.Getter;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.servicename.Const;
import com.megaease.easeagent.plugin.servicename.ServiceNamePlugin;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@MockEaseAgent
public class FeignBlockingLoadBalancerClientInterceptorTest {


    @Test
    public void before() {
        FeignBlockingLoadBalancerClientInterceptor interceptor = new FeignBlockingLoadBalancerClientInterceptor();
        BaseServiceNameInterceptorTest.initInterceptor(interceptor);
        EaseAgent.getContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);


        RequestTemplate requestTemplate = new RequestTemplate();
        String host = "TEST-SERVER";
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://" + host,
            requestTemplate.headers(),
            Request.Body.create(requestTemplate.body()),
            requestTemplate
        );

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{request}).build();

        interceptor.before(methodInfo, EaseAgent.getContext());

        Request newRequest = (Request) methodInfo.getArgs()[0];
        CheckUtils.check(name -> {
            Collection<String> head = newRequest.headers().get(name);
            assertNotNull(head);
            return head.iterator().next();
        }, host);
    }
}
