/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.okhttp.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class OkHttpForwardedInterceptorTest {

    @Test
    public void before() {
        OkHttpForwardedInterceptor okHttpForwardedInterceptor = new OkHttpForwardedInterceptor();
        Request request = new Request.Builder()
            .url("http://127.0.0.1:8080/test")
            .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        MethodInfo methodInfo = MethodInfo.builder().invoker(call).build();
        Context context = EaseAgent.getContext();
        okHttpForwardedInterceptor.before(methodInfo, context);
        assertNull(call.request().header(TestConst.FORWARDED_NAME));
        context.put(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE);
        try {
            okHttpForwardedInterceptor.before(methodInfo, context);
            assertNotNull(call.request().header(TestConst.FORWARDED_NAME));
            assertEquals(TestConst.FORWARDED_VALUE, call.request().header(TestConst.FORWARDED_NAME));
        } finally {
            context.remove(TestConst.FORWARDED_NAME);
        }
    }

    @Test
    public void getType() {
        OkHttpForwardedInterceptor okHttpForwardedInterceptor = new OkHttpForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, okHttpForwardedInterceptor.getType());
    }
}
