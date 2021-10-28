package com.megaease.easeagent.plugin.jdkhttpserver.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.annotation.Pointcut;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

@Pointcut
public class DoFilterAdvice implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasClassName("sun.net.httpserver.AuthFilter")
            .notAbstract()
            .notInterface()
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("doFilter")
                .isPublic()
                .argsLength(2)
                .returnType("void")
                .qualifier("default")
                .build())
            .build();
    }
}
