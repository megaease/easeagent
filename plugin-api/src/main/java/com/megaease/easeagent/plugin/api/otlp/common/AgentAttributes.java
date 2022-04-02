package com.megaease.easeagent.plugin.api.otlp.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
@SuppressWarnings("unchecked, rawtype, unused")
public final class AgentAttributes extends HashMap<AttributeKey<?>, Object> implements Attributes {
    @Nullable
    @Override
    public <T> T get(AttributeKey<T> key) {
        Object v = super.get(key);
        if (v == null) {
            return null;
        }
        return (T)v;
    }

    @Override
    public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
        super.forEach(consumer);
    }

    @Override
    public Map<AttributeKey<?>, Object> asMap() {
        return this;
    }

    @Override
    public AttributesBuilder toBuilder() {
        return new Builder().putAll(this);
    }

    public static AttributesBuilder builder() {
        return new Builder();
    }

    static class Builder implements AttributesBuilder {
        AgentAttributes attrs = new AgentAttributes();
        private final long capacity;
        private final int lengthLimit;

        public Builder() {
            this.capacity = Integer.MAX_VALUE;
            this.lengthLimit = Integer.MAX_VALUE;
        }
        public Builder(int capacity, int limit) {
            this.capacity = capacity;
            this.lengthLimit = limit;
        }

        @Override
        public Attributes build() {
            return this.attrs;
        }

        @Override
        public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
            if (this.attrs.size() > this.capacity) {
                return this;
            }
            this.attrs.put(key, value);
            return this;
        }

        @Override
        public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
            if (this.attrs.size() > this.capacity) {
                return this;
            }
            this.attrs.put(key, value);
            return this;
        }

        @Override
        public AttributesBuilder putAll(Attributes attributes) {
            if (attributes.size() + this.attrs.size() > this.capacity)  {
                return this;
            }
            this.attrs.putAll(attributes.asMap());
            return this;
        }
    }
}
