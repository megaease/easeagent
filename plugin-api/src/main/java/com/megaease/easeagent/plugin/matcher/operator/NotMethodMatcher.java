package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class NotMethodMatcher implements IMethodMatcher {
    private String qualifier = DEFAULT_QUALIFIER;
    protected IMethodMatcher matcher;

    public NotMethodMatcher(IMethodMatcher matcher) {
        this.matcher = matcher;
    }

    public IMethodMatcher qualifier(String q) {
        this.qualifier = q;
        return this;
    }
}
