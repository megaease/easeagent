package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.BaseInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcConsumerTraceInterceptorTest extends BaseInterceptorTest {
	private final SofaRpcConsumerTraceInterceptor consumerTraceInterceptor = new SofaRpcConsumerTraceInterceptor();

	@Override
	protected SofaRpcTraceBaseInterceptor getInterceptor() {
		return consumerTraceInterceptor;
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
	public void testConsumerSuccess() {
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse("success");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		consumerTraceInterceptor.before(methodInfo, context);
		consumerTraceInterceptor.after(methodInfo, context);

		assertConsumerTrace(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	public void testConsumerWithResultHasException() {
		RuntimeException runtimeException = new RuntimeException("call timeout");
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse(runtimeException);
		sofaResponse.setErrorMsg(runtimeException.getMessage());

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		consumerTraceInterceptor.before(methodInfo, context);
		consumerTraceInterceptor.after(methodInfo, context);

		assertConsumerTrace(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	public void testConsumerWithExecuteException() {
		Throwable executeException = new Throwable("method execute exception");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.throwable(executeException)
				.build();

		Context context = EaseAgent.getContext();
		consumerTraceInterceptor.before(methodInfo, context);
		consumerTraceInterceptor.after(methodInfo, context);

		assertConsumerTrace(sofaRequest, executeException);
	}


	@Test(expected = NullPointerException.class)
	public void testConsumerNoObtainedRequestContext() {

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		consumerTraceInterceptor.before(methodInfo, context);
		RequestContext requestContext = context.remove(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
		requestContext.span().finish();
		requestContext.scope().close();
		consumerTraceInterceptor.after(methodInfo, context);
	}
	
}