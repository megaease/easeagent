package com.megaease.easeagent.plugin.api.trace;

public interface Extractor<R extends MessagingRequest> {
    Message extract(R request);
}
