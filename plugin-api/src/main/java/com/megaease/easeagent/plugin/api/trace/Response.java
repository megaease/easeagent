package com.megaease.easeagent.plugin.api.trace;

import java.util.Set;

public interface Response {
    Set<String> keys();

    String header(String name);
}
