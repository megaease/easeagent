package com.megaease.easeagent.config;

class Common {
    static String fixPrefix(String text) {
        if (text.endsWith(".")) {
            return text;
        }
        return text + ".";
    }
}
