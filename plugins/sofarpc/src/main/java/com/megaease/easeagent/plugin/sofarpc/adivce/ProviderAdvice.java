package com.megaease.easeagent.plugin.sofarpc.adivce;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class ProviderAdvice implements Points {
	private static final String PROVIDER_INVOKER_CLASS_FULL_NAME = "com.alipay.sofa.rpc.filter.ProviderInvoker";
	private static final String PROVIDER_INVOKER_METHOD_NAME = "invoke";
	private static final String PROVIDER_INVOKER_METHOD_PARAMETER_FULL_CLASS_NAME = "com.alipay.sofa.rpc.core.request.SofaRequest";
	private static final String PROVIDER_INVOKER_METHOD_RETURN_VALUE_FULL_CLASS_NAME = "com.alipay.sofa.rpc.core.response.SofaResponse";

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasClassName(PROVIDER_INVOKER_CLASS_FULL_NAME)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.isPublic()
				.named(PROVIDER_INVOKER_METHOD_NAME)
				.arg(0,PROVIDER_INVOKER_METHOD_PARAMETER_FULL_CLASS_NAME)
				.returnType(PROVIDER_INVOKER_METHOD_RETURN_VALUE_FULL_CLASS_NAME)
				.build()
				.toSet();
	}
}
