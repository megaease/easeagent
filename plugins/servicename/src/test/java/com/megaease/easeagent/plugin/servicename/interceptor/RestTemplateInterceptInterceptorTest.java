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
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import feign.Request;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.junit.Assert.*;

@MockEaseAgent
public class RestTemplateInterceptInterceptorTest {

    @Test
    public void before() throws URISyntaxException {
        RestTemplateInterceptInterceptor interceptor = new RestTemplateInterceptInterceptor();
        BaseServiceNameInterceptorTest.initInterceptor(interceptor);
        EaseAgent.getContext().put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);

        MockClientHttpRequest httpRequest = new MockClientHttpRequest();
        String host = "TEST-SERVER";
        httpRequest.setURI(new URI("http://" + host));
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpRequest}).build();

        interceptor.before(methodInfo, EaseAgent.getContext());

        MockClientHttpRequest newRequest = (MockClientHttpRequest) methodInfo.getArgs()[0];
        CheckUtils.check(name -> newRequest.getHeaders().getFirst(name), host);

    }
}
