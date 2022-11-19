package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboTraceInterceptorTest extends AlibabaDubboBaseTest {

	private AlibabaDubboTraceInterceptor alibabaDubboTraceInterceptor;

	@Before
	public void setup() {
		super.setup();
		DubboPlugin dubboPlugin = new DubboPlugin();
		this.alibabaDubboTraceInterceptor = new AlibabaDubboTraceInterceptor();
		InterceptorTestUtils.init(alibabaDubboTraceInterceptor, dubboPlugin);
	}

	@Test
	public void init() {
        assertNotNull(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG);
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.resultCollectEnabled());
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.argsCollectEnabled());
		assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(AlibabaDubboTraceInterceptor.class, "DUBBO_TRACE_CONFIG"));
	}

	@Test
	public void rpcConsumerCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{consumerInvoker, consumerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		alibabaDubboTraceInterceptor.before(methodInfo, context);
		alibabaDubboTraceInterceptor.after(methodInfo, context);
		this.assertConsumerTrace(successResult.getValue(), null);
	}

    @Test
    public void rpcConsumerCallFailure() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .retValue(this.failureResult)
            .build();

        Context context = EaseAgent.getContext();
        alibabaDubboTraceInterceptor.before(methodInfo, context);
        alibabaDubboTraceInterceptor.after(methodInfo, context);
        this.assertConsumerTrace(null, failureResult.getException().getMessage());
    }

    @Test
    public void rpcConsumerCallException() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{consumerInvoker, consumerInvocation})
            .throwable(new RuntimeException("mock exception"))
            .build();

        Context context = EaseAgent.getContext();
        alibabaDubboTraceInterceptor.before(methodInfo, context);
        alibabaDubboTraceInterceptor.after(methodInfo, context);
        this.assertConsumerTrace(null, methodInfo.getThrowable().getMessage());
    }

	@Test
	public void rpcProviderCallSuccess() {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{providerInvoker, providerInvocation})
				.retValue(this.successResult)
				.build();

		Context context = EaseAgent.getContext();
		alibabaDubboTraceInterceptor.before(methodInfo, context);
		alibabaDubboTraceInterceptor.after(methodInfo, context);
		this.assertProviderTrace(successResult.getValue(), null);
	}

    @Test
    public void rpcProviderCallFailure() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{providerInvoker, providerInvocation})
            .retValue(this.failureResult)
            .build();

        Context context = EaseAgent.getContext();
        alibabaDubboTraceInterceptor.before(methodInfo, context);
        alibabaDubboTraceInterceptor.after(methodInfo, context);
        this.assertProviderTrace(null, failureResult.getException().getMessage());
    }

    @Test
    public void rpcProviderCallException() {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{providerInvoker, providerInvocation})
            .throwable(new RuntimeException("mock exception"))
            .build();


        Context context = EaseAgent.getContext();
        alibabaDubboTraceInterceptor.before(methodInfo, context);
        alibabaDubboTraceInterceptor.after(methodInfo, context);
        this.assertProviderTrace(null, methodInfo.getThrowable().getMessage());
    }

	@Test
	public void order() {
		assertEquals(Order.TRACING.getOrder(), alibabaDubboTraceInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.TRACING.getName(), alibabaDubboTraceInterceptor.getType());
	}
}
