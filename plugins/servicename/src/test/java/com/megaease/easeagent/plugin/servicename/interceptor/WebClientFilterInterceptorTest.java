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

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.MockClientRequest;

import java.net.URI;
import java.net.URISyntaxException;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class WebClientFilterInterceptorTest {

    @Test
    public void before() throws URISyntaxException {
        WebClientFilterInterceptor interceptor = new WebClientFilterInterceptor();
        BaseServiceNameInterceptorTest.initInterceptor(interceptor);
        EaseAgent.getContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);

        String host = "TEST-SERVER";
        ClientRequest clientRequest = MockClientRequest.build(new URI("http://" + host));

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{clientRequest}).build();

        interceptor.before(methodInfo, EaseAgent.getContext());

        ClientRequest newRequest = (ClientRequest) methodInfo.getArgs()[0];
        CheckUtils.check(name -> newRequest.headers().getFirst(name), host);

    }
}
