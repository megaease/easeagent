package com.megaease.easeagent.plugin.motan.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils;
import com.megaease.easeagent.plugin.tools.matcher.MethodMatcherUtils;

import java.util.Set;

public class MotanProviderAdvice implements Points {
    private static final String ENHANCE_CLASS = "com.weibo.api.motan.transport.ProviderMessageRouter";

    private static final String ENHANCE_METHOD = "call";

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcherUtils.name(ENHANCE_CLASS);
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(
                MethodMatcherUtils.name(ENHANCE_METHOD)
            )
            .build();
    }
}
