package com.megaease.easeagent.plugin.tools.matcher;

import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

public class MethodMatcherUtils {
    public static IMethodMatcher constructor() {
        return MethodMatcher.builder().named("<init>")
            .qualifier("constructor")
            .build();
    }
}
