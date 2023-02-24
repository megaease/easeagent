package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.filter.ConsumerInvoker;
import com.alipay.sofa.rpc.filter.ProviderInvoker;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcMetricsTags;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public abstract class BaseMetricsInterceptorTest {

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
    private AutoCloseable autoCloseable;

	@BeforeClass
	public static void beforeClass() {
	}

	@AfterClass
	public static void afterClass() {
	}

	@Test
	public void testGetType() {
		String type = getInterceptor().getType();
		Assert.assertEquals(Order.METRIC.getName(), type);
	}

	@Test
	public void testOrder() {
		int order = getInterceptor().order();
		Assert.assertEquals(Order.METRIC.getOrder(), order);
	}
    @After
    public void destroy() throws Exception {
        autoCloseable.close();
    }

	@Before
	public void init() {
		autoCloseable = MockitoAnnotations.openMocks(this);
		when(consumerInvoker.getConfig()).thenReturn(consumerConfig);
		when(consumerConfig.getAppName()).thenReturn("sofa-client");

		when(providerInvoker.getConfig()).thenReturn(providerConfig);
		when(providerConfig.getAppName()).thenReturn("sofa-server");

		when(sofaRequest.getMethod()).thenReturn(mockMethod);
		@SuppressWarnings("unchecked")
		Class<BaseMetricsInterceptorTest> declaringClass = (Class<BaseMetricsInterceptorTest>) mockMethod.getDeclaringClass();
		when(declaringClass).thenReturn(BaseMetricsInterceptorTest.class);
		when(mockMethod.getName()).thenReturn("mock");
		when(mockMethod.getParameterTypes()).thenReturn(new Class[]{String.class, Integer.class});
		when(sofaRequest.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_SYNC);
		when(sofaRequest.getMethodArgs()).thenReturn(new Object[]{"abc", 3});

		allArguments = new Object[]{sofaRequest};

		SofaRpcPlugin sofaRpcPlugin = new SofaRpcPlugin();
		SofaRpcMetricsBaseInterceptor sofaRpcMetricsBaseInterceptor = getInterceptor();
		InterceptorTestUtils.init(sofaRpcMetricsBaseInterceptor, sofaRpcPlugin);
	}

	protected abstract SofaRpcMetricsBaseInterceptor getInterceptor();

	protected void assertMetrics(SofaRequest sofaRequest, Object result) {
		TagVerifier tagVerifier = new TagVerifier()
				.add(Tags.CATEGORY, SofaRpcMetricsTags.CATEGORY.name)
				.add(Tags.TYPE, SofaRpcMetricsTags.TYPE.name)
				.add(SofaRpcMetricsTags.LABEL_NAME.name, SofaRpcCtxUtils.methodSignature(sofaRequest));
		LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
		Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();

		assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
		if (result instanceof Throwable) {
			assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
		}
	}

	private static double metricValue(Map<String,Object> metrics, MetricField metricField) {
		return (double) metrics.get(metricField.getField());
	}
}
