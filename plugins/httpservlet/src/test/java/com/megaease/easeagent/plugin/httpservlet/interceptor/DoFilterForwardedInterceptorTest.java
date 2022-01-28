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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;

@MockEaseAgent
public class DoFilterForwardedInterceptorTest {

    public void testForwarded() {
        HttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest}).build();
        DoFilterForwardedInterceptor doFilterForwardedInterceptor = new DoFilterForwardedInterceptor();
        doFilterForwardedInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        assertEquals(TestConst.FORWARDED_VALUE, EaseAgent.getContext().get(TestConst.FORWARDED_NAME));
        doFilterForwardedInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertEquals(null, (String) EaseAgent.getContext().get(TestConst.FORWARDED_NAME));
    }

    @Test
    public void doBefore() {
        testForwarded();
    }

    @Test
    public void doAfter() {
        testForwarded();
    }

    @Test
    public void getType() {
        DoFilterForwardedInterceptor doFilterForwardedInterceptor = new DoFilterForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, doFilterForwardedInterceptor.getType());
    }
}
