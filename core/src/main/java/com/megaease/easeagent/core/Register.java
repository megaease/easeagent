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

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;

class Register {
    private static final Logger LOGGER = LoggerFactory.getLogger(Register.class);

    private final Iterable<QualifiedBean> beans;
    private final Set<Integer> applied;

    Register(Iterable<QualifiedBean> beans) {
        this.beans = beans;
        applied = new HashSet<Integer>();
    }

    void apply(String adviceClassName, ClassLoader external) {
        if (!applied.add(Objects.hashCode(external, adviceClassName))) return;

        try {
            final Class<?> aClass = compound(getClass().getClassLoader(), external).loadClass(adviceClassName);
            final Object obj = newInstanceOf(aClass);
            final FluentIterable<Method> methods = adviceFactoryMethods(aClass);
            for (Method method : methods) {
                String name = aClass.getName() + "#" + method.getName();
                Dispatcher.register(name, (Dispatcher.Advice) method.invoke(obj));
                LOGGER.debug("Registered {} for {}", name, external);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private ClassLoader compound(ClassLoader parent, ClassLoader external) throws Exception {
        try {
            parent.getClass().getDeclaredMethod("add", ClassLoader.class).invoke(parent, external);
        } catch (Exception e) {
            LOGGER.warn("{}, this may be a bug if it was running in production", e.toString());
        }
        return parent;
    }

    private Object newInstanceOf(Class<?> aClass) {
        return from(aClass.getConstructors()).firstMatch(Injection.Autowire.AUTOWIRED_CONS)
                .transform(new ConstructorNew())
                .or(new DefaultConstructorNew(aClass));
    }

    private FluentIterable<Method> adviceFactoryMethods(Class<?> aClass) {
        return from(aClass.getDeclaredMethods()).filter(new Predicate<Method>() {
            @Override
            public boolean apply(Method input) {
                return Dispatcher.Advice.class.isAssignableFrom(input.getReturnType());
            }
        });
    }

    private static class DefaultConstructorNew implements Supplier<Object> {

        private final Class<?> aClass;

        DefaultConstructorNew(Class<?> aClass) {
            this.aClass = aClass;
        }

        @Override
        public Object get() {
            try {
                LOGGER.debug("No @AutoWired constructor found, so use default to instead");
                return aClass.newInstance();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private class ConstructorNew implements Function<Constructor<?>, Object> {

        @Override
        public Object apply(Constructor<?> input) {
            try {
                return input.newInstance(args(input));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private Object[] args(final Constructor<?> cons) {
            final List<Object> params = Lists.newArrayList();
            final Class<?>[] parameterTypes = cons.getParameterTypes();
            final Annotation[][] parameterAnnotations = cons.getParameterAnnotations();

            for (int i = 0; i < parameterTypes.length; i++) {

                final Class<?> aClass = parameterTypes[i];

                final String qualifier = qualifier(parameterAnnotations[i]);

                final Object bean = from(beans).firstMatch(new Predicate<QualifiedBean>() {
                    @Override
                    public boolean apply(QualifiedBean input) {
                        return input.matches(aClass, qualifier);
                    }
                }).transform(new Function<QualifiedBean, Object>() {
                    @Override
                    public Object apply(QualifiedBean input) {
                        return input.bean;
                    }
                }).or(new Supplier<QualifiedBean>() {
                    @Override
                    public QualifiedBean get() {
                        final String append = Strings.isNullOrEmpty(qualifier) ? "" : "[" + qualifier + "]";
                        final String msg = String.format("Miss bean %s%s for %s",
                                aClass.getCanonicalName(), append, cons.getDeclaringClass());
                        throw new IllegalStateException(msg);
                    }
                });


                params.add(bean);
            }

            return params.toArray();
        }

        private String qualifier(Annotation[] annotations) {
            return from(annotations).firstMatch(new Predicate<Annotation>() {
                @Override
                public boolean apply(Annotation input) {
                    return input instanceof Injection.Qualifier;
                }
            }).transform(new Function<Annotation, String>() {
                @Override
                public String apply(Annotation input) {
                    return ((Injection.Qualifier) input).value();
                }
            }).or("");
        }
    }

}