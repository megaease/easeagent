package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.alibaba.dubbo.rpc.RpcContext;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.interceptor.AlibabaDubboBaseTest;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class AlibabaDubboAsyncTraceInterceptorTest extends AlibabaDubboBaseTest {

    private AlibabaDubboAsyncTraceInterceptor alibabaDubboAsyncTraceInterceptor;
    private AlibabaDubboTraceInterceptor alibabaDubboTraceInterceptor;


	@Before
	public void setup() {
		super.setup();
		DubboPlugin dubboPlugin = new DubboPlugin();
		this.alibabaDubboAsyncTraceInterceptor = new AlibabaDubboAsyncTraceInterceptor();
		this.alibabaDubboTraceInterceptor = new AlibabaDubboTraceInterceptor();
		InterceptorTestUtils.init(alibabaDubboAsyncTraceInterceptor, dubboPlugin);
	}

	@Test
	public void init() {
        assertNotNull(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG);
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.resultCollectEnabled());
        assertTrue(AlibabaDubboTraceInterceptor.DUBBO_TRACE_CONFIG.argsCollectEnabled());
	}

	@Test
	public void rpcConsumerAsyncCallSuccess() throws InterruptedException {
		MethodInfo methodInfo = MethodInfo.builder()
				.args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
				.build();

		Context context = EaseAgent.getContext();
        RpcContext.getContext().setFuture(futureAdapter);
        alibabaDubboTraceInterceptor.before(methodInfo,context);
        methodInfo.setArgs(new Object[]{responseCallback});
		alibabaDubboAsyncTraceInterceptor.before(methodInfo, context);
        AlibabaDubboTraceCallback alibabaDubboTraceCallback = (AlibabaDubboTraceCallback) methodInfo.getArgs()[0];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                alibabaDubboTraceCallback.done(successResult);
            }
        });
        thread.start();
        thread.join();
        this.assertConsumerTrace(successResult.getValue(), null);
	}

	@Test
	public void rpcConsumerAsyncCallFail() throws InterruptedException {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
            .build();

        Context context = EaseAgent.getContext();
        RpcContext.getContext().setFuture(futureAdapter);
        alibabaDubboTraceInterceptor.before(methodInfo,context);
        methodInfo.setArgs(new Object[]{responseCallback});
        alibabaDubboAsyncTraceInterceptor.before(methodInfo, context);
        AlibabaDubboTraceCallback alibabaDubboTraceCallback = (AlibabaDubboTraceCallback) methodInfo.getArgs()[0];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                alibabaDubboTraceCallback.caught(failureResult.getException());
            }
        });
        thread.start();
        thread.join();
        this.assertConsumerTrace(null,failureResult.getException().getMessage());
	}


    @Test
    public void rpcConsumerAsyncCallTimeout() throws InterruptedException {
        MethodInfo methodInfo = MethodInfo.builder()
            .args(new Object[]{asyncConsumerInvocation.getInvoker(), asyncConsumerInvocation})
            .build();

        Context context = EaseAgent.getContext();
        RpcContext.getContext().setFuture(futureAdapter);
        alibabaDubboTraceInterceptor.before(methodInfo,context);
        methodInfo.setArgs(new Object[]{responseCallback});
        alibabaDubboAsyncTraceInterceptor.before(methodInfo, context);
        AlibabaDubboTraceCallback alibabaDubboTraceCallback = (AlibabaDubboTraceCallback)methodInfo.getArgs()[0];
        futureAdapter.getFuture().setCallback(alibabaDubboTraceCallback);
        TimeUnit.SECONDS.sleep(1);
        this.assertConsumerTrace(null,responseCallback.throwable().getMessage());
    }


	@Test
	public void order() {
		assertEquals(Order.TRACING.getOrder(), alibabaDubboAsyncTraceInterceptor.order());
	}

	@Test
	public void getType() {
		assertEquals(Order.TRACING.getName(), alibabaDubboAsyncTraceInterceptor.getType());
	}
}
