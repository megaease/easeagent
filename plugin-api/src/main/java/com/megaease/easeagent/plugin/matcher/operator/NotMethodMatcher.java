package com.megaease.easeagent.plugin.matcher.operator;

import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import lombok.Getter;

@Getter
public class NotMethodMatcher extends MethodMatcher {
    protected MethodMatcher matcher;

    public NotMethodMatcher(MethodMatcher matcher) {
        this.matcher = matcher;
    }
}
