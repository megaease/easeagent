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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.jdbc.JdbcDataSourceMetricPlugin;
import com.megaease.easeagent.plugin.jdbc.advice.JdbcStatementAdvice;
import com.megaease.easeagent.plugin.jdbc.common.MD5SQLCompression;
import com.megaease.easeagent.plugin.jdbc.common.SQLCompression;
import com.megaease.easeagent.plugin.jdbc.common.SQLCompressionFactory;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;

@AdviceTo(value = JdbcStatementAdvice.class, plugin = JdbcDataSourceMetricPlugin.class)
public class JdbcStmMetricInterceptor implements NonReentrantInterceptor {
    private static final int maxCacheSize = 1000;
    private static volatile JdbcMetric metric;
    private static SQLCompression sqlCompression;
    private static Cache<String, String> cache;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        if (metric == null) {
            synchronized (JdbcStmMetricInterceptor.class) {
                if (metric == null) {
                    Tags tags = JdbcMetric.newStmTags();
                    metric = ServiceMetricRegistry.getOrCreate(config,
                        tags,
                        JdbcMetric.METRIC_SUPPLIER);
                    sqlCompression = SQLCompressionFactory.getSqlCompression();
                    cache = CacheBuilder.newBuilder()
                        .maximumSize(maxCacheSize).removalListener(metric).build();

                }
            }
        }
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        SqlInfo sqlInfo = context.get(SqlInfo.class);
        String sql = sqlInfo.getSql();
        String key = sqlCompression.compress(sql);
        metric.collectMetric(key, methodInfo.getThrowable() == null, context);
        String value = cache.getIfPresent(key);
        if (value == null) {
            cache.put(key, "");
        }
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
