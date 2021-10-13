package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class NotMethodMatcher implements IMethodMatcher {
    protected IMethodMatcher matcher;

    public NotMethodMatcher(IMethodMatcher matcher) {
        this.matcher = matcher;
    }
}
