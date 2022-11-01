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
import com.weibo.api.motan.rpc.DefaultRequest;
import com.weibo.api.motan.rpc.DefaultResponse;
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import com.weibo.api.motan.rpc.URL;
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
        URL clientUrl = URL.valueOf("motan://127.0.0.1:1234/com.megaease.easeagent.motan.TestService.test(String,Integer)?nodeType=referer");
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
    public void after() {
        MotanMetricsInterceptor motanMetricsInterceptor = new MotanMetricsInterceptor();
        InterceptorTestUtils.init(motanMetricsInterceptor, new MotanPlugin());
        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);

        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(defaultRpcReferer)
            .args(new Object[]{request})
            .retValue(failureResponse)
            .build();
        motanMetricsInterceptor.after(methodInfo, context);

        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "motan")
            .add("service", MotanCtxUtils.endpoint(defaultRpcReferer.getUrl(), request));
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

        //1-15 minute rate
        assertNotNull(metrics.get(MetricField.M1_ERROR_RATE.getField()));
        assertNotNull(metrics.get(MetricField.M5_ERROR_RATE.getField()));
        assertNotNull(metrics.get(MetricField.M15_ERROR_RATE.getField()));
        assertNotNull(metrics.get(MetricField.M1_RATE.getField()));
        assertNotNull(metrics.get(MetricField.M5_RATE.getField()));
        assertNotNull(metrics.get(MetricField.M15_RATE.getField()));
        assertNotNull(metrics.get(MetricField.MEAN_RATE.getField()));


        // execution time
        assertNotNull(metrics.get(MetricField.MIN_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.MAX_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.MEAN_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P25_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P50_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P75_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P95_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P98_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P99_EXECUTION_TIME.getField()));
        assertNotNull(metrics.get(MetricField.P999_EXECUTION_TIME.getField()));

        methodInfo.setRetValue(successResponse);
        motanMetricsInterceptor.after(methodInfo, context);
        lastJsonReporter.clean();
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(2, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

        DefaultResponseFuture asyncResponse = new DefaultResponseFuture(null, -1, null);
        methodInfo.setRetValue(asyncResponse);
        motanMetricsInterceptor.after(methodInfo, context);
        asyncResponse.onSuccess(successResponse);
        lastJsonReporter.clean();
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(3, metrics.get(MetricField.EXECUTION_COUNT.getField()));
    }
}
