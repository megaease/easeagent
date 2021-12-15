package com.megaease.easeagent.plugin.api.trace;

public interface Getter {
    /**
     * @param name
     * @return
     * @see Request#header(String)
     */
    String header(String name);
}
