package com.megaease.easeagent.plugin.dubbo.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class AlibabaDubboResponseFutureAdvice implements Points {
    private static final String RESPONSE_FUTURE_CLASS_NAME = "com.alibaba.dubbo.remoting.exchange.ResponseFuture";
    private static final String SET_CALLBACK_METHOD = "setCallback";

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasSuperClass(RESPONSE_FUTURE_CLASS_NAME)
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.builder()
            .named(SET_CALLBACK_METHOD)
            .build()
            .toSet();
    }
}
