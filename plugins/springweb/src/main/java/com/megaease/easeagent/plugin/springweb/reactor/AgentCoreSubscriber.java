/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.megaease.easeagent.plugin.springweb.reactor;

import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.springweb.interceptor.tracing.WebClientFilterTracingInterceptor.WebClientResponse;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import org.reactivestreams.Subscription;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public class AgentCoreSubscriber implements CoreSubscriber<ClientResponse> {

    private final CoreSubscriber<ClientResponse> actual;
    private final MethodInfo methodInfo;
    // private final Integer chain;
    private final RequestContext requestContext;
    private AtomicReference<ClientResponse> result = new AtomicReference<>();

    @SuppressWarnings("unchecked")
    public AgentCoreSubscriber(CoreSubscriber<? super ClientResponse> actual, MethodInfo methodInfo,
                               RequestContext context) {
        this.actual = (CoreSubscriber<ClientResponse>) actual;
        this.methodInfo = methodInfo;
        // this.chain = chain;
        this.requestContext = context;
    }

    @Override
    public reactor.util.context.Context currentContext() {
        return actual.currentContext();
    }

    @Override
    public void onSubscribe(@Nonnull Subscription s) {
        actual.onSubscribe(s);
    }

    @Override
    public void onNext(ClientResponse t) {
        actual.onNext(t);
        if (result.get() == null) {
            result.set(t);
        }
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
        methodInfo.setThrowable(t);
        finish();
        // EaseAgent.dispatcher.exit(chain, methodInfo, getContext(), results, t);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
        finish();
    }

    private void finish() {
        ClientResponse resp = result.get();
        if (resp != null) {
            WebClientResponse webClientResponse = new WebClientResponse(methodInfo.getThrowable(), resp);
            HttpUtils.save(requestContext.span(), webClientResponse);
            this.requestContext.finish(webClientResponse);
        } else if (methodInfo.isSuccess()) {
            requestContext.span().finish();
        } else {
            Span span = requestContext.span();
            span.error(methodInfo.getThrowable());
            span.finish();
        }
    }
}
