package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.apache;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.interceptor.ApacheDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ApacheDubboMetricsInterceptorTest extends ApacheDubboBaseTest {

	private static final ApacheDubboMetricsInterceptor apacheDubboMetricsInterceptor = new ApacheDubboMetricsInterceptor();

	@Override
	protected Interceptor createInterceptor() {
		return apacheDubboMetricsInterceptor;
	}

	@Test
	public void order() {
		assertEquals(Order.METRIC.getOrder(), apacheDubboMetricsInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.METRIC.getName(), apacheDubboMetricsInterceptor.getType());
	}

	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(successResponse)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboMetricsInterceptor.before(methodInfo, context);
		apacheDubboMetricsInterceptor.after(methodInfo, context);

		assertSuccessMetrics();
	}

	@Test
	public void rpcConsumerCallFailure() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(failureResponse)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboMetricsInterceptor.before(methodInfo, context);
		apacheDubboMetricsInterceptor.after(methodInfo, context);

		assertFailureMetrics();
	}

	@Test
	public void rpcConsumerCallException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.throwable(failureResponse.getException())
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboMetricsInterceptor.before(methodInfo, context);
		apacheDubboMetricsInterceptor.after(methodInfo, context);

		assertFailureMetrics();
	}


	@Test
	public void rpcConsumerAsyncCallSuccess() throws InterruptedException {
        MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(asyncRpcResult)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboMetricsInterceptor.before(methodInfo, context);
		apacheDubboMetricsInterceptor.after(methodInfo, context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRpcResult.complete(successResponse);
            }
        });
        thread.start();
        thread.join();

        assertSuccessMetrics();
	}

	@Test
	public void rpcConsumerAsyncCallFailure() throws InterruptedException {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.method(consumerInvocation.getMethodName())
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(asyncRpcResult)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboMetricsInterceptor.before(methodInfo, context);
		apacheDubboMetricsInterceptor.after(methodInfo, context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRpcResult.complete(failureResponse);
            }
        });
        thread.start();
        thread.join();

		assertFailureMetrics();
	}
}
