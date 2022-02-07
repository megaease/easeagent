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

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.interceptor.RequestUtils;
import com.megaease.easeagent.plugin.springweb.interceptor.TestConst;
import feign.Request;
import feign.RequestTemplate;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

@MockEaseAgent
public class FeignClientForwardedInterceptorTest {

    @Test
    public void before() {
        FeignClientForwardedInterceptor feignClientForwardedInterceptor = new FeignClientForwardedInterceptor();
        Request request = RequestUtils.buildFeignClient();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{request}).build();
        Context context = EaseAgent.getContext();
        feignClientForwardedInterceptor.before(methodInfo, context);
        assertNull(header(request, TestConst.FORWARDED_NAME));
        context.put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        try {
            feignClientForwardedInterceptor.before(methodInfo, context);
            assertNotNull(header(request, TestConst.FORWARDED_NAME));
            assertEquals(TestConst.FORWARDED_VALUE, header(request, TestConst.FORWARDED_NAME));
        } finally {
            context.remove(TestConst.FORWARDED_NAME);
        }

    }

    public String header(Request request, String name) {
        Collection<String> collection = request.headers().get(name);
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        return collection.iterator().next();
    }

    @Test
    public void getType() {
        FeignClientForwardedInterceptor feignClientForwardedInterceptor = new FeignClientForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, feignClientForwardedInterceptor.getType());

    }
}
