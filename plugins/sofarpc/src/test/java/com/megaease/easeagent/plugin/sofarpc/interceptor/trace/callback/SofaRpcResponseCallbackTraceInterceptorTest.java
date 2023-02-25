package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.callback;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.exception.SofaTimeOutException;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.BaseInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common.SofaRpcConsumerTraceInterceptor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcResponseCallbackTraceInterceptorTest extends BaseInterceptorTest {
	private final SofaRpcResponseCallbackTraceInterceptor sofaRpcResponseCallbackTraceInterceptor = new SofaRpcResponseCallbackTraceInterceptor();
	private final SofaRpcConsumerTraceInterceptor sofaRpcConsumerTraceInterceptor = new SofaRpcConsumerTraceInterceptor();
	private final MockSofaResponseCallback mockSofaResponseCallback = new MockSofaResponseCallback();
	private final Object[] responseCallbackTraceInterceptorArgs = new Object[6];


	@Override
	protected SofaRpcTraceBaseInterceptor getInterceptor() {
		return sofaRpcResponseCallbackTraceInterceptor;
	}

	@Before
	public void setup() {
		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_CALLBACK);

		responseCallbackTraceInterceptorArgs[2] = mockSofaResponseCallback;
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
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		sofaRpcConsumerTraceInterceptor.after(methodInfo,context);
		methodInfo.setArgs(responseCallbackTraceInterceptorArgs);
		sofaRpcResponseCallbackTraceInterceptor.before(methodInfo, context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackTrace sofaRpcResponseCallbackTrace = (SofaRpcResponseCallbackTrace) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackTrace.onAppResponse("success", sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, mockSofaResponseCallback.getResult());
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackWithException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		methodInfo.setArgs(responseCallbackTraceInterceptorArgs);
		sofaRpcResponseCallbackTraceInterceptor.before(methodInfo, context);

		Throwable throwable = new Throwable("call exception");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackTrace sofaRpcResponseCallbackTrace = (SofaRpcResponseCallbackTrace) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackTrace.onAppException(throwable, sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, mockSofaResponseCallback.getResult());
	}

	@SneakyThrows
	@Test
	public void testConsumerCallbackWithSofaException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		methodInfo.setArgs(responseCallbackTraceInterceptorArgs);
		sofaRpcResponseCallbackTraceInterceptor.before(methodInfo, context);

		SofaTimeOutException sofaTimeOutException = new SofaTimeOutException("call timeout");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackTrace sofaRpcResponseCallbackTrace = (SofaRpcResponseCallbackTrace) methodInfo.getArgs()[2];
				sofaRpcResponseCallbackTrace.onSofaException(sofaTimeOutException, sofaRequest.getMethod().getName(), sofaRequest);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, mockSofaResponseCallback.getResult());
	}


	@Test(expected = NullPointerException.class)
	public void testConsumerNoObtainedRequestContext() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		methodInfo.setArgs(responseCallbackTraceInterceptorArgs);
		RequestContext requestContext = ContextUtils.removeFromContext(context, SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
		requestContext.span().finish();
		requestContext.scope().close();
		sofaRpcResponseCallbackTraceInterceptor.before(methodInfo, context);

	}


	@SneakyThrows
	@Test
	public void testConsumerCallbackNoObtainedRequestContext() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();


		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		methodInfo.setArgs(responseCallbackTraceInterceptorArgs);
		sofaRpcResponseCallbackTraceInterceptor.before(methodInfo, context);

		SofaTimeOutException sofaTimeOutException = new SofaTimeOutException("call timeout");
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SofaRpcResponseCallbackTrace sofaRpcResponseCallbackTrace = (SofaRpcResponseCallbackTrace) methodInfo.getArgs()[2];
				AsyncContext asyncContext = AgentFieldReflectAccessor.getFieldValue(sofaRpcResponseCallbackTrace, "asyncContext");
				Assert.assertNotNull(asyncContext);
				RequestContext requestContext = asyncContext.get(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
				asyncContext.put(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY, null);
				requestContext.span().finish();
				requestContext.scope().close();
				try {
					sofaRpcResponseCallbackTrace.onSofaException(sofaTimeOutException, sofaRequest.getMethod().getName(), sofaRequest);
				} catch (NullPointerException ignore) {
				    //Must be throw NullPointerException
				    return;
				}
				throw new RuntimeException("Must be throw NullPointerException");
			}
		});
		asyncThread.start();
		asyncThread.join();
	}
}
