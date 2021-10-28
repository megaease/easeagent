package com.megaease.easeagent.plugin.springweb.advice;

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
            .hasClassName("org.springframework.web.filter.OncePerRequestFilter")
            .isAbstract()
            .notInterface()
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("doFilter")
                .isPublic()
                .argsLength(3)
                .returnType("void")
                .qualifier("default")
                .build())
            .build();
    }
}
