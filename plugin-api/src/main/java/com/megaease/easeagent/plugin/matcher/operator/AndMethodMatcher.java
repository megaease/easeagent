package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class AndMethodMatcher implements IMethodMatcher {
    private String qualifier = DEFAULT_QUALIFIER;
    protected IMethodMatcher left;
    protected IMethodMatcher right;

    public AndMethodMatcher(IMethodMatcher left, IMethodMatcher right) {
        this.left = left;
        this.right = right;
    }

    public IMethodMatcher qualifier(String q) {
        this.qualifier = q;
        return this;
    }
}
