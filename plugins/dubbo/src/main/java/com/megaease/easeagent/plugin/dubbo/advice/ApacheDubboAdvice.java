package com.megaease.easeagent.plugin.dubbo.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class ApacheDubboAdvice implements Points {
	private static final String ENHANCE_CLASS = "org.apache.dubbo.monitor.support.MonitorFilter";

	private static final String ENHANCE_METHOD = "invoke";
	private static final String ENHANCE_METHOD_PARAMS_ONE = "org.apache.dubbo.rpc.Invoker";
	private static final String ENHANCE_METHOD_PARAMS_TWO = "org.apache.dubbo.rpc.Invocation";
	private static final String ENHANCE_METHOD_RETURN_TYPE = "org.apache.dubbo.rpc.Result";


	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasClassName(ENHANCE_CLASS)
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.builder()
				.isPublic()
				.named(ENHANCE_METHOD)
				.arg(0, ENHANCE_METHOD_PARAMS_ONE)
				.arg(1, ENHANCE_METHOD_PARAMS_TWO)
				.returnType(ENHANCE_METHOD_RETURN_TYPE)
				.build()
				.toSet();
	}
}
