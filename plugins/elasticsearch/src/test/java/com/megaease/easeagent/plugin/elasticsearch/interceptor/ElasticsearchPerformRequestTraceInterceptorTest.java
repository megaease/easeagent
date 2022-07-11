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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ElasticsearchPerformRequestTraceInterceptorTest extends ElasticsearchBaseTest {
    ElasticsearchPerformRequestTraceInterceptor interceptor;

    @Before
    public void before() {
        super.before();
        interceptor = new ElasticsearchPerformRequestTraceInterceptor();
    }

    @Test
    public void performSuccess() {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(this)
            .method("perform")
            .args(new Object[]{request})
            .retValue(this.successResponse)
            .build();

        Context context = EaseAgent.getContext();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        this.assertTrace(true, null);
    }

    @Test
    public void performFail() {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(this)
            .method("perform")
            .args(new Object[]{request})
            .retValue(this.failResponse)
            .build();

        Context context = EaseAgent.getContext();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        this.assertTrace(false, "500");
    }

    @Test
    public void performFailThrowable() {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(this)
            .method("perform")
            .args(new Object[]{request})
            .retValue(this.failResponse)
            .throwable(new RuntimeException(errMsg))
            .build();

        Context context = EaseAgent.getContext();
        interceptor.before(methodInfo, context);
        interceptor.after(methodInfo, context);
        this.assertTrace(false, errMsg);
    }
}
