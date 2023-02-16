package com.megaease.easeagent.plugin.sofarpc.adivce;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.*;

import java.util.Set;

public class ConsumerAdvice implements Points {
	private static final String CONSUMER_INVOKER_CLASS_FULL_NAME = "com.alipay.sofa.rpc.filter.ConsumerInvoker";
	private static final String CONSUMER_INVOKER_METHOD_NAME = "invoke";
	private static final String CONSUMER_INVOKER_METHOD_PARAMETER_FULL_CLASS_NAME = "com.alipay.sofa.rpc.core.request.SofaRequest";
	private static final String CONSUMER_INVOKER_METHOD_RETURN_VALUE_FULL_CLASS_NAME = "com.alipay.sofa.rpc.core.response.SofaResponse";

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasClassName(CONSUMER_INVOKER_CLASS_FULL_NAME)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.isPublic()
				.named(CONSUMER_INVOKER_METHOD_NAME)
				.arg(0,CONSUMER_INVOKER_METHOD_PARAMETER_FULL_CLASS_NAME)
				.returnType(CONSUMER_INVOKER_METHOD_RETURN_VALUE_FULL_CLASS_NAME)
				.build()
				.toSet();
	}
}
