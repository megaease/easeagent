package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class AndMethodMatcher extends MethodMatcher {
    protected MethodMatcher left;
    protected MethodMatcher right;

    public AndMethodMatcher(MethodMatcher left, MethodMatcher right) {
        this.left = left;
        this.right = right;
    }
}
