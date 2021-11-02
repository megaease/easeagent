package com.megaease.easeagent.plugin.springweb.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.annotation.Pointcut;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

@Pointcut
public class FeignClientAdvice implements Points {

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasInterface("feign.Client")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("execute")
                .isPublic()
                .argsLength(2)
                .qualifier("default")
                .build())
            .build();
    }
}
