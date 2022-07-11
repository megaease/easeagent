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

import brave.Span;
import org.junit.Test;

import static org.junit.Assert.*;

public class RemoteSetterImplTest {

    @Test
    public void spanKind() {
        RemoteSetterImpl remoteSetter = new RemoteSetterImpl(Span.Kind.PRODUCER);
        assertEquals(Span.Kind.PRODUCER, remoteSetter.spanKind());
    }

    @Test
    public void put() {
        RemoteSetterImpl remoteSetter = new RemoteSetterImpl(Span.Kind.PRODUCER);
        RequestMock requestMock = new RequestMock();
        String name = "test_name";
        String value = "test_value";
        remoteSetter.put(requestMock, name, value);
        assertEquals(value, requestMock.header(name));
    }
}
