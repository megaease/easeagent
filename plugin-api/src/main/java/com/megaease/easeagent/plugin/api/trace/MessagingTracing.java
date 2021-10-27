package com.megaease.easeagent.plugin.api.trace;

import java.util.function.Function;

public interface MessagingTracing<R extends MessagingRequest> {
    Extractor<R> extractor();

    Injector<R> injector();

    Function<R, Boolean> consumerSampler();

    Function<R, Boolean> producerSampler();

    boolean consumerSampler(R request);

    boolean producerSampler(R request);
}
