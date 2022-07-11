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

package com.megaease.easeagent.core.plugin.transformer.advice;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.LatentMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaModule;

import java.util.*;

import static net.bytebuddy.agent.builder.AgentBuilder.*;

@SuppressWarnings("unused")
public class AgentForAdvice extends Transformer.ForAdvice {
    /**
     * The advice to use.
     */
    private final AgentAdvice.WithCustomMapping advice;

    /**
     * The exception handler to register for the advice.
     */
    private final Advice.ExceptionHandler exceptionHandler;

    /**
     * The assigner to use for the advice.
     */
    private final Assigner assigner;

    /**
     * The class file locator to query for the advice class.
     */
    private final ClassFileLocator classFileLocator;

    /**
     * The pool strategy to use for looking up an advice.
     */
    private final PoolStrategy poolStrategy;

    /**
     * The location strategy to use for class loaders when resolving advice classes.
     */
    private final LocationStrategy locationStrategy;

    /**
     * The advice entries to apply.
     */
    private final List<Entry> entries;

    public AgentForAdvice() {
        this(new AgentAdvice.WithCustomMapping());
    }

    public AgentForAdvice(AgentAdvice.WithCustomMapping advice) {
        this(advice,
            Advice.ExceptionHandler.Default.SUPPRESSING,
            Assigner.DEFAULT,
            ClassFileLocator.NoOp.INSTANCE,
            PoolStrategy.Default.FAST,
            LocationStrategy.ForClassLoader.STRONG,
            Collections.emptyList());
    }

    protected AgentForAdvice(AgentAdvice.WithCustomMapping advice,
                             Advice.ExceptionHandler exceptionHandler,
                             Assigner assigner,
                             ClassFileLocator classFileLocator,
                             PoolStrategy poolStrategy,
                             LocationStrategy locationStrategy,
                             List<Entry> entries) {
        this.advice = advice;
        this.exceptionHandler = exceptionHandler;
        this.assigner = assigner;
        this.classFileLocator = classFileLocator;
        this.poolStrategy = poolStrategy;
        this.locationStrategy = locationStrategy;
        this.entries = entries;
    }

    /**
     * Includes the supplied class loaders as a source for looking up an advice class or its dependencies.
     * Note that the supplied class loaders are queried for types before the class loader of the instrumented class.
     *
     * @param classLoader The class loaders to include when looking up classes in their order. Duplicates are filtered.
     * @return A new instance of this advice transformer that considers the supplied class loaders as a lookup source.
     */
    @Override
    public AgentForAdvice include(ClassLoader... classLoader) {
        Set<ClassFileLocator> classFileLocators = new LinkedHashSet<>();
        for (ClassLoader aClassLoader : classLoader) {
            classFileLocators.add(ClassFileLocator.ForClassLoader.of(aClassLoader));
        }
        return include(new ArrayList<>(classFileLocators));
    }

    /**
     * Includes the supplied class file locators as a source for looking up an advice class or its dependencies.
     * Note that the supplied class loaders are queried for types before the class loader of the instrumented class.
     *
     * @param classFileLocator The class file locators to include when looking up classes in their order. Duplicates are filtered.
     * @return A new instance of this advice transformer that considers the supplied class file locators as a lookup source.
     */
    @Override
    public AgentForAdvice include(ClassFileLocator... classFileLocator) {
        return include(Arrays.asList(classFileLocator));
    }

    /**
     * Includes the supplied class file locators as a source for looking up an advice class or its dependencies.
     * Note that the supplied class loaders are queried for types before the class loader of the instrumented class.
     *
     * @param classFileLocators The class file locators to include when looking up classes in their order. Duplicates are filtered.
     * @return A new instance of this advice transformer that considers the supplied class file locators as a lookup source.
     */
    @Override
    public AgentForAdvice include(List<? extends ClassFileLocator> classFileLocators) {
        return new AgentForAdvice(advice,
            exceptionHandler,
            assigner,
            new ClassFileLocator.Compound(CompoundList.of(classFileLocator, classFileLocators)),
            poolStrategy,
            locationStrategy,
            entries);
    }

