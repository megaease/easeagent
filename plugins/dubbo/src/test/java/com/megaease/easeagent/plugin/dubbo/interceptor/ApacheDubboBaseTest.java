package com.megaease.easeagent.plugin.dubbo.interceptor;

import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboTags;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache.ApacheDubboTracingInterceptor;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.*;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public abstract class ApacheDubboBaseTest {


	@Mock
	protected Invoker<?> consumerInvoker;

	protected Invocation consumerInvocation;

	@Mock
	protected Invoker<?> providerInvoker;

	protected Invocation providerInvocation;

	protected AppResponse successResult = new AppResponse("test");

	protected AppResponse failureResult = new AppResponse(new RuntimeException("mock exception"));

	protected AsyncRpcResult asyncSuccessResult = AsyncRpcResult.newDefaultAsyncResult("test", null);

	protected AsyncRpcResult asyncFailureResult = AsyncRpcResult.newDefaultAsyncResult(new RuntimeException("mock async call fail"), null);

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);

		URL consumerURL = URL.valueOf("dubbo://127.0.0.1:0/com.magaease.easeagent.service.DubboService?side=consumer&application=dubbo-consumer&group=consumer&version=1.0.0");
		when(consumerInvoker.getUrl()).thenReturn(consumerURL);
		Map<String, String> consumerAttachment = new HashMap<>();
		consumerInvocation = new RpcInvocation("sayHello", new Class[]{String.class}, new Object[]{"hello"}, consumerAttachment, consumerInvoker);

		Map<String, String> providerAttachment = new HashMap<>();
		URL providerURL = URL.valueOf("dubbo://127.0.0.1:20880/com.magaease.easeagent.service.DubboService?side=provider&application=dubbo-provider&group=provider&version=1.0.0");
		when(providerInvoker.getUrl()).thenReturn(providerURL);
		providerInvocation = new RpcInvocation("sayHello", new Class[]{String.class}, new Object[]{"hello"}, providerAttachment, providerInvoker);

		EaseAgent.configFactory = MockConfig.getPluginConfigManager();
	}

	protected void assertConsumerTrace(Object retValue, String errorMessage) {
		assertTrace(consumerInvocation, retValue, errorMessage);
	}

	protected void assertProviderTrace(Object retValue, String errorMessage) {
		assertTrace(providerInvocation, retValue, errorMessage);
	}


	protected void assertTrace(Invocation invocation, Object retValue, String errorMessage) {
		DubboTraceConfig dubboTraceConfig = AgentFieldReflectAccessor.getStaticFieldValue(ApacheDubboTracingInterceptor.class, "DUBBO_TRACE_CONFIG");
		URL url = invocation.getInvoker().getUrl();
		boolean isConsumer = url.getParameter(SIDE_KEY).equals(CONSUMER_SIDE);
		ReportSpan mockSpan = MockEaseAgent.getLastSpan();
		assertNotNull(mockSpan);
		if (isConsumer) {
			assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
			assertEquals(url.getParameter(CommonConstants.APPLICATION_KEY), mockSpan.tag(DubboTags.CLIENT_APPLICATION.name));
			assertEquals(url.getParameter(CommonConstants.GROUP_KEY), mockSpan.tag(DubboTags.GROUP.name));
			assertEquals(url.getParameter(CommonConstants.VERSION_KEY), mockSpan.tag(DubboTags.SERVICE_VERSION.name));
			assertEquals(ApacheDubboCtxUtils.method(invocation), mockSpan.tag(DubboTags.METHOD.name));
			assertEquals(url.getPath(), mockSpan.tag(DubboTags.SERVICE.name));
			String expectedArgs = dubboTraceConfig.argsCollectEnabled() ? JsonUtil.toJson(invocation.getArguments()) : null;
			String expectedResult = dubboTraceConfig.argsCollectEnabled() && retValue != null ? JsonUtil.toJson(retValue) : null;
			assertEquals(expectedArgs, mockSpan.tag(DubboTags.ARGS.name));
			assertEquals(expectedResult, mockSpan.tag(DubboTags.RESULT.name));
		} else {
			assertEquals(Span.Kind.SERVER.name(), mockSpan.kind());
			assertEquals(url.getParameter(CommonConstants.APPLICATION_KEY), mockSpan.tag(DubboTags.SERVER_APPLICATION.name));
		}
		assertEquals(ApacheDubboCtxUtils.name(invocation).toLowerCase(), mockSpan.name());
		assertEquals(ConfigConst.Namespace.DUBBO, mockSpan.remoteServiceName());
		if (retValue != null) {
			assertFalse(mockSpan.hasError());
			assertEquals(successResult.getValue(), retValue);
		} else {
			assertTrue(mockSpan.hasError());
			assertEquals(errorMessage, mockSpan.errorInfo());
		}
		assertNull(mockSpan.parentId());
	}

	protected void assertMetric(String interfaceSignature, NameFactory nameFactory, MetricRegistry metricRegistry, boolean success) {
		final Map<String, Metric> metrics = metricRegistry.getMetrics();
		assertFalse(metrics.isEmpty());
		assertNotNull(metrics.get(nameFactory.timerName(interfaceSignature, MetricSubType.DEFAULT)));
		assertNotNull(metrics.get(nameFactory.meterName(interfaceSignature, MetricSubType.DEFAULT)));
		assertNotNull(metrics.get(nameFactory.counterName(interfaceSignature, MetricSubType.DEFAULT)));
		assertNotNull(metrics.get(nameFactory.gaugeName(interfaceSignature, MetricSubType.DEFAULT)));

		if (success) {
			assertNull(metrics.get(nameFactory.meterName(interfaceSignature, MetricSubType.ERROR)));
			assertNull(metrics.get(nameFactory.counterName(interfaceSignature, MetricSubType.ERROR)));
		} else {
			assertNotNull(metrics.get(nameFactory.meterName(interfaceSignature, MetricSubType.ERROR)));
			assertNotNull(metrics.get(nameFactory.counterName(interfaceSignature, MetricSubType.ERROR)));
		}

		//clean up metrics
		MockEaseAgent.cleanAllMetric();
	}
}
