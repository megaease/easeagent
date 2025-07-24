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

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GlobalFilterInterceptorTest {

    @Test
    public void before() {
        GlobalFilterInterceptor interceptor = new GlobalFilterInterceptor();
        List arg = new ArrayList();
        MethodInfo methodInfo = MethodInfo.builder().method("filteringWebHandler").args(new Object[]{arg}).build();
        interceptor.before(methodInfo, null);
        assertEquals(1, arg.size());
        assertEquals(1, arg.size());
        arg.clear();
        methodInfo = MethodInfo.builder().method("gatewayControllerEndpoint").args(new Object[]{arg}).build();
        interceptor.before(methodInfo, null);
        assertEquals(1, arg.size());
        arg.clear();
        methodInfo = MethodInfo.builder().method("gatewayLegacyControllerEndpoint").args(new Object[]{null, arg}).build();
        interceptor.before(methodInfo, null);
        assertEquals(1, arg.size());

    }

    @Test
    public void getType() {
        GlobalFilterInterceptor interceptor = new GlobalFilterInterceptor();
        assertEquals(ConfigConst.PluginID.INIT, interceptor.getType());
    }

    @Test
    public void order() {
        GlobalFilterInterceptor interceptor = new GlobalFilterInterceptor();
        assertEquals(Order.INIT.getOrder(), interceptor.order());
    }
}
