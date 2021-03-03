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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.megaease.easeagent.core.utils.SQLCompression;
import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.jdbc.ExecutionInfo;
import com.megaease.easeagent.core.jdbc.JdbcContextInfo;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.jdbc.AbstractJdbcMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

public class JdbcStatementMetricInterceptor extends AbstractJdbcMetric implements RemovalListener<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    // TODO: 2021/2/26 maxCacheSize should use configuration for JMX
    private static final int maxCacheSize = 1000;

    private final SQLCompression sqlCompression;

    private final Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).removalListener(this).build();

    public JdbcStatementMetricInterceptor(MetricRegistry metricRegistry, SQLCompression sqlCompression) {
        super(metricRegistry);
        this.sqlCompression = sqlCompression;
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        JdbcContextInfo jdbcContextInfo = (JdbcContextInfo) context.get(JdbcContextInfo.class);
        ExecutionInfo executionInfo = jdbcContextInfo.getExecutionInfo((Statement) invoker);
        String sql = executionInfo.getSql();
        String key = this.sqlCompression.compress(sql);
        this.collectMetric(key, exception == null, context);
        String value = cache.getIfPresent(key);
        if (value == null) {
            cache.put(key, "");
        }
    }


    @Override
    public void onRemoval(RemovalNotification<String, String> notification) {
        try {
            String key = notification.getKey();
            ImmutableList<String> list = ImmutableList.of(
                    Optional.ofNullable(this.metricNameFactory.counterName(key, MetricSubType.DEFAULT)).orElse(""),
                    Optional.ofNullable(this.metricNameFactory.counterName(key, MetricSubType.ERROR)).orElse(""),
                    Optional.ofNullable(this.metricNameFactory.meterName(key, MetricSubType.DEFAULT)).orElse(""),
                    Optional.ofNullable(this.metricNameFactory.meterName(key, MetricSubType.ERROR)).orElse(""),
                    Optional.ofNullable(this.metricNameFactory.timerName(key, MetricSubType.DEFAULT)).orElse(""),
                    Optional.ofNullable(this.metricNameFactory.gaugeName(key, MetricSubType.DEFAULT)).orElse(""));

            list.forEach(metricRegistry::remove);
        } catch (Exception e) {
            logger.warn("remove lru cache failed: " + e.getMessage());
        }
    }
}
