package com.megaease.easeagent.zipkin.impl.message;

import brave.messaging.ProducerRequest;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;

public class ZipkinProducerRequest<R extends MessagingRequest> extends ProducerRequest {
    private final R request;

    public ZipkinProducerRequest(R request) {
        this.request = request;
    }

    @Override
    public String operation() {
        return request.operation();
    }

    @Override
    public String channelKind() {
        return request.channelKind();
    }

    @Override
    public String channelName() {
        return request.channelName();
    }

    @Override
    public Object unwrap() {
        return request.unwrap();
    }
}
