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

package com.megaease.easeagent.metrics.jdbc.interceptor;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.jdbc.JdbcUtils;
import com.megaease.easeagent.metrics.jdbc.AbstractJdbcMetric;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class JdbcConMetricInterceptor extends AbstractJdbcMetric  implements AgentInterceptor {

    public JdbcConMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Object after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context, AgentInterceptorChain chain) {
        Connection connection = (Connection) retValue;
        try {
            String key;
            boolean success = true;
            if (retValue == null) {
                key = ERR_CON_METRIC_KEY;
                success = false;
            } else {
                key = getMetricKey(connection, throwable);
            }
            this.collectMetric(key, success, context);
        } catch (SQLException ignored) {
        }
        return chain.doAfter(invoker, method, args, retValue, throwable, context);
    }

    private static String getMetricKey(Connection con, Throwable throwable) throws SQLException {
        if (throwable != null) {
            return ERR_CON_METRIC_KEY;
        }
        return JdbcUtils.getUrl(con);
    }


}
