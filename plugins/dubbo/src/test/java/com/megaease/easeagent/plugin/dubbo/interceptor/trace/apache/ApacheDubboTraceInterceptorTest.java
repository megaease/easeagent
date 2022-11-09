package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.ApacheDubboBaseTest;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba.AlibabaDubboTraceInterceptor;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ApacheDubboTraceInterceptorTest extends ApacheDubboBaseTest {

	private ApacheDubboTraceInterceptor apacheDubboTraceInterceptor;

	@Before
	public void setup() {
		super.setup();
		DubboPlugin dubboPlugin = new DubboPlugin();
		this.apacheDubboTraceInterceptor = new ApacheDubboTraceInterceptor();
		InterceptorTestUtils.init(apacheDubboTraceInterceptor, dubboPlugin);
	}

	@Test
	public void init() {
        assertNotNull(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG);
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.resultCollectEnabled());
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.argsCollectEnabled());
	}

	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.successResponse)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboTraceInterceptor.before(methodInfo, context);
		apacheDubboTraceInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(retValue.getValue(), null);
	}

    @Test
    public void rpcConsumerCallFailure() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .retValue(this.failureResponse)
            .build();

        Context context = EaseAgent.getContext();
        apacheDubboTraceInterceptor.before(methodInfo, context);
        apacheDubboTraceInterceptor.after(methodInfo, context);
        Result retValue = (Result) methodInfo.getRetValue();
        this.assertConsumerTrace(null, failureResponse.getException().getMessage());
    }

    @Test
    public void rpcConsumerCallException() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .throwable(new RuntimeException("mock exception"))
            .build();

        Context context = EaseAgent.getContext();
        apacheDubboTraceInterceptor.before(methodInfo, context);
        apacheDubboTraceInterceptor.after(methodInfo, context);
        this.assertConsumerTrace(null, methodInfo.getThrowable().getMessage());
    }

	@Test
	public void rpcProviderCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{providerInvoker, providerInvocation})
				.retValue(this.successResponse)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboTraceInterceptor.before(methodInfo, context);
		apacheDubboTraceInterceptor.after(methodInfo, context);
		Result retValue = (Result) methodInfo.getRetValue();
		this.assertProviderTrace(retValue.getValue(), null);
	}

    @Test
    public void rpcProviderCallFailure() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{providerInvoker, providerInvocation})
            .retValue(this.failureResponse)
            .build();

        Context context = EaseAgent.getContext();
        apacheDubboTraceInterceptor.before(methodInfo, context);
        apacheDubboTraceInterceptor.after(methodInfo, context);
        this.assertProviderTrace(null, failureResponse.getException().getMessage());
    }

    @Test
    public void rpcProviderCallException() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{providerInvoker, providerInvocation})
            .throwable(new RuntimeException("mock exception"))
            .build();

        Context context = EaseAgent.getContext();
        apacheDubboTraceInterceptor.before(methodInfo, context);
        apacheDubboTraceInterceptor.after(methodInfo, context);
        this.assertProviderTrace(null, methodInfo.getThrowable().getMessage());
    }

	@Test
	public void rpcConsumerAsyncCallSuccess() throws InterruptedException {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.asyncRpcResult)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboTraceInterceptor.before(methodInfo, context);
		apacheDubboTraceInterceptor.after(methodInfo, context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRpcResult.complete(successResponse);
            }
        });
        thread.start();
        thread.join();

		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(retValue.getValue(), null);
	}
    @Test
	public void rpcConsumerAsyncCallFail() throws InterruptedException {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.asyncRpcResult)
				.build();

		Context context = EaseAgent.getContext();
		apacheDubboTraceInterceptor.before(methodInfo, context);
		apacheDubboTraceInterceptor.after(methodInfo, context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                asyncRpcResult.complete(failureResponse);
            }
        });
        thread.start();
        thread.join();

		Result retValue = (Result) methodInfo.getRetValue();
		this.assertConsumerTrace(null, retValue.getException().getMessage());
	}

	@Test
	public void order() {
		assertEquals(Order.TRACING.getOrder(), apacheDubboTraceInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.TRACING.getName(), apacheDubboTraceInterceptor.getType());
	}
}
