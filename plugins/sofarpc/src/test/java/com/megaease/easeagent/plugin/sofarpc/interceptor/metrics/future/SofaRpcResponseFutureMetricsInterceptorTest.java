package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.future;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.interceptor.MockBoltResponseFuture;
import com.megaease.easeagent.plugin.sofarpc.interceptor.initalize.SofaRpcFutureInvokeCallbackConstructInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.BaseMetricsInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.common.SofaRpcMetricsInterceptor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcResponseFutureMetricsInterceptorTest extends BaseMetricsInterceptorTest {
	private final SofaRpcMetricsInterceptor sofaRpcMetricsInterceptor = new SofaRpcMetricsInterceptor();
	private final SofaRpcResponseFutureMetricsInterceptor sofaRpcResponseFutureMetricsInterceptor = new SofaRpcResponseFutureMetricsInterceptor();
	private final SofaRpcFutureInvokeCallbackConstructInterceptor sofaRpcFutureInvokeCallbackConstructInterceptor = new SofaRpcFutureInvokeCallbackConstructInterceptor();
	private final Object[] futureInvokeCallbackConstructMetricsInterceptorArgs = new Object[6];

	@Override
	protected SofaRpcMetricsBaseInterceptor getInterceptor() {
		return sofaRpcResponseFutureMetricsInterceptor;
	}

	@Before
	public void setup() {
		futureInvokeCallbackConstructMetricsInterceptorArgs[2] = new MockBoltResponseFuture();
		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_FUTURE);

		ProviderInfo providerInfo = new ProviderInfo();
		providerInfo.setHost("127.0.0.1");
		providerInfo.setPort(12200);
		rpcContext.setProviderInfo(providerInfo);
	}


	@Test
	public void testConsumerFutureInvokeSuccess() throws InterruptedException {

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructMetricsInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo,context);
		String result = "success";
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.setInvoker(futureInvokeCallbackConstructMetricsInterceptorArgs[2]);
				methodInfo.setArgs(new Object[]{result});
				sofaRpcResponseFutureMetricsInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, result);
	}

	@Test
	@SneakyThrows
	public void testConsumerFutureInvokeFailure() {
		Throwable throwable = new Throwable("Unknown Exception");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructMetricsInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo,context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.setInvoker(futureInvokeCallbackConstructMetricsInterceptorArgs[2]);
				methodInfo.setArgs(new Object[]{throwable});
				sofaRpcResponseFutureMetricsInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, throwable);
	}

	@Test
	@SneakyThrows
	public void testConsumerFutureInvokeException() {
		Throwable throwable = new Throwable("Unknown Exception");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructMetricsInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo,context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.throwable(throwable);
				methodInfo.setInvoker(futureInvokeCallbackConstructMetricsInterceptorArgs[2]);
				sofaRpcResponseFutureMetricsInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, throwable);
	}

	@Test(expected = NullPointerException.class)
	public void testNotObtainedAsyncContext() {

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		methodInfo.setInvoker(futureInvokeCallbackConstructMetricsInterceptorArgs[2]);
		sofaRpcResponseFutureMetricsInterceptor.after(methodInfo, context);
	}
}
