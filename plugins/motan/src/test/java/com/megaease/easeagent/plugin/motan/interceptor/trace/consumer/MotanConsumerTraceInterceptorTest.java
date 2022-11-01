package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
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
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanConsumerTraceInterceptorTest extends MotanTraceInterceptorTest {

    private MotanConsumerTraceInterceptor motanConsumerTraceInterceptor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        motanConsumerTraceInterceptor = new MotanConsumerTraceInterceptor();
        InterceptorTestUtils.init(motanConsumerTraceInterceptor, new MotanPlugin());
        motanPluginConfig = AgentFieldReflectAccessor.getStaticFieldValue(MotanConsumerTraceInterceptor.class, "MOTAN_PLUGIN_CONFIG");
    }


    @Test
    public void order() {
        assertEquals(Order.TRACING.getOrder(), motanConsumerTraceInterceptor.order());
    }

    @Test
    public void getType() {
        assertEquals(ConfigConst.PluginID.TRACING, motanConsumerTraceInterceptor.getType());
    }

    @Test
    public void init() {
        InterceptorTestUtils.init(motanConsumerTraceInterceptor, new MotanPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(MotanConsumerTraceInterceptor.class, "MOTAN_PLUGIN_CONFIG"));
    }

    @Test
    public void rpcConsumerAsyncCallFail() {

        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(successResponse)
            .build();

        DefaultResponseFuture successResponseFuture = new DefaultResponseFuture(null, 0, null);
        methodInfo.setRetValue(successResponseFuture);
        motanConsumerTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.CLIENT_REQUEST_CONTEXT)).span();
        motanConsumerTraceInterceptor.after(methodInfo, context);
        successResponseFuture.onSuccess(successResponse);
        assertTrace(successResponse, null);
    }

    @Test
    public void rpcConsumerAsyncCallSuccess() {
        DefaultResponseFuture failureResponseFuture = new DefaultResponseFuture(null, 0, null);
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponseFuture)
            .build();

        Context context = EaseAgent.getContext();
        motanConsumerTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.CLIENT_REQUEST_CONTEXT)).span();
        motanConsumerTraceInterceptor.after(methodInfo, context);
        failureResponseFuture.onFailure(failureResponse);
        assertTrace(failureResponseFuture, motanException.getMessage());
    }

    @Test
    public void rpcConsumerCallException() {

        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .throwable(motanException)
            .build();

        motanConsumerTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.CLIENT_REQUEST_CONTEXT)).span();
        motanConsumerTraceInterceptor.after(methodInfo, context);
        assertTrace(null, motanException.getMessage());
    }

    @Test
    public void rpcConsumerCallFail() {

        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponse)
            .build();


        motanConsumerTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.CLIENT_REQUEST_CONTEXT)).span();
        motanConsumerTraceInterceptor.after(methodInfo, context);
        assertTrace(failureResponse, motanException.getMessage());
    }

    @Test
    public void rpcConsumerCallSuccess() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(successResponse)
            .build();

        motanConsumerTraceInterceptor.before(methodInfo, context);
        Span span = ((RequestContext) context.get(MotanCtxUtils.CLIENT_REQUEST_CONTEXT)).span();
        motanConsumerTraceInterceptor.after(methodInfo, context);
        assertTrace(successResponse, null);
    }

}