    /**
     * Applies the given advice class onto all methods that satisfy the supplied matcher.
     *
     * @param matcher The matcher to determine what methods the advice should be applied to.
     * @param name    The fully-qualified, binary name of the advice class.
     * @return A new instance of this advice transformer that applies the given advice to all matched methods of an instrumented type.
     */
    @Override
    public AgentForAdvice advice(ElementMatcher<? super MethodDescription> matcher, String name) {
        return advice(new LatentMatcher.Resolved<>(matcher), name);
    }

    /**
     * Applies the given advice class onto all methods that satisfy the supplied matcher.
     *
     * @param matcher The matcher to determine what methods the advice should be applied to.
     * @param name    The fully-qualified, binary name of the advice class.
     * @return A new instance of this advice transformer that applies the given advice to all matched methods of an instrumented type.
     */
    @Override
    public AgentForAdvice advice(LatentMatcher<? super MethodDescription> matcher, String name) {
        return new AgentForAdvice(advice,
            exceptionHandler,
            assigner,
            classFileLocator,
            poolStrategy,
            locationStrategy,
            CompoundList.of(entries, new ForUnifiedAdvice(matcher, name)));
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                            TypeDescription typeDescription,
                                            ClassLoader classLoader,
                                            JavaModule module) {
        ClassFileLocator classFileLocator = new ClassFileLocator.Compound(this.classFileLocator,
            locationStrategy.classFileLocator(classLoader, module));

        TypePool typePool = poolStrategy.typePool(classFileLocator, classLoader);
        AsmVisitorWrapper.ForDeclaredMethods asmVisitorWrapper = new AsmVisitorWrapper.ForDeclaredMethods();
        for (Entry entry : entries) {
            asmVisitorWrapper = asmVisitorWrapper.invokable(entry.getMatcher().resolve(typeDescription),
                entry.resolve(advice, typePool, classFileLocator)
                    .withAssigner(assigner)
                    .withExceptionHandler(exceptionHandler));
        }
        return builder.visit(asmVisitorWrapper);
    }

    @HashCodeAndEqualsPlugin.Enhance
    protected abstract static class Entry {
        /**
         * The matcher for advised methods.
         */
        private final LatentMatcher<? super MethodDescription> matcher;

        /**
         * Creates a new entry.
         *
         * @param matcher The matcher for advised methods.
         */
        protected Entry(LatentMatcher<? super MethodDescription> matcher) {
            this.matcher = matcher;
        }

        /**
         * Returns the matcher for advised methods.
         *
         * @return The matcher for advised methods.
         */
        protected LatentMatcher<? super MethodDescription> getMatcher() {
            return matcher;
        }

        /**
         * Resolves the advice for this entry.
         *
         * @param advice           The advice configuration.
         * @param typePool         The type pool to use.
         * @param classFileLocator The class file locator to use.
         * @return The resolved advice.
         */
        protected abstract AgentAdvice resolve(AgentAdvice.WithCustomMapping advice,
                                               TypePool typePool, ClassFileLocator classFileLocator);
    }

    @HashCodeAndEqualsPlugin.Enhance
    protected static class ForUnifiedAdvice extends Entry {
        /**
         * The name of the advice class.
         */
        protected final String name;

        /**
         * Creates a new entry for an advice class where both the (optional) entry and exit advice methods are declared by the same class.
         *
         * @param matcher The matcher for advised methods.
         * @param name    The name of the advice class.
         */
        protected ForUnifiedAdvice(LatentMatcher<? super MethodDescription> matcher, String name) {
            super(matcher);
            this.name = name;
        }

        @Override
        protected AgentAdvice resolve(AgentAdvice.WithCustomMapping advice, TypePool typePool, ClassFileLocator classFileLocator) {
            return advice.to(typePool.describe(name).resolve(), classFileLocator);
        }
    }
}
