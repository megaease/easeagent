package com.megaease.easeagent.zipkin;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.StrictCurrentTraceContext;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.jdbc.SQLCompression;
import com.megaease.easeagent.common.jdbc.SqlInfo;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.jdbc.JdbcStmTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcStmTracingInterceptorTest extends BaseZipkinTest{

    @Test
    public void success() {
        Map<String, String> spanInfoMap = new HashMap<>();
        StrictCurrentTraceContext currentTraceContext = StrictCurrentTraceContext.create();

        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        tmpMap.put("name", span.name());
                        tmpMap.put("remoteServiceName", span.remoteServiceName());
                        tmpMap.put("remotePort", span.remotePort() + "");
                        tmpMap.put("remoteIp", span.remoteIp() + "");
                        tmpMap.put("kind", span.kind().name());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();


        ScopedSpan root = tracer.startScopedSpan("root");

        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        try {
            when(metaData.getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true");
            when(connection.getMetaData()).thenReturn(metaData);
            when(connection.getCatalog()).thenReturn("demo");
        } catch (SQLException ignored) {
        }

        String sql = "select * from user";

        SqlInfo sqlInfo=new SqlInfo(connection);
        sqlInfo.addSql(sql,false);

        Map<Object, Object> context = ContextUtils.createContext();
        context.put(SqlInfo.class,sqlInfo);

        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor(SQLCompression.DEFAULT);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(statement)
                .method("execute")
                .args(new Object[]{sql})
                .retValue(null)
                .throwable(null)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        root.finish();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put(JdbcStmTracingInterceptor.SPAN_SQL_QUERY_TAG_NAME, sql);
        expectedMap.put(JdbcStmTracingInterceptor.SPAN_URL, "jdbc:mysql://127.0.0.1:3306/demo");
        expectedMap.put(JdbcStmTracingInterceptor.SPAN_LOCAL_COMPONENT_TAG_NAME, "database");
        expectedMap.put("name", "execute");
        expectedMap.put("remoteServiceName", "demo");
        expectedMap.put("remotePort", "3306");
        expectedMap.put("remoteIp", "127.0.0.1");
        expectedMap.put("kind", Span.Kind.CLIENT.name());
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

}
