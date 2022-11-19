package com.megaease.easeagent.plugin.motan.interceptor.trace;

import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
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

public abstract class MotanTraceInterceptorTest {
	@Mock
	protected DefaultRpcReferer<?> defaultRpcReferer;

	@Mock
	protected Provider<?> provider;

	protected DefaultRequest request;

	protected DefaultResponse successResponse;

	protected DefaultResponse failureResponse;

	protected RuntimeException motanException = new RuntimeException("motan exception");


	@Before
	public void setUp() throws Exception {
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

		failureResponse = new DefaultResponse();
		failureResponse.setException(motanException);
		successResponse = new DefaultResponse("success");

		EaseAgent.configFactory = MockConfig.getPluginConfigManager();
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
