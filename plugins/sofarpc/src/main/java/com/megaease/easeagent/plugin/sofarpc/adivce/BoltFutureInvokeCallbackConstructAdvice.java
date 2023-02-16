package com.megaease.easeagent.plugin.sofarpc.adivce;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class BoltFutureInvokeCallbackConstructAdvice implements Points {
	private static final String BOLT_FUTURE_INVOKE_CALLBACK_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.bolt.BoltFutureInvokeCallback";
	private static final String CONSTRUCT_METHOD_NAME = "<init>";
	private static final String BOLT_RESPONSE_FUTURE_FULL_CLASS_NAME = "com.alipay.sofa.rpc.message.bolt.BoltResponseFuture";

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.isPublic()
				.hasClassName(BOLT_FUTURE_INVOKE_CALLBACK_FULL_CLASS_NAME)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.isPublic()
				.named(CONSTRUCT_METHOD_NAME)
				.argsLength(6)
				.arg(2, BOLT_RESPONSE_FUTURE_FULL_CLASS_NAME)
				.build()
				.toSet();
	}

}
