package com.megaease.easeagent.plugin.motan.interceptor.trace;

import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.megaease.easeagent.plugin.motan.interceptor.metrics.MotanMetricTags;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.weibo.api.motan.protocol.rpc.DefaultRpcReferer;
import com.weibo.api.motan.rpc.*;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public abstract class MotanInterceptorTest {
	@Mock
	protected DefaultRpcReferer<?> defaultRpcReferer;

	@Mock
	protected Provider<?> provider;

	protected DefaultRequest request;

	protected DefaultResponse successResponse = new DefaultResponse("success");

	protected DefaultResponse failureResponse = new DefaultResponse();

	protected RuntimeException motanException = new RuntimeException("motan exception");
	private static final MotanPlugin motanPlugin = new MotanPlugin();

	protected abstract Interceptor createInterceptor();

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);

		URL url = URL.valueOf("motan://127.0.0.1:1234/com.megaease.easeagent.motan.TestService.test(String,Integer)");
		when(defaultRpcReferer.getUrl()).thenReturn(url);
		when(provider.getUrl()).thenReturn(url);

		Map<String, String> attachments = new HashMap<>();

		request = new DefaultRequest();
		request.setInterfaceName("com.megaease.easeagent.motan.TestService");
		request.setMethodName("test");
		request.setParamtersDesc("String,Integer");
		request.setArguments(new Object[]{"motan test", 123});
		request.setAttachments(attachments);

		failureResponse.setException(motanException);

		EaseAgent.configFactory = MockConfig.getPluginConfigManager();
		InterceptorTestUtils.init(createInterceptor(), motanPlugin);
	}

	protected void assertSuccessMetrics() {
		assertMetrics(true);
	}

	protected void assertFailureMetrics() {
		assertMetrics(false);
	}

	private void assertMetrics(boolean isSuccess) {
		TagVerifier tagVerifier = new TagVerifier()
				.add(Tags.CATEGORY, MotanMetricTags.CATEGORY.name)
				.add(Tags.TYPE, MotanMetricTags.TYPE.name)
				.add(MotanMetricTags.LABEL_NAME.name, MotanCtxUtils.interfaceSignature(request));
		LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
		Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
		assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
		if (isSuccess) {
			assertEquals(0, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		} else {
			assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		}
	}

	protected void assertConsumerTrace(Object result, String errorMessage) {
		URL clientUrl = defaultRpcReferer.getUrl();
		ReportSpan reportSpan = MockEaseAgent.getLastSpan();
		assertNotNull(reportSpan);
		assertEquals(reportSpan.name(), MotanCtxUtils.name(request).toLowerCase());
		assertEquals(Span.Kind.CLIENT.name(), reportSpan.kind());
		assertEquals(clientUrl.getApplication(), reportSpan.tag(MotanTags.APPLICATION.name));
		assertEquals(clientUrl.getGroup(), reportSpan.tag(MotanTags.GROUP.name));
		assertEquals(clientUrl.getModule(), reportSpan.tag(MotanTags.MODULE.name));
		assertEquals(request.getInterfaceName(), reportSpan.tag(MotanTags.SERVICE.name));
		assertEquals(clientUrl.getVersion(), reportSpan.tag(MotanTags.SERVICE_VERSION.name));
		assertEquals(MotanCtxUtils.method(request), reportSpan.tag(MotanTags.METHOD.name));
		String expectedArgs = null;
		if (MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.argsCollectEnabled()) {
			expectedArgs = JsonUtil.toJson(request.getArguments());
		}
		String expectedResult = null;
		if (MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.resultCollectEnabled() && result != null) {
			expectedResult = JsonUtil.toJson(result);
		}
		assertEquals(expectedArgs, reportSpan.tag(MotanTags.ARGUMENTS.name));
		assertEquals(expectedResult, reportSpan.tag(MotanTags.RESULT.name));
		if (result != null) {
			assertFalse(reportSpan.hasError());
		} else {
			assertTrue(reportSpan.hasError());
			if (errorMessage != null) {
				assertEquals(errorMessage, reportSpan.errorInfo());
			}
		}
	}

	protected void assertProviderTrace(Object result, String errorMessage) {
		ReportSpan reportSpan = MockEaseAgent.getLastSpan();
		assertNotNull(reportSpan);
		assertEquals(reportSpan.name(), MotanCtxUtils.name(request).toLowerCase());
		assertEquals(Span.Kind.SERVER.name(), reportSpan.kind());
		String expectedResult = null;
		if (MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.resultCollectEnabled() && result != null) {
			expectedResult = JsonUtil.toJson(result);
		}
		assertEquals(expectedResult, reportSpan.tag(MotanTags.RESULT.name));
		if (result != null) {
			assertFalse(reportSpan.hasError());
		} else {
			assertTrue(reportSpan.hasError());
			assertEquals(errorMessage, reportSpan.errorInfo());
		}
	}

}
