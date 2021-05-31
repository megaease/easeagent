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

package com.megaease.easeagent.sniffer.jdbc.interceptor;

import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.Map;

public class JdbcStmPrepareSqlInterceptor implements AgentInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JdbcStmPrepareSqlInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Statement stm = (Statement) methodInfo.getInvoker();
        if (!(stm instanceof DynamicFieldAccessor)) {
            logger.warn("statement must implements " + DynamicFieldAccessor.class.getName());
            chain.doBefore(methodInfo, context);
            return;
        }
        SqlInfo sqlInfo = (SqlInfo) ((DynamicFieldAccessor) stm).getEaseAgent$$DynamicField$$Data();
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
        chain.doBefore(methodInfo, context);
    }
}
