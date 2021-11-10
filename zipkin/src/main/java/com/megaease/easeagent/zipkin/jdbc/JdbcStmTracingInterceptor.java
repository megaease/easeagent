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

package com.megaease.easeagent.zipkin.jdbc;

import brave.Span;
import brave.propagation.ThreadLocalSpan;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.common.jdbc.DatabaseInfo;
import com.megaease.easeagent.common.jdbc.JdbcUtils;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

public class JdbcStmTracingInterceptor implements AgentInterceptor {

    public static final String ENABLE_KEY = "observability.tracings.jdbc.enabled";
    public static final String SPAN_SQL_QUERY_TAG_NAME = "sql";
    public static final String SPAN_ERROR_TAG_NAME = "error";
    public static final String SPAN_LOCAL_COMPONENT_TAG_NAME = "local-component";
    public static final String SPAN_URL = "url";

    private static final Logger logger = LoggerFactory.getLogger(JdbcStmTracingInterceptor.class);

    private final SQLCompression sqlCompression;
    private final Config config;

    public JdbcStmTracingInterceptor(SQLCompression sqlCompression, Config config) {
        this.sqlCompression = sqlCompression;
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        SqlInfo sqlInfo = ContextUtils.getFromContext(context, SqlInfo.class);
        if (sqlInfo == null) {
            logger.warn("must get sqlInfo from context");
            chain.doBefore(methodInfo, context);
            return;
        }
        Optional.ofNullable(ThreadLocalSpan.CURRENT_TRACER.next()).ifPresent(span -> {
            Statement stm = (Statement) methodInfo.getInvoker();
            span.name(methodInfo.getMethod());
            span.kind(Span.Kind.CLIENT);
            span.tag(SPAN_SQL_QUERY_TAG_NAME, sqlCompression.compress(sqlInfo.getSql()));
            span.tag(SPAN_LOCAL_COMPONENT_TAG_NAME, "database");
            Connection conn = sqlInfo.getConnection();
            String url = JdbcUtils.getUrl(conn);
            if (url != null) {
                span.tag(SPAN_URL, url);
            }
            DatabaseInfo databaseInfo = DatabaseInfo.getFromConnection(conn);
            if (databaseInfo != null) {
                span.remoteServiceName(databaseInfo.getDatabase());
                span.remoteIpAndPort(databaseInfo.getHost(), databaseInfo.getPort());
            }
            span.start();
        });
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Optional.ofNullable(ThreadLocalSpan.CURRENT_TRACER.remove()).ifPresent(span -> {
            if (methodInfo.getThrowable() == null) {
//                span.tag(SPAN_ROW_COUNT_TAG_NAME, String.valueOf(executionInfo.getResult()));
            } else {
                span.tag(SPAN_ERROR_TAG_NAME, getExceptionMessage(methodInfo.getThrowable()));
            }
            span.finish();
        });
        return chain.doAfter(methodInfo, context);
    }

    private String getExceptionMessage(Throwable throwable) {
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
    }
}
