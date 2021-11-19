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

package com.megaease.easeagent.plugin.jdbc.interceptor.tracing;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.jdbc.common.DatabaseInfo;
import com.megaease.easeagent.plugin.jdbc.common.JdbcUtils;
import com.megaease.easeagent.plugin.jdbc.common.MD5SQLCompression;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;
import com.megaease.easeagent.plugin.jdbc.advice.JdbcStatementAdvice;
import com.megaease.easeagent.plugin.utils.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.utils.common.ExceptionUtil;

import java.sql.Connection;
// import java.sql.Statement;

@AdviceTo(JdbcStatementAdvice.class)
public class JdbcStmTracingInterceptor implements FirstEnterInterceptor {
    private final static Logger log = EaseAgent.getLogger(JdbcStmTracingInterceptor.class);

    public static final String SPAN_SQL_QUERY_TAG_NAME = "sql";
    public static final String SPAN_ERROR_TAG_NAME = "error";
    public static final String SPAN_LOCAL_COMPONENT_TAG_NAME = "local-component";
    public static final String SPAN_URL = "url";

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        SqlInfo sqlInfo = ContextUtils.getFromContext(context, SqlInfo.class);
        if (sqlInfo == null) {
            log.warn("must get sqlInfo from context");
            return;
        }
        Span span = context.currentTracing().nextSpan();
        // Statement stm = (Statement) methodInfo.getInvoker();
        span.name(methodInfo.getMethod());
        span.kind(Span.Kind.CLIENT);
        span.tag(SPAN_SQL_QUERY_TAG_NAME,
            MD5SQLCompression.getInstance(context.getConfig()).compress(sqlInfo.getSql()));
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
        ContextUtils.setToContext(context, "span", span);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Span span = ContextUtils.getFromContext(context, "span");
        if (methodInfo.getThrowable() != null) {
            span.tag(SPAN_ERROR_TAG_NAME, ExceptionUtil.getExceptionMessage(methodInfo.getThrowable()));
        }
        span.finish();
    }

    @Override
    public String getName() {
        return Order.TRACING.getName();
    }

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }
}
