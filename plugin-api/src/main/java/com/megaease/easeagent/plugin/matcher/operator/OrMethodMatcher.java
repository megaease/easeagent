package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class OrMethodMatcher implements IMethodMatcher {
    protected IMethodMatcher left;
    protected IMethodMatcher right;

    public OrMethodMatcher(IMethodMatcher left, IMethodMatcher right) {
        this.left = left;
        this.right = right;
    }
}
