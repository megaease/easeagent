/*
 * Copyright (c) 2021, MegaEase
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
 */

package easeagent.plugin.spring.gateway.interceptor.initialize;

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import easeagent.plugin.spring.gateway.SpringGatewayPlugin;
import easeagent.plugin.spring.gateway.advice.InitGlobalFilterAdvice;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.List;

@AdviceTo(value = InitGlobalFilterAdvice.class, plugin = SpringGatewayPlugin.class)
public class GlobalFilterInterceptor implements Interceptor {

    @Override
    @SuppressWarnings("unchecked")
    public void before(MethodInfo methodInfo, Context context) {
        List<GlobalFilter> list = null;
        switch (methodInfo.getMethod()) {
            case "filteringWebHandler":
            case "gatewayControllerEndpoint":
                list = (List<GlobalFilter>) methodInfo.getArgs()[0];
                break;
            case "gatewayLegacyControllerEndpoint":
                list = (List<GlobalFilter>) methodInfo.getArgs()[1];
                break;
        }
        if (list == null || hasAgentFilter(list)) {
            return;
        }
        list.add(0, new AgentGlobalFilter());
    }

    private boolean hasAgentFilter(List<GlobalFilter> list) {
        for (GlobalFilter globalFilter : list) {
            if (globalFilter instanceof AgentGlobalFilter) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return Order.INIT.getName();
    }

    @Override
    public int order() {
        return Order.INIT.getOrder();
    }
}
