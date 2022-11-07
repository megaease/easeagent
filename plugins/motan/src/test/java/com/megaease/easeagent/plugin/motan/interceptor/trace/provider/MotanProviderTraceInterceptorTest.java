package com.megaease.easeagent.plugin.motan.interceptor.trace.provider;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanTraceInterceptorTest;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.weibo.api.motan.rpc.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanProviderTraceInterceptorTest extends MotanTraceInterceptorTest {

    private MotanProviderTraceInterceptor motanProviderTraceInterceptor;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        motanProviderTraceInterceptor = new MotanProviderTraceInterceptor();
        InterceptorTestUtils.init(motanProviderTraceInterceptor, new MotanPlugin());
        motanPluginConfig = AgentFieldReflectAccessor.getStaticFieldValue(MotanProviderTraceInterceptor.class, "MOTAN_PLUGIN_CONFIG");
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
    public void init() {
        InterceptorTestUtils.init(motanProviderTraceInterceptor, new MotanPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(MotanProviderTraceInterceptor.class, "MOTAN_PLUGIN_CONFIG"));
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
