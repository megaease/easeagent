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

package com.megaease.easeagent.zipkin.impl;

import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.zipkin.TracingProviderImplMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageImplTest {
    Tracing tracing;
    TraceContext.Injector<Request> injector;
    TraceContext.Extractor<Request> extractor;


    @Before
    public void before() {
        tracing = TracingProviderImplMock.TRACING_PROVIDER.tracing();
        injector = tracing.propagation().injector(Request::setHeader);
        extractor = tracing.propagation().extractor(Request::header);
    }

    @Test
    public void get() {
        brave.Span bSpanN = SpanImpl.nextBraveSpan(tracing, extractor, new RequestMock());
        RequestMock requestMock = new RequestMock();
        injector.inject(bSpanN.context(), requestMock);
        TraceContextOrSamplingFlags traceContextOrSamplingFlags = extractor.extract(requestMock);
        MessageImpl message = new MessageImpl(traceContextOrSamplingFlags);
        assertEquals(traceContextOrSamplingFlags, message.get());

    }
}
