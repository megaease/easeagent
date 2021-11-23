/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.zipkin.impl;

import brave.Span;
import brave.propagation.Propagation;
import com.megaease.easeagent.plugin.api.trace.Request;

public class RemoteGetterImpl<R extends Request> implements Propagation.RemoteGetter<R> {
    private final brave.Span.Kind kind;

    public RemoteGetterImpl(Span.Kind kind) {
        this.kind = kind;
    }

    @Override
    public Span.Kind spanKind() {
        return kind;
    }

    @Override
    public String get(R request, String fieldName) {
        return request.header(fieldName);
    }

}
