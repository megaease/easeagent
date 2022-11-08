package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.protocol.rpc.DefaultRpcReferer;
import com.weibo.api.motan.rpc.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MotanMetricsInterceptorTest {

    @Mock
    DefaultRpcReferer<?> defaultRpcReferer;

    @Mock
    DefaultRequest request;

    DefaultResponse successResponse;

    DefaultResponse failureResponse;


    @Test
    public void getType() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, motanMetricsInterceptor.getType());
    }
    @Test
    public void order() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        assertEquals(Order.METRIC.getOrder(), motanMetricsInterceptor.order());
    }

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        URL clientUrl = URL.valueOf("motan://127.0.0.1:1234/com.megaease.easeagent.motan.TestService.test(String,Integer)");
        when(defaultRpcReferer.getUrl()).thenReturn(clientUrl);
        when(request.getInterfaceName()).thenReturn("com.megaease.easeagent.motan.TestService");
        when(request.getMethodName()).thenReturn("test");
        when(request.getParamtersDesc()).thenReturn("String,Integer");
        when(request.getArguments()).thenReturn(new Object[]{"motan test", 1234});

        RuntimeException motanException = new RuntimeException("motan exception");
        successResponse = new DefaultResponse("success");
        failureResponse = new DefaultResponse();
        failureResponse.setException(motanException);
    }

    @Test
    public void init() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(motanMetricsInterceptor, "motanMetric"));
    }

    @Test
    public void before() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());

        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponse)
            .build();
        motanMetricsInterceptor.before(methodInfo, context);
        assertNotNull(ContextUtils.getBeginTime(context));
    }

    @Test
    public void rpcCallFailure() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(failureResponse)
                .build();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        TagVerifier tagVerifier = new TagVerifier()
                .add("category", "application")
                .add("type", "motan")
                .add("service", MotanCtxUtils.interfaceSignature(request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
    }

    @Test
    public void rpcCallException() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .throwable(failureResponse.getException())
                .build();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        TagVerifier tagVerifier = new TagVerifier()
                .add("category", "application")
                .add("type", "motan")
                .add("service", MotanCtxUtils.interfaceSignature(request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
    }

    @Test
    public void rpcCallSuccess() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(successResponse)
                .build();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);

        TagVerifier tagVerifier = new TagVerifier()
                .add("category", "application")
                .add("type", "motan")
                .add("service", MotanCtxUtils.interfaceSignature(request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(0, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
    }


    @Test
    public void rpcAsyncCallFailure() throws InterruptedException {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();
        DefaultResponseFuture defaultResponseFuture = new DefaultResponseFuture(null, 0, null);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(defaultResponseFuture)
                .build();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);
        ResponseFuture retValue = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            retValue.onFailure(failureResponse);
        });
        thread.start();
        thread.join();

        TagVerifier tagVerifier = new TagVerifier()
                .add("category", "application")
                .add("type", "motan")
                .add("service", MotanCtxUtils.interfaceSignature(request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
    }

    @Test
    public void rpcAsyncCallSuccess() throws InterruptedException {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();
        DefaultResponseFuture defaultResponseFuture = new DefaultResponseFuture(null, 0, null);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(defaultRpcReferer)
                .args(new Object[]{request})
                .retValue(defaultResponseFuture)
                .build();
        motanMetricsInterceptor.before(methodInfo, context);
        motanMetricsInterceptor.after(methodInfo, context);
        ResponseFuture retValue = (ResponseFuture) methodInfo.getRetValue();
        Thread thread = new Thread(() -> {
            retValue.onSuccess(successResponse);
        });
        thread.start();
        thread.join();

        TagVerifier tagVerifier = new TagVerifier()
                .add("category", "application")
                .add("type", "motan")
                .add("service", MotanCtxUtils.interfaceSignature(request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(0, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
    }
}
