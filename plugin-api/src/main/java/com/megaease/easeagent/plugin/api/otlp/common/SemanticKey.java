package com.megaease.easeagent.plugin.api.otlp.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class SemanticKey {
    public static final String SCHEMA_URL = SemanticAttributes.SCHEMA_URL;
    public static final AttributeKey<String> THREAD_NAME = SemanticAttributes.THREAD_NAME;
    public static final AttributeKey<Long> THREAD_ID = SemanticAttributes.THREAD_ID;
}
