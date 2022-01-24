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

package com.megaease.easeagent.zipkin;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;

import java.util.Map;
import java.util.function.Supplier;

public class CustomTagsSpanHandler extends SpanHandler {
    public static final String TAG_INSTANCE = "i";
    private final String instance;
    private final Supplier<String> serviceName;

    public CustomTagsSpanHandler(Supplier<String> serviceName, String instance) {
        this.serviceName = serviceName;
        this.instance = instance;
    }

    @Override
    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
        span.tag(TAG_INSTANCE, this.instance);
        span.localServiceName(this.serviceName.get());
        fillTags(span, ProgressFields.getServiceTags());
        fillTags(span, RedirectProcessor.tags());
        return true;
    }

    protected void fillTags(MutableSpan span, Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            span.tag(entry.getKey(), entry.getValue());
        }
    }
}
