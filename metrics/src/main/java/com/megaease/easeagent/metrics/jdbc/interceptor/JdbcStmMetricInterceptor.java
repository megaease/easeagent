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
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.jdbc.AbstractJdbcMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class JdbcStmMetricInterceptor extends AbstractJdbcMetric implements RemovalListener<String, String>, AgentInterceptor {

    public static final String ENABLE_KEY = "observability.metrics.jdbcStatement.enabled";

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private static final int maxCacheSize = 1000;

    private final SQLCompression sqlCompression;

    private final Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).removalListener(this).build();

    private final Config config;

    public JdbcStmMetricInterceptor(MetricRegistry metricRegistry, SQLCompression sqlCompression, Config config) {
        super(metricRegistry);
        this.sqlCompression = sqlCompression;
        this.config = config;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
            return chain.doAfter(methodInfo, context);
        }
        SqlInfo sqlInfo = (SqlInfo) context.get(SqlInfo.class);
        String sql = sqlInfo.getSql();
        String key = this.sqlCompression.compress(sql);
        this.collectMetric(key, methodInfo.getThrowable() == null, context);
        String value = cache.getIfPresent(key);
        if (value == null) {
            cache.put(key, "");
        }
        return chain.doAfter(methodInfo, context);
    }


    @Override
    public void onRemoval(@Nonnull RemovalNotification<String, String> notification) {
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

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new JDBCConverter("application", "jdbc-statement",
                "signature", attributes);
    }
}
