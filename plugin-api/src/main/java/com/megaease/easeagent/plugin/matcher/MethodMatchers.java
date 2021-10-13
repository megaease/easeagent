package com.megaease.easeagent.plugin.matcher;

import com.megaease.easeagent.plugin.enums.StringMatch;

public class MethodMatchers {
    public static MethodMatcher named(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.EQUALS)
            .build();
    }

    public static MethodMatcher nameStartWith(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.START_WITH)
            .build();
    }

    public static MethodMatcher nameEndWith(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.END_WITH)
            .build();
    }

    public static MethodMatcher nameContains(String methodName) {
        return MethodMatcher.builder()
            .name(methodName)
            .nameMatchType(StringMatch.CONTAINS)
            .build();
    }
}
