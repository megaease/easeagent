/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.sniffer.elasticsearch.interceptor;

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringElasticsearchInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_ES);
        if (cnf == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        String method = methodInfo.getMethod();
        List<String> uris = this.formatUris(cnf.getUriList());
        if (method.equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.getArgs()[0] = cnf.getUserName();
        } else if (method.equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.getArgs()[0] = cnf.getPassword();
        } else if (method.equals("setEndpoints")) {
            methodInfo.getArgs()[0] = uris;
        } else if (method.equals("setUris")) {
            methodInfo.getArgs()[0] = uris;
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

    private List<String> formatUris(List<String> uriList) {
        List<String> list = new ArrayList<>();
        for (String uri : uriList) {
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                list.add(uri);
            } else {
                list.add("http://" + uri);
            }
        }
        return list;
    }
}
