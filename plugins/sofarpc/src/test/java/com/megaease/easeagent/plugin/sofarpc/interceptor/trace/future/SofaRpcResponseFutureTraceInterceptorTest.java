package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.future;

import com.alipay.sofa.rpc.client.ProviderInfo;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.BaseInterceptorTest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.MockBoltResponseFuture;
import com.megaease.easeagent.plugin.sofarpc.interceptor.initalize.SofaRpcFutureInvokeCallbackConstructInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common.SofaRpcConsumerTraceInterceptor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class SofaRpcResponseFutureTraceInterceptorTest extends BaseInterceptorTest {
	private final SofaRpcConsumerTraceInterceptor sofaRpcConsumerTraceInterceptor = new SofaRpcConsumerTraceInterceptor();
	private final SofaRpcResponseFutureTraceInterceptor sofaRpcResponseFutureTraceInterceptor = new SofaRpcResponseFutureTraceInterceptor();
	private final SofaRpcFutureInvokeCallbackConstructInterceptor sofaRpcFutureInvokeCallbackConstructInterceptor = new SofaRpcFutureInvokeCallbackConstructInterceptor();
	private final Object[] futureInvokeCallbackConstructTracingInterceptorArgs = new Object[6];


	@Override
	protected SofaRpcTraceBaseInterceptor getInterceptor() {
		return sofaRpcResponseFutureTraceInterceptor;
	}

	@Before
	public void setup() {
		futureInvokeCallbackConstructTracingInterceptorArgs[2] = new MockBoltResponseFuture();
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
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		sofaRpcConsumerTraceInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructTracingInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo, context);
		String result = "success";
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.setInvoker(futureInvokeCallbackConstructTracingInterceptorArgs[2]);
				methodInfo.setArgs(new Object[]{result});
				sofaRpcResponseFutureTraceInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, result);
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
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		sofaRpcConsumerTraceInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructTracingInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo, context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.setInvoker(futureInvokeCallbackConstructTracingInterceptorArgs[2]);
				methodInfo.setArgs(new Object[]{throwable});
				sofaRpcResponseFutureTraceInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, throwable);
	}

	@Test
	@SneakyThrows
	public void testConsumerFutureInvokeException() {
		IllegalStateException illegalStateException = new IllegalStateException("complete already");

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		sofaRpcConsumerTraceInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructTracingInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo, context);
		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				methodInfo.throwable(illegalStateException);
				methodInfo.setInvoker(futureInvokeCallbackConstructTracingInterceptorArgs[2]);
				sofaRpcResponseFutureTraceInterceptor.after(methodInfo, context);
			}
		});
		asyncThread.start();
		asyncThread.join();

		assertConsumerTrace(sofaRequest, illegalStateException);
	}

	@Test(expected = NullPointerException.class)
	public void testNotObtainedAsyncContext() {

		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		methodInfo.setInvoker(futureInvokeCallbackConstructTracingInterceptorArgs[2]);
		sofaRpcResponseFutureTraceInterceptor.after(methodInfo, context);
	}

	@Test
	@SneakyThrows
	public void testNotObtainedRequestContext() {
		MethodInfo methodInfo = MethodInfo.builder()
				.invoker(consumerInvoker)
				.args(allArguments)
				.build();

		Context context = EaseAgent.getContext();
		sofaRpcConsumerTraceInterceptor.before(methodInfo, context);
		sofaRpcConsumerTraceInterceptor.after(methodInfo, context);
		methodInfo.setArgs(futureInvokeCallbackConstructTracingInterceptorArgs);
		sofaRpcFutureInvokeCallbackConstructInterceptor.before(methodInfo, context);
		MockBoltResponseFuture mockBoltResponseFuture = (MockBoltResponseFuture) futureInvokeCallbackConstructTracingInterceptorArgs[2];

		Thread asyncThread = new Thread(new Runnable() {
			@Override
			public void run() {
				AsyncContext asyncContext = AgentDynamicFieldAccessor.getDynamicFieldValue(mockBoltResponseFuture);
				Assert.assertNotNull(asyncContext);
				RequestContext requestContext = asyncContext.get(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
				requestContext.span().finish();
				asyncContext.put(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY, null);
				methodInfo.setInvoker(futureInvokeCallbackConstructTracingInterceptorArgs[2]);
				try {
					sofaRpcResponseFutureTraceInterceptor.after(methodInfo, context);
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
