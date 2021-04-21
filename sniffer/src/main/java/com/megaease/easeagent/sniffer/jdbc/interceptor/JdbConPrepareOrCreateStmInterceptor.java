package com.megaease.easeagent.sniffer.jdbc.interceptor;

import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;


public class JdbConPrepareOrCreateStmInterceptor implements AgentInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JdbConPrepareOrCreateStmInterceptor.class);

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Statement stm = (Statement) methodInfo.getRetValue();
        SqlInfo sqlInfo = new SqlInfo((Connection) methodInfo.getInvoker());
        if (methodInfo.getMethod().startsWith("prepare")) {
            if (methodInfo.getArgs() != null && methodInfo.getArgs().length > 0) {
                String sql = (String) methodInfo.getArgs()[0];
                sqlInfo.addSql(sql, false);
            }
        }
        if (stm instanceof DynamicFieldAccessor) {
            ((DynamicFieldAccessor) stm).setEaseAgent$$DynamicField$$Data(sqlInfo);
        } else {
            logger.error("statement must implements " + DynamicFieldAccessor.class.getName());
        }
        return chain.doAfter(methodInfo, context);
    }
}
