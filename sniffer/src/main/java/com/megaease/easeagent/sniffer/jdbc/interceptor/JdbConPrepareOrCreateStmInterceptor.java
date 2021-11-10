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
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;


public class JdbConPrepareOrCreateStmInterceptor implements AgentInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JdbConPrepareOrCreateStmInterceptor.class);

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Statement stm = (Statement) methodInfo.getRetValue();
        SqlInfo sqlInfo = new SqlInfo((Connection) methodInfo.getInvoker());
        if (methodInfo.getMethod().startsWith("prepare")
                && methodInfo.getArgs() != null && methodInfo.getArgs().length > 0) {
            String sql = (String) methodInfo.getArgs()[0];
            if (sql != null) {
                sqlInfo.addSql(sql, false);
            }
        }
        if (stm instanceof DynamicFieldAccessor) {
            ((DynamicFieldAccessor) stm).setEaseAgent$$DynamicField$$Data(sqlInfo);
        } else {
            logger.warn("statement must implements " + DynamicFieldAccessor.class.getName());
        }
        return chain.doAfter(methodInfo, context);
    }
}
