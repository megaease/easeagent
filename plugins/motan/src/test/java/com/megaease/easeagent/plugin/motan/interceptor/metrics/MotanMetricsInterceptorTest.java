package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanInterceptorTest;
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import com.weibo.api.motan.rpc.ResponseFuture;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanMetricsInterceptorTest extends MotanInterceptorTest {

    private static final MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();

    @Test
    public void getType() {
        assertEquals(ConfigConst.PluginID.METRIC, motanMetricsInterceptor.getType());
    }

    @Test
    public void order() {
        assertEquals(Order.METRIC.getOrder(), motanMetricsInterceptor.order());
    }

    @Override
    protected Interceptor createInterceptor() {
        return motanMetricsInterceptor;
    }


    @Test
    public void before() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponse)
            .build();
        motanMetricsInterceptor.before(methodInfo, context);
        assertNotNull(ContextUtils.getFromContext(context, MotanCtxUtils.BEGIN_TIME));
    }

    @Test
    public void rpcCallFailure() {

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(failureResponse)
                .build();

        Context context = EaseAgent.getContext();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        assertFailureMetrics();
    }

    @Test
    public void rpcCallException() {

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .throwable(failureResponse.getException())
                .build();

        Context context = EaseAgent.getContext();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        assertFailureMetrics();
    }

    @Test
    public void rpcCallSuccess() {

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(successResponse)
                .build();

        Context context = EaseAgent.getContext();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        assertSuccessMetrics();
    }


    @Test
    public void rpcAsyncCallFailure() throws InterruptedException {
        DefaultResponseFuture defaultResponseFuture = new DefaultResponseFuture(null, 0, null);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(defaultResponseFuture)
                .build();

        Context context = EaseAgent.getContext();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);
        ResponseFuture retValue = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            retValue.onFailure(failureResponse);
        });
        thread.start();
        thread.join();

        assertFailureMetrics();
    }

    @Test
    public void rpcAsyncCallSuccess() throws InterruptedException {
        DefaultResponseFuture defaultResponseFuture = new DefaultResponseFuture(null, 0, null);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(defaultResponseFuture)
                .build();

        Context context = EaseAgent.getContext();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);
        ResponseFuture retValue = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            retValue.onSuccess(successResponse);
        });
        thread.start();
        thread.join();

        assertSuccessMetrics();
    }
}
