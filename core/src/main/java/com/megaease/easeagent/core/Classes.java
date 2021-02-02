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

 package com.megaease.easeagent.core;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

public class Classes {
    public static Transforming transform(String... names) {
        return new Transforming(new ByteBuddy(), names);
    }

    public static class Transforming {
        private final ByteBuddy byteBuddy;
        private final String[] names;

        Transforming(ByteBuddy byteBuddy, String[] names) {
            this.byteBuddy = byteBuddy;
            this.names = names;
        }

        public Loaded with(Definition.Default def, final Object... beans) {
            final Register register = new Register(from(beans).transform(new Function<Object, QualifiedBean>() {
                @Override
                public QualifiedBean apply(Object input) {
                    return new QualifiedBean("", input);
                }
            }));
            return new Loaded(register, def.asMap());
        }

        public class Loaded {

            private final Register register;
            private final Map<ElementMatcher<? super TypeDescription>, Iterable<Definition.Transformer>> map;

            Loaded(Register register, Map<ElementMatcher<? super TypeDescription>, Iterable<Definition.Transformer>> map) {
                this.register = register;
                this.map = map;
            }

            public List<Class<?>> load(final ClassLoader loader) {
                final Map<ElementMatcher<? super TypeDescription>, Iterable<AgentBuilder.Transformer>> transformers =
                        Maps.transformValues(map, new Mapping(loader));

                final ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
                final TypePool pool = TypePool.Default.of(locator);

                return from(names).transform(new Function<String, TypeDescription>() {
                    @Override
                    public TypeDescription apply(String input) {
                        return pool.describe(input).resolve();
                    }
                }).transform(new Function<TypeDescription, Class<?>>() {
                    @Override
                    public Class<?> apply(TypeDescription input) {
                        for (ElementMatcher<? super TypeDescription> matcher : transformers.keySet()) {
                            if (matcher.matches(input)) {
                                DynamicType.Builder<?> builder = byteBuddy.redefine(input, locator);
                                for (AgentBuilder.Transformer transformer : transformers.get(matcher)) {
                                    builder = transformer.transform(builder, input, loader, null);
                                }
                                return builder.make().load(loader, ClassLoadingStrategy.Default.INJECTION).getLoaded();
                            }
                        }
                        return null;
                    }
                }).toList();
            }

            private class Mapping implements Function<Iterable<Definition.Transformer>, Iterable<AgentBuilder.Transformer>> {
                private final ClassLoader loader;

                Mapping(ClassLoader loader) {this.loader = loader;}

                @Override
                public Iterable<AgentBuilder.Transformer> apply(Iterable<Definition.Transformer> input) {
                    return Iterables.transform(input, new Function<Definition.Transformer, AgentBuilder.Transformer>() {
                        @Override
                        public AgentBuilder.Transformer apply(Definition.Transformer input) {
                            register.apply(input.adviceFactoryClassName, loader);
                            return new AgentBuilder.Transformer.ForAdvice().advice(input.matcher, input.inlineAdviceClassName);
                        }
                    });
                }
            }
        }

    }
}
