package com.megaease.easeagent.plugin.sofarpc.interceptor;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.filter.ConsumerInvoker;
import com.alipay.sofa.rpc.filter.ProviderInvoker;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcTags;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common.SofaRpcConsumerTraceInterceptor;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

public abstract class BaseInterceptorTest {

	protected RpcInternalContext rpcContext = RpcInternalContext.getContext();

	@Mock
	protected SofaRequest sofaRequest;

	@Mock
	protected ConsumerInvoker consumerInvoker;

	@Mock
	protected ConsumerConfig<?> consumerConfig;

	@Mock
	protected ProviderInvoker<?> providerInvoker;

	@Mock
	protected ProviderConfig<?> providerConfig;

	@Mock
	protected Method mockMethod;

	protected Object[] allArguments;

	@BeforeClass
	public static void beforeClass() {
	}

	@AfterClass
	public static void afterClass() {
	}

	@Test
	public void testGetType() {
		String type = getInterceptor().getType();
		Assert.assertEquals(Order.TRACING.getName(), type);
	}

	@Test
	public void testOrder() {
		int order = getInterceptor().order();
		Assert.assertEquals(Order.TRACING.getOrder(), order);
	}

	@Before
	public void init() {
		MockitoAnnotations.openMocks(this);
		when(consumerInvoker.getConfig()).thenReturn(consumerConfig);
		when(consumerConfig.getAppName()).thenReturn("sofa-client");

		when(providerInvoker.getConfig()).thenReturn(providerConfig);
		when(providerConfig.getAppName()).thenReturn("sofa-server");

		when(sofaRequest.getMethod()).thenReturn(mockMethod);
		@SuppressWarnings("unchecked")
		Class<BaseInterceptorTest> declaringClass = (Class<BaseInterceptorTest>) mockMethod.getDeclaringClass();
		when(declaringClass).thenReturn(BaseInterceptorTest.class);
		when(mockMethod.getName()).thenReturn("mock");
		when(mockMethod.getParameterTypes()).thenReturn(new Class[]{String.class, Integer.class});

		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_SYNC);

//		when(sofaRequest.getMethodName()).thenReturn("test");
//		when(sofaRequest.getMethodArgSigs()).thenReturn(new String[]{"java.lang.String"});
		when(sofaRequest.getMethodArgs()).thenReturn(new Object[]{"abc", 3});
//		when(sofaRequest.getInterfaceName()).thenReturn("org.apache.skywalking.apm.test.TestSofaRpcService");

		allArguments = new Object[]{sofaRequest};

		EaseAgent.configFactory = MockConfig.getPluginConfigManager();
		SofaRpcPlugin sofaRpcPlugin = new SofaRpcPlugin();
		SofaRpcTraceBaseInterceptor sofaRpcTraceBaseInterceptor = getInterceptor();
		InterceptorTestUtils.init(sofaRpcTraceBaseInterceptor, sofaRpcPlugin);
	}

	protected abstract SofaRpcTraceBaseInterceptor getInterceptor();

	protected void assertProviderTrace(SofaRequest sofaRequest, Object result) {
		assertTrace(false, sofaRequest, result);
	}

	protected void assertConsumerTrace(SofaRequest sofaRequest, Object result) {
		assertTrace(true, sofaRequest, result);
	}

	private void assertTrace(boolean isClientSide, SofaRequest sofaRequest, Object result) {
		ReportSpan lastSpan = MockEaseAgent.getLastSpan();
		Assert.assertNotNull(lastSpan);
		Assert.assertNull(lastSpan.parentId());
		Assert.assertEquals(ConfigConst.Namespace.SOFARPC, lastSpan.remoteServiceName());
		Assert.assertEquals(SofaRpcCtxUtils.name(sofaRequest).toLowerCase(), lastSpan.name());
		if (isClientSide) {
			Assert.assertEquals(Span.Kind.CLIENT.name(), lastSpan.kind());
			Assert.assertEquals(consumerConfig.getAppName(), lastSpan.tag(SofaRpcTags.CLIENT_APPLICATION.name));
			Assert.assertEquals(rpcContext.getProviderInfo().getHost(), lastSpan.remoteEndpoint().ipv4());
			Assert.assertEquals(rpcContext.getProviderInfo().getPort(), lastSpan.remoteEndpoint().port());
			Assert.assertEquals(SofaRpcCtxUtils.method(sofaRequest), lastSpan.tag(SofaRpcTags.METHOD.name));
			if (SofaRpcConsumerTraceInterceptor.SOFA_RPC_TRACE_CONFIG.argsCollectEnabled()) {
				Assert.assertEquals(JsonUtil.toJson(sofaRequest.getMethodArgs()), lastSpan.tag(SofaRpcTags.ARGS.name));
			}
			if (result instanceof Throwable) {
				Assert.assertTrue(lastSpan.hasError());
				Assert.assertNull(lastSpan.tag(SofaRpcTags.RESULT.name));
				Assert.assertEquals(lastSpan.errorInfo(), ((Throwable) result).getMessage());
			} else if (SofaRpcConsumerTraceInterceptor.SOFA_RPC_TRACE_CONFIG.resultCollectEnabled()) {
				Assert.assertFalse(lastSpan.hasError());
				Assert.assertEquals(JsonUtil.toJson(result), lastSpan.tag(SofaRpcTags.RESULT.name));
			}
		} else {
			Assert.assertEquals(Span.Kind.SERVER.name(), lastSpan.kind());
			Assert.assertEquals(providerConfig.getAppName(), lastSpan.tag(SofaRpcTags.SERVER_APPLICATION.name));
			Assert.assertEquals(rpcContext.getRemoteAddress().getHostString(), lastSpan.remoteEndpoint().ipv4());
			Assert.assertEquals(rpcContext.getRemoteAddress().getPort(), lastSpan.remoteEndpoint().port());
		}

	}
}
