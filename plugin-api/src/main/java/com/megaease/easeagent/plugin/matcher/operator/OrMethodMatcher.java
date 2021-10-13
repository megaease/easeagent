package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class OrMethodMatcher extends MethodMatcher {
    protected MethodMatcher left;
    protected MethodMatcher right;

    public OrMethodMatcher(MethodMatcher left, MethodMatcher right) {
        this.left = left;
        this.right = right;
    }
}
