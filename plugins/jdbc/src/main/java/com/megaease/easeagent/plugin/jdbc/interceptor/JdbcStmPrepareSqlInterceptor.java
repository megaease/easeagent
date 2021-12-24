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

package com.megaease.easeagent.plugin.jdbc.interceptor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.jdbc.JdbcTracingPlugin;
import com.megaease.easeagent.plugin.jdbc.advice.JdbcStatementAdvice;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;

import java.sql.Statement;

@AdviceTo(value = JdbcStatementAdvice.class, plugin = JdbcTracingPlugin.class)
@AdviceTo(value = JdbcStatementAdvice.class, qualifier = "batch", plugin = JdbcTracingPlugin.class)
public class JdbcStmPrepareSqlInterceptor implements NonReentrantInterceptor {
    private static final Logger log = EaseAgent.getLogger(JdbcStmPrepareSqlInterceptor.class);

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        Statement stm = (Statement) methodInfo.getInvoker();
        if (!(stm instanceof DynamicFieldAccessor)) {
            log.warn("statement must implements " + DynamicFieldAccessor.class.getName());
            return;
        }

        SqlInfo sqlInfo = AgentDynamicFieldAccessor.getDynamicFieldValue(stm);
        if (sqlInfo == null) {
            /*
             * This happens:
             * 1. StatementA contains StatementB.
             * 2. Intercept: statementA = con.preparedStatement.
             * 3. Not intercept: statementB = new StatementB(). StatementB can not be set dynamicField value.
             * 4. StatementB invoke other method, like: clearBatch, so interceptor will find dynamicField value is null.
             */
            return;
        }
        String sql = null;
        if (methodInfo.getArgs() != null && methodInfo.getArgs().length > 0) {
            sql = (String) methodInfo.getArgs()[0];
        }
        String method = methodInfo.getMethod();
        if (method.equals("addBatch")) {
            /*
             * user creates PreparedStatement with con.preparedStatement(sql).
             * User can invokes PreparedStatement.addBatch() multi times.
             * In this scenario, sqlInfo should has only one sql.
             */
            if (sql != null) {
                sqlInfo.addSql(sql, true);
            }
        } else if (method.equals("clearBatch")) {
            sqlInfo.clearSql();
        } else if (method.startsWith("execute") && sql != null) {
            sqlInfo.addSql(sql, false);
        }
        context.put(SqlInfo.class, sqlInfo);
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

    @Override
    public int order() {
        return Order.HIGH.getOrder();
    }
}
