package com.megaease.easeagent.plugin.dubbo.interceptor;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboTags;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.dubbo.common.Constants.CONSUMER_SIDE;
import static com.alibaba.dubbo.common.Constants.SIDE_KEY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class AlibabaDubboBaseTest {

	@Mock
	protected Invoker consumerInvoker;

	protected Invocation consumerInvocation;

	protected Invocation asyncConsumerInvocation;

	@Mock
	protected Invoker providerInvoker;

	protected Invocation providerInvocation;

	protected RpcResult successResult = new RpcResult("hello");

	protected RpcResult failureResult = new RpcResult(new IllegalArgumentException("mock exception"));
    protected FutureAdapter<Object> futureAdapter;

    protected EmptyResponseCallback responseCallback = new EmptyResponseCallback();

    protected static class EmptyResponseCallback implements ResponseCallback{
        private Throwable throwable;

        public Throwable throwable() {
            return throwable;
        }

        @Override
        public void done(Object response) {
        }

        @Override
        public void caught(Throwable exception) {
            this.throwable = exception;
        }
    }

    @Before
	public void setup() {
		MockitoAnnotations.openMocks(this);

		URL consumerURL = URL.valueOf("dubbo://127.0.0.1:0/com.magaease.easeagent.service.DubboService?side=consumer&application=dubbo-consumer&group=consumer&version=1.0.0");
		when(consumerInvoker.getUrl()).thenReturn(consumerURL);
		Map<String, String> consumerAttachment = new HashMap<>();
		consumerInvocation = new RpcInvocation("sayHello", new Class<?>[]{String.class}, new Object[]{"hello"}, consumerAttachment, consumerInvoker);

		Map<String, String> asyncConsumerAttachment = new HashMap<>();
		asyncConsumerAttachment.put(Constants.ASYNC_KEY, Boolean.TRUE.toString());
		asyncConsumerInvocation = new RpcInvocation("sayHello", new Class<?>[]{String.class}, new Object[]{"hello"}, asyncConsumerAttachment, consumerInvoker);

		Map<String, String> providerAttachment = new HashMap<>();
		URL providerURL = URL.valueOf("dubbo://127.0.0.1:20880/com.magaease.easeagent.service.DubboService?side=provider&application=dubbo-provider&group=provider&version=1.0.0");
		when(providerInvoker.getUrl()).thenReturn(providerURL);
		providerInvocation = new RpcInvocation("sayHello", new Class[]{String.class}, new Object[]{"hello"}, providerAttachment, providerInvoker);

        Request request = new Request(1);
        Channel channel = mock(Channel.class);
        DefaultFuture defaultFuture = new DefaultFuture(channel,request,300);
        futureAdapter = new FutureAdapter<>(defaultFuture);

        EaseAgent.configFactory = MockConfig.getPluginConfigManager();
	}

	protected void assertConsumerTrace(Object retValue, String errorMessage) {
		assertTrace(consumerInvocation, retValue, errorMessage);
	}

	protected void assertProviderTrace(Object retValue, String errorMessage) {
		assertTrace(providerInvocation, retValue, errorMessage);
	}


	protected void assertTrace(Invocation invocation, Object retValue, String errorMessage) {
		DubboTraceConfig dubboTraceConfig = DubboBaseInterceptor.DUBBO_TRACE_CONFIG;
		URL url = invocation.getInvoker().getUrl();
		boolean isConsumer = url.getParameter(SIDE_KEY).equals(CONSUMER_SIDE);

		ReportSpan mockSpan = MockEaseAgent.getLastSpan();
		assertNotNull(mockSpan);
		assertEquals(AlibabaDubboCtxUtils.name(invocation).toLowerCase(), mockSpan.name());
		assertEquals(ConfigConst.Namespace.DUBBO, mockSpan.remoteServiceName());
		if (isConsumer) {
			assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
			assertEquals(url.getParameter(Constants.APPLICATION_KEY), mockSpan.tag(DubboTags.CLIENT_APPLICATION.name));
			assertEquals(url.getParameter(Constants.GROUP_KEY), mockSpan.tag(DubboTags.GROUP.name));
			assertEquals(url.getParameter(Constants.VERSION_KEY), mockSpan.tag(DubboTags.SERVICE_VERSION.name));
			assertEquals(AlibabaDubboCtxUtils.method(invocation), mockSpan.tag(DubboTags.METHOD.name));
			assertEquals(url.getPath(), mockSpan.tag(DubboTags.SERVICE.name));
			String expectedArgs = dubboTraceConfig.argsCollectEnabled() ? JsonUtil.toJson(invocation.getArguments()) : null;
			String expectedResult = dubboTraceConfig.resultCollectEnabled() && retValue != null ? JsonUtil.toJson(retValue) : null;
			assertEquals(expectedArgs, mockSpan.tag(DubboTags.ARGS.name));
			assertEquals(expectedResult, mockSpan.tag(DubboTags.RESULT.name));
		} else {
			assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
			assertEquals(url.getParameter(Constants.APPLICATION_KEY), mockSpan.tag(DubboTags.SERVER_APPLICATION.name));
		}

		if (retValue != null) {
			assertFalse(mockSpan.hasError());
			assertEquals(successResult.getValue(), retValue);
		} else {
			assertTrue(mockSpan.hasError());
            if (errorMessage != null) {
    			assertEquals(errorMessage, mockSpan.errorInfo());
            }
		}
		assertNull(mockSpan.parentId());
	}

}
