package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.BaseInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcProviderTraceInterceptorTest extends BaseInterceptorTest {

	private final SofaRpcProviderTraceInterceptor sofaRpcProviderTraceInterceptor = new SofaRpcProviderTraceInterceptor();

	@Override
	protected SofaRpcTraceBaseInterceptor getInterceptor() {
		return sofaRpcProviderTraceInterceptor;
	}

	@Before
	public void setUp() {
		EaseAgent.configFactory = MockConfig.getPluginConfigManager();

		rpcContext.setRemoteAddress("127.0.0.1",12200);
	}


	@Test
	public void testProviderSuccess() {
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse("success");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(providerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcProviderTraceInterceptor.before(methodInfo, context);
		sofaRpcProviderTraceInterceptor.after(methodInfo, context);

		assertProviderTrace(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	public void testProviderWithResultHasException() {
		RuntimeException runtimeException = new RuntimeException("call exception");
		SofaResponse sofaResponse = new SofaResponse();
		sofaResponse.setAppResponse(runtimeException);
		sofaResponse.setErrorMsg(runtimeException.getMessage());

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(providerInvoker)
				.args(allArguments)
				.retValue(sofaResponse)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcProviderTraceInterceptor.before(methodInfo, context);
		sofaRpcProviderTraceInterceptor.after(methodInfo, context);

		assertProviderTrace(sofaRequest, sofaResponse.getAppResponse());
	}

	@Test
	public void testProviderWithExecuteException() {
		RuntimeException executeException = new RuntimeException("provider call exception");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(providerInvoker)
				.args(allArguments)
				.throwable(executeException)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcProviderTraceInterceptor.before(methodInfo, context);
		sofaRpcProviderTraceInterceptor.after(methodInfo, context);

		assertProviderTrace(sofaRequest, executeException);
	}

	@Test(expected = NullPointerException.class)
	public void testProviderNoObtainedRequestContext() {

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(providerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcProviderTraceInterceptor.before(methodInfo, context);
		RequestContext requestContext = ContextUtils.removeFromContext(context,SofaRpcCtxUtils.SERVER_REQUEST_CONTEXT_KEY);
		requestContext.span().finish();
		requestContext.scope().close();
		sofaRpcProviderTraceInterceptor.after(methodInfo, context);
	}
}