package com.megaease.easeagent.plugin.tomcat.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class FilterChainPoints implements Points {
    private static final String FILTER_NAME = "jakarta.servlet.FilterChain";
    private static final String HTTP_SERVLET_NAME = "jakarta.servlet.http.HttpServlet";
    static final String SERVLET_REQUEST = "jakarta.servlet.ServletRequest";
    static final String SERVLET_RESPONSE = "jakarta.servlet.ServletResponse";

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasInterface(FILTER_NAME)
            .build().or(ClassMatcher.builder()
                .hasSuperClass(HTTP_SERVLET_NAME)
                .build());
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("doFilter")
                .arg(0, SERVLET_REQUEST)
                .arg(1, SERVLET_RESPONSE)
                .or()
                .named("service")
                .arg(0, SERVLET_REQUEST)
                .arg(1, SERVLET_RESPONSE)
                .qualifier("default")
                .build())
            .build();
    }
}
