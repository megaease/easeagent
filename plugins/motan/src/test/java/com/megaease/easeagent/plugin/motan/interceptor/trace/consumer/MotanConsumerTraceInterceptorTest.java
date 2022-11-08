package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.interceptor.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanTraceInterceptorTest;
import com.weibo.api.motan.rpc.DefaultResponse;
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import com.weibo.api.motan.rpc.ResponseFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanConsumerTraceInterceptorTest extends MotanTraceInterceptorTest {

    private MotanConsumerTraceInterceptor motanConsumerTraceInterceptor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        motanConsumerTraceInterceptor = new MotanConsumerTraceInterceptor();
        InterceptorTestUtils.init(motanConsumerTraceInterceptor, new MotanPlugin());
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
        assertNotNull(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG);
        assertTrue(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.argsCollectEnabled());
        assertTrue(MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.resultCollectEnabled());
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
        motanConsumerTraceInterceptor.after(methodInfo, context);
        DefaultResponse defaultResponse = (DefaultResponse) methodInfo.getRetValue();
        assertConsumerTrace(defaultResponse.getValue(), null);
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
        motanConsumerTraceInterceptor.after(methodInfo, context);
        DefaultResponse defaultResponse = (DefaultResponse) methodInfo.getRetValue();
        assertConsumerTrace(null, defaultResponse.getException().getMessage());
    }

    @Test
    public void rpcConsumerAsyncCallFail() throws InterruptedException {
        DefaultResponseFuture failureResponseFuture = new DefaultResponseFuture(null, 0, null);
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponseFuture)
            .build();

        motanConsumerTraceInterceptor.before(methodInfo, context);
        motanConsumerTraceInterceptor.after(methodInfo, context);
        ResponseFuture responseFuture = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            responseFuture.onFailure(failureResponse);
        });
        thread.start();
        thread.join();
        assertConsumerTrace(null, responseFuture.getException().getMessage());
    }

    @Test
    public void rpcConsumerAsyncCallSuccess() throws InterruptedException {
        DefaultResponseFuture successResponseFuture = new DefaultResponseFuture(null, 0, null);
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(successResponseFuture)
            .build();

        Context context = EaseAgent.getContext();
        motanConsumerTraceInterceptor.before(methodInfo, context);
        motanConsumerTraceInterceptor.after(methodInfo, context);
        ResponseFuture retValue = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            retValue.onSuccess(successResponse);
        });
        thread.start();
        thread.join();
        assertConsumerTrace(retValue.getValue(), null);
    }

}
