package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.rpc.RpcContext;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboAsyncMetricsInterceptorTest extends AlibabaDubboBaseTest {

	private static final AlibabaDubboMetricsInterceptor alibabaDubboMetricsInterceptor = new AlibabaDubboMetricsInterceptor();
	private static final AlibabaDubboAsyncMetricsInterceptor alibabaDubboAsyncMetricsInterceptor = new AlibabaDubboAsyncMetricsInterceptor();


	@Test
	public void order() {
		assertEquals(Order.METRIC.getOrder(), alibabaDubboAsyncMetricsInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.METRIC.getName(), alibabaDubboAsyncMetricsInterceptor.getType());
	}

	@Override
	protected Interceptor createInterceptor() {
		return alibabaDubboMetricsInterceptor;
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

		assertSuccessMetrics();
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

        assertFailureMetrics();
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

		assertFailureMetrics();
	}
}
