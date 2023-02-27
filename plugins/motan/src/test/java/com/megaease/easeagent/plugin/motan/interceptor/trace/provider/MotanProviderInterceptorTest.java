package com.megaease.easeagent.plugin.motan.interceptor.trace.provider;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanInterceptorTest;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.weibo.api.motan.rpc.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanProviderInterceptorTest extends MotanInterceptorTest {

    private static final MotanProviderTraceInterceptor motanProviderTraceInterceptor = new MotanProviderTraceInterceptor();

    @Override
    protected Interceptor createInterceptor() {
        return motanProviderTraceInterceptor;
    }


    @Test
    public void order() {
        assertEquals(Order.TRACING.getOrder(), motanProviderTraceInterceptor.order());
    }

    @Test
    public void getType(){
        assertEquals(ConfigConst.PluginID.TRACING,motanProviderTraceInterceptor.getType());
    }

    @Test
    public void testExternalConfig() {
        assertNotNull(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG);
        assertTrue(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.argsCollectEnabled());
        assertTrue(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.resultCollectEnabled());
    }

    @Test
    public void rpcProviderCallSuccess() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(this)
            .args(new Object[]{request, provider})
            .retValue(successResponse)
            .build();

        motanProviderTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.SERVER_REQUEST_CONTEXT)).span();
        motanProviderTraceInterceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        Response retValue = (Response) methodInfo.getRetValue();
        assertProviderTrace(retValue.getValue(),null);
        SpanTestUtils.sameId(span, reportSpan);
    }

    @Test
    public void rpcProviderCallFailure() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(this)
                .args(new Object[]{request, provider})
                .retValue(failureResponse)
                .build();

        motanProviderTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.SERVER_REQUEST_CONTEXT)).span();
        motanProviderTraceInterceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertProviderTrace(null, failureResponse.getException().getMessage());
        SpanTestUtils.sameId(span, reportSpan);
    }

    @Test
    public void rpcProviderCallException() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(this)
                .args(new Object[]{request, provider})
                .throwable(motanException)
                .build();

        motanProviderTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.SERVER_REQUEST_CONTEXT)).span();
        motanProviderTraceInterceptor.after(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        assertProviderTrace(null, methodInfo.getThrowable().getMessage());
        SpanTestUtils.sameId(span, reportSpan);
    }
}
