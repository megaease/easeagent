package com.megaease.easeagent.plugin.httpfilter.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.annotation.Pointcut;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

@Pointcut
public class DoFilterAdvice implements Points {
    private static final String FILTER_NAME = "javax.servlet.Filter";
    private static final String HTTP_SERVLET_NAME = "javax.servlet.http.HttpServlet";
    static final String SERVLET_REQUEST = "javax.servlet.ServletRequest";
    static final String SERVLET_RESPONSE = "javax.servlet.ServletResponse";

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasInterface(FILTER_NAME)
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("doFilter")
                .isPublic()
                .argsLength(3)
                .arg(0, SERVLET_REQUEST)
                .arg(1, SERVLET_RESPONSE)
                .returnType("void")
                .qualifier("default")
                .build())
            .build();
    }
}
