package com.megaease.easeagent.report.util;

public class TextUtils {
    public static boolean hasText(String content) {
        return content != null && content.trim().length() > 0;
    }

    public static boolean isEmpty(String value) {
        return !hasText(value);
    }
}
