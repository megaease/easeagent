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

package com.megaease.easeagent.plugin.jdbc.interceptor.metric;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.jdbc.JdbcConnectionMetricPlugin;
import com.megaease.easeagent.plugin.jdbc.advice.JdbcDataSourceAdvice;
import com.megaease.easeagent.plugin.jdbc.common.JdbcUtils;

import java.sql.Connection;

@AdviceTo(value = JdbcDataSourceAdvice.class, plugin = JdbcConnectionMetricPlugin.class)
public class JdbcDataSourceMetricInterceptor implements NonReentrantInterceptor {
    private static JdbcMetric metric;
    public static final String ERR_CON_METRIC_KEY = "err-con";

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Tags tags = JdbcMetric.newConnectionTags();
        metric = ServiceMetricRegistry.getOrCreate(config,
            tags, JdbcMetric.METRIC_SUPPLIER);
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Connection connection = (Connection) methodInfo.getRetValue();
        String key;
        boolean success = true;
        if (methodInfo.getRetValue() == null || methodInfo.getThrowable() != null) {
            key = ERR_CON_METRIC_KEY;
            success = false;
        } else {
            key = JdbcUtils.getUrl(connection);
        }
        metric.collectMetric(key, success, context);
    }


    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
