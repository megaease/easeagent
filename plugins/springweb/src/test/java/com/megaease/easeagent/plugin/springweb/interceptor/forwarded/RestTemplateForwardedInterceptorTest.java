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

package com.megaease.easeagent.plugin.springweb.interceptor.forwarded;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.interceptor.TestConst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RestTemplateForwardedInterceptorTest {

    @Test
    public void before() throws URISyntaxException, IOException {
        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(new URI("http://127.0.0.1:8080/test"), HttpMethod.GET);
        RestTemplateForwardedInterceptor restTemplateForwardedInterceptor = new RestTemplateForwardedInterceptor();

        MethodInfo methodInfo = MethodInfo.builder().invoker(request).build();
        Context context = EaseAgent.getOrCreateTracingContext();
        restTemplateForwardedInterceptor.before(methodInfo, context);
        assertNull(request.getHeaders().getFirst(TestConst.FORWARDED_NAME));
        context.put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        try {
            restTemplateForwardedInterceptor.before(methodInfo, context);
            assertNotNull(request.getHeaders().get(TestConst.FORWARDED_NAME));
            assertEquals(TestConst.FORWARDED_VALUE, request.getHeaders().getFirst(TestConst.FORWARDED_NAME));
        } finally {
            context.remove(TestConst.FORWARDED_NAME);
        }


    }

    @Test
    public void getType() {
        RestTemplateForwardedInterceptor restTemplateForwardedInterceptor = new RestTemplateForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, restTemplateForwardedInterceptor.getType());

    }
}
