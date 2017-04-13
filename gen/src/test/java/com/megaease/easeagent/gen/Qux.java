package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Injection;

public abstract class Qux {

    @Injection.Bean
    public String str() {
        return "";
    }
}
