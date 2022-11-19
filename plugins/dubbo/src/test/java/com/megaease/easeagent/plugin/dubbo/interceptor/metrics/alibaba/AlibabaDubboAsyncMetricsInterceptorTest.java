package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.rpc.RpcContext;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboAsyncMetricsInterceptorTest extends AlibabaDubboBaseTest {

	private AlibabaDubboMetricsInterceptor alibabaDubboMetricsInterceptor;
	private AlibabaDubboAsyncMetricsInterceptor alibabaDubboAsyncMetricsInterceptor;


	@Before
	public void setup() {
		super.setup();
		alibabaDubboMetricsInterceptor = new AlibabaDubboMetricsInterceptor();
        alibabaDubboAsyncMetricsInterceptor = new AlibabaDubboAsyncMetricsInterceptor();
		initDubboMetrics();
	}

	@Test
	public void order() {
		assertEquals(Order.METRIC.getOrder(), alibabaDubboAsyncMetricsInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.METRIC.getName(), alibabaDubboAsyncMetricsInterceptor.getType());
	}

	@Test
	public void init() {
		assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(AlibabaDubboAsyncMetricsInterceptor.class, "DUBBO_METRICS"));
	}

	@Test
	public void rpcConsumerAsyncCallSuccess() throws InterruptedException {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(asyncConsumerInvocation.getInvoker())
				.method(asyncConsumerInvocation.getMethodName())
				.args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
				.build();

		Context context = EaseAgent.getContext();
		RpcContext.getContext().setFuture(futureAdapter);
		alibabaDubboMetricsInterceptor.before(methodInfo, context);
        methodInfo.setArgs(new Object[]{responseCallback});
        alibabaDubboAsyncMetricsInterceptor.before(methodInfo,context);

        AlibabaDubboMetricsCallback alibabaDubboMetricsCallback = (AlibabaDubboMetricsCallback) methodInfo.getArgs()[0];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                alibabaDubboMetricsCallback.done(successResult);
            }
        });
        thread.start();
        thread.join();

        LastJsonReporter lastJsonReporter = getLastJsonReporter();
		Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
		assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
		assertEquals(0, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		lastJsonReporter.clean();
	}

	@Test
	public void rpcConsumerAsyncCallFailure() throws InterruptedException {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(asyncConsumerInvocation.getInvoker())
            .method(asyncConsumerInvocation.getMethodName())
            .args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
            .build();

        Context context = EaseAgent.getContext();
        RpcContext.getContext().setFuture(futureAdapter);
        alibabaDubboMetricsInterceptor.before(methodInfo, context);
        methodInfo.setArgs(new Object[]{responseCallback});
        alibabaDubboAsyncMetricsInterceptor.before(methodInfo,context);

        AlibabaDubboMetricsCallback alibabaDubboMetricsCallback = (AlibabaDubboMetricsCallback) methodInfo.getArgs()[0];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                alibabaDubboMetricsCallback.caught(failureResult.getException());
            }
        });
        thread.start();
        thread.join();

        LastJsonReporter lastJsonReporter = getLastJsonReporter();
        Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
        lastJsonReporter.clean();
	}

	@Test
	public void rpcConsumerAsyncCallTimeout() throws InterruptedException {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(asyncConsumerInvocation.getInvoker())
            .method(asyncConsumerInvocation.getMethodName())
            .args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
            .build();

        Context context = EaseAgent.getContext();
        RpcContext.getContext().setFuture(futureAdapter);
        alibabaDubboMetricsInterceptor.before(methodInfo, context);
        methodInfo.setArgs(new Object[]{responseCallback});
        alibabaDubboAsyncMetricsInterceptor.before(methodInfo,context);

        AlibabaDubboMetricsCallback alibabaDubboMetricsCallback = (AlibabaDubboMetricsCallback)methodInfo.getArgs()[0];
        futureAdapter.getFuture().setCallback(alibabaDubboMetricsCallback);
        TimeUnit.SECONDS.sleep(1);

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
				.add("service", AlibabaDubboCtxUtils.interfaceSignature(asyncConsumerInvocation));
		LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
		return lastJsonReporter;
	}

}
