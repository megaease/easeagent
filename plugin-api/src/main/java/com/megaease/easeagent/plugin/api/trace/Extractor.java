package com.megaease.easeagent.plugin.api.trace;

public interface Extractor<R extends Request> {
    Object extract(R request);
}
