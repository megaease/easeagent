package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.alibaba.dubbo.rpc.RpcContext;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.AppResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboTracingInterceptorTest extends AlibabaDubboBaseTest {

	private AlibabaDubboTracingInterceptor alibabaDubboTracingInterceptor;

	@Before
	public void setup() {
		super.setup();
		this.alibabaDubboTracingInterceptor = new AlibabaDubboTracingInterceptor();
	}

	@Test
	public void init() {
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(AlibabaDubboTracingInterceptor.class, "DUBBO_TRACE_CONFIG"));
	}

	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		alibabaDubboTracingInterceptor.before(methodInfo, context);
		alibabaDubboTracingInterceptor.after(methodInfo, context);
		this.assertConsumerTrace(successResult.getValue(), null);
	}

	@Test
	public void rpcProviderCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{providerInvoker, providerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		alibabaDubboTracingInterceptor.before(methodInfo, context);
		alibabaDubboTracingInterceptor.after(methodInfo, context);
		this.assertProviderTrace(successResult.getValue(), null);
	}

	@Test
	public void rpcConsumerAsyncCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
				.build();

		AppResponse appResponse = new AppResponse(successResult);
		CompletableFuture<AppResponse> completeFuture = new CompletableFuture<>();
		RpcContext.getContext().setFuture(completeFuture);
		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		alibabaDubboTracingInterceptor.before(methodInfo, context);
		alibabaDubboTracingInterceptor.after(methodInfo, context);
		completeFuture.complete(appResponse);
		this.assertConsumerTrace(successResult.getValue(), null);
	}

	@Test
	public void rpcConsumerAsyncCallFail() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, asyncConsumerInvocation})
				.build();

		AppResponse failureResponse = new AppResponse(failureResult);
		CompletableFuture<AppResponse> completeFuture = new CompletableFuture<>();
		RpcContext.getContext().setFuture(completeFuture);
		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		alibabaDubboTracingInterceptor.before(methodInfo, context);
		alibabaDubboTracingInterceptor.after(methodInfo, context);
		completeFuture.complete(failureResponse);
		this.assertConsumerTrace(null, failureResult.getException().getMessage());
	}

	@Test
	public void rpcConsumerCallException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.throwable(new RuntimeException("mock exception"))
				.build();


		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(alibabaDubboTracingInterceptor, dubboPlugin);
		alibabaDubboTracingInterceptor.before(methodInfo, context);
		alibabaDubboTracingInterceptor.after(methodInfo, context);
		this.assertConsumerTrace(null, methodInfo.getThrowable().getMessage());
	}


	@Test
	public void order() {
		assertEquals(Order.TRACING.getOrder(), alibabaDubboTracingInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.TRACING.getName(), alibabaDubboTracingInterceptor.getType());
	}
}
