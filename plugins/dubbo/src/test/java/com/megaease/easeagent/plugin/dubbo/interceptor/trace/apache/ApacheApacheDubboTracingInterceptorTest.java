package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.ApacheDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ApacheApacheDubboTracingInterceptorTest extends ApacheDubboBaseTest {

	private ApacheDubboTracingInterceptor apacheDubboTracingInterceptor;

	@Before
	public void setup() {
		super.setup();
		this.apacheDubboTracingInterceptor = new ApacheDubboTracingInterceptor();
	}

	@Test
	public void init() {
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(ApacheDubboTracingInterceptor.class, "DUBBO_TRACE_CONFIG"));
	}

	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		apacheDubboTracingInterceptor.before(methodInfo, context);
		apacheDubboTracingInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(retValue.getValue(), null);
	}

	@Test
	public void rpcProviderCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{providerInvoker, providerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		apacheDubboTracingInterceptor.before(methodInfo, context);
		apacheDubboTracingInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertProviderTrace(retValue.getValue(), null);
	}

	@Test
	public void rpcConsumerAsyncCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.asyncSuccessResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		apacheDubboTracingInterceptor.before(methodInfo, context);
		apacheDubboTracingInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(retValue.getValue(), null);
	}

	@Test
	public void rpcConsumerAsyncCallFail() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.asyncFailureResult)
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		apacheDubboTracingInterceptor.before(methodInfo, context);
		apacheDubboTracingInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(null, retValue.getException().getMessage());
	}

	@Test
	public void rpcConsumerCallException() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.throwable(new RuntimeException("mock exception"))
				.build();

		Context context = EaseAgent.getContext();
		DubboPlugin dubboPlugin = new DubboPlugin();
		InterceptorTestUtils.init(apacheDubboTracingInterceptor, dubboPlugin);
		apacheDubboTracingInterceptor.before(methodInfo, context);
		apacheDubboTracingInterceptor.after(methodInfo, context);
		this.assertConsumerTrace(null, methodInfo.getThrowable().getMessage());
	}


	@Test
	public void order() {
		assertEquals(Order.TRACING.getOrder(), apacheDubboTracingInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.TRACING.getName(), apacheDubboTracingInterceptor.getType());
	}
}
