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

package com.megaease.easeagent.plugin.httpclient.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseagent;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.junit.Test;

import static org.junit.Assert.*;

@MockEaseagent
public class HttpClient5AsyncForwardedInterceptorTest {

    @Test
    public void before() {
        SimpleHttpRequest simpleHttpRequest = SimpleHttpRequest.create("GET", "http://127.0.0.1:8080");
        SimpleRequestProducer simpleRequestProducer = SimpleRequestProducer.create(simpleHttpRequest);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{simpleRequestProducer}).build();
        HttpClient5AsyncForwardedInterceptor httpClientDoExecuteForwardedInterceptor = new HttpClient5AsyncForwardedInterceptor();
        Context context = EaseAgent.getContext();
        httpClientDoExecuteForwardedInterceptor.before(methodInfo, context);
        assertNull(simpleHttpRequest.getFirstHeader(TestConst.FORWARDED_NAME));
        context.put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        try {
            httpClientDoExecuteForwardedInterceptor.before(methodInfo, context);
            assertNotNull(simpleHttpRequest.getFirstHeader(TestConst.FORWARDED_NAME));
            assertEquals(TestConst.FORWARDED_VALUE, simpleHttpRequest.getFirstHeader(TestConst.FORWARDED_NAME).getValue());
        } finally {
            context.remove(TestConst.FORWARDED_NAME);
        }
    }
}
