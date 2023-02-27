package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboMetricsInterceptorTest extends AlibabaDubboBaseTest {

	private final AlibabaDubboMetricsInterceptor alibabaDubboMetricsInterceptor = new AlibabaDubboMetricsInterceptor();

	@Test
	public void order() {
		assertEquals(Order.METRIC.getOrder(), alibabaDubboMetricsInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.METRIC.getName(), alibabaDubboMetricsInterceptor.getType());
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

		assertSuccessMetrics();
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

		assertFailureMetrics();
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

		assertFailureMetrics();
    }

	@Override
	protected Interceptor createInterceptor() {
		return alibabaDubboMetricsInterceptor;
	}
}
