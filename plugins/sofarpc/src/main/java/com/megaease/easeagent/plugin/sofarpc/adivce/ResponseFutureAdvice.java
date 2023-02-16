package com.megaease.easeagent.plugin.sofarpc.adivce;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class ResponseFutureAdvice implements Points {
	private static final String ABSTRACT_RESPONSE_FUTURE_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.AbstractResponseFuture";
	private static final String BOLT_RESPONSE_FUTURE_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.BoltResponseFuture";
	private static final String SET_SUCCESS_METHOD_NAME = "setSuccess";
	private static final String SET_FAILURE_METHOD_NAME = "setFailure";

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.isPublic()
				.isAbstract()
				.hasClassName(ABSTRACT_RESPONSE_FUTURE_FULL_CLASS_NAME)
				.or()
				.isPublic()
				.hasClassName(BOLT_RESPONSE_FUTURE_FULL_CLASS_NAME)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.named(SET_SUCCESS_METHOD_NAME)
				.argsLength(1)
				.arg(0,Object.class.getName())
				.or()
				.named(SET_FAILURE_METHOD_NAME)
				.argsLength(1)
				.arg(0, Throwable.class.getName())
				.build()
				.toSet();
	}

	@Override
	public boolean isAddDynamicField() {
		return true;
	}
}
