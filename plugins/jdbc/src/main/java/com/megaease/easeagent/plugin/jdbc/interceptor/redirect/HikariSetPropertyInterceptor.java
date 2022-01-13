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
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.jdbc.JdbcRedirectPlugin;
import com.megaease.easeagent.plugin.jdbc.advice.HikariDataSourceAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(value = HikariDataSourceAdvice.class, plugin = JdbcRedirectPlugin.class)
public class HikariSetPropertyInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(HikariSetPropertyInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.DATABASE.getConfig();
        if (cnf == null) {
            return;
        }
        if (methodInfo.getMethod().equals("setJdbcUrl")) {
            String jdbcUrl = cnf.getFirstUri();
            LOGGER.info("Redirect JDBC Url: {} to {}", methodInfo.getArgs()[0], jdbcUrl);
            methodInfo.changeArg(0, jdbcUrl);
            RedirectProcessor.redirected(Redirect.DATABASE, jdbcUrl);
        } else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            LOGGER.info("Redirect JDBC Username: {} to {}", methodInfo.getArgs()[0], cnf.getUserName());
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            LOGGER.info("Redirect JDBC Password: *** to ***");
            methodInfo.changeArg(0, cnf.getPassword());
        }
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
