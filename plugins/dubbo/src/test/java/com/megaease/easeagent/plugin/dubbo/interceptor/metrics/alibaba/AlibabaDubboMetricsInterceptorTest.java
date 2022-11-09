package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboMetricsInterceptorTest extends AlibabaDubboBaseTest {

	private AlibabaDubboMetricsInterceptor alibabaDubboMetricsInterceptor;

	@Before
	public void setup() {
		super.setup();
		alibabaDubboMetricsInterceptor = new AlibabaDubboMetricsInterceptor();
		initDubboMetrics();
	}

	@Test
	public void order() {
		assertEquals(Order.METRIC.getOrder(), alibabaDubboMetricsInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.METRIC.getName(), alibabaDubboMetricsInterceptor.getType());
	}

	@Test
	public void init() {
		assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(AlibabaDubboMetricsInterceptor.class, "DUBBO_METRICS"));
	}

	@Test
	public void before() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .build();

		Context context = EaseAgent.getContext();
		alibabaDubboMetricsInterceptor.before(methodInfo, context);
		assertNotNull(ContextUtils.getBeginTime(context));
	}


	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(successResult)
				.build();

		Context context = EaseAgent.getContext();
		alibabaDubboMetricsInterceptor.before(methodInfo, context);
		alibabaDubboMetricsInterceptor.after(methodInfo, context);
		LastJsonReporter lastJsonReporter = getLastJsonReporter();
		Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
		assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
		assertEquals(0, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		lastJsonReporter.clean();
	}

	@Test
	public void rpcConsumerCallFailure() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(failureResult)
				.build();

		Context context = EaseAgent.getContext();
		alibabaDubboMetricsInterceptor.before(methodInfo, context);
		alibabaDubboMetricsInterceptor.after(methodInfo, context);
		LastJsonReporter lastJsonReporter = getLastJsonReporter();
		Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
		assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
		assertEquals(1, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		lastJsonReporter.clean();
	}


    @Test
    public void rpcConsumerCallException() {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(consumerInvoker)
            .method(consumerInvocation.getMethodName())
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .throwable(new RuntimeException("mock exception"))
            .build();

        Context context = EaseAgent.getContext();
        alibabaDubboMetricsInterceptor.before(methodInfo, context);
        alibabaDubboMetricsInterceptor.after(methodInfo, context);
        LastJsonReporter lastJsonReporter = getLastJsonReporter();
        Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
        lastJsonReporter.clean();
    }

	private void initDubboMetrics() {
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboMetricsInterceptor, dubboPlugin);
	}

	@NotNull
	private LastJsonReporter getLastJsonReporter() {
		TagVerifier tagVerifier = new TagVerifier()
				.add("category", "application")
				.add("type", "dubbo")
				.add("service", AlibabaDubboCtxUtils.interfaceSignature(consumerInvocation));
		LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
		return lastJsonReporter;
	}

}
