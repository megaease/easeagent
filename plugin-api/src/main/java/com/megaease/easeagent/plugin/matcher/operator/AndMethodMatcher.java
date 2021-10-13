package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class AndMethodMatcher implements IMethodMatcher {
    protected IMethodMatcher left;
    protected IMethodMatcher right;

    public AndMethodMatcher(IMethodMatcher left, IMethodMatcher right) {
        this.left = left;
        this.right = right;
    }
}
