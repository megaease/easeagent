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

package easeagent.plugin.spring.gateway.interceptor.initialize;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import easeagent.plugin.spring.gateway.TestConst;
import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.server.MockServerWebExchange;

import static easeagent.plugin.spring.gateway.TestServerWebExchangeUtils.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class GatewayServerForwardedInterceptorTest {

    @Test
    public void doBefore() {
        GatewayServerForwardedInterceptor interceptor = new GatewayServerForwardedInterceptor();
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.build(builder().header(TestConst.FORWARDED_NAME, TestConst.FORWARDED_VALUE));
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{mockServerWebExchange}).build();
        interceptor.doBefore(methodInfo, context);
        assertEquals(TestConst.FORWARDED_VALUE, context.get(TestConst.FORWARDED_NAME));
        interceptor.doAfter(methodInfo, context);
        assertNull(context.get(TestConst.FORWARDED_NAME));
    }

    @Test
    public void doAfter() {
        doBefore();
    }

    @Test
    public void getType() {
        GatewayServerForwardedInterceptor interceptor = new GatewayServerForwardedInterceptor();
        assertEquals(ConfigConst.PluginID.FORWARDED, interceptor.getType());
    }
}
