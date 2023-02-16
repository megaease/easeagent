package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.callback;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.exception.SofaTimeOutException;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.BaseMetricsInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.common.SofaRpcMetricsInterceptor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcResponseCallbackMetricsInterceptorTest extends BaseMetricsInterceptorTest {
	private final SofaRpcResponseCallbackMetricsInterceptor sofaRpcResponseCallbackMetricsInterceptor = new SofaRpcResponseCallbackMetricsInterceptor();
	private final SofaRpcMetricsInterceptor sofaRpcMetricsInterceptor = new SofaRpcMetricsInterceptor();
	private final MockSofaResponseCallback mockSofaResponseCallback = new MockSofaResponseCallback();
	private final Object[] responseCallbackMetricsInterceptorArgs = new Object[6];


	@Override
	protected SofaRpcMetricsBaseInterceptor getInterceptor() {
		return sofaRpcResponseCallbackMetricsInterceptor;
	}

	@Before
	public void setup() {
		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_CALLBACK);

		responseCallbackMetricsInterceptorArgs[2] = mockSofaResponseCallback;
		ProviderInfo providerInfo = new ProviderInfo();
		providerInfo.setHost("127.0.0.1");
		providerInfo.setPort(12200);
		rpcContext.setProviderInfo(providerInfo);
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(responseCallbackMetricsInterceptorArgs);
		sofaRpcResponseCallbackMetricsInterceptor.before(methodInfo, context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackMetrics sofaRpcResponseCallbackMetrics = (SofaRpcResponseCallbackMetrics) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackMetrics.onAppResponse("success", sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, mockSofaResponseCallback.getResult());
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackWithException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(responseCallbackMetricsInterceptorArgs);
		sofaRpcResponseCallbackMetricsInterceptor.before(methodInfo, context);

		Throwable throwable = new Throwable("call exception");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackMetrics sofaRpcResponseCallbackMetrics = (SofaRpcResponseCallbackMetrics) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackMetrics.onAppException(throwable, sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, mockSofaResponseCallback.getResult());
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackWithSofaException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(responseCallbackMetricsInterceptorArgs);
		sofaRpcResponseCallbackMetricsInterceptor.before(methodInfo, context);

		SofaTimeOutException sofaTimeOutException = new SofaTimeOutException("call timeout");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackMetrics sofaRpcResponseCallbackMetrics = (SofaRpcResponseCallbackMetrics) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackMetrics.onSofaException(sofaTimeOutException, sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertMetrics(sofaRequest, mockSofaResponseCallback.getResult());
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackNoObtainedRequestContext() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		sofaRpcMetricsInterceptor.after(methodInfo, context);
		methodInfo.setArgs(responseCallbackMetricsInterceptorArgs);
		sofaRpcResponseCallbackMetricsInterceptor.before(methodInfo, context);

		SofaTimeOutException sofaTimeOutException = new SofaTimeOutException("call timeout");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackMetrics sofaRpcResponseCallbackMetrics = (SofaRpcResponseCallbackMetrics) methodInfo.getArgs()[2];
				AsyncContext asyncContext = AgentFieldReflectAccessor.getFieldValue(sofaRpcResponseCallbackMetrics, "asyncContext");
				asyncContext.put(SofaRpcCtxUtils.METRICS_INTERFACE_NAME, null);
				sofaRpcResponseCallbackMetrics.onSofaException(sofaTimeOutException, sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();
	}
}
