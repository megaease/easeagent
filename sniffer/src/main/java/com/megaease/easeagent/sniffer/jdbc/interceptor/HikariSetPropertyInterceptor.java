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

package com.megaease.easeagent.sniffer.jdbc.interceptor;

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class HikariSetPropertyInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_DATABASE);
        if (cnf == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        if (methodInfo.getMethod().equals("setJdbcUrl")) {
            String jdbcUrl = cnf.getFirstUri();
            methodInfo.getArgs()[0] = jdbcUrl;
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.getArgs()[0] = cnf.getUserName();
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.getArgs()[0] = cnf.getPassword();
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }
}
