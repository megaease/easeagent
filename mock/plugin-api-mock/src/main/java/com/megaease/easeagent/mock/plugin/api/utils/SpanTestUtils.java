/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.mock.plugin.api.utils;

import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.plugin.api.trace.Span;

import static org.junit.Assert.assertEquals;

public class SpanTestUtils {
    public static void sameId(Span span, MockSpan mockSpan) {
        assertEquals(span.traceIdString(), mockSpan.traceId());
        assertEquals(span.spanIdString(), mockSpan.spanId());
        assertEquals(span.parentIdString(), mockSpan.parentId());
    }
}
