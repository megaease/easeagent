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

package com.megaease.easeagent.plugin.jdbc.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.jdbc.JdbcRedirectPlugin;
import com.megaease.easeagent.plugin.jdbc.advice.HikariDataSourceAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(value = HikariDataSourceAdvice.class, plugin = JdbcRedirectPlugin.class)
public class HikariSetPropertyInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_DATABASE);
        if (cnf == null) {
            return;
        }
        if (methodInfo.getMethod().equals("setJdbcUrl")) {
            String jdbcUrl = cnf.getFirstUri();
            methodInfo.changeArg(0, jdbcUrl);
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.changeArg(0, cnf.getPassword());
        }
    }

    @Override
    public String getName() {
        return Order.REDIRECT.getName();
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
