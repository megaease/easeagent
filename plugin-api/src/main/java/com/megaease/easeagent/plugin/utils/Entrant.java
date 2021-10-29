package com.megaease.easeagent.plugin.utils;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;

public class Entrant {
    public static boolean firstEnter(Config config, Context context, Object key) {
        if (!config.enable()) {
            return false;
        }
        if (context.enter(key) != 1) {
            return true;
        }
        return false;
    }

    public static boolean firstOut(Config config, Context context, Object key) {
        if (!config.enable()) {
            return false;
        }
        if (context.out(key) != 1) {
            return true;
        }
        return false;
    }
}
