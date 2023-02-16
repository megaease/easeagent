package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.common;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.exception.SofaTimeOutException;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.BaseMetricsInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcMetricsInterceptorTest extends BaseMetricsInterceptorTest {
	private final SofaRpcMetricsInterceptor sofaRpcMetricsInterceptor = new SofaRpcMetricsInterceptor();

	@Override
	protected SofaRpcMetricsBaseInterceptor getInterceptor() {
		return sofaRpcMetricsInterceptor;
	}

	@Before
	public void setUp() {
		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_SYNC);

		ProviderInfo providerInfo = new ProviderInfo();
		providerInfo.setHost("127.0.0.1");
		providerInfo.setPort(12200);
		rpcContext.setProviderInfo(providerInfo);
	}


	@Test
	@SneakyThrows
	public void testSuccess() {
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse("success");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100));
		sofaRpcMetricsInterceptor.after(methodInfo, context);

		assertMetrics(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	@SneakyThrows
	public void testWithResultHasException() {
		SofaTimeOutException timeoutException = new SofaTimeOutException("call timeout");
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse(timeoutException);
		sofaResponse.setErrorMsg(timeoutException.getMessage());

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100));
		sofaRpcMetricsInterceptor.after(methodInfo, context);

		assertMetrics(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	@SneakyThrows
	public void testWithExecuteException() {
		Throwable executeException = new Throwable("method execute exception");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.throwable(executeException)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcMetricsInterceptor.before(methodInfo, context);
		TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100));
		sofaRpcMetricsInterceptor.after(methodInfo, context);

		assertMetrics(sofaRequest, executeException);
	}
	
}