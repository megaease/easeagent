package com.megaease.easeagent.plugin.motan.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class MotanConsumerAdvice implements Points {
    private static final String ENHANCE_CLASS = "com.weibo.api.motan.rpc.AbstractReferer";

    private static final String ENHANCE_METHOD = "doCall";

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasSuperClass(ENHANCE_CLASS)
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(
                MethodMatcher.builder()
                    .named(ENHANCE_METHOD)
                    .build()
            )
            .build();
    }
}
