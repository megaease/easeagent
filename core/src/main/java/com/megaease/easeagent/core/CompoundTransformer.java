package com.megaease.easeagent.core;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.List;

public class CompoundTransformer implements AgentBuilder.Transformer {
    private final List<AgentBuilder.Transformer> transformers;

    public CompoundTransformer(List<AgentBuilder.Transformer> transformers) {
        this.transformers = new ArrayList<>();
        for (AgentBuilder.Transformer transformer : transformers) {
            if (transformer instanceof CompoundTransformer) {
                this.transformers.addAll(((CompoundTransformer) transformer).transformers);
                continue;
            }
            this.transformers.add(transformer);
        }
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        for (AgentBuilder.Transformer transformer : this.transformers) {
            builder = transformer.transform(builder, typeDescription, classLoader, module);
        }
        return builder;
    }
}
