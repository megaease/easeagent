package com.megaease.easeagent.plugin.sofarpc.adivce;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class ResponseCallbackAdvice implements Points {
	private static final String BOLT_INVOKER_CALLBACK_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.BoltInvokerCallback";
	private static final String GREATER_THAN_VERSION_5_3_0_BOLT_INVOKER_CALLBACK_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.bolt.BoltInvokerCallback";
	private static final String CONSTRUCT_METHOD_NAME = "<init>";
	private static final String SOFA_RESPONSE_CALLBACK_FULL_CLASS_NAME = "com.alipay.sofa.rpc.core.invoke.SofaResponseCallback";


	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.isPublic()
				.hasClassName(BOLT_INVOKER_CALLBACK_FULL_CLASS_NAME)
				.or()
				.isPublic()
				.hasClassName(GREATER_THAN_VERSION_5_3_0_BOLT_INVOKER_CALLBACK_FULL_CLASS_NAME)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.isPublic()
				.named(CONSTRUCT_METHOD_NAME)
				.argsLength(6)
				.arg(2, SOFA_RESPONSE_CALLBACK_FULL_CLASS_NAME)
				.build()
				.toSet();
	}
}
