/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.plugin.transformer.advice;

import com.megaease.easeagent.core.plugin.registry.AdviceRegistry;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.enumeration.EnumerationDescription;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.scaffold.FieldLocator;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.*;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.*;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaConstant;
import net.bytebuddy.utility.JavaType;
import net.bytebuddy.utility.OpenedClassReader;
import net.bytebuddy.utility.visitor.ExceptionTableSensitiveMethodVisitor;
import net.bytebuddy.utility.visitor.FramePaddingMethodVisitor;
import net.bytebuddy.utility.visitor.LineNumberPrependingMethodVisitor;
import net.bytebuddy.utility.visitor.StackAwareMethodVisitor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static net.bytebuddy.matcher.ElementMatchers.*;

@SuppressWarnings("unused, rawtypes, unchecked")
public class AgentAdvice extends Advice {
    private static final ClassReader UNDEFINED = null;

    /**
     * A reference to the {@link OnMethodEnter#skipOn()} method.
     */
    private static final MethodDescription.InDefinedShape SKIP_ON;

    /**
     * A reference to the {@link OnMethodEnter#prependLineNumber()} method.
     */
    private static final MethodDescription.InDefinedShape PREPEND_LINE_NUMBER;

    /**
     * A reference to the {@link OnMethodEnter#inline()} method.
     */
    private static final MethodDescription.InDefinedShape INLINE_ENTER;

    /**
     * A reference to the {@link OnMethodEnter#suppress()} method.
     */
    private static final MethodDescription.InDefinedShape SUPPRESS_ENTER;

    /**
     * A reference to the {@link OnMethodExit#repeatOn()} method.
     */
    private static final MethodDescription.InDefinedShape REPEAT_ON;

    /**
     * A reference to the {@link OnMethodExit#onThrowable()} method.
     */
    private static final MethodDescription.InDefinedShape ON_THROWABLE;

    /**
     * A reference to the {@link OnMethodExit#backupArguments()} method.
     */
    private static final MethodDescription.InDefinedShape BACKUP_ARGUMENTS;

    /**
     * A reference to the {@link OnMethodExit#inline()} method.
     */
    private static final MethodDescription.InDefinedShape INLINE_EXIT;

    /**
     * A reference to the {@link OnMethodExit#suppress()} method.
     */
    private static final MethodDescription.InDefinedShape SUPPRESS_EXIT;

    static {
        MethodList<MethodDescription.InDefinedShape> enter = TypeDescription.ForLoadedType.of(OnMethodEnter.class).getDeclaredMethods();
        SKIP_ON = enter.filter(named("skipOn")).getOnly();
        PREPEND_LINE_NUMBER = enter.filter(named("prependLineNumber")).getOnly();
        INLINE_ENTER = enter.filter(named("inline")).getOnly();
        SUPPRESS_ENTER = enter.filter(named("suppress")).getOnly();
        MethodList<MethodDescription.InDefinedShape> exit = TypeDescription.ForLoadedType.of(OnMethodExit.class).getDeclaredMethods();
        REPEAT_ON = exit.filter(named("repeatOn")).getOnly();
        ON_THROWABLE = exit.filter(named("onThrowable")).getOnly();
        BACKUP_ARGUMENTS = exit.filter(named("backupArguments")).getOnly();
        INLINE_EXIT = exit.filter(named("inline")).getOnly();
        SUPPRESS_EXIT = exit.filter(named("suppress")).getOnly();
    }



    /**
     * The dispatcher for instrumenting the instrumented method upon entering.
     */
    private final Dispatcher.Resolved.ForMethodEnter methodEnter;

    /**
     * The dispatcher for instrumenting the instrumented method upon exiting.
     */
    private final Dispatcher.Resolved.ForMethodExit methodExit;

    private final Dispatcher.Resolved.ForMethodExit methodExitNonThrowable;

    /**
     * The assigner to use.
     */
    private final Assigner assigner;

    /**
     * The exception handler to apply.
     */
    private final ExceptionHandler exceptionHandler;

    /**
     * The delegate implementation to apply if this advice is used as an instrumentation.
     */
    private final Implementation delegate;

    /**
     * Creates a new advice.
     *
     * @param methodEnter The dispatcher for instrumenting the instrumented method upon entering.
     * @param methodExit  The dispatcher for instrumenting the instrumented method upon exiting.
     */
    protected AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter,
                          // Dispatcher.Resolved.ForMethodExit methodExitNonThrowable,
                          Dispatcher.Resolved.ForMethodExit methodExit) {
        this(methodEnter, methodExit,
            null,
            Assigner.DEFAULT, ExceptionHandler.Default.SUPPRESSING, SuperMethodCall.INSTANCE);
    }

    protected AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter,
                          Dispatcher.Resolved.ForMethodExit methodExitNonThrowable,
                          Dispatcher.Resolved.ForMethodExit methodExit) {
        this(methodEnter, methodExit,
            methodExitNonThrowable,
            Assigner.DEFAULT,
            ExceptionHandler.Default.SUPPRESSING,
            SuperMethodCall.INSTANCE);
    }

    /**
     * Creates a new advice.
     *
     * @param methodEnter      The dispatcher for instrumenting the instrumented method upon entering.
     * @param methodExit       The dispatcher for instrumenting the instrumented method upon exiting.
     * @param assigner         The assigner to use.
     * @param exceptionHandler The exception handler to apply.
     * @param delegate         The delegate implementation to apply if this advice is used as an instrumentation.
     */
    private AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter,
                        Dispatcher.Resolved.ForMethodExit methodExit,
                        Dispatcher.Resolved.ForMethodExit methodExitNonThrowable,
                        Assigner assigner,
                        ExceptionHandler exceptionHandler,
                        Implementation delegate) {
        super(null, null);
        this.methodEnter = methodEnter;
        this.methodExit = methodExit;
        this.methodExitNonThrowable = methodExitNonThrowable;
        this.assigner = assigner;
        this.exceptionHandler = exceptionHandler;
        this.delegate = delegate;
    }

    private static boolean isNoExceptionHandler(TypeDescription t) {
        return t.getName().endsWith("NoExceptionHandler");
        // return t.represents(NoExceptionHandler.class) || t.represents()
    }

    /**
     * Creates a new advice.
     *
     * @param advice               A description of the type declaring the advice.
     * @param postProcessorFactory The post processor factory to use.
     * @param classFileLocator     The class file locator for locating the advisory class's class file.
     * @param userFactories        A list of custom factories for user generated offset mappings.
     * @param delegator            The delegator to use.
     * @return A method visitor wrapper representing the supplied advice.
     */
    protected static AgentAdvice tto(TypeDescription advice,
                                    PostProcessor.Factory postProcessorFactory,
                                    ClassFileLocator classFileLocator,
                                    List<? extends OffsetMapping.Factory<?>> userFactories,
                                    Delegator delegator) {
        Dispatcher.Unresolved methodEnter = Dispatcher.Inactive.INSTANCE,
            methodExit = Dispatcher.Inactive.INSTANCE,
            methodExitNoException = Dispatcher.Inactive.INSTANCE;
        for (MethodDescription.InDefinedShape methodDescription : advice.getDeclaredMethods()) {
            methodEnter = locate(OnMethodEnter.class, INLINE_ENTER, methodEnter, methodDescription, delegator);
            AnnotationDescription.Loadable<?> al = methodDescription.getDeclaredAnnotations().ofType(OnMethodExit.class);
            if (al != null) {
                TypeDescription throwable = al.getValue(ON_THROWABLE).resolve(TypeDescription.class);
                if (isNoExceptionHandler(throwable)) {
                    methodExitNoException = locate(OnMethodExit.class, INLINE_EXIT, methodExitNoException, methodDescription, delegator);
                } else {
                    methodExit = locate(OnMethodExit.class, INLINE_EXIT, methodExit, methodDescription, delegator);
                }
            }
        }
        if (methodExit == Dispatcher.Inactive.INSTANCE) {
            methodEnter = methodExitNoException;
        }
        if (!methodEnter.isAlive() && !methodExit.isAlive() && !methodExitNoException.isAlive()) {
            throw new IllegalArgumentException("No advice defined by " + advice);
        }
        try {
            ClassReader classReader = methodEnter.isBinary() || methodExit.isBinary()
                ? OpenedClassReader.of(classFileLocator.locate(advice.getName()).resolve())
                : UNDEFINED;
            return new AgentAdvice(methodEnter.asMethodEnter(userFactories, classReader, methodExit, postProcessorFactory),
                methodExitNoException.asMethodExit(userFactories, classReader, methodEnter, postProcessorFactory),
                methodExit.asMethodExit(userFactories, classReader, methodEnter, postProcessorFactory));
        } catch (IOException exception) {
            throw new IllegalStateException("Error reading class file of " + advice, exception);
        }
    }

    public static AgentAdvice.WithCustomMapping withCustomMapping() {
        return new WithCustomMapping();
    }
    private static Dispatcher.Unresolved locate(Class<? extends Annotation> type,
                                                MethodDescription.InDefinedShape property,
                                                Dispatcher.Unresolved dispatcher,
                                                MethodDescription.InDefinedShape methodDescription,
                                                Delegator delegator) {
        AnnotationDescription annotation = methodDescription.getDeclaredAnnotations().ofType(type);
        if (annotation == null) {
            return dispatcher;
        } else if (dispatcher.isAlive()) {
            throw new IllegalStateException("Duplicate advice for " + dispatcher + " and " + methodDescription);
        } else if (!methodDescription.isStatic()) {
            throw new IllegalStateException("Advice for " + methodDescription + " is not static");
        } else {
            return new Dispatcher.Inlining(methodDescription);
        }
    }

    /**
     * Configures this advice to use the specified assigner. Any previous or default assigner is replaced.
     *
     * @param assigner The assigner to use,
     * @return A version of this advice that uses the specified assigner.
     */
    public AgentAdvice withAssigner(Assigner assigner) {
        return new AgentAdvice(methodEnter, methodExitNonThrowable,
            methodExit, assigner, exceptionHandler, delegate);
    }

    /**
     * Configures this advice to execute the given exception handler upon a suppressed exception. The stack manipulation is executed with a
     * {@link Throwable} instance on the operand stack. The stack must be empty upon completing the exception handler.
     *
     * @param exceptionHandler The exception handler to apply.
     * @return A version of this advice that applies the supplied exception handler.
     */
    public AgentAdvice withExceptionHandler(ExceptionHandler exceptionHandler) {
        return new AgentAdvice(methodEnter, methodExitNonThrowable,
            methodExit, assigner, exceptionHandler, delegate);
    }

    /**
     * A builder step for creating an {@link Advice} that uses custom mappings of annotations to constant pool values.
     */
    @HashCodeAndEqualsPlugin.Enhance
    public static class WithCustomMapping extends Advice.WithCustomMapping {
        /**
         * The post processor factory to apply.
         */
        private final PostProcessor.Factory postProcessorFactory;

        /**
         * The delegator to use.
         */
        private final Delegator delegator;

        /**
         * A map containing dynamically computed constant pool values that are mapped by their triggering annotation type.
         */
        private final Map<Class<? extends Annotation>, OffsetMapping.Factory<?>> offsetMappings;

        /**
         * Creates a new custom mapping builder step without including any custom mappings.
         */
        public WithCustomMapping() {
            this(PostProcessor.NoOp.INSTANCE, Collections.emptyMap(), Delegator.ForStaticInvocation.INSTANCE);
        }


        /**
         * Creates a new custom mapping builder step with the given custom mappings.
         *
         * @param postProcessorFactory The post processor factory to apply.
         * @param offsetMappings       A map containing dynamically computed constant pool values that are mapped by their triggering annotation type.
         * @param delegator            The delegator to use.
         */
        protected WithCustomMapping(PostProcessor.Factory postProcessorFactory,
                                    Map<Class<? extends Annotation>, OffsetMapping.Factory<?>> offsetMappings,
                                    Delegator delegator) {
            this.postProcessorFactory = postProcessorFactory;
            this.offsetMappings = offsetMappings;
            this.delegator = delegator;
        }

        /**
         * Binds the supplied annotation to a type constant of the supplied value. Constants can be strings, method handles, method types
         * and any primitive or the value {@code null}.
         *
         * @param type  The type of the annotation being bound.
         * @param value The value to bind to the annotation.
         * @param <T>   The annotation type.
         * @return A new builder for an advice that considers the supplied annotation type during binding.
         */
        public <T extends Annotation> WithCustomMapping bind(Class<T> type, Object value) {
            return bind(OffsetMapping.ForStackManipulation.Factory.of(type, value));
        }


        /**
         * Binds an annotation to a dynamically computed value. Whenever the {@link Advice} component discovers the given annotation on
         * a parameter of an advice method, the dynamic value is asked to provide a value that is then assigned to the parameter in question.
         *
         * @param offsetMapping The dynamic value that is computed for binding the parameter to a value.
         * @return A new builder for an advice that considers the supplied annotation type during binding.
         */
        public WithCustomMapping bind(OffsetMapping.Factory<?> offsetMapping) {
            Map<Class<? extends Annotation>, OffsetMapping.Factory<?>> offsetMappings = new HashMap<>(this.offsetMappings);
            if (!offsetMapping.getAnnotationType().isAnnotation()) {
                throw new IllegalArgumentException("Not an annotation type: " + offsetMapping.getAnnotationType());
            } else if (offsetMappings.put(offsetMapping.getAnnotationType(), offsetMapping) != null) {
                throw new IllegalArgumentException("Annotation type already mapped: " + offsetMapping.getAnnotationType());
            }
            return new WithCustomMapping(postProcessorFactory, offsetMappings, delegator);
        }


        /**
         * Implements advice where every matched method is advised by the given type's advisory methods.
         *
         * @param advice           A description of the type declaring the advice.
         * @param classFileLocator The class file locator for locating the advisory class's class file.
         * @return A method visitor wrapper representing the supplied advice.
         */
        public AgentAdvice to(TypeDescription advice, ClassFileLocator classFileLocator) {
            return AgentAdvice.tto(advice, postProcessorFactory, classFileLocator,
                  new ArrayList<>(offsetMappings.values()), delegator);
        }
    }

    /**
     * Wraps the method visitor to implement this advice.
     *
     * @param instrumentedType      The instrumented type.
     * @param instrumentedMethod    The instrumented method.
     * @param methodVisitor         The method visitor to write to.
     * @param implementationContext The implementation context to use.
     * @param writerFlags           The ASM writer flags to use.
     * @param readerFlags           The, plies this advice.
     */
    protected MethodVisitor doWrap(TypeDescription instrumentedType,
                                   MethodDescription instrumentedMethod,
                                   MethodVisitor methodVisitor,
                                   Implementation.Context implementationContext,
                                   int writerFlags,
                                   int readerFlags) {
        Dispatcher.Resolved.ForMethodExit exit;
        if (instrumentedMethod.isConstructor()) {
            exit = methodExitNonThrowable;
        } else {
            exit = methodExit;
        }

        if (AdviceRegistry.check(instrumentedType, instrumentedMethod, methodEnter, exit) == 0) {
            return methodVisitor;
        }

        methodVisitor = new FramePaddingMethodVisitor(methodEnter.isPrependLineNumber()
            ? new LineNumberPrependingMethodVisitor(methodVisitor)
            : methodVisitor);

        if (instrumentedMethod.isConstructor()) {
            return new WithExitAdvice.WithoutExceptionHandling(methodVisitor,
                implementationContext,
                assigner,
                exceptionHandler.resolve(instrumentedMethod, instrumentedType),
                instrumentedType,
                instrumentedMethod,
                methodEnter,
                methodExitNonThrowable,
                writerFlags,
                readerFlags);
        } else {
            return new WithExitAdvice.WithExceptionHandling(methodVisitor,
                implementationContext,
                assigner,
                exceptionHandler.resolve(instrumentedMethod, instrumentedType),
                instrumentedType,
                instrumentedMethod,
                methodEnter,
                methodExit,
                writerFlags,
                readerFlags,
                methodExit.getThrowable());
        }
    }

    /**
     * A method visitor that weaves the advice methods' byte codes.
     */
    protected abstract static class AdviceVisitor
        extends ExceptionTableSensitiveMethodVisitor implements Dispatcher.RelocationHandler.Relocation {

        /**
         * The expected index for the {@code this} variable.
         */
        private static final int THIS_VARIABLE_INDEX = 0;

        /**
         * The expected name for the {@code this} variable.
         */
        private static final String THIS_VARIABLE_NAME = "this";

        /**
         * A description of the instrumented method.
         */
        protected final MethodDescription instrumentedMethod;

        /**
         * A label that indicates the start of the preparation of a user method execution.
         */
        private final Label preparationStart;

        /**
         * The dispatcher to be used for method enter.
         */
        private final Dispatcher.Bound methodEnter;

        /**
         * The dispatcher to be used for method exit.
         */
        protected final Dispatcher.Bound methodExit;

        /**
         * The handler for accessing arguments of the method's local variable array.
         */
        protected final ArgumentHandler.ForInstrumentedMethod argumentHandler;

        /**
         * A handler for computing the method size requirements.
         */
        protected final MethodSizeHandler.ForInstrumentedMethod methodSizeHandler;

        /**
         * A handler for translating and injecting stack map frames.
         */
        protected final StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler;

        /**
         * Creates a new advice visitor.
         *
         * @param methodVisitor         The actual method visitor that is underlying this method visitor to which all instructions are written.
         * @param implementationContext The implementation context to use.
         * @param assigner              The assigner to use.
         * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
         * @param instrumentedType      A description of the instrumented type.
         * @param instrumentedMethod    The instrumented method.
         * @param methodEnter           The method enter advice.
         * @param methodExit            The method exit advice.
         * @param postMethodTypes       A list of virtual method arguments that are available after the instrumented method has completed.
         * @param writerFlags           The ASM writer flags that were set.
         * @param readerFlags           The ASM reader flags that were set.
         */
        protected AdviceVisitor(MethodVisitor methodVisitor,
                                Context implementationContext,
                                Assigner assigner,
                                StackManipulation exceptionHandler,
                                TypeDescription instrumentedType,
                                MethodDescription instrumentedMethod,
                                Dispatcher.Resolved.ForMethodEnter methodEnter,
                                Dispatcher.Resolved.ForMethodExit methodExit,
                                List<? extends TypeDescription> postMethodTypes,
                                int writerFlags,
                                int readerFlags) {
            super(OpenedClassReader.ASM_API, methodVisitor);
            this.instrumentedMethod = instrumentedMethod;
            preparationStart = new Label();
            SortedMap<String, TypeDefinition> namedTypes = new TreeMap<>();
            namedTypes.putAll(methodEnter.getNamedTypes());
            namedTypes.putAll(methodExit.getNamedTypes());
            argumentHandler = methodExit.getArgumentHandlerFactory().resolve(instrumentedMethod,
                methodEnter.getAdviceType(),
                methodExit.getAdviceType(),
                namedTypes);
            List<TypeDescription> initialTypes = CompoundList.of(methodExit.getAdviceType().represents(void.class)
                ? Collections.emptyList()
                : Collections.singletonList(methodExit.getAdviceType().asErasure()), argumentHandler.getNamedTypes());
            List<TypeDescription> latentTypes = methodEnter.getActualAdviceType().represents(void.class)
                ? Collections.emptyList()
                : Collections.singletonList(methodEnter.getActualAdviceType().asErasure());
            List<TypeDescription> preMethodTypes = methodEnter.getAdviceType().represents(void.class)
                ? Collections.emptyList()
                : Collections.singletonList(methodEnter.getAdviceType().asErasure());
            methodSizeHandler = MethodSizeHandler.Default.of(instrumentedMethod,
                initialTypes,
                preMethodTypes,
                postMethodTypes,
                argumentHandler.isCopyingArguments(),
                writerFlags);
            stackMapFrameHandler = StackMapFrameHandler.Default.of(instrumentedType,
                instrumentedMethod,
                initialTypes,
                latentTypes,
                preMethodTypes,
                postMethodTypes,
                methodExit.isAlive(),
                argumentHandler.isCopyingArguments(),
                implementationContext.getClassFileVersion(),
                writerFlags,
                readerFlags);
            this.methodEnter = methodEnter.bind(instrumentedType,
                instrumentedMethod,
                methodVisitor,
                implementationContext,
                assigner,
                argumentHandler,
                methodSizeHandler,
                stackMapFrameHandler,
                exceptionHandler,
                this);
            this.methodExit = methodExit.bind(instrumentedType,
                instrumentedMethod,
                methodVisitor,
                implementationContext,
                assigner,
                argumentHandler,
                methodSizeHandler,
                stackMapFrameHandler,
                exceptionHandler,
                new ForLabel(preparationStart));
        }

        @Override
        protected void onAfterExceptionTable() {
            methodEnter.prepare();
            onUserPrepare();
            methodExit.prepare();
            methodEnter.initialize();
            methodExit.initialize();
            stackMapFrameHandler.injectInitializationFrame(mv);
            methodEnter.apply();
            mv.visitLabel(preparationStart);
            methodSizeHandler.requireStackSize(argumentHandler.prepare(mv));
            stackMapFrameHandler.injectStartFrame(mv);
            onUserStart();
        }

        /**
         * Invoked when the user method's exception handler (if any) is supposed to be prepared.
         */
        protected abstract void onUserPrepare();

        /**
         * Writes the advice for entering the instrumented method.
         */
        protected abstract void onUserStart();

        @Override
        protected void onVisitVarInsn(int opcode, int offset) {
            mv.visitVarInsn(opcode, argumentHandler.argument(offset));
        }

        @Override
        protected void onVisitIincInsn(int offset, int increment) {
            mv.visitIincInsn(argumentHandler.argument(offset), increment);
        }

        @Override
        public void onVisitFrame(int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
            stackMapFrameHandler.translateFrame(mv, type, localVariableLength, localVariable, stackSize, stack);
        }

        @Override
        public void visitMaxs(int stackSize, int localVariableLength) {
            onUserEnd();
            mv.visitMaxs(methodSizeHandler.compoundStackSize(stackSize), methodSizeHandler.compoundLocalVariableLength(localVariableLength));
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            // The 'this' variable is exempt from remapping as it is assumed immutable and remapping it confuses debuggers to not display the variable.
            mv.visitLocalVariable(name, descriptor, signature, start, end, index == THIS_VARIABLE_INDEX && THIS_VARIABLE_NAME.equals(name)
                ? index
                : argumentHandler.variable(index));
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeReference,
                                                              TypePath typePath,
                                                              Label[] start,
                                                              Label[] end,
                                                              int[] index,
                                                              String descriptor,
                                                              boolean visible) {
            int[] translated = new int[index.length];
            for (int anIndex = 0; anIndex < index.length; anIndex++) {
                translated[anIndex] = argumentHandler.variable(index[anIndex]);
            }
            return mv.visitLocalVariableAnnotation(typeReference, typePath, start, end, translated, descriptor, visible);
        }

        /**
         * Writes the advice for completing the instrumented method.
         */
        protected abstract void onUserEnd();

    }

    /**
     * An advice visitor that does not apply exit advice.
     */
    protected static class WithoutExitAdvice extends AdviceVisitor {

        /**
         * Creates an advice visitor that does not apply exit advice.
         *
         * @param methodVisitor         The method visitor for the instrumented method.
         * @param implementationContext The implementation context to use.
         * @param assigner              The assigner to use.
         * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
         * @param instrumentedType      A description of the instrumented type.
         * @param instrumentedMethod    A description of the instrumented method.
         * @param methodEnter           The dispatcher to be used for method enter.
         * @param writerFlags           The ASM writer flags that were set.
         * @param readerFlags           The ASM reader flags that were set.
         */
        protected WithoutExitAdvice(MethodVisitor methodVisitor,
                                    Implementation.Context implementationContext,
                                    Assigner assigner,
                                    StackManipulation exceptionHandler,
                                    TypeDescription instrumentedType,
                                    MethodDescription instrumentedMethod,
                                    Dispatcher.Resolved.ForMethodEnter methodEnter,
                                    int writerFlags,
                                    int readerFlags) {
            super(methodVisitor,
                implementationContext,
                assigner,
                exceptionHandler,
                instrumentedType,
                instrumentedMethod,
                methodEnter,
                Dispatcher.Inactive.INSTANCE,
                Collections.emptyList(),
                writerFlags,
                readerFlags);
        }

        /**
         * {@inheritDoc}
         */
        public void apply(MethodVisitor methodVisitor) {
            if (instrumentedMethod.getReturnType().represents(boolean.class)
                || instrumentedMethod.getReturnType().represents(byte.class)
                || instrumentedMethod.getReturnType().represents(short.class)
                || instrumentedMethod.getReturnType().represents(char.class)
                || instrumentedMethod.getReturnType().represents(int.class)) {
                methodVisitor.visitInsn(Opcodes.ICONST_0);
                methodVisitor.visitInsn(Opcodes.IRETURN);
            } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                methodVisitor.visitInsn(Opcodes.LCONST_0);
                methodVisitor.visitInsn(Opcodes.LRETURN);
            } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                methodVisitor.visitInsn(Opcodes.FCONST_0);
                methodVisitor.visitInsn(Opcodes.FRETURN);
            } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                methodVisitor.visitInsn(Opcodes.DCONST_0);
                methodVisitor.visitInsn(Opcodes.DRETURN);
            } else if (instrumentedMethod.getReturnType().represents(void.class)) {
                methodVisitor.visitInsn(Opcodes.RETURN);
            } else {
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                methodVisitor.visitInsn(Opcodes.ARETURN);
            }
        }

        @Override
        protected void onUserPrepare() {
            /* do nothing */
        }

        @Override
        protected void onUserStart() {
            /* do nothing */
        }

        @Override
        protected void onUserEnd() {
            /* do nothing */
        }
    }

     /**
     * An advice visitor that applies exit advice.
     */
    protected abstract static class WithExitAdvice extends AdviceVisitor {
        /**
         * Indicates the handler for the value returned by the advice method.
         */
        protected final Label returnHandler;

        /**
         * Creates an advice visitor that applies exit advice.
         *
         * @param methodVisitor         The method visitor for the instrumented method.
         * @param implementationContext The implementation context to use.
         * @param assigner              The assigner to use.
         * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
         * @param instrumentedType      A description of the instrumented type.
         * @param instrumentedMethod    A description of the instrumented method.
         * @param methodEnter           The dispatcher to be used for method enter.
         * @param methodExit            The dispatcher to be used for method exit.
         * @param postMethodTypes       A list of virtual method arguments that are available after the instrumented method has completed.
         * @param writerFlags           The ASM writer flags that were set.
         * @param readerFlags           The ASM reader flags that were set.
         */
        protected WithExitAdvice(MethodVisitor methodVisitor,
                                 Implementation.Context implementationContext,
                                 Assigner assigner,
                                 StackManipulation exceptionHandler,
                                 TypeDescription instrumentedType,
                                 MethodDescription instrumentedMethod,
                                 Dispatcher.Resolved.ForMethodEnter methodEnter,
                                 Dispatcher.Resolved.ForMethodExit methodExit,
                                 List<? extends TypeDescription> postMethodTypes,
                                 int writerFlags,
                                 int readerFlags) {
            super(new StackAwareMethodVisitor(methodVisitor, instrumentedMethod),
                implementationContext,
                assigner,
                exceptionHandler,
                instrumentedType,
                instrumentedMethod,
                methodEnter,
                methodExit,
                postMethodTypes,
                writerFlags,
                readerFlags);
            returnHandler = new Label();
        }

        /**
         * {@inheritDoc}
         */
        public void apply(MethodVisitor methodVisitor) {
            if (instrumentedMethod.getReturnType().represents(boolean.class)
                || instrumentedMethod.getReturnType().represents(byte.class)
                || instrumentedMethod.getReturnType().represents(short.class)
                || instrumentedMethod.getReturnType().represents(char.class)
                || instrumentedMethod.getReturnType().represents(int.class)) {
                methodVisitor.visitInsn(Opcodes.ICONST_0);
            } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                methodVisitor.visitInsn(Opcodes.LCONST_0);
            } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                methodVisitor.visitInsn(Opcodes.FCONST_0);
            } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                methodVisitor.visitInsn(Opcodes.DCONST_0);
            } else if (!instrumentedMethod.getReturnType().represents(void.class)) {
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
            }
            methodVisitor.visitJumpInsn(Opcodes.GOTO, returnHandler);
        }

        @Override
        protected void onVisitInsn(int opcode) {
            switch (opcode) {
                case Opcodes.RETURN:
                    ((StackAwareMethodVisitor) mv).drainStack();
                    break;
                case Opcodes.IRETURN:
                    methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.ISTORE, Opcodes.ILOAD, StackSize.SINGLE));
                    break;
                case Opcodes.FRETURN:
                    methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.FSTORE, Opcodes.FLOAD, StackSize.SINGLE));
                    break;
                case Opcodes.DRETURN:
                    methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.DSTORE, Opcodes.DLOAD, StackSize.DOUBLE));
                    break;
                case Opcodes.LRETURN:
                    methodSizeHandler.requireLocalVariableLength((((StackAwareMethodVisitor) mv).drainStack(Opcodes.LSTORE, Opcodes.LLOAD, StackSize.DOUBLE)));
                    break;
                case Opcodes.ARETURN:
                    methodSizeHandler.requireLocalVariableLength((((StackAwareMethodVisitor) mv).drainStack(Opcodes.ASTORE, Opcodes.ALOAD, StackSize.SINGLE)));
                    break;
                default:
                    mv.visitInsn(opcode);
                    return;
            }
            mv.visitJumpInsn(Opcodes.GOTO, returnHandler);
        }

        @Override
        protected void onUserEnd() {
            mv.visitLabel(returnHandler);
            onUserReturn();
            stackMapFrameHandler.injectCompletionFrame(mv);
            methodExit.apply();
            onExitAdviceReturn();
            if (instrumentedMethod.getReturnType().represents(boolean.class)
                || instrumentedMethod.getReturnType().represents(byte.class)
                || instrumentedMethod.getReturnType().represents(short.class)
                || instrumentedMethod.getReturnType().represents(char.class)
                || instrumentedMethod.getReturnType().represents(int.class)) {
                mv.visitVarInsn(Opcodes.ILOAD, argumentHandler.returned());
                mv.visitInsn(Opcodes.IRETURN);
            } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                mv.visitVarInsn(Opcodes.LLOAD, argumentHandler.returned());
                mv.visitInsn(Opcodes.LRETURN);
            } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                mv.visitVarInsn(Opcodes.FLOAD, argumentHandler.returned());
                mv.visitInsn(Opcodes.FRETURN);
            } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                mv.visitVarInsn(Opcodes.DLOAD, argumentHandler.returned());
                mv.visitInsn(Opcodes.DRETURN);
            } else if (!instrumentedMethod.getReturnType().represents(void.class)) {
                mv.visitVarInsn(Opcodes.ALOAD, argumentHandler.returned());
                mv.visitInsn(Opcodes.ARETURN);
            } else {
                mv.visitInsn(Opcodes.RETURN);
            }
            methodSizeHandler.requireStackSize(instrumentedMethod.getReturnType().getStackSize().getSize());
        }

        /**
         * Invoked after the user method has returned.
         */
        protected abstract void onUserReturn();

        /**
         * Invoked after the exit advice method has returned.
         */
        protected abstract void onExitAdviceReturn();

        /**
         * An advice visitor that does not capture exceptions.
         */
        protected static class WithoutExceptionHandling extends WithExitAdvice {

            /**
             * Creates a new advice visitor that does not capture exceptions.
             *
             * @param methodVisitor         The method visitor for the instrumented method.
             * @param implementationContext The implementation context to use.
             * @param assigner              The assigner to use.
             * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
             * @param instrumentedType      A description of the instrumented type.
             * @param instrumentedMethod    A description of the instrumented method.
             * @param methodEnter           The dispatcher to be used for method enter.
             * @param methodExit            The dispatcher to be used for method exit.
             * @param writerFlags           The ASM writer flags that were set.
             * @param readerFlags           The ASM reader flags that were set.
             */
            protected WithoutExceptionHandling(MethodVisitor methodVisitor,
                                               Implementation.Context implementationContext,
                                               Assigner assigner,
                                               StackManipulation exceptionHandler,
                                               TypeDescription instrumentedType,
                                               MethodDescription instrumentedMethod,
                                               Dispatcher.Resolved.ForMethodEnter methodEnter,
                                               Dispatcher.Resolved.ForMethodExit methodExit,
                                               int writerFlags,
                                               int readerFlags) {
                super(methodVisitor,
                    implementationContext,
                    assigner,
                    exceptionHandler,
                    instrumentedType,
                    instrumentedMethod,
                    methodEnter,
                    methodExit,
                    instrumentedMethod.getReturnType().represents(void.class)
                        ? Collections.emptyList()
                        : Collections.singletonList(instrumentedMethod.getReturnType().asErasure()),
                    writerFlags,
                    readerFlags);
            }

            @Override
            protected void onUserPrepare() {
                /* do nothing */
            }

            @Override
            protected void onUserStart() {
                /* do nothing */
            }

            @Override
            protected void onUserReturn() {
                if (instrumentedMethod.getReturnType().represents(boolean.class)
                    || instrumentedMethod.getReturnType().represents(byte.class)
                    || instrumentedMethod.getReturnType().represents(short.class)
                    || instrumentedMethod.getReturnType().represents(char.class)
                    || instrumentedMethod.getReturnType().represents(int.class)) {
                    stackMapFrameHandler.injectReturnFrame(mv);
                    mv.visitVarInsn(Opcodes.ISTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                    stackMapFrameHandler.injectReturnFrame(mv);
                    mv.visitVarInsn(Opcodes.LSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                    stackMapFrameHandler.injectReturnFrame(mv);
                    mv.visitVarInsn(Opcodes.FSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                    stackMapFrameHandler.injectReturnFrame(mv);
                    mv.visitVarInsn(Opcodes.DSTORE, argumentHandler.returned());
                } else if (!instrumentedMethod.getReturnType().represents(void.class)) {
                    stackMapFrameHandler.injectReturnFrame(mv);
                    mv.visitVarInsn(Opcodes.ASTORE, argumentHandler.returned());
                }
            }

            @Override
            protected void onExitAdviceReturn() {
                /* do nothing */
            }
        }

        /**
         * An advice visitor that captures exceptions by weaving try-catch blocks around user code.
         */
        protected static class WithExceptionHandling extends WithExitAdvice {
            /**
             * The type of the handled throwable type for which this advice is invoked.
             */
            private final TypeDescription throwable;

            /**
             * Indicates the exception handler.
             */
            private final Label exceptionHandler;

            /**
             * Indicates the start of the user method.
             */
            protected final Label userStart;

            /**
             * Creates a new advice visitor that captures exception by weaving try-catch blocks around user code.
             *
             * @param methodVisitor         The method visitor for the instrumented method.
             * @param instrumentedType      A description of the instrumented type.
             * @param implementationContext The implementation context to use.
             * @param assigner              The assigner to use.
             * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
             * @param instrumentedMethod    A description of the instrumented method.
             * @param methodEnter           The dispatcher to be used for method enter.
             * @param methodExit            The dispatcher to be used for method exit.
             * @param writerFlags           The ASM writer flags that were set.
             * @param readerFlags           The ASM reader flags that were set.
             * @param throwable             The type of the handled throwable type for which this advice is invoked.
             */
            protected WithExceptionHandling(MethodVisitor methodVisitor,
                                            Implementation.Context implementationContext,
                                            Assigner assigner,
                                            StackManipulation exceptionHandler,
                                            TypeDescription instrumentedType,
                                            MethodDescription instrumentedMethod,
                                            Dispatcher.Resolved.ForMethodEnter methodEnter,
                                            Dispatcher.Resolved.ForMethodExit methodExit,
                                            int writerFlags,
                                            int readerFlags,
                                            TypeDescription throwable) {
                super(methodVisitor,
                    implementationContext,
                    assigner,
                    exceptionHandler,
                    instrumentedType,
                    instrumentedMethod,
                    methodEnter,
                    methodExit,
                    instrumentedMethod.getReturnType().represents(void.class)
                        ? Collections.singletonList(TypeDescription.THROWABLE)
                        : Arrays.asList(instrumentedMethod.getReturnType().asErasure(), TypeDescription.THROWABLE),
                    writerFlags,
                    readerFlags);
                this.throwable = throwable;
                this.exceptionHandler = new Label();
                userStart = new Label();
            }

            @Override
            protected void onUserPrepare() {
                mv.visitTryCatchBlock(userStart, returnHandler, exceptionHandler, throwable.getInternalName());
            }

            @Override
            protected void onUserStart() {
                mv.visitLabel(userStart);
            }

            @Override
            protected void onUserReturn() {
                stackMapFrameHandler.injectReturnFrame(mv);
                if (instrumentedMethod.getReturnType().represents(boolean.class)
                    || instrumentedMethod.getReturnType().represents(byte.class)
                    || instrumentedMethod.getReturnType().represents(short.class)
                    || instrumentedMethod.getReturnType().represents(char.class)
                    || instrumentedMethod.getReturnType().represents(int.class)) {
                    mv.visitVarInsn(Opcodes.ISTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                    mv.visitVarInsn(Opcodes.LSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                    mv.visitVarInsn(Opcodes.FSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                    mv.visitVarInsn(Opcodes.DSTORE, argumentHandler.returned());
                } else if (!instrumentedMethod.getReturnType().represents(void.class)) {
                    mv.visitVarInsn(Opcodes.ASTORE, argumentHandler.returned());
                }
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitVarInsn(Opcodes.ASTORE, argumentHandler.thrown());
                Label endOfHandler = new Label();
                mv.visitJumpInsn(Opcodes.GOTO, endOfHandler);
                mv.visitLabel(exceptionHandler);
                stackMapFrameHandler.injectExceptionFrame(mv);
                mv.visitVarInsn(Opcodes.ASTORE, argumentHandler.thrown());
                if (instrumentedMethod.getReturnType().represents(boolean.class)
                    || instrumentedMethod.getReturnType().represents(byte.class)
                    || instrumentedMethod.getReturnType().represents(short.class)
                    || instrumentedMethod.getReturnType().represents(char.class)
                    || instrumentedMethod.getReturnType().represents(int.class)) {
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitVarInsn(Opcodes.ISTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(long.class)) {
                    mv.visitInsn(Opcodes.LCONST_0);
                    mv.visitVarInsn(Opcodes.LSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(float.class)) {
                    mv.visitInsn(Opcodes.FCONST_0);
                    mv.visitVarInsn(Opcodes.FSTORE, argumentHandler.returned());
                } else if (instrumentedMethod.getReturnType().represents(double.class)) {
                    mv.visitInsn(Opcodes.DCONST_0);
                    mv.visitVarInsn(Opcodes.DSTORE, argumentHandler.returned());
                } else if (!instrumentedMethod.getReturnType().represents(void.class)) {
                    mv.visitInsn(Opcodes.ACONST_NULL);
                    mv.visitVarInsn(Opcodes.ASTORE, argumentHandler.returned());
                }
                mv.visitLabel(endOfHandler);
                methodSizeHandler.requireStackSize(StackSize.SINGLE.getSize());
            }

            @Override
            protected void onExitAdviceReturn() {
                mv.visitVarInsn(Opcodes.ALOAD, argumentHandler.thrown());
                Label endOfHandler = new Label();
                mv.visitJumpInsn(Opcodes.IFNULL, endOfHandler);
                mv.visitVarInsn(Opcodes.ALOAD, argumentHandler.thrown());
                mv.visitInsn(Opcodes.ATHROW);
                mv.visitLabel(endOfHandler);
                stackMapFrameHandler.injectPostCompletionFrame(mv);
            }
        }
    }


    /**
     * A dispatcher for implementing advice.
     */
    public interface Dispatcher {

        /**
         * Indicates that a method does not represent advice and does not need to be visited.
         */
        MethodVisitor IGNORE_METHOD = null;

        /**
         * Expresses that an annotation should not be visited.
         */
        AnnotationVisitor IGNORE_ANNOTATION = null;

        /**
         * Returns {@code true} if this dispatcher is alive.
         *
         * @return {@code true} if this dispatcher is alive.
         */
        boolean isAlive();

        /**
         * The type that is produced as a result of executing this advice method.
         *
         * @return A description of the type that is produced by this advice method.
         */
        TypeDefinition getAdviceType();

        /**
         * A dispatcher that is not yet resolved.
         */
        interface Unresolved extends Dispatcher {

            /**
             * Indicates that this dispatcher requires access to the class file declaring the advice method.
             *
             * @return {@code true} if this dispatcher requires access to the advice method's class file.
             */
            boolean isBinary();

            /**
             * Returns the named types declared by this enter advice.
             *
             * @return The named types declared by this enter advice.
             */
            Map<String, TypeDefinition> getNamedTypes();

            /**
             * Resolves this dispatcher as a dispatcher for entering a method.
             *
             * @param userFactories        A list of custom factories for binding parameters of an advice method.
             * @param classReader          A class reader to query for a class file which might be {@code null} if this dispatcher is not binary.
             * @param methodExit           The unresolved dispatcher for the method exit advice.
             * @param postProcessorFactory The post processor factory to use.
             * @return This dispatcher as a dispatcher for entering a method.
             */
            Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                  ClassReader classReader,
                                                  Unresolved methodExit,
                                                  PostProcessor.Factory postProcessorFactory);

            /**
             * Resolves this dispatcher as a dispatcher for exiting a method.
             *
             * @param userFactories        A list of custom factories for binding parameters of an advice method.
             * @param classReader          A class reader to query for a class file which might be {@code null} if this dispatcher is not binary.
             * @param methodEnter          The unresolved dispatcher for the method enter advice.
             * @param postProcessorFactory The post processor factory to use.
             * @return This dispatcher as a dispatcher for exiting a method.
             */
            Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                ClassReader classReader,
                                                Unresolved methodEnter,
                                                PostProcessor.Factory postProcessorFactory);
        }



        /**
         * A suppression handler for optionally suppressing exceptions.
         */
        interface SuppressionHandler {

            /**
             * Binds the suppression handler for instrumenting a specific method.
             *
             * @param exceptionHandler The stack manipulation to apply within a suppression handler.
             * @return A bound version of the suppression handler.
             */
            Bound bind(StackManipulation exceptionHandler);

            /**
             * A bound version of a suppression handler that must not be reused.
             */
            interface Bound {

                /**
                 * Invoked to prepare the suppression handler, i.e. to write an exception handler entry if appropriate.
                 *
                 * @param methodVisitor The method visitor to apply the preparation to.
                 */
                void onPrepare(MethodVisitor methodVisitor);

                /**
                 * Invoked at the start of a method.
                 *
                 * @param methodVisitor The method visitor of the instrumented method.
                 */
                void onStart(MethodVisitor methodVisitor);

                /**
                 * Invoked at the end of a method.
                 *
                 * @param methodVisitor         The method visitor of the instrumented method.
                 * @param implementationContext The implementation context to use.
                 * @param methodSizeHandler     The advice method's method size handler.
                 * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                 * @param returnType            The return type of the advice method.
                 */
                void onEnd(MethodVisitor methodVisitor,
                           Implementation.Context implementationContext,
                           MethodSizeHandler.ForAdvice methodSizeHandler,
                           StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                           TypeDefinition returnType);

                /**
                 * Invoked at the end of a method if the exception handler should be wrapped in a skipping block.
                 *
                 * @param methodVisitor         The method visitor of the instrumented method.
                 * @param implementationContext The implementation context to use.
                 * @param methodSizeHandler     The advice method's method size handler.
                 * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                 * @param returnType            The return type of the advice method.
                 */
                void onEndWithSkip(MethodVisitor methodVisitor,
                                   Implementation.Context implementationContext,
                                   MethodSizeHandler.ForAdvice methodSizeHandler,
                                   StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                   TypeDefinition returnType);
            }

            /**
             * A non-operational suppression handler that does not suppress any method.
             */
            enum NoOp implements SuppressionHandler, Bound {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * {@inheritDoc}
                 */
                public Bound bind(StackManipulation exceptionHandler) {
                    return this;
                }

                /**
                 * {@inheritDoc}
                 */
                public void onPrepare(MethodVisitor methodVisitor) {
                    /* do nothing */
                }

                /**
                 * {@inheritDoc}
                 */
                public void onStart(MethodVisitor methodVisitor) {
                    /* do nothing */
                }

                /**
                 * {@inheritDoc}
                 */
                public void onEnd(MethodVisitor methodVisitor,
                                  Implementation.Context implementationContext,
                                  MethodSizeHandler.ForAdvice methodSizeHandler,
                                  StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                  TypeDefinition returnType) {
                    /* do nothing */
                }

                /**
                 * {@inheritDoc}
                 */
                public void onEndWithSkip(MethodVisitor methodVisitor,
                                          Implementation.Context implementationContext,
                                          MethodSizeHandler.ForAdvice methodSizeHandler,
                                          StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                          TypeDefinition returnType) {
                    /* do nothing */
                }
            }

            /**
             * A suppression handler that suppresses a given throwable type.
             */
            @HashCodeAndEqualsPlugin.Enhance
            class Suppressing implements SuppressionHandler {

                /**
                 * The suppressed throwable type.
                 */
                private final TypeDescription suppressedType;

                /**
                 * Creates a new suppressing suppression handler.
                 *
                 * @param suppressedType The suppressed throwable type.
                 */
                protected Suppressing(TypeDescription suppressedType) {
                    this.suppressedType = suppressedType;
                }

                /**
                 * Resolves an appropriate suppression handler.
                 *
                 * @param suppressedType The suppressed type or {@link NoExceptionHandler} if no type should be suppressed.
                 * @return An appropriate suppression handler.
                 */
                protected static SuppressionHandler of(TypeDescription suppressedType) {
                    return isNoExceptionHandler(suppressedType)
                        ? NoOp.INSTANCE
                        : new Suppressing(suppressedType);
                }

                /**
                 * {@inheritDoc}
                 */
                public SuppressionHandler.Bound bind(StackManipulation exceptionHandler) {
                    return new Bound(suppressedType, exceptionHandler);
                }

                /**
                 * An active, bound suppression handler.
                 */
                protected static class Bound implements SuppressionHandler.Bound {

                    /**
                     * The suppressed throwable type.
                     */
                    private final TypeDescription suppressedType;

                    /**
                     * The stack manipulation to apply within a suppression handler.
                     */
                    private final StackManipulation exceptionHandler;

                    /**
                     * A label indicating the start of the method.
                     */
                    private final Label startOfMethod;

                    /**
                     * A label indicating the end of the method.
                     */
                    private final Label endOfMethod;

                    /**
                     * Creates a new active, bound suppression handler.
                     *
                     * @param suppressedType   The suppressed throwable type.
                     * @param exceptionHandler The stack manipulation to apply within a suppression handler.
                     */
                    protected Bound(TypeDescription suppressedType, StackManipulation exceptionHandler) {
                        this.suppressedType = suppressedType;
                        this.exceptionHandler = exceptionHandler;
                        startOfMethod = new Label();
                        endOfMethod = new Label();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void onPrepare(MethodVisitor methodVisitor) {
                        methodVisitor.visitTryCatchBlock(startOfMethod, endOfMethod, endOfMethod, suppressedType.getInternalName());
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void onStart(MethodVisitor methodVisitor) {
                        methodVisitor.visitLabel(startOfMethod);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void onEnd(MethodVisitor methodVisitor,
                                      Implementation.Context implementationContext,
                                      MethodSizeHandler.ForAdvice methodSizeHandler,
                                      StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                      TypeDefinition returnType) {
                        methodVisitor.visitLabel(endOfMethod);
                        stackMapFrameHandler.injectExceptionFrame(methodVisitor);
                        methodSizeHandler.requireStackSize(1 + exceptionHandler.apply(methodVisitor, implementationContext).getMaximalSize());
                        if (returnType.represents(boolean.class)
                            || returnType.represents(byte.class)
                            || returnType.represents(short.class)
                            || returnType.represents(char.class)
                            || returnType.represents(int.class)) {
                            methodVisitor.visitInsn(Opcodes.ICONST_0);
                        } else if (returnType.represents(long.class)) {
                            methodVisitor.visitInsn(Opcodes.LCONST_0);
                        } else if (returnType.represents(float.class)) {
                            methodVisitor.visitInsn(Opcodes.FCONST_0);
                        } else if (returnType.represents(double.class)) {
                            methodVisitor.visitInsn(Opcodes.DCONST_0);
                        } else if (!returnType.represents(void.class)) {
                            methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                        }
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void onEndWithSkip(MethodVisitor methodVisitor,
                                              Implementation.Context implementationContext,
                                              MethodSizeHandler.ForAdvice methodSizeHandler,
                                              StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                              TypeDefinition returnType) {
                        Label skipExceptionHandler = new Label();
                        methodVisitor.visitJumpInsn(Opcodes.GOTO, skipExceptionHandler);
                        onEnd(methodVisitor, implementationContext, methodSizeHandler, stackMapFrameHandler, returnType);
                        methodVisitor.visitLabel(skipExceptionHandler);
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                    }
                }
            }
        }

        /**
         * A relocation handler is responsible for chaining the usual control flow of an instrumented method.
         */
        interface RelocationHandler {

            /**
             * Binds this relocation handler to a relocation dispatcher.
             *
             * @param instrumentedMethod The instrumented method.
             * @param relocation         The relocation to apply.
             * @return A bound relocation handler.
             */
            Bound bind(MethodDescription instrumentedMethod, Relocation relocation);

            /**
             * A re-locator is responsible for triggering a relocation if a relocation handler triggers a relocating condition.
             */
            interface Relocation {

                /**
                 * Applies this relocator.
                 *
                 * @param methodVisitor The method visitor to use.
                 */
                void apply(MethodVisitor methodVisitor);

                /**
                 * A relocation that unconditionally jumps to a given label.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                class ForLabel implements Relocation {

                    /**
                     * The label to jump to.
                     */
                    private final Label label;

                    /**
                     * Creates a new relocation for an unconditional jump to a given label.
                     *
                     * @param label The label to jump to.
                     */
                    public ForLabel(Label label) {
                        this.label = label;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void apply(MethodVisitor methodVisitor) {
                        methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
                    }
                }
            }

            /**
             * A bound {@link RelocationHandler}.
             */
            interface Bound {

                /**
                 * Indicates that this relocation handler does not require a minimal stack size.
                 */
                int NO_REQUIRED_SIZE = 0;

                /**
                 * Applies this relocation handler.
                 *
                 * @param methodVisitor The method visitor to use.
                 * @param offset        The offset of the relevant value.
                 * @return The minimal required stack size to apply this relocation handler.
                 */
                int apply(MethodVisitor methodVisitor, int offset);
            }

            /**
             * A disabled relocation handler that does never trigger a relocation.
             */
            enum Disabled implements RelocationHandler, Bound {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * {@inheritDoc}
                 */
                public Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
                    return this;
                }

                /**
                 * {@inheritDoc}
                 */
                public int apply(MethodVisitor methodVisitor, int offset) {
                    return NO_REQUIRED_SIZE;
                }
            }

            /**
             * A relocation handler that triggers a relocation for a default or non-default value.
             */
            enum ForValue implements RelocationHandler {

                /**
                 * A relocation handler for an {@code int} type or any compatible type.
                 */
                INTEGER(Opcodes.ILOAD, Opcodes.IFNE, Opcodes.IFEQ, 0) {
                    @Override
                    protected void convertValue(MethodVisitor methodVisitor) {
                        /* do nothing */
                    }
                },

                /**
                 * A relocation handler for a {@code long} type.
                 */
                LONG(Opcodes.LLOAD, Opcodes.IFNE, Opcodes.IFEQ, 0) {
                    @Override
                    protected void convertValue(MethodVisitor methodVisitor) {
                        methodVisitor.visitInsn(Opcodes.L2I);
                    }
                },

                /**
                 * A relocation handler for a {@code float} type.
                 */
                FLOAT(Opcodes.FLOAD, Opcodes.IFNE, Opcodes.IFEQ, 2) {
                    @Override
                    protected void convertValue(MethodVisitor methodVisitor) {
                        methodVisitor.visitInsn(Opcodes.FCONST_0);
                        methodVisitor.visitInsn(Opcodes.FCMPL);
                    }
                },

                /**
                 * A relocation handler for a {@code double} type.
                 */
                DOUBLE(Opcodes.DLOAD, Opcodes.IFNE, Opcodes.IFEQ, 4) {
                    @Override
                    protected void convertValue(MethodVisitor methodVisitor) {
                        methodVisitor.visitInsn(Opcodes.DCONST_0);
                        methodVisitor.visitInsn(Opcodes.DCMPL);
                    }
                },

                /**
                 * A relocation handler for a reference type.
                 */
                REFERENCE(Opcodes.ALOAD, Opcodes.IFNONNULL, Opcodes.IFNULL, 0) {
                    @Override
                    protected void convertValue(MethodVisitor methodVisitor) {
                        /* do nothing */
                    }
                };

                /**
                 * An opcode for loading a value of the represented type from the local variable array.
                 */
                private final int load;

                /**
                 * The opcode to check for a non-default value.
                 */
                private final int defaultJump;

                /**
                 * The opcode to check for a default value.
                 */
                private final int nonDefaultJump;

                /**
                 * The minimal required stack size to apply this relocation handler.
                 */
                private final int requiredSize;

                /**
                 * Creates a new relocation handler for a type's default or non-default value.
                 *
                 * @param load           An opcode for loading a value of the represented type from the local variable array.
                 * @param defaultJump    The opcode to check for a non-default value.
                 * @param nonDefaultJump The opcode to check for a default value.
                 * @param requiredSize   The minimal required stack size to apply this relocation handler.
                 */
                ForValue(int load, int defaultJump, int nonDefaultJump, int requiredSize) {
                    this.load = load;
                    this.defaultJump = defaultJump;
                    this.nonDefaultJump = nonDefaultJump;
                    this.requiredSize = requiredSize;
                }

                /**
                 * Resolves a relocation handler for a given type.
                 *
                 * @param typeDefinition The type to be resolved for a relocation attempt.
                 * @param inverted       {@code true} if the relocation should be applied for any non-default value of a type.
                 * @return An appropriate relocation handler.
                 */
                protected static RelocationHandler of(TypeDefinition typeDefinition, boolean inverted) {
                    ForValue skipDispatcher;
                    if (typeDefinition.represents(long.class)) {
                        skipDispatcher = LONG;
                    } else if (typeDefinition.represents(float.class)) {
                        skipDispatcher = FLOAT;
                    } else if (typeDefinition.represents(double.class)) {
                        skipDispatcher = DOUBLE;
                    } else if (typeDefinition.represents(void.class)) {
                        throw new IllegalStateException("Cannot skip on default value for void return type");
                    } else if (typeDefinition.isPrimitive()) { // anyOf(byte, short, char, int)
                        skipDispatcher = INTEGER;
                    } else {
                        skipDispatcher = REFERENCE;
                    }
                    return inverted
                        ? skipDispatcher.new Inverted()
                        : skipDispatcher;
                }

                /**
                 * Applies a value conversion prior to a applying a conditional jump.
                 *
                 * @param methodVisitor The method visitor to use.
                 */
                protected abstract void convertValue(MethodVisitor methodVisitor);

                /**
                 * {@inheritDoc}
                 */
                public RelocationHandler.Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
                    return new Bound(instrumentedMethod, relocation, false);
                }

                /**
                 * An inverted version of the outer relocation handler.
                 */
                @HashCodeAndEqualsPlugin.Enhance(includeSyntheticFields = true)
                protected class Inverted implements RelocationHandler {

                    /**
                     * {@inheritDoc}
                     */
                    public Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
                        return new ForValue.Bound(instrumentedMethod, relocation, true);
                    }
                }

                /**
                 * A bound relocation handler for {@link ForValue}.
                 */
                @HashCodeAndEqualsPlugin.Enhance(includeSyntheticFields = true)
                protected class Bound implements RelocationHandler.Bound {

                    /**
                     * The instrumented method.
                     */
                    private final MethodDescription instrumentedMethod;

                    /**
                     * The relocation to apply.
                     */
                    private final Relocation relocation;

                    /**
                     * {@code true} if the relocation should be applied for any non-default value of a type.
                     */
                    private final boolean inverted;

                    /**
                     * Creates a new bound relocation handler.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param relocation         The relocation to apply.
                     * @param inverted           {@code true} if the relocation should be applied for any non-default value of a type.
                     */
                    protected Bound(MethodDescription instrumentedMethod, Relocation relocation, boolean inverted) {
                        this.instrumentedMethod = instrumentedMethod;
                        this.relocation = relocation;
                        this.inverted = inverted;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int apply(MethodVisitor methodVisitor, int offset) {
                        if (instrumentedMethod.isConstructor()) {
                            throw new IllegalStateException("Cannot skip code execution from constructor: " + instrumentedMethod);
                        }
                        methodVisitor.visitVarInsn(load, offset);
                        convertValue(methodVisitor);
                        Label noSkip = new Label();
                        methodVisitor.visitJumpInsn(inverted
                            ? nonDefaultJump
                            : defaultJump, noSkip);
                        relocation.apply(methodVisitor);
                        methodVisitor.visitLabel(noSkip);
                        return requiredSize;
                    }
                }
            }

            /**
             * A relocation handler that is triggered if the checked value is an instance of a given type.
             */
            @HashCodeAndEqualsPlugin.Enhance
            class ForType implements RelocationHandler {

                /**
                 * The type that triggers a relocation.
                 */
                private final TypeDescription typeDescription;

                /**
                 * Creates a new relocation handler that triggers a relocation if a value is an instance of a given type.
                 *
                 * @param typeDescription The type that triggers a relocation.
                 */
                protected ForType(TypeDescription typeDescription) {
                    this.typeDescription = typeDescription;
                }

                /**
                 * Resolves a relocation handler that is triggered if the checked instance is of a given type.
                 *
                 * @param typeDescription The type that triggers a relocation.
                 * @param checkedType     The type that is carrying the checked value.
                 * @return An appropriate relocation handler.
                 */
                protected static RelocationHandler of(TypeDescription typeDescription, TypeDefinition checkedType) {
                    if (typeDescription.represents(void.class)) {
                        return Disabled.INSTANCE;
                    } else if (typeDescription.represents(OnDefaultValue.class)) {
                        return ForValue.of(checkedType, false);
                    } else if (typeDescription.represents(OnNonDefaultValue.class)) {
                        return ForValue.of(checkedType, true);
                    } else if (typeDescription.isPrimitive() || checkedType.isPrimitive()) {
                        throw new IllegalStateException("Cannot skip method by instance type for primitive return type " + checkedType);
                    } else {
                        return new ForType(typeDescription);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public RelocationHandler.Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
                    return new Bound(instrumentedMethod, relocation);
                }

                /**
                 * A bound relocation handler for {@link ForType}.
                 */
                @HashCodeAndEqualsPlugin.Enhance(includeSyntheticFields = true)
                protected class Bound implements RelocationHandler.Bound {

                    /**
                     * The instrumented method.
                     */
                    private final MethodDescription instrumentedMethod;

                    /**
                     * The relocation to use.
                     */
                    private final Relocation relocation;

                    /**
                     * Creates a new bound relocation handler.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param relocation         The relocation to apply.
                     */
                    protected Bound(MethodDescription instrumentedMethod, Relocation relocation) {
                        this.instrumentedMethod = instrumentedMethod;
                        this.relocation = relocation;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int apply(MethodVisitor methodVisitor, int offset) {
                        if (instrumentedMethod.isConstructor()) {
                            throw new IllegalStateException("Cannot skip code execution from constructor: " + instrumentedMethod);
                        }
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, offset);
                        methodVisitor.visitTypeInsn(Opcodes.INSTANCEOF, typeDescription.getInternalName());
                        Label noSkip = new Label();
                        methodVisitor.visitJumpInsn(Opcodes.IFEQ, noSkip);
                        relocation.apply(methodVisitor);
                        methodVisitor.visitLabel(noSkip);
                        return NO_REQUIRED_SIZE;
                    }
                }
            }
        }

        /**
         * Represents a resolved dispatcher.
         */
        interface Resolved extends Dispatcher {

            /**
             * Returns the named types defined by this advice.
             *
             * @return The named types defined by this advice.
             */
            Map<String, TypeDefinition> getNamedTypes();

            /**
             * Binds this dispatcher for resolution to a specific method.
             *
             * @param instrumentedType      The instrumented type.
             * @param instrumentedMethod    The instrumented method.
             * @param methodVisitor         The method visitor for writing the instrumented method.
             * @param implementationContext The implementation context to use.
             * @param assigner              The assigner to use.
             * @param argumentHandler       A handler for accessing values on the local variable array.
             * @param methodSizeHandler     A handler for computing the method size requirements.
             * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
             * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
             * @param relocation            A relocation to use with a relocation handler.
             * @return A dispatcher that is bound to the instrumented method.
             */
            Bound bind(TypeDescription instrumentedType,
                       MethodDescription instrumentedMethod,
                       MethodVisitor methodVisitor,
                       Implementation.Context implementationContext,
                       Assigner assigner,
                       ArgumentHandler.ForInstrumentedMethod argumentHandler,
                       MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                       StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                       StackManipulation exceptionHandler,
                       RelocationHandler.Relocation relocation);


            Map<Integer, OffsetMapping> getOffsetMapping();

            /**
             * Represents a resolved dispatcher for entering a method.
             */
            interface ForMethodEnter extends Resolved {

                /**
                 * Returns {@code true} if the first discovered line number information should be prepended to the advice code.
                 *
                 * @return {@code true} if the first discovered line number information should be prepended to the advice code.
                 */
                boolean isPrependLineNumber();

                /**
                 * Returns the actual advice type, even if it is not required post advice processing.
                 *
                 * @return The actual advice type, even if it is not required post advice processing.
                 */
                TypeDefinition getActualAdviceType();
            }

            /**
             * Represents a resolved dispatcher for exiting a method.
             */
            interface ForMethodExit extends Resolved {

                /**
                 * Returns the type of throwable for which this exit advice is supposed to be invoked.
                 *
                 * @return The {@link Throwable} type for which to invoke this exit advice or a description of {@link NoExceptionHandler}
                 * if this exit advice does not expect to be invoked upon any throwable.
                 */
                TypeDescription getThrowable();

                /**
                 * Returns a factory for creating an {@link ArgumentHandler}.
                 *
                 * @return A factory for creating an {@link ArgumentHandler}.
                 */
                ArgumentHandler.Factory getArgumentHandlerFactory();
            }

            /**
             * An abstract base implementation of a {@link Resolved} dispatcher.
             */
            @HashCodeAndEqualsPlugin.Enhance
            abstract class AbstractBase implements Resolved {

                /**
                 * The represented advice method.
                 */
                protected final MethodDescription.InDefinedShape adviceMethod;

                /**
                 * The post processor to apply.
                 */
                protected final PostProcessor postProcessor;

                /**
                 * A mapping from offset to a mapping for this offset with retained iteration order of the method's parameters.
                 */
                protected final Map<Integer, OffsetMapping> offsetMappings;

                /**
                 * The suppression handler to use.
                 */
                protected final SuppressionHandler suppressionHandler;

                /**
                 * The relocation handler to use.
                 */
                protected final RelocationHandler relocationHandler;

                /**
                 * Creates a new resolved version of a dispatcher.
                 *
                 * @param adviceMethod    The represented advice method.
                 * @param postProcessor   The post processor to use.
                 * @param factories       A list of factories to resolve for the parameters of the advice method.
                 * @param throwableType   The type to handle by a suppression handler or {@link NoExceptionHandler} to not handle any exceptions.
                 * @param relocatableType The type to trigger a relocation of the method's control flow or {@code void} if no relocation should be executed.
                 * @param adviceType      The applied advice type.
                 */
                protected AbstractBase(MethodDescription.InDefinedShape adviceMethod,
                                       PostProcessor postProcessor,
                                       List<? extends OffsetMapping.Factory<?>> factories,
                                       TypeDescription throwableType,
                                       TypeDescription relocatableType,
                                       OffsetMapping.Factory.AdviceType adviceType) {
                    this.adviceMethod = adviceMethod;
                    this.postProcessor = postProcessor;
                    Map<TypeDescription, OffsetMapping.Factory<?>> offsetMappings = new HashMap<>();
                    for (OffsetMapping.Factory<?> factory : factories) {
                        offsetMappings.put(TypeDescription.ForLoadedType.of(factory.getAnnotationType()), factory);
                    }
                    this.offsetMappings = new LinkedHashMap<>();
                    for (ParameterDescription.InDefinedShape parameterDescription : adviceMethod.getParameters()) {
                        OffsetMapping offsetMapping = null;
                        for (AnnotationDescription annotationDescription : parameterDescription.getDeclaredAnnotations()) {
                            OffsetMapping.Factory<?> factory = offsetMappings.get(annotationDescription.getAnnotationType());
                            if (factory != null) {
                                @SuppressWarnings("unchecked, rawtypes")
                                OffsetMapping current = factory.make(parameterDescription,
                                    (AnnotationDescription.Loadable) annotationDescription.prepare(factory.getAnnotationType()),
                                    adviceType);
                                if (offsetMapping == null) {
                                    offsetMapping = current;
                                } else {
                                    throw new IllegalStateException(parameterDescription + " is bound to both " + current + " and " + offsetMapping);
                                }
                            }
                        }
                        this.offsetMappings.put(parameterDescription.getOffset(), offsetMapping == null
                            ? new OffsetMapping.ForArgument.Unresolved(parameterDescription)
                            : offsetMapping);
                    }
                    suppressionHandler = SuppressionHandler.Suppressing.of(throwableType);
                    relocationHandler = RelocationHandler.ForType.of(relocatableType, adviceMethod.getReturnType());
                }

                /**
                 * {@inheritDoc}
                 */
                public boolean isAlive() {
                    return true;
                }

                @Override
                public Map<Integer, OffsetMapping> getOffsetMapping() {
                    return this.offsetMappings;
                }
            }
        }

        /**
         * A bound resolution of an advice method.
         */
        interface Bound {

            /**
             * Prepares the advice method's exception handlers.
             */
            void prepare();

            /**
             * Initialized the advice's methods local variables.
             */
            void initialize();

            /**
             * Applies this dispatcher.
             */
            void apply();
        }

        /**
         * An implementation for inactive devise that does not write any byte code.
         */
        enum Inactive implements Dispatcher.Unresolved, Resolved.ForMethodEnter, Resolved.ForMethodExit, Bound {

            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * {@inheritDoc}
             */
            public boolean isAlive() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isBinary() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            public TypeDescription getAdviceType() {
                return TypeDescription.VOID;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isPrependLineNumber() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            public TypeDefinition getActualAdviceType() {
                return TypeDescription.VOID;
            }

            /**
             * {@inheritDoc}
             */
            public Map<String, TypeDefinition> getNamedTypes() {
                return Collections.emptyMap();
            }

            /**
             * {@inheritDoc}
             */
            public TypeDescription getThrowable() {
                return NoExceptionHandler.DESCRIPTION;
            }

            /**
             * {@inheritDoc}
             */
            public ArgumentHandler.Factory getArgumentHandlerFactory() {
                return ArgumentHandler.Factory.SIMPLE;
            }

            /**
             * {@inheritDoc}
             */
            public Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                         ClassReader classReader,
                                                         Unresolved methodExit,
                                                         PostProcessor.Factory postProcessorFactory) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                       ClassReader classReader,
                                                       Unresolved methodEnter,
                                                       PostProcessor.Factory postProcessorFactory) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public void prepare() {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void initialize() {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void apply() {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public Bound bind(TypeDescription instrumentedType,
                              MethodDescription instrumentedMethod,
                              MethodVisitor methodVisitor,
                              Implementation.Context implementationContext,
                              Assigner assigner,
                              ArgumentHandler.ForInstrumentedMethod argumentHandler,
                              MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                              StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                              StackManipulation exceptionHandler,
                              RelocationHandler.Relocation relocation) {
                return this;
            }

            @Override
            public Map<Integer, OffsetMapping> getOffsetMapping() {
                return null;
            }
        }

        /**
         * A dispatcher for an advice method that is being inlined into the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class Inlining implements Unresolved {

            /**
             * The advice method.
             */
            protected final MethodDescription.InDefinedShape adviceMethod;

            /**
             * A mapping of all available local variables by their name to their type.
             */
            private final Map<String, TypeDefinition> namedTypes;

            /**
             * Creates a dispatcher for inlined advice method.
             *
             * @param adviceMethod The advice method.
             */
            protected Inlining(MethodDescription.InDefinedShape adviceMethod) {
                this.adviceMethod = adviceMethod;
                namedTypes = new HashMap<>();
                for (ParameterDescription parameterDescription
                    : adviceMethod.getParameters().filter(isAnnotatedWith(Local.class))) {
                    String name = parameterDescription.getDeclaredAnnotations()
                        .ofType(Local.class).getValue(OffsetMapping.ForLocalValue.Factory.LOCAL_VALUE)
                        .resolve(String.class);
                    TypeDefinition previous = namedTypes.put(name, parameterDescription.getType());
                    if (previous != null && !previous.equals(parameterDescription.getType())) {
                        throw new IllegalStateException("Local variable for " + name + " is defined with inconsistent types");
                    }
                }
            }

            /**
             * {@inheritDoc}
             */
            public boolean isAlive() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isBinary() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public TypeDescription getAdviceType() {
                return adviceMethod.getReturnType().asErasure();
            }

            /**
             * {@inheritDoc}
             */
            public Map<String, TypeDefinition> getNamedTypes() {
                return namedTypes;
            }

            /**
             * {@inheritDoc}
             */
            public Dispatcher.Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                    ClassReader classReader,
                                                                    Unresolved methodExit,
                                                                    PostProcessor.Factory postProcessorFactory) {
                return Resolved.ForMethodEnter.of(adviceMethod,
                    postProcessorFactory.make(adviceMethod, false),
                    namedTypes,
                    userFactories,
                    methodExit.getAdviceType(),
                    classReader,
                    methodExit.isAlive());
            }

            /**
             * {@inheritDoc}
             */
            public Dispatcher.Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                  ClassReader classReader,
                                                                  Unresolved methodEnter,
                                                                  PostProcessor.Factory postProcessorFactory) {
                Map<String, TypeDefinition> namedTypes = new HashMap<>(methodEnter.getNamedTypes()), uninitializedNamedTypes = new HashMap<>();
                for (Map.Entry<String, TypeDefinition> entry : this.namedTypes.entrySet()) {
                    TypeDefinition typeDefinition = namedTypes.get(entry.getKey()), uninitializedTypeDefinition = uninitializedNamedTypes.get(entry.getKey());
                    if (typeDefinition == null && uninitializedTypeDefinition == null) {
                        namedTypes.put(entry.getKey(), entry.getValue());
                        uninitializedNamedTypes.put(entry.getKey(), entry.getValue());
                    } else if (!(typeDefinition == null ? uninitializedTypeDefinition : typeDefinition).equals(entry.getValue())) {
                        throw new IllegalStateException("Local variable for " + entry.getKey() + " is defined with inconsistent types");
                    }
                }
                return Resolved.ForMethodExit.of(adviceMethod,
                    postProcessorFactory.make(adviceMethod, true),
                    namedTypes, uninitializedNamedTypes, userFactories,
                    classReader, methodEnter.getAdviceType());
            }

            public Dispatcher.Resolved.ForMethodExit asMethodExitNonThrowable(
                List<? extends OffsetMapping.Factory<?>> userFactories,
                ClassReader classReader,
                Unresolved methodEnter,
                PostProcessor.Factory postProcessorFactory) {
                Map<String, TypeDefinition> namedTypes = new HashMap<>(methodEnter.getNamedTypes()), uninitializedNamedTypes = new HashMap<>();
                for (Map.Entry<String, TypeDefinition> entry : this.namedTypes.entrySet()) {
                    TypeDefinition typeDefinition = namedTypes.get(entry.getKey()), uninitializedTypeDefinition = uninitializedNamedTypes.get(entry.getKey());
                    if (typeDefinition == null && uninitializedTypeDefinition == null) {
                        namedTypes.put(entry.getKey(), entry.getValue());
                        uninitializedNamedTypes.put(entry.getKey(), entry.getValue());
                    } else if (!(typeDefinition == null ? uninitializedTypeDefinition : typeDefinition).equals(entry.getValue())) {
                        throw new IllegalStateException("Local variable for " + entry.getKey() + " is defined with inconsistent types");
                    }
                }
                return Resolved.ForMethodExit.ofNonThrowable(adviceMethod,
                    postProcessorFactory.make(adviceMethod, true),
                    namedTypes, uninitializedNamedTypes, userFactories,
                    classReader, methodEnter.getAdviceType());
            }

            /**
             * A resolved version of a dispatcher.
             */
            protected abstract static class Resolved extends Dispatcher.Resolved.AbstractBase {

                /**
                 * A class reader to query for the class file of the advice method.
                 */
                protected final ClassReader classReader;

                /**
                 * Creates a new resolved version of a dispatcher.
                 *
                 * @param adviceMethod    The represented advice method.
                 * @param postProcessor   The post processor to apply.
                 * @param factories       A list of factories to resolve for the parameters of the advice method.
                 * @param throwableType   The type to handle by a suppression handler or {@link NoExceptionHandler} to not handle any exceptions.
                 * @param relocatableType The type to trigger a relocation of the method's control flow or {@code void} if no relocation should be executed.
                 * @param classReader     A class reader to query for the class file of the advice method.
                 */
                protected Resolved(MethodDescription.InDefinedShape adviceMethod,
                                   PostProcessor postProcessor,
                                   List<? extends OffsetMapping.Factory<?>> factories,
                                   TypeDescription throwableType,
                                   TypeDescription relocatableType,
                                   ClassReader classReader) {
                    super(adviceMethod, postProcessor, factories, throwableType, relocatableType, OffsetMapping.Factory.AdviceType.INLINING);
                    this.classReader = classReader;
                }

                /**
                 * Resolves the initialization types of this advice method.
                 *
                 * @param argumentHandler The argument handler to use for resolving the initialization.
                 * @return A mapping of parameter offsets to the type to initialize.
                 */
                protected abstract Map<Integer, TypeDefinition> resolveInitializationTypes(ArgumentHandler argumentHandler);

                /**
                 * Applies a resolution for a given instrumented method.
                 *
                 * @param methodVisitor         A method visitor for writing byte code to the instrumented method.
                 * @param implementationContext The implementation context to use.
                 * @param assigner              The assigner to use.
                 * @param argumentHandler       A handler for accessing values on the local variable array.
                 * @param methodSizeHandler     A handler for computing the method size requirements.
                 * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                 * @param instrumentedType      A description of the instrumented type.
                 * @param instrumentedMethod    A description of the instrumented method.
                 * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                 * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                 * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                 * @return A method visitor for visiting the advice method's byte code.
                 */
                protected abstract MethodVisitor apply(MethodVisitor methodVisitor,
                                                       Implementation.Context implementationContext,
                                                       Assigner assigner,
                                                       ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                                       MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                                       StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                                       TypeDescription instrumentedType,
                                                       MethodDescription instrumentedMethod,
                                                       SuppressionHandler.Bound suppressionHandler,
                                                       RelocationHandler.Bound relocationHandler,
                                                       StackManipulation exceptionHandler);

                /**
                 * A bound advice method that copies the code by first extracting the exception table and later appending the
                 * code of the method without copying any meta data.
                 */
                protected class AdviceMethodInliner extends ClassVisitor implements Bound {

                    /**
                     * A description of the instrumented type.
                     */
                    protected final TypeDescription instrumentedType;

                    /**
                     * The instrumented method.
                     */
                    protected final MethodDescription instrumentedMethod;

                    /**
                     * The method visitor for writing the instrumented method.
                     */
                    protected final MethodVisitor methodVisitor;

                    /**
                     * The implementation context to use.
                     */
                    protected final Implementation.Context implementationContext;

                    /**
                     * The assigner to use.
                     */
                    protected final Assigner assigner;

                    /**
                     * A handler for accessing values on the local variable array.
                     */
                    protected final ArgumentHandler.ForInstrumentedMethod argumentHandler;

                    /**
                     * A handler for computing the method size requirements.
                     */
                    protected final MethodSizeHandler.ForInstrumentedMethod methodSizeHandler;

                    /**
                     * A handler for translating and injecting stack map frames.
                     */
                    protected final StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler;

                    /**
                     * A bound suppression handler that is used for suppressing exceptions of this advice method.
                     */
                    protected final SuppressionHandler.Bound suppressionHandler;

                    /**
                     * A bound relocation handler that is responsible for considering a non-standard control flow.
                     */
                    protected final RelocationHandler.Bound relocationHandler;

                    /**
                     * The exception handler that is resolved for the instrumented method.
                     */
                    protected final StackManipulation exceptionHandler;

                    /**
                     * A class reader for parsing the class file containing the represented advice method.
                     */
                    protected final ClassReader classReader;

                    /**
                     * The labels that were found during parsing the method's exception handler in the order of their discovery.
                     */
                    protected final List<Label> labels;

                    /**
                     * Creates a new advice method inliner.
                     *
                     * @param instrumentedType      A description of the instrumented type.
                     * @param instrumentedMethod    The instrumented method.
                     * @param methodVisitor         The method visitor for writing the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param assigner              The assigner to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                     * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @param classReader           A class reader for parsing the class file containing the represented advice method.
                     */
                    protected AdviceMethodInliner(TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  MethodVisitor methodVisitor,
                                                  Implementation.Context implementationContext,
                                                  Assigner assigner,
                                                  ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                                  MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                                  StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                                  SuppressionHandler.Bound suppressionHandler,
                                                  RelocationHandler.Bound relocationHandler,
                                                  StackManipulation exceptionHandler,
                                                  ClassReader classReader) {
                        super(OpenedClassReader.ASM_API);
                        this.instrumentedType = instrumentedType;
                        this.instrumentedMethod = instrumentedMethod;
                        this.methodVisitor = methodVisitor;
                        this.implementationContext = implementationContext;
                        this.assigner = assigner;
                        this.argumentHandler = argumentHandler;
                        this.methodSizeHandler = methodSizeHandler;
                        this.stackMapFrameHandler = stackMapFrameHandler;
                        this.suppressionHandler = suppressionHandler;
                        this.relocationHandler = relocationHandler;
                        this.exceptionHandler = exceptionHandler;
                        this.classReader = classReader;
                        labels = new ArrayList<>();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void prepare() {
                        classReader.accept(new ExceptionTableExtractor(), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                        suppressionHandler.onPrepare(methodVisitor);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void initialize() {
                        for (Map.Entry<Integer, TypeDefinition> typeDefinition : resolveInitializationTypes(argumentHandler).entrySet()) {
                            if (typeDefinition.getValue().represents(boolean.class)
                                || typeDefinition.getValue().represents(byte.class)
                                || typeDefinition.getValue().represents(short.class)
                                || typeDefinition.getValue().represents(char.class)
                                || typeDefinition.getValue().represents(int.class)) {
                                methodVisitor.visitInsn(Opcodes.ICONST_0);
                                methodVisitor.visitVarInsn(Opcodes.ISTORE, typeDefinition.getKey());
                            } else if (typeDefinition.getValue().represents(long.class)) {
                                methodVisitor.visitInsn(Opcodes.LCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.LSTORE, typeDefinition.getKey());
                            } else if (typeDefinition.getValue().represents(float.class)) {
                                methodVisitor.visitInsn(Opcodes.FCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.FSTORE, typeDefinition.getKey());
                            } else if (typeDefinition.getValue().represents(double.class)) {
                                methodVisitor.visitInsn(Opcodes.DCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.DSTORE, typeDefinition.getKey());
                            } else {
                                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                                methodVisitor.visitVarInsn(Opcodes.ASTORE, typeDefinition.getKey());
                            }
                            methodSizeHandler.requireStackSize(typeDefinition.getValue().getStackSize().getSize());
                        }
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void apply() {
                        classReader.accept(this, ClassReader.SKIP_DEBUG | stackMapFrameHandler.getReaderHint());
                    }

                    @Override
                    public MethodVisitor visitMethod(int modifiers, String internalName, String descriptor, String signature, String[] exception) {
                        return adviceMethod.getInternalName().equals(internalName) && adviceMethod.getDescriptor().equals(descriptor)
                            ? new ExceptionTableSubstitutor(Inlining.Resolved.this.apply(methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            instrumentedType,
                            instrumentedMethod,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler)) : IGNORE_METHOD;
                    }

                    /**
                     * A class visitor that extracts the exception tables of the advice method.
                     */
                    protected class ExceptionTableExtractor extends ClassVisitor {

                        /**
                         * Creates a new exception table extractor.
                         */
                        protected ExceptionTableExtractor() {
                            super(OpenedClassReader.ASM_API);
                        }

                        @Override
                        public MethodVisitor visitMethod(int modifiers, String internalName, String descriptor, String signature, String[] exception) {
                            return adviceMethod.getInternalName().equals(internalName) && adviceMethod.getDescriptor().equals(descriptor)
                                ? new ExceptionTableCollector(methodVisitor)
                                : IGNORE_METHOD;
                        }
                    }

                    /**
                     * A visitor that only writes try-catch-finally blocks to the supplied method visitor. All labels of these tables are collected
                     * for substitution when revisiting the reminder of the method.
                     */
                    protected class ExceptionTableCollector extends MethodVisitor {

                        /**
                         * The method visitor for which the try-catch-finally blocks should be written.
                         */
                        private final MethodVisitor methodVisitor;

                        /**
                         * Creates a new exception table collector.
                         *
                         * @param methodVisitor The method visitor for which the try-catch-finally blocks should be written.
                         */
                        protected ExceptionTableCollector(MethodVisitor methodVisitor) {
                            super(OpenedClassReader.ASM_API);
                            this.methodVisitor = methodVisitor;
                        }

                        @Override
                        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                            methodVisitor.visitTryCatchBlock(start, end, handler, type);
                            labels.addAll(Arrays.asList(start, end, handler));
                        }

                        @Override
                        public AnnotationVisitor visitTryCatchAnnotation(int typeReference, TypePath typePath, String descriptor, boolean visible) {
                            return methodVisitor.visitTryCatchAnnotation(typeReference, typePath, descriptor, visible);
                        }
                    }

                    /**
                     * A label substitutor allows to visit an advice method a second time after the exception handlers were already written.
                     * Doing so, this visitor substitutes all labels that were already created during the first visit to keep the mapping
                     * consistent. It is not required to resolve labels for non-code instructions as meta information is not propagated to
                     * the target method visitor for advice code.
                     */
                    protected class ExceptionTableSubstitutor extends MethodVisitor {

                        /**
                         * A map containing resolved substitutions.
                         */
                        private final Map<Label, Label> substitutions;

                        /**
                         * The current index of the visited labels that are used for try-catch-finally blocks.
                         */
                        private int index;

                        /**
                         * Creates a label substitutor.
                         *
                         * @param methodVisitor The method visitor for which to substitute labels.
                         */
                        protected ExceptionTableSubstitutor(MethodVisitor methodVisitor) {
                            super(OpenedClassReader.ASM_API, methodVisitor);
                            substitutions = new IdentityHashMap<>();
                        }

                        @Override
                        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                            substitutions.put(start, labels.get(index++));
                            substitutions.put(end, labels.get(index++));
                            Label actualHandler = labels.get(index++);
                            substitutions.put(handler, actualHandler);
                            ((CodeTranslationVisitor) mv).propagateHandler(actualHandler);
                        }

                        @Override
                        public AnnotationVisitor visitTryCatchAnnotation(int typeReference, TypePath typePath, String descriptor, boolean visible) {
                            return IGNORE_ANNOTATION;
                        }

                        @Override
                        public void visitLabel(Label label) {
                            super.visitLabel(resolve(label));
                        }

                        @Override
                        public void visitJumpInsn(int opcode, Label label) {
                            super.visitJumpInsn(opcode, resolve(label));
                        }

                        @Override
                        public void visitTableSwitchInsn(int minimum, int maximum, Label defaultOption, Label... label) {
                            super.visitTableSwitchInsn(minimum, maximum, defaultOption, resolve(label));
                        }

                        @Override
                        public void visitLookupSwitchInsn(Label defaultOption, int[] keys, Label[] label) {
                            super.visitLookupSwitchInsn(resolve(defaultOption), keys, resolve(label));
                        }

                        /**
                         * Resolves an array of labels.
                         *
                         * @param label The labels to resolved.
                         * @return An array containing the resolved arrays.
                         */
                        private Label[] resolve(Label[] label) {
                            Label[] resolved = new Label[label.length];
                            int index = 0;
                            for (Label aLabel : label) {
                                resolved[index++] = resolve(aLabel);
                            }
                            return resolved;
                        }

                        /**
                         * Resolves a single label if mapped or returns the original label.
                         *
                         * @param label The label to resolve.
                         * @return The resolved label.
                         */
                        private Label resolve(Label label) {
                            Label substitution = substitutions.get(label);
                            return substitution == null
                                ? label
                                : substitution;
                        }
                    }
                }

                /**
                 * A resolved dispatcher for implementing method enter advice.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected abstract static class ForMethodEnter extends Inlining.Resolved implements Dispatcher.Resolved.ForMethodEnter {

                    /**
                     * A mapping of all available local variables by their name to their type.
                     */
                    private final Map<String, TypeDefinition> namedTypes;

                    /**
                     * {@code true} if the first discovered line number information should be prepended to the advice code.
                     */
                    private final boolean prependLineNumber;

                    /**
                     * Creates a new resolved dispatcher for implementing method enter advice.
                     *
                     * @param adviceMethod  The represented advice method.
                     * @param postProcessor The post processor to apply.
                     * @param namedTypes    A mapping of all available local variables by their name to their type.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param exitType      The exit type or {@code void} if no exit type is defined.
                     * @param classReader   A class reader to query for the class file of the advice method.
                     */
                    protected ForMethodEnter(MethodDescription.InDefinedShape adviceMethod,
                                             PostProcessor postProcessor,
                                             Map<String, TypeDefinition> namedTypes,
                                             List<? extends OffsetMapping.Factory<?>> userFactories,
                                             TypeDefinition exitType,
                                             ClassReader classReader) {
                        super(adviceMethod,
                            postProcessor,
                            CompoundList.of(Arrays.asList(OffsetMapping.ForArgument.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForAllArguments.Factory.INSTANCE,
                                OffsetMapping.ForThisReference.Factory.INSTANCE,
                                OffsetMapping.ForField.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForOrigin.Factory.INSTANCE,
                                OffsetMapping.ForUnusedValue.Factory.INSTANCE,
                                OffsetMapping.ForStubValue.INSTANCE,
                                OffsetMapping.ForThrowable.Factory.INSTANCE,
                                OffsetMapping.ForExitValue.Factory.of(exitType),
                                new OffsetMapping.ForLocalValue.Factory(namedTypes),
                                new OffsetMapping.Factory.Illegal<>(Thrown.class),
                                new OffsetMapping.Factory.Illegal<>(Enter.class),
                                new OffsetMapping.Factory.Illegal<>(Return.class)), userFactories),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(SUPPRESS_ENTER).resolve(TypeDescription.class),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(SKIP_ON).resolve(TypeDescription.class),
                            classReader);
                        this.namedTypes = namedTypes;
                        prependLineNumber = adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(PREPEND_LINE_NUMBER).resolve(Boolean.class);
                    }

                    /**
                     * Resolves enter advice that only exposes the enter type if this is necessary.
                     *
                     * @param adviceMethod  The advice method.
                     * @param postProcessor The post processor to apply.
                     * @param namedTypes    A mapping of all available local variables by their name to their type.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param exitType      The exit type or {@code void} if no exit type is defined.
                     * @param classReader   The class reader for parsing the advice method's class file.
                     * @param methodExit    {@code true} if exit advice is applied.
                     * @return An appropriate enter handler.
                     */
                    protected static Resolved.ForMethodEnter of(MethodDescription.InDefinedShape adviceMethod,
                                                                PostProcessor postProcessor,
                                                                Map<String, TypeDefinition> namedTypes,
                                                                List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                TypeDefinition exitType,
                                                                ClassReader classReader,
                                                                boolean methodExit) {
                        return methodExit
                            ? new WithRetainedEnterType(adviceMethod, postProcessor, namedTypes, userFactories, exitType, classReader)
                            : new WithDiscardedEnterType(adviceMethod, postProcessor, namedTypes, userFactories, exitType, classReader);
                    }

                    @Override
                    protected Map<Integer, TypeDefinition> resolveInitializationTypes(ArgumentHandler argumentHandler) {
                        SortedMap<Integer, TypeDefinition> resolved = new TreeMap<>();
                        for (Map.Entry<String, TypeDefinition> entry : namedTypes.entrySet()) {
                            resolved.put(argumentHandler.named(entry.getKey()), entry.getValue());
                        }
                        return resolved;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Bound bind(TypeDescription instrumentedType,
                                      MethodDescription instrumentedMethod,
                                      MethodVisitor methodVisitor,
                                      Implementation.Context implementationContext,
                                      Assigner assigner,
                                      ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                      MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                      StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                      StackManipulation exceptionHandler,
                                      RelocationHandler.Relocation relocation) {
                        return new AdviceMethodInliner(instrumentedType,
                            instrumentedMethod,
                            methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            suppressionHandler.bind(exceptionHandler),
                            relocationHandler.bind(instrumentedMethod, relocation),
                            exceptionHandler,
                            classReader);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public boolean isPrependLineNumber() {
                        return prependLineNumber;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public TypeDefinition getActualAdviceType() {
                        return adviceMethod.getReturnType();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Map<String, TypeDefinition> getNamedTypes() {
                        return namedTypes;
                    }

                    @Override
                    protected MethodVisitor apply(MethodVisitor methodVisitor,
                                                  Context implementationContext,
                                                  Assigner assigner,
                                                  ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                                  MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                                  StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                                  TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  SuppressionHandler.Bound suppressionHandler,
                                                  RelocationHandler.Bound relocationHandler,
                                                  StackManipulation exceptionHandler) {
                        return doApply(methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler.bindEnter(adviceMethod),
                            methodSizeHandler.bindEnter(adviceMethod),
                            stackMapFrameHandler.bindEnter(adviceMethod),
                            instrumentedType,
                            instrumentedMethod,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler);
                    }

                    /**
                     * Applies a resolution for a given instrumented method.
                     *
                     * @param instrumentedType      A description of the instrumented type.
                     * @param instrumentedMethod    The instrumented method that is being bound.
                     * @param methodVisitor         The method visitor for writing to the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param assigner              The assigner to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    The bound suppression handler to use.
                     * @param relocationHandler     The bound relocation handler to use.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @return A method visitor for visiting the advice method's byte code.
                     */
                    protected MethodVisitor doApply(MethodVisitor methodVisitor,
                                                    Implementation.Context implementationContext,
                                                    Assigner assigner,
                                                    ArgumentHandler.ForAdvice argumentHandler,
                                                    MethodSizeHandler.ForAdvice methodSizeHandler,
                                                    StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                    TypeDescription instrumentedType,
                                                    MethodDescription instrumentedMethod,
                                                    SuppressionHandler.Bound suppressionHandler,
                                                    RelocationHandler.Bound relocationHandler,
                                                    StackManipulation exceptionHandler) {
                        Map<Integer, OffsetMapping.Target> offsetMappings = new HashMap<>();
                        for (Map.Entry<Integer, OffsetMapping> entry : this.offsetMappings.entrySet()) {
                            offsetMappings.put(entry.getKey(), entry.getValue().resolve(instrumentedType,
                                instrumentedMethod,
                                assigner,
                                argumentHandler,
                                OffsetMapping.Sort.ENTER));
                        }
                        return new CodeTranslationVisitor(methodVisitor,
                            implementationContext,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            instrumentedType,
                            instrumentedMethod,
                            assigner,
                            adviceMethod,
                            offsetMappings,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler,
                            postProcessor,
                            false);
                    }

                    /**
                     * Implementation of an advice that does expose an enter type.
                     */
                    protected static class WithRetainedEnterType extends Inlining.Resolved.ForMethodEnter {


                        /**
                         * Creates a new resolved dispatcher for implementing method enter advice that does expose the enter type.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor to apply.
                         * @param namedTypes    A mapping of all available local variables by their name to their type.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param exitType      The exit type or {@code void} if no exit type is defined.
                         * @param classReader   A class reader to query for the class file of the advice method.
                         */
                        protected WithRetainedEnterType(MethodDescription.InDefinedShape adviceMethod,
                                                        PostProcessor postProcessor,
                                                        Map<String, TypeDefinition> namedTypes,
                                                        List<? extends OffsetMapping.Factory<?>> userFactories,
                                                        TypeDefinition exitType,
                                                        ClassReader classReader) {
                            super(adviceMethod, postProcessor, namedTypes, userFactories, exitType, classReader);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDefinition getAdviceType() {
                            return adviceMethod.getReturnType();
                        }
                    }

                    /**
                     * Implementation of an advice that does not expose an enter type.
                     */
                    protected static class WithDiscardedEnterType extends Inlining.Resolved.ForMethodEnter {

                        /**
                         * Creates a new resolved dispatcher for implementing method enter advice that does not expose the enter type.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor to apply.
                         * @param namedTypes    A mapping of all available local variables by their name to their type.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param exitType      The exit type or {@code void} if no exit type is defined.
                         * @param classReader   A class reader to query for the class file of the advice method.
                         */
                        protected WithDiscardedEnterType(MethodDescription.InDefinedShape adviceMethod,
                                                         PostProcessor postProcessor,
                                                         Map<String, TypeDefinition> namedTypes,
                                                         List<? extends OffsetMapping.Factory<?>> userFactories,
                                                         TypeDefinition exitType,
                                                         ClassReader classReader) {
                            super(adviceMethod, postProcessor, namedTypes, userFactories, exitType, classReader);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDefinition getAdviceType() {
                            return TypeDescription.VOID;
                        }

                        /**
                         * {@inheritDoc}
                         */
                        protected MethodVisitor doApply(MethodVisitor methodVisitor,
                                                        Context implementationContext,
                                                        Assigner assigner,
                                                        ArgumentHandler.ForAdvice argumentHandler,
                                                        MethodSizeHandler.ForAdvice methodSizeHandler,
                                                        StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                        TypeDescription instrumentedType,
                                                        MethodDescription instrumentedMethod,
                                                        SuppressionHandler.Bound suppressionHandler,
                                                        RelocationHandler.Bound relocationHandler,
                                                        StackManipulation exceptionHandler) {
                            methodSizeHandler.requireLocalVariableLengthPadding(adviceMethod.getReturnType().getStackSize().getSize());
                            return super.doApply(methodVisitor,
                                implementationContext,
                                assigner,
                                argumentHandler,
                                methodSizeHandler,
                                stackMapFrameHandler,
                                instrumentedType,
                                instrumentedMethod,
                                suppressionHandler,
                                relocationHandler,
                                exceptionHandler);
                        }
                    }
                }

                /**
                 * A resolved dispatcher for implementing method exit advice.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected abstract static class ForMethodExit extends Inlining.Resolved implements Dispatcher.Resolved.ForMethodExit {

                    /**
                     * A mapping of uninitialized local variables by their name.
                     */
                    private final Map<String, TypeDefinition> uninitializedNamedTypes;

                    /**
                     * {@code true} if the arguments of the instrumented method should be copied before executing the instrumented method.
                     */
                    private final boolean backupArguments;

                    /**
                     * Creates a new resolved dispatcher for implementing method exit advice.
                     *
                     * @param adviceMethod            The represented advice method.
                     * @param postProcessor           The post processor to apply.
                     * @param namedTypes              A mapping of all available local variables by their name to their type.
                     * @param uninitializedNamedTypes A mapping of all uninitialized local variables by their name to their type.
                     * @param userFactories           A list of user-defined factories for offset mappings.
                     * @param classReader             The class reader for parsing the advice method's class file.
                     * @param enterType               The type of the value supplied by the enter advice method or {@code void} if no such value exists.
                     */
                    protected ForMethodExit(MethodDescription.InDefinedShape adviceMethod,
                                            PostProcessor postProcessor,
                                            Map<String, TypeDefinition> namedTypes,
                                            Map<String, TypeDefinition> uninitializedNamedTypes,
                                            List<? extends OffsetMapping.Factory<?>> userFactories,
                                            ClassReader classReader,
                                            TypeDefinition enterType) {
                        super(adviceMethod,
                            postProcessor,
                            CompoundList.of(Arrays.asList(OffsetMapping.ForArgument.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForAllArguments.Factory.INSTANCE,
                                OffsetMapping.ForThisReference.Factory.INSTANCE,
                                OffsetMapping.ForField.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForOrigin.Factory.INSTANCE,
                                OffsetMapping.ForUnusedValue.Factory.INSTANCE,
                                OffsetMapping.ForStubValue.INSTANCE,
                                OffsetMapping.ForEnterValue.Factory.of(enterType),
                                OffsetMapping.ForExitValue.Factory.of(adviceMethod.getReturnType()),
                                new OffsetMapping.ForLocalValue.Factory(namedTypes),
                                OffsetMapping.ForReturnValue.Factory.INSTANCE,
                                OffsetMapping.ForThrowable.Factory.of(adviceMethod)
                            ), userFactories),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(SUPPRESS_EXIT).resolve(TypeDescription.class),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(REPEAT_ON).resolve(TypeDescription.class),
                            classReader);
                        this.uninitializedNamedTypes = uninitializedNamedTypes;
                        backupArguments = adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(BACKUP_ARGUMENTS).resolve(Boolean.class);
                    }

                    /**
                     * Resolves exit advice that handles exceptions depending on the specification of the exit advice.
                     *
                     * @param adviceMethod            The advice method.
                     * @param postProcessor           The post processor to apply.
                     * @param namedTypes              A mapping of all available local variables by their name to their type.
                     * @param uninitializedNamedTypes A mapping of all uninitialized local variables by their name to their type.
                     * @param userFactories           A list of user-defined factories for offset mappings.
                     * @param classReader             The class reader for parsing the advice method's class file.
                     * @param enterType               The type of the value supplied by the enter advice method or {@code void} if no such value exists.
                     * @return An appropriate exit handler.
                     */
                    protected static Resolved.ForMethodExit of(MethodDescription.InDefinedShape adviceMethod,
                                                               PostProcessor postProcessor,
                                                               Map<String, TypeDefinition> namedTypes,
                                                               Map<String, TypeDefinition> uninitializedNamedTypes,
                                                               List<? extends OffsetMapping.Factory<?>> userFactories,
                                                               ClassReader classReader,
                                                               TypeDefinition enterType) {
                        TypeDescription throwable = adviceMethod.getDeclaredAnnotations()
                            .ofType(OnMethodExit.class)
                            .getValue(ON_THROWABLE).resolve(TypeDescription.class);
                        return isNoExceptionHandler(throwable)
                            ? new WithoutExceptionHandler(adviceMethod, postProcessor, namedTypes, uninitializedNamedTypes, userFactories, classReader, enterType)
                            : new WithExceptionHandler(adviceMethod, postProcessor, namedTypes, uninitializedNamedTypes, userFactories, classReader, enterType, throwable);
                    }

                    protected static Resolved.ForMethodExit ofNonThrowable(MethodDescription.InDefinedShape adviceMethod,
                                                               PostProcessor postProcessor,
                                                               Map<String, TypeDefinition> namedTypes,
                                                               Map<String, TypeDefinition> uninitializedNamedTypes,
                                                               List<? extends OffsetMapping.Factory<?>> userFactories,
                                                               ClassReader classReader,
                                                               TypeDefinition enterType) {
                        return new WithoutExceptionHandler(adviceMethod, postProcessor, namedTypes,
                            uninitializedNamedTypes, userFactories, classReader, enterType);
                    }

                    @Override
                    public Map<String, TypeDefinition> getNamedTypes() {
                        return uninitializedNamedTypes;
                    }

                    @Override
                    protected Map<Integer, TypeDefinition> resolveInitializationTypes(ArgumentHandler argumentHandler) {
                        SortedMap<Integer, TypeDefinition> resolved = new TreeMap<>();
                        for (Map.Entry<String, TypeDefinition> entry : uninitializedNamedTypes.entrySet()) {
                            resolved.put(argumentHandler.named(entry.getKey()), entry.getValue());
                        }
                        if (!adviceMethod.getReturnType().represents(void.class)) {
                            resolved.put(argumentHandler.exit(), adviceMethod.getReturnType());
                        }
                        return resolved;
                    }

                    @Override
                    protected MethodVisitor apply(MethodVisitor methodVisitor,
                                                  Implementation.Context implementationContext,
                                                  Assigner assigner,
                                                  ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                                  MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                                  StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                                  TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  SuppressionHandler.Bound suppressionHandler,
                                                  RelocationHandler.Bound relocationHandler,
                                                  StackManipulation exceptionHandler) {
                        return doApply(methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler.bindExit(adviceMethod, isNoExceptionHandler(getThrowable())),
                            methodSizeHandler.bindExit(adviceMethod),
                            stackMapFrameHandler.bindExit(adviceMethod),
                            instrumentedType,
                            instrumentedMethod,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler);
                    }

                    /**
                     * Applies a resolution for a given instrumented method.
                     *
                     * @param instrumentedType      A description of the instrumented type.
                     * @param instrumentedMethod    The instrumented method that is being bound.
                     * @param methodVisitor         The method visitor for writing to the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param assigner              The assigner to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    The bound suppression handler to use.
                     * @param relocationHandler     The bound relocation handler to use.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @return A method visitor for visiting the advice method's byte code.
                     */
                    private MethodVisitor doApply(MethodVisitor methodVisitor,
                                                  Implementation.Context implementationContext,
                                                  Assigner assigner,
                                                  ArgumentHandler.ForAdvice argumentHandler,
                                                  MethodSizeHandler.ForAdvice methodSizeHandler,
                                                  StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                  TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  SuppressionHandler.Bound suppressionHandler,
                                                  RelocationHandler.Bound relocationHandler,
                                                  StackManipulation exceptionHandler) {
                        Map<Integer, OffsetMapping.Target> offsetMappings = new HashMap<>();
                        for (Map.Entry<Integer, OffsetMapping> entry : this.offsetMappings.entrySet()) {
                            offsetMappings.put(entry.getKey(), entry.getValue().resolve(instrumentedType,
                                instrumentedMethod,
                                assigner,
                                argumentHandler,
                                OffsetMapping.Sort.EXIT));
                        }
                        return new CodeTranslationVisitor(methodVisitor,
                            implementationContext,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            instrumentedType,
                            instrumentedMethod,
                            assigner,
                            adviceMethod,
                            offsetMappings,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler,
                            postProcessor,
                            true);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public ArgumentHandler.Factory getArgumentHandlerFactory() {
                        return backupArguments
                            ? ArgumentHandler.Factory.COPYING
                            : ArgumentHandler.Factory.SIMPLE;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public TypeDefinition getAdviceType() {
                        return adviceMethod.getReturnType();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Bound bind(TypeDescription instrumentedType,
                                      MethodDescription instrumentedMethod,
                                      MethodVisitor methodVisitor,
                                      Implementation.Context implementationContext,
                                      Assigner assigner,
                                      ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                      MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                      StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                      StackManipulation exceptionHandler,
                                      RelocationHandler.Relocation relocation) {
                        return new AdviceMethodInliner(instrumentedType,
                            instrumentedMethod,
                            methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            suppressionHandler.bind(exceptionHandler),
                            relocationHandler.bind(instrumentedMethod, relocation),
                            exceptionHandler,
                            classReader);
                    }

                    /**
                     * Implementation of exit advice that handles exceptions.
                     */
                    @HashCodeAndEqualsPlugin.Enhance
                    protected static class WithExceptionHandler extends Inlining.Resolved.ForMethodExit {

                        /**
                         * The type of the handled throwable type for which this advice is invoked.
                         */
                        private final TypeDescription throwable;

                        /**
                         * Creates a new resolved dispatcher for implementing method exit advice that handles exceptions.
                         *
                         * @param adviceMethod            The represented advice method.
                         * @param postProcessor           The post processor to apply.
                         * @param namedTypes              A mapping of all available local variables by their name to their type.
                         * @param uninitializedNamedTypes A mapping of all uninitialized local variables by their name to their type.
                         * @param userFactories           A list of user-defined factories for offset mappings.
                         * @param classReader             The class reader for parsing the advice method's class file.
                         * @param enterType               The type of the value supplied by the enter advice method or
                         *                                a description of {@code void} if no such value exists.
                         * @param throwable               The type of the handled throwable type for which this advice is invoked.
                         */
                        protected WithExceptionHandler(MethodDescription.InDefinedShape adviceMethod,
                                                       PostProcessor postProcessor,
                                                       Map<String, TypeDefinition> namedTypes,
                                                       Map<String, TypeDefinition> uninitializedNamedTypes,
                                                       List<? extends OffsetMapping.Factory<?>> userFactories,
                                                       ClassReader classReader,
                                                       TypeDefinition enterType,
                                                       TypeDescription throwable) {
                            super(adviceMethod, postProcessor, namedTypes, uninitializedNamedTypes, userFactories, classReader, enterType);
                            this.throwable = throwable;
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDescription getThrowable() {
                            return throwable;
                        }
                    }

                    /**
                     * Implementation of exit advice that ignores exceptions.
                     */
                    protected static class WithoutExceptionHandler extends Inlining.Resolved.ForMethodExit {

                        /**
                         * Creates a new resolved dispatcher for implementing method exit advice that does not handle exceptions.
                         *
                         * @param adviceMethod            The represented advice method.
                         * @param postProcessor           The post processor to apply.
                         * @param namedTypes              A mapping of all available local variables by their name to their type.
                         * @param uninitializedNamedTypes A mapping of all uninitialized local variables by their name to their type.
                         * @param userFactories           A list of user-defined factories for offset mappings.
                         * @param classReader             A class reader to query for the class file of the advice method.
                         * @param enterType               The type of the value supplied by the enter advice method or
                         *                                a description of {@code void} if no such value exists.
                         */
                        protected WithoutExceptionHandler(MethodDescription.InDefinedShape adviceMethod,
                                                          PostProcessor postProcessor,
                                                          Map<String, TypeDefinition> namedTypes,
                                                          Map<String, TypeDefinition> uninitializedNamedTypes,
                                                          List<? extends OffsetMapping.Factory<?>> userFactories,
                                                          ClassReader classReader,
                                                          TypeDefinition enterType) {
                            super(adviceMethod, postProcessor, namedTypes, uninitializedNamedTypes, userFactories, classReader, enterType);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDescription getThrowable() {
                            return NoExceptionHandler.DESCRIPTION;
                        }
                    }
                }
            }

            /**
             * A visitor for translating an advice method's byte code for inlining into the instrumented method.
             */
            protected static class CodeTranslationVisitor extends MethodVisitor {

                /**
                 * The original method visitor to which all instructions are eventually written to.
                 */
                protected final MethodVisitor methodVisitor;

                /**
                 * The implementation context to use.
                 */
                protected final Context implementationContext;

                /**
                 * A handler for accessing values on the local variable array.
                 */
                protected final ArgumentHandler.ForAdvice argumentHandler;

                /**
                 * A handler for computing the method size requirements.
                 */
                protected final MethodSizeHandler.ForAdvice methodSizeHandler;

                /**
                 * A handler for translating and injecting stack map frames.
                 */
                protected final StackMapFrameHandler.ForAdvice stackMapFrameHandler;

                /**
                 * The instrumented type.
                 */
                private final TypeDescription instrumentedType;

                /**
                 * The instrumented method.
                 */
                private final MethodDescription instrumentedMethod;

                /**
                 * The assigner to use.
                 */
                private final Assigner assigner;

                /**
                 * The advice method.
                 */
                protected final MethodDescription.InDefinedShape adviceMethod;

                /**
                 * A mapping of offsets to resolved target offsets in the instrumented method.
                 */
                private final Map<Integer, OffsetMapping.Target> offsetMappings;

                /**
                 * A bound suppression handler that is used for suppressing exceptions of this advice method.
                 */
                private final SuppressionHandler.Bound suppressionHandler;

                /**
                 * A bound relocation handler that is responsible for considering a non-standard control flow.
                 */
                private final RelocationHandler.Bound relocationHandler;

                /**
                 * The exception handler that is resolved for the instrumented method.
                 */
                private final StackManipulation exceptionHandler;

                /**
                 * The post processor to apply.
                 */
                private final PostProcessor postProcessor;

                /**
                 * {@code true} if this visitor is for exit advice.
                 */
                private final boolean exit;

                /**
                 * A label indicating the end of the advice byte code.
                 */
                protected final Label endOfMethod;

                /**
                 * Creates a new code translation visitor.
                 *
                 * @param methodVisitor         A method visitor for writing the instrumented method's byte code.
                 * @param implementationContext The implementation context to use.
                 * @param argumentHandler       A handler for accessing values on the local variable array.
                 * @param methodSizeHandler     A handler for computing the method size requirements.
                 * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                 * @param instrumentedType      The instrumented type.
                 * @param instrumentedMethod    The instrumented method.
                 * @param assigner              The assigner to use.
                 * @param adviceMethod          The advice method.
                 * @param offsetMappings        A mapping of offsets to resolved target offsets in the instrumented method.
                 * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                 * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                 * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                 * @param postProcessor         The post processor to apply.
                 * @param exit                  {@code true} if this visitor is for exit advice.
                 */
                protected CodeTranslationVisitor(MethodVisitor methodVisitor,
                                                 Context implementationContext,
                                                 ArgumentHandler.ForAdvice argumentHandler,
                                                 MethodSizeHandler.ForAdvice methodSizeHandler,
                                                 StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                 TypeDescription instrumentedType,
                                                 MethodDescription instrumentedMethod,
                                                 Assigner assigner,
                                                 MethodDescription.InDefinedShape adviceMethod,
                                                 Map<Integer, OffsetMapping.Target> offsetMappings,
                                                 SuppressionHandler.Bound suppressionHandler,
                                                 RelocationHandler.Bound relocationHandler,
                                                 StackManipulation exceptionHandler,
                                                 PostProcessor postProcessor,
                                                 boolean exit) {
                    super(OpenedClassReader.ASM_API, new StackAwareMethodVisitor(methodVisitor, instrumentedMethod));
                    this.methodVisitor = methodVisitor;
                    this.implementationContext = implementationContext;
                    this.argumentHandler = argumentHandler;
                    this.methodSizeHandler = methodSizeHandler;
                    this.stackMapFrameHandler = stackMapFrameHandler;
                    this.instrumentedType = instrumentedType;
                    this.instrumentedMethod = instrumentedMethod;
                    this.assigner = assigner;
                    this.adviceMethod = adviceMethod;
                    this.offsetMappings = offsetMappings;
                    this.suppressionHandler = suppressionHandler;
                    this.relocationHandler = relocationHandler;
                    this.exceptionHandler = exceptionHandler;
                    this.postProcessor = postProcessor;
                    this.exit = exit;
                    endOfMethod = new Label();
                }

                /**
                 * Propagates a label for an exception handler that is typically suppressed by the overlaying
                 * {@link Resolved.AdviceMethodInliner.ExceptionTableSubstitutor}.
                 *
                 * @param label The label to register as a target for an exception handler.
                 */
                protected void propagateHandler(Label label) {
                    ((StackAwareMethodVisitor) mv).register(label, Collections.singletonList(StackSize.SINGLE));
                }

                @Override
                public void visitParameter(String name, int modifiers) {
                    /* do nothing */
                }

                @Override
                public void visitAnnotableParameterCount(int count, boolean visible) {
                    /* do nothing */
                }

                @Override
                public AnnotationVisitor visitAnnotationDefault() {
                    return IGNORE_ANNOTATION;
                }

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    return IGNORE_ANNOTATION;
                }

                @Override
                public AnnotationVisitor visitTypeAnnotation(int typeReference, TypePath typePath, String descriptor, boolean visible) {
                    return IGNORE_ANNOTATION;
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int index, String descriptor, boolean visible) {
                    return IGNORE_ANNOTATION;
                }

                @Override
                public void visitAttribute(Attribute attribute) {
                    /* do nothing */
                }

                @Override
                public void visitCode() {
                    suppressionHandler.onStart(methodVisitor);
                }

                @Override
                public void visitFrame(int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
                    stackMapFrameHandler.translateFrame(methodVisitor, type, localVariableLength, localVariable, stackSize, stack);
                }

                @Override
                public void visitVarInsn(int opcode, int offset) {
                    OffsetMapping.Target target = offsetMappings.get(offset);
                    if (target != null) {
                        StackManipulation stackManipulation;
                        StackSize expectedGrowth;
                        switch (opcode) {
                            case Opcodes.ILOAD:
                            case Opcodes.FLOAD:
                            case Opcodes.ALOAD:
                                stackManipulation = target.resolveRead();
                                expectedGrowth = StackSize.SINGLE;
                                break;
                            case Opcodes.DLOAD:
                            case Opcodes.LLOAD:
                                stackManipulation = target.resolveRead();
                                expectedGrowth = StackSize.DOUBLE;
                                break;
                            case Opcodes.ISTORE:
                            case Opcodes.FSTORE:
                            case Opcodes.ASTORE:
                            case Opcodes.LSTORE:
                            case Opcodes.DSTORE:
                                stackManipulation = target.resolveWrite();
                                expectedGrowth = StackSize.ZERO;
                                break;
                            default:
                                throw new IllegalStateException("Unexpected opcode: " + opcode);
                        }
                        methodSizeHandler.requireStackSizePadding(stackManipulation.apply(mv, implementationContext).getMaximalSize() - expectedGrowth.getSize());
                    } else {
                        mv.visitVarInsn(opcode, argumentHandler.mapped(offset));
                    }
                }

                @Override
                public void visitIincInsn(int offset, int value) {
                    OffsetMapping.Target target = offsetMappings.get(offset);
                    if (target != null) {
                        methodSizeHandler.requireStackSizePadding(target.resolveIncrement(value).apply(mv, implementationContext).getMaximalSize());
                    } else {
                        mv.visitIincInsn(argumentHandler.mapped(offset), value);
                    }
                }

                @Override
                public void visitInsn(int opcode) {
                    switch (opcode) {
                        case Opcodes.RETURN:
                            ((StackAwareMethodVisitor) mv).drainStack();
                            break;
                        case Opcodes.IRETURN:
                            methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.ISTORE, Opcodes.ILOAD, StackSize.SINGLE));
                            break;
                        case Opcodes.ARETURN:
                            methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.ASTORE, Opcodes.ALOAD, StackSize.SINGLE));
                            break;
                        case Opcodes.FRETURN:
                            methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.FSTORE, Opcodes.FLOAD, StackSize.SINGLE));
                            break;
                        case Opcodes.LRETURN:
                            methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.LSTORE, Opcodes.LLOAD, StackSize.DOUBLE));
                            break;
                        case Opcodes.DRETURN:
                            methodSizeHandler.requireLocalVariableLength(((StackAwareMethodVisitor) mv).drainStack(Opcodes.DSTORE, Opcodes.DLOAD, StackSize.DOUBLE));
                            break;
                        default:
                            mv.visitInsn(opcode);
                            return;
                    }
                    mv.visitJumpInsn(Opcodes.GOTO, endOfMethod);
                }

                @Override
                public void visitEnd() {
                    suppressionHandler.onEnd(methodVisitor, implementationContext, methodSizeHandler, stackMapFrameHandler, adviceMethod.getReturnType());
                    methodVisitor.visitLabel(endOfMethod);
                    if (adviceMethod.getReturnType().represents(boolean.class)
                        || adviceMethod.getReturnType().represents(byte.class)
                        || adviceMethod.getReturnType().represents(short.class)
                        || adviceMethod.getReturnType().represents(char.class)
                        || adviceMethod.getReturnType().represents(int.class)) {
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                        methodVisitor.visitVarInsn(Opcodes.ISTORE, exit ? argumentHandler.exit() : argumentHandler.enter());
                    } else if (adviceMethod.getReturnType().represents(long.class)) {
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                        methodVisitor.visitVarInsn(Opcodes.LSTORE, exit ? argumentHandler.exit() : argumentHandler.enter());
                    } else if (adviceMethod.getReturnType().represents(float.class)) {
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                        methodVisitor.visitVarInsn(Opcodes.FSTORE, exit ? argumentHandler.exit() : argumentHandler.enter());
                    } else if (adviceMethod.getReturnType().represents(double.class)) {
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                        methodVisitor.visitVarInsn(Opcodes.DSTORE, exit ? argumentHandler.exit() : argumentHandler.enter());
                    } else if (!adviceMethod.getReturnType().represents(void.class)) {
                        stackMapFrameHandler.injectReturnFrame(methodVisitor);
                        methodVisitor.visitVarInsn(Opcodes.ASTORE, exit ? argumentHandler.exit() : argumentHandler.enter());
                    }
                    methodSizeHandler.requireStackSize(postProcessor
                        .resolve(instrumentedType, instrumentedMethod, assigner, argumentHandler, stackMapFrameHandler, exceptionHandler)
                        .apply(methodVisitor, implementationContext).getMaximalSize());
                    methodSizeHandler.requireStackSize(relocationHandler.apply(methodVisitor, exit ? argumentHandler.exit() : argumentHandler.enter()));
                    stackMapFrameHandler.injectCompletionFrame(methodVisitor);
                }

                @Override
                public void visitMaxs(int stackSize, int localVariableLength) {
                    methodSizeHandler.recordMaxima(stackSize, localVariableLength);
                }
            }
        }

        /**
         * A dispatcher for an advice method that is being invoked from the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class Delegating implements Unresolved {

            /**
             * The advice method.
             */
            protected final MethodDescription.InDefinedShape adviceMethod;

            /**
             * The delegator to use.
             */
            protected final Delegator delegator;

            /**
             * Creates a new delegating advice dispatcher.
             *
             * @param adviceMethod The advice method.
             * @param delegator    The delegator to use.
             */
            protected Delegating(MethodDescription.InDefinedShape adviceMethod, Delegator delegator) {
                this.adviceMethod = adviceMethod;
                this.delegator = delegator;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isAlive() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isBinary() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            public TypeDescription getAdviceType() {
                return adviceMethod.getReturnType().asErasure();
            }

            /**
             * {@inheritDoc}
             */
            public Map<String, TypeDefinition> getNamedTypes() {
                return Collections.emptyMap();
            }

            /**
             * {@inheritDoc}
             */
            public Dispatcher.Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                    ClassReader classReader,
                                                                    Unresolved methodExit,
                                                                    PostProcessor.Factory postProcessorFactory) {
                return Resolved.ForMethodEnter.of(adviceMethod, postProcessorFactory.make(adviceMethod, false), delegator, userFactories, methodExit.getAdviceType(), methodExit.isAlive());
            }

            /**
             * {@inheritDoc}
             */
            public Dispatcher.Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                  ClassReader classReader,
                                                                  Unresolved methodEnter,
                                                                  PostProcessor.Factory postProcessorFactory) {
                Map<String, TypeDefinition> namedTypes = methodEnter.getNamedTypes();
                for (ParameterDescription parameterDescription : adviceMethod.getParameters().filter(isAnnotatedWith(Local.class))) {
                    String name = parameterDescription.getDeclaredAnnotations()
                        .ofType(Local.class).getValue(OffsetMapping.ForLocalValue.Factory.LOCAL_VALUE)
                        .resolve(String.class);
                    TypeDefinition typeDefinition = namedTypes.get(name);
                    if (typeDefinition == null) {
                        throw new IllegalStateException(adviceMethod + " attempts use of undeclared local variable " + name);
                    } else if (!typeDefinition.equals(parameterDescription.getType())) {
                        throw new IllegalStateException(adviceMethod + " does not read variable " + name + " as " + typeDefinition);
                    }
                }
                return Resolved.ForMethodExit.of(adviceMethod, postProcessorFactory.make(adviceMethod, true), delegator, namedTypes, userFactories, methodEnter.getAdviceType());
            }

            /**
             * A resolved version of a dispatcher.
             */
            protected abstract static class Resolved extends Dispatcher.Resolved.AbstractBase {

                /**
                 * The delegator to use.
                 */
                protected final Delegator delegator;

                /**
                 * Creates a new resolved version of a dispatcher.
                 *
                 * @param adviceMethod    The represented advice method.
                 * @param postProcessor   The post processor to apply.
                 * @param factories       A list of factories to resolve for the parameters of the advice method.
                 * @param throwableType   The type to handle by a suppression handler or {@link NoExceptionHandler} to not handle any exceptions.
                 * @param relocatableType The type to trigger a relocation of the method's control flow or {@code void} if no relocation should be executed.
                 * @param delegator       The delegator to use.
                 */
                protected Resolved(MethodDescription.InDefinedShape adviceMethod,
                                   PostProcessor postProcessor,
                                   List<? extends OffsetMapping.Factory<?>> factories,
                                   TypeDescription throwableType,
                                   TypeDescription relocatableType,
                                   Delegator delegator) {
                    super(adviceMethod, postProcessor, factories, throwableType, relocatableType, OffsetMapping.Factory.AdviceType.DELEGATION);
                    this.delegator = delegator;
                }

                /**
                 * {@inheritDoc}
                 */
                public Map<String, TypeDefinition> getNamedTypes() {
                    return Collections.emptyMap();
                }

                /**
                 * {@inheritDoc}
                 */
                public Bound bind(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  MethodVisitor methodVisitor,
                                  Implementation.Context implementationContext,
                                  Assigner assigner,
                                  ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                  MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                  StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                  StackManipulation exceptionHandler,
                                  RelocationHandler.Relocation relocation) {
                    if (!adviceMethod.isVisibleTo(instrumentedType)) {
                        throw new IllegalStateException(adviceMethod + " is not visible to " + instrumentedMethod.getDeclaringType());
                    }
                    return resolve(instrumentedType,
                        instrumentedMethod,
                        methodVisitor,
                        implementationContext,
                        assigner,
                        argumentHandler,
                        methodSizeHandler,
                        stackMapFrameHandler,
                        exceptionHandler,
                        relocation);
                }

                /**
                 * Binds this dispatcher for resolution to a specific method.
                 *
                 * @param instrumentedType      A description of the instrumented type.
                 * @param instrumentedMethod    The instrumented method that is being bound.
                 * @param methodVisitor         The method visitor for writing to the instrumented method.
                 * @param implementationContext The implementation context to use.
                 * @param assigner              The assigner to use.
                 * @param argumentHandler       A handler for accessing values on the local variable array.
                 * @param methodSizeHandler     A handler for computing the method size requirements.
                 * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                 * @param exceptionHandler      The stack manipulation to apply within a suppression handler.
                 * @param relocation            A relocation to use with a relocation handler.
                 * @return An appropriate bound advice dispatcher.
                 */
                protected abstract Bound resolve(TypeDescription instrumentedType,
                                                 MethodDescription instrumentedMethod,
                                                 MethodVisitor methodVisitor,
                                                 Implementation.Context implementationContext,
                                                 Assigner assigner,
                                                 ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                                 MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                                 StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                                 StackManipulation exceptionHandler,
                                                 RelocationHandler.Relocation relocation);

                /**
                 * A bound advice method that copies the code by first extracting the exception table and later appending the
                 * code of the method without copying any meta data.
                 */
                protected abstract static class AdviceMethodWriter implements Bound {

                    /**
                     * The advice method.
                     */
                    protected final MethodDescription.InDefinedShape adviceMethod;

                    /**
                     * The instrumented type.
                     */
                    private final TypeDescription instrumentedType;

                    /**
                     * The instrumented method.
                     */
                    private final MethodDescription instrumentedMethod;

                    /**
                     * The assigner to use.
                     */
                    private final Assigner assigner;

                    /**
                     * The offset mappings available to this advice.
                     */
                    private final List<OffsetMapping.Target> offsetMappings;

                    /**
                     * The method visitor for writing the instrumented method.
                     */
                    protected final MethodVisitor methodVisitor;

                    /**
                     * The implementation context to use.
                     */
                    protected final Implementation.Context implementationContext;

                    /**
                     * A handler for accessing values on the local variable array.
                     */
                    protected final ArgumentHandler.ForAdvice argumentHandler;

                    /**
                     * A handler for computing the method size requirements.
                     */
                    protected final MethodSizeHandler.ForAdvice methodSizeHandler;

                    /**
                     * A handler for translating and injecting stack map frames.
                     */
                    protected final StackMapFrameHandler.ForAdvice stackMapFrameHandler;

                    /**
                     * A bound suppression handler that is used for suppressing exceptions of this advice method.
                     */
                    private final SuppressionHandler.Bound suppressionHandler;

                    /**
                     * A bound relocation handler that is responsible for considering a non-standard control flow.
                     */
                    private final RelocationHandler.Bound relocationHandler;

                    /**
                     * The exception handler that is resolved for the instrumented method.
                     */
                    private final StackManipulation exceptionHandler;

                    /**
                     * The post processor to apply.
                     */
                    private final PostProcessor postProcessor;

                    /**
                     * The delegator to use.
                     */
                    private final Delegator delegator;

                    /**
                     * Creates a new advice method writer.
                     *
                     * @param adviceMethod          The advice method.
                     * @param instrumentedType      The instrumented type.
                     * @param instrumentedMethod    The instrumented method.
                     * @param assigner              The assigner to use.
                     * @param postProcessor         The post processor to apply.
                     * @param offsetMappings        The offset mappings available to this advice.
                     * @param methodVisitor         The method visitor for writing the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                     * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @param delegator             The delegator to use.
                     */
                    protected AdviceMethodWriter(MethodDescription.InDefinedShape adviceMethod,
                                                 TypeDescription instrumentedType,
                                                 MethodDescription instrumentedMethod,
                                                 Assigner assigner,
                                                 PostProcessor postProcessor,
                                                 List<OffsetMapping.Target> offsetMappings,
                                                 MethodVisitor methodVisitor,
                                                 Context implementationContext,
                                                 ArgumentHandler.ForAdvice argumentHandler,
                                                 MethodSizeHandler.ForAdvice methodSizeHandler,
                                                 StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                 SuppressionHandler.Bound suppressionHandler,
                                                 RelocationHandler.Bound relocationHandler,
                                                 StackManipulation exceptionHandler,
                                                 Delegator delegator) {
                        this.adviceMethod = adviceMethod;
                        this.instrumentedType = instrumentedType;
                        this.instrumentedMethod = instrumentedMethod;
                        this.assigner = assigner;
                        this.postProcessor = postProcessor;
                        this.offsetMappings = offsetMappings;
                        this.methodVisitor = methodVisitor;
                        this.implementationContext = implementationContext;
                        this.argumentHandler = argumentHandler;
                        this.methodSizeHandler = methodSizeHandler;
                        this.stackMapFrameHandler = stackMapFrameHandler;
                        this.suppressionHandler = suppressionHandler;
                        this.relocationHandler = relocationHandler;
                        this.exceptionHandler = exceptionHandler;
                        this.delegator = delegator;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void prepare() {
                        suppressionHandler.onPrepare(methodVisitor);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void apply() {
                        suppressionHandler.onStart(methodVisitor);
                        int index = 0, currentStackSize = 0, maximumStackSize = 0;
                        for (OffsetMapping.Target offsetMapping : offsetMappings) {
                            currentStackSize += adviceMethod.getParameters().get(index++).getType().getStackSize().getSize();
                            maximumStackSize = Math.max(maximumStackSize, currentStackSize + offsetMapping.resolveRead()
                                .apply(methodVisitor, implementationContext)
                                .getMaximalSize());
                        }
                        delegator.apply(methodVisitor, adviceMethod, instrumentedType, instrumentedMethod, isExitAdvice());
                        suppressionHandler.onEndWithSkip(methodVisitor,
                            implementationContext,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            adviceMethod.getReturnType());
                        if (adviceMethod.getReturnType().represents(boolean.class)
                            || adviceMethod.getReturnType().represents(byte.class)
                            || adviceMethod.getReturnType().represents(short.class)
                            || adviceMethod.getReturnType().represents(char.class)
                            || adviceMethod.getReturnType().represents(int.class)) {
                            methodVisitor.visitVarInsn(Opcodes.ISTORE, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter());
                        } else if (adviceMethod.getReturnType().represents(long.class)) {
                            methodVisitor.visitVarInsn(Opcodes.LSTORE, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter());
                        } else if (adviceMethod.getReturnType().represents(float.class)) {
                            methodVisitor.visitVarInsn(Opcodes.FSTORE, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter());
                        } else if (adviceMethod.getReturnType().represents(double.class)) {
                            methodVisitor.visitVarInsn(Opcodes.DSTORE, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter());
                        } else if (!adviceMethod.getReturnType().represents(void.class)) {
                            methodVisitor.visitVarInsn(Opcodes.ASTORE, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter());
                        }
                        methodSizeHandler.requireStackSize(postProcessor.resolve(instrumentedType,
                            instrumentedMethod, assigner, argumentHandler, stackMapFrameHandler, exceptionHandler)
                            .apply(methodVisitor, implementationContext).getMaximalSize());
                        methodSizeHandler.requireStackSize(relocationHandler.apply(methodVisitor, isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter()));
                        stackMapFrameHandler.injectCompletionFrame(methodVisitor);
                        methodSizeHandler.requireStackSize(Math.max(maximumStackSize, adviceMethod.getReturnType().getStackSize().getSize()));
                        methodSizeHandler.requireLocalVariableLength(instrumentedMethod.getStackSize() + adviceMethod.getReturnType().getStackSize().getSize());
                    }

                    /**
                     * Returns {@code true} if this writer represents exit advice.
                     *
                     * @return {@code true} if this writer represents exit advice.
                     */
                    protected abstract boolean isExitAdvice();

                    /**
                     * An advice method writer for a method enter.
                     */
                    protected static class ForMethodEnter extends AdviceMethodWriter {

                        /**
                         * Creates a new advice method writer.
                         *
                         * @param adviceMethod          The advice method.
                         * @param instrumentedType      The instrumented type.
                         * @param instrumentedMethod    The instrumented method.
                         * @param assigner              The assigner to use.
                         * @param postProcessor         The post processor to apply.
                         * @param offsetMappings        The offset mappings available to this advice.
                         * @param methodVisitor         The method visitor for writing the instrumented method.
                         * @param implementationContext The implementation context to use.
                         * @param argumentHandler       A handler for accessing values on the local variable array.
                         * @param methodSizeHandler     A handler for computing the method size requirements.
                         * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                         * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                         * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                         * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                         * @param delegator             The delegator to use.
                         */
                        protected ForMethodEnter(MethodDescription.InDefinedShape adviceMethod,
                                                 TypeDescription instrumentedType,
                                                 MethodDescription instrumentedMethod,
                                                 Assigner assigner,
                                                 PostProcessor postProcessor,
                                                 List<OffsetMapping.Target> offsetMappings,
                                                 MethodVisitor methodVisitor,
                                                 Implementation.Context implementationContext,
                                                 ArgumentHandler.ForAdvice argumentHandler,
                                                 MethodSizeHandler.ForAdvice methodSizeHandler,
                                                 StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                 SuppressionHandler.Bound suppressionHandler,
                                                 RelocationHandler.Bound relocationHandler,
                                                 StackManipulation exceptionHandler,
                                                 Delegator delegator) {
                            super(adviceMethod,
                                instrumentedType,
                                instrumentedMethod,
                                assigner,
                                postProcessor,
                                offsetMappings,
                                methodVisitor,
                                implementationContext,
                                argumentHandler,
                                methodSizeHandler,
                                stackMapFrameHandler,
                                suppressionHandler,
                                relocationHandler,
                                exceptionHandler,
                                delegator);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public void initialize() {
                            /* do nothing */
                        }

                        @Override
                        protected boolean isExitAdvice() {
                            return false;
                        }
                    }

                    /**
                     * An advice method writer for a method exit.
                     */
                    protected static class ForMethodExit extends AdviceMethodWriter {

                        /**
                         * Creates a new advice method writer.
                         *
                         * @param adviceMethod          The advice method.
                         * @param instrumentedType      The instrumented type.
                         * @param instrumentedMethod    The instrumented method.
                         * @param assigner              The assigner to use.
                         * @param postProcessor         The post processor to apply.
                         * @param offsetMappings        The offset mappings available to this advice.
                         * @param methodVisitor         The method visitor for writing the instrumented method.
                         * @param implementationContext The implementation context to use.
                         * @param argumentHandler       A handler for accessing values on the local variable array.
                         * @param methodSizeHandler     A handler for computing the method size requirements.
                         * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                         * @param suppressionHandler    A bound suppression handler that is used for suppressing exceptions of this advice method.
                         * @param relocationHandler     A bound relocation handler that is responsible for considering a non-standard control flow.
                         * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                         * @param delegator             The delegator to use.
                         */
                        protected ForMethodExit(MethodDescription.InDefinedShape adviceMethod,
                                                TypeDescription instrumentedType,
                                                MethodDescription instrumentedMethod,
                                                Assigner assigner,
                                                PostProcessor postProcessor,
                                                List<OffsetMapping.Target> offsetMappings,
                                                MethodVisitor methodVisitor,
                                                Implementation.Context implementationContext,
                                                ArgumentHandler.ForAdvice argumentHandler,
                                                MethodSizeHandler.ForAdvice methodSizeHandler,
                                                StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                SuppressionHandler.Bound suppressionHandler,
                                                RelocationHandler.Bound relocationHandler,
                                                StackManipulation exceptionHandler,
                                                Delegator delegator) {
                            super(adviceMethod,
                                instrumentedType,
                                instrumentedMethod,
                                assigner,
                                postProcessor,
                                offsetMappings,
                                methodVisitor,
                                implementationContext,
                                argumentHandler,
                                methodSizeHandler,
                                stackMapFrameHandler,
                                suppressionHandler,
                                relocationHandler,
                                exceptionHandler,
                                delegator);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public void initialize() {
                            if (adviceMethod.getReturnType().represents(boolean.class)
                                || adviceMethod.getReturnType().represents(byte.class)
                                || adviceMethod.getReturnType().represents(short.class)
                                || adviceMethod.getReturnType().represents(char.class)
                                || adviceMethod.getReturnType().represents(int.class)) {
                                methodVisitor.visitInsn(Opcodes.ICONST_0);
                                methodVisitor.visitVarInsn(Opcodes.ISTORE, argumentHandler.exit());
                            } else if (adviceMethod.getReturnType().represents(long.class)) {
                                methodVisitor.visitInsn(Opcodes.LCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.LSTORE, argumentHandler.exit());
                            } else if (adviceMethod.getReturnType().represents(float.class)) {
                                methodVisitor.visitInsn(Opcodes.FCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.FSTORE, argumentHandler.exit());
                            } else if (adviceMethod.getReturnType().represents(double.class)) {
                                methodVisitor.visitInsn(Opcodes.DCONST_0);
                                methodVisitor.visitVarInsn(Opcodes.DSTORE, argumentHandler.exit());
                            } else if (!adviceMethod.getReturnType().represents(void.class)) {
                                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
                                methodVisitor.visitVarInsn(Opcodes.ASTORE, argumentHandler.exit());
                            }
                            methodSizeHandler.requireStackSize(adviceMethod.getReturnType().getStackSize().getSize());
                        }

                        @Override
                        protected boolean isExitAdvice() {
                            return true;
                        }
                    }
                }

                /**
                 * A resolved dispatcher for implementing method enter advice.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected abstract static class ForMethodEnter extends Delegating.Resolved implements Dispatcher.Resolved.ForMethodEnter {

                    /**
                     * {@code true} if the first discovered line number information should be prepended to the advice code.
                     */
                    private final boolean prependLineNumber;

                    /**
                     * Creates a new resolved dispatcher for implementing method enter advice.
                     *
                     * @param adviceMethod  The represented advice method.
                     * @param postProcessor The post processor to apply.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param exitType      The exit type or {@code void} if no exit type is defined.
                     * @param delegator     The delegator to use.
                     */
                    protected ForMethodEnter(MethodDescription.InDefinedShape adviceMethod,
                                             PostProcessor postProcessor,
                                             List<? extends OffsetMapping.Factory<?>> userFactories,
                                             TypeDefinition exitType,
                                             Delegator delegator) {
                        super(adviceMethod,
                            postProcessor,
                            CompoundList.of(Arrays.asList(OffsetMapping.ForArgument.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForAllArguments.Factory.INSTANCE,
                                OffsetMapping.ForThisReference.Factory.INSTANCE,
                                OffsetMapping.ForField.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForOrigin.Factory.INSTANCE,
                                OffsetMapping.ForUnusedValue.Factory.INSTANCE,
                                OffsetMapping.ForStubValue.INSTANCE,
                                OffsetMapping.ForExitValue.Factory.of(exitType),
                                new OffsetMapping.Factory.Illegal<>(Thrown.class),
                                new OffsetMapping.Factory.Illegal<>(Enter.class),
                                new OffsetMapping.Factory.Illegal<>(Local.class),
                                new OffsetMapping.Factory.Illegal<>(Return.class)), userFactories),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(SUPPRESS_ENTER).resolve(TypeDescription.class),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(SKIP_ON).resolve(TypeDescription.class),
                            delegator);
                        prependLineNumber = adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(PREPEND_LINE_NUMBER).resolve(Boolean.class);
                    }

                    /**
                     * Resolves enter advice that only exposes the enter type if this is necessary.
                     *
                     * @param adviceMethod  The advice method.
                     * @param postProcessor The post processor to apply.
                     * @param delegator     The delegator to use.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param exitType      The exit type or {@code void} if no exit type is defined.
                     * @param methodExit    {@code true} if exit advice is applied.
                     * @return An appropriate enter handler.
                     */
                    protected static Resolved.ForMethodEnter of(MethodDescription.InDefinedShape adviceMethod,
                                                                PostProcessor postProcessor,
                                                                Delegator delegator,
                                                                List<? extends OffsetMapping.Factory<?>> userFactories,
                                                                TypeDefinition exitType,
                                                                boolean methodExit) {
                        return methodExit
                            ? new WithRetainedEnterType(adviceMethod, postProcessor, userFactories, exitType, delegator)
                            : new WithDiscardedEnterType(adviceMethod, postProcessor, userFactories, exitType, delegator);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public boolean isPrependLineNumber() {
                        return prependLineNumber;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public TypeDefinition getActualAdviceType() {
                        return adviceMethod.getReturnType();
                    }

                    @Override
                    protected Bound resolve(TypeDescription instrumentedType,
                                            MethodDescription instrumentedMethod,
                                            MethodVisitor methodVisitor,
                                            Implementation.Context implementationContext,
                                            Assigner assigner,
                                            ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                            MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                            StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                            StackManipulation exceptionHandler,
                                            RelocationHandler.Relocation relocation) {
                        return doResolve(instrumentedType,
                            instrumentedMethod,
                            methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler.bindEnter(adviceMethod),
                            methodSizeHandler.bindEnter(adviceMethod),
                            stackMapFrameHandler.bindEnter(adviceMethod),
                            suppressionHandler.bind(exceptionHandler),
                            relocationHandler.bind(instrumentedMethod, relocation),
                            exceptionHandler);
                    }

                    /**
                     * Binds this dispatcher for resolution to a specific method.
                     *
                     * @param instrumentedType      A description of the instrumented type.
                     * @param instrumentedMethod    The instrumented method that is being bound.
                     * @param methodVisitor         The method visitor for writing to the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param assigner              The assigner to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    The bound suppression handler to use.
                     * @param relocationHandler     The bound relocation handler to use.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @return An appropriate bound advice dispatcher.
                     */
                    protected Bound doResolve(TypeDescription instrumentedType,
                                              MethodDescription instrumentedMethod,
                                              MethodVisitor methodVisitor,
                                              Implementation.Context implementationContext,
                                              Assigner assigner,
                                              ArgumentHandler.ForAdvice argumentHandler,
                                              MethodSizeHandler.ForAdvice methodSizeHandler,
                                              StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                              SuppressionHandler.Bound suppressionHandler,
                                              RelocationHandler.Bound relocationHandler,
                                              StackManipulation exceptionHandler) {
                        List<OffsetMapping.Target> offsetMappings = new ArrayList<>(this.offsetMappings.size());
                        for (OffsetMapping offsetMapping : this.offsetMappings.values()) {
                            offsetMappings.add(offsetMapping.resolve(instrumentedType,
                                instrumentedMethod,
                                assigner,
                                argumentHandler,
                                OffsetMapping.Sort.ENTER));
                        }
                        return new AdviceMethodWriter.ForMethodEnter(adviceMethod,
                            instrumentedType,
                            instrumentedMethod,
                            assigner,
                            postProcessor,
                            offsetMappings,
                            methodVisitor,
                            implementationContext,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler,
                            delegator);
                    }

                    /**
                     * Implementation of an advice that does expose an enter type.
                     */
                    protected static class WithRetainedEnterType extends Delegating.Resolved.ForMethodEnter {

                        /**
                         * Creates a new resolved dispatcher for implementing method enter advice that does expose the enter type.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor to apply.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param exitType      The exit type or {@code void} if no exit type is defined.
                         * @param delegator     The delegator to use.
                         */
                        protected WithRetainedEnterType(MethodDescription.InDefinedShape adviceMethod,
                                                        PostProcessor postProcessor,
                                                        List<? extends OffsetMapping.Factory<?>> userFactories,
                                                        TypeDefinition exitType,
                                                        Delegator delegator) {
                            super(adviceMethod, postProcessor, userFactories, exitType, delegator);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDefinition getAdviceType() {
                            return adviceMethod.getReturnType();
                        }
                    }

                    /**
                     * Implementation of an advice that does not expose an enter type.
                     */
                    protected static class WithDiscardedEnterType extends Delegating.Resolved.ForMethodEnter {

                        /**
                         * Creates a new resolved dispatcher for implementing method enter advice that does not expose the enter type.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor to apply.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param exitType      The exit type or {@code void} if no exit type is defined.
                         * @param delegator     The delegator to use.
                         */
                        protected WithDiscardedEnterType(MethodDescription.InDefinedShape adviceMethod,
                                                         PostProcessor postProcessor,
                                                         List<? extends OffsetMapping.Factory<?>> userFactories,
                                                         TypeDefinition exitType,
                                                         Delegator delegator) {
                            super(adviceMethod, postProcessor, userFactories, exitType, delegator);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDefinition getAdviceType() {
                            return TypeDescription.VOID;
                        }

                        /**
                         * {@inheritDoc}
                         */
                        protected Bound doResolve(TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  MethodVisitor methodVisitor,
                                                  Context implementationContext,
                                                  Assigner assigner,
                                                  ArgumentHandler.ForAdvice argumentHandler,
                                                  MethodSizeHandler.ForAdvice methodSizeHandler,
                                                  StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                                  SuppressionHandler.Bound suppressionHandler,
                                                  RelocationHandler.Bound relocationHandler,
                                                  StackManipulation exceptionHandler) {
                            methodSizeHandler.requireLocalVariableLengthPadding(adviceMethod.getReturnType().getStackSize().getSize());
                            return super.doResolve(instrumentedType,
                                instrumentedMethod,
                                methodVisitor,
                                implementationContext,
                                assigner,
                                argumentHandler,
                                methodSizeHandler,
                                stackMapFrameHandler,
                                suppressionHandler,
                                relocationHandler,
                                exceptionHandler);
                        }
                    }
                }

                /**
                 * A resolved dispatcher for implementing method exit advice.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected abstract static class ForMethodExit extends Delegating.Resolved implements Dispatcher.Resolved.ForMethodExit {

                    /**
                     * {@code true} if the arguments of the instrumented method should be copied prior to execution.
                     */
                    private final boolean backupArguments;

                    /**
                     * Creates a new resolved dispatcher for implementing method exit advice.
                     *
                     * @param adviceMethod  The represented advice method.
                     * @param postProcessor The post processor to apply.
                     * @param namedTypes    A mapping of all available local variables by their name to their type.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param enterType     The type of the value supplied by the enter advice method or {@code void} if no such value exists.
                     * @param delegator     The delegator to use.
                     */
                    protected ForMethodExit(MethodDescription.InDefinedShape adviceMethod,
                                            PostProcessor postProcessor,
                                            Map<String, TypeDefinition> namedTypes,
                                            List<? extends OffsetMapping.Factory<?>> userFactories,
                                            TypeDefinition enterType,
                                            Delegator delegator) {
                        super(adviceMethod,
                            postProcessor,
                            CompoundList.of(Arrays.asList(OffsetMapping.ForArgument.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForAllArguments.Factory.INSTANCE,
                                OffsetMapping.ForThisReference.Factory.INSTANCE,
                                OffsetMapping.ForField.Unresolved.Factory.INSTANCE,
                                OffsetMapping.ForOrigin.Factory.INSTANCE,
                                OffsetMapping.ForUnusedValue.Factory.INSTANCE,
                                OffsetMapping.ForStubValue.INSTANCE,
                                OffsetMapping.ForEnterValue.Factory.of(enterType),
                                OffsetMapping.ForExitValue.Factory.of(adviceMethod.getReturnType()),
                                new OffsetMapping.ForLocalValue.Factory(namedTypes),
                                OffsetMapping.ForReturnValue.Factory.INSTANCE,
                                OffsetMapping.ForThrowable.Factory.of(adviceMethod)
                            ), userFactories),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(SUPPRESS_EXIT).resolve(TypeDescription.class),
                            adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(REPEAT_ON).resolve(TypeDescription.class),
                            delegator);
                        backupArguments = adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(BACKUP_ARGUMENTS).resolve(Boolean.class);
                    }

                    /**
                     * Resolves exit advice that handles exceptions depending on the specification of the exit advice.
                     *
                     * @param adviceMethod  The advice method.
                     * @param postProcessor The post processor to apply.
                     * @param delegator     The delegator to use.
                     * @param namedTypes    A mapping of all available local variables by their name to their type.
                     * @param userFactories A list of user-defined factories for offset mappings.
                     * @param enterType     The type of the value supplied by the enter advice method or {@code void} if no such value exists.
                     * @return An appropriate exit handler.
                     */
                    protected static Resolved.ForMethodExit of(MethodDescription.InDefinedShape adviceMethod,
                                                               PostProcessor postProcessor,
                                                               Delegator delegator,
                                                               Map<String, TypeDefinition> namedTypes,
                                                               List<? extends OffsetMapping.Factory<?>> userFactories,
                                                               TypeDefinition enterType) {
                        TypeDescription throwable = adviceMethod.getDeclaredAnnotations()
                            .ofType(OnMethodExit.class)
                            .getValue(ON_THROWABLE)
                            .resolve(TypeDescription.class);
                        return isNoExceptionHandler(throwable)
                            ? new WithoutExceptionHandler(adviceMethod, postProcessor, namedTypes, userFactories, enterType, delegator)
                            : new WithExceptionHandler(adviceMethod, postProcessor, namedTypes, userFactories, enterType, throwable, delegator);
                    }

                    @Override
                    protected Bound resolve(TypeDescription instrumentedType,
                                            MethodDescription instrumentedMethod,
                                            MethodVisitor methodVisitor,
                                            Implementation.Context implementationContext,
                                            Assigner assigner,
                                            ArgumentHandler.ForInstrumentedMethod argumentHandler,
                                            MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
                                            StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
                                            StackManipulation exceptionHandler,
                                            RelocationHandler.Relocation relocation) {
                        return doResolve(instrumentedType,
                            instrumentedMethod,
                            methodVisitor,
                            implementationContext,
                            assigner,
                            argumentHandler.bindExit(adviceMethod, isNoExceptionHandler(getThrowable())),
                            methodSizeHandler.bindExit(adviceMethod),
                            stackMapFrameHandler.bindExit(adviceMethod),
                            suppressionHandler.bind(exceptionHandler),
                            relocationHandler.bind(instrumentedMethod, relocation),
                            exceptionHandler);
                    }

                    /**
                     * Binds this dispatcher for resolution to a specific method.
                     *
                     * @param instrumentedType      A description of the instrumented type.
                     * @param instrumentedMethod    The instrumented method that is being bound.
                     * @param methodVisitor         The method visitor for writing to the instrumented method.
                     * @param implementationContext The implementation context to use.
                     * @param assigner              The assigner to use.
                     * @param argumentHandler       A handler for accessing values on the local variable array.
                     * @param methodSizeHandler     A handler for computing the method size requirements.
                     * @param stackMapFrameHandler  A handler for translating and injecting stack map frames.
                     * @param suppressionHandler    The bound suppression handler to use.
                     * @param relocationHandler     The bound relocation handler to use.
                     * @param exceptionHandler      The exception handler that is resolved for the instrumented method.
                     * @return An appropriate bound advice dispatcher.
                     */
                    private Bound doResolve(TypeDescription instrumentedType,
                                            MethodDescription instrumentedMethod,
                                            MethodVisitor methodVisitor,
                                            Implementation.Context implementationContext,
                                            Assigner assigner,
                                            ArgumentHandler.ForAdvice argumentHandler,
                                            MethodSizeHandler.ForAdvice methodSizeHandler,
                                            StackMapFrameHandler.ForAdvice stackMapFrameHandler,
                                            SuppressionHandler.Bound suppressionHandler,
                                            RelocationHandler.Bound relocationHandler,
                                            StackManipulation exceptionHandler) {
                        List<OffsetMapping.Target> offsetMappings = new ArrayList<>(this.offsetMappings.size());
                        for (OffsetMapping offsetMapping : this.offsetMappings.values()) {
                            offsetMappings.add(offsetMapping.resolve(instrumentedType,
                                instrumentedMethod,
                                assigner,
                                argumentHandler,
                                OffsetMapping.Sort.EXIT));
                        }
                        return new AdviceMethodWriter.ForMethodExit(adviceMethod,
                            instrumentedType,
                            instrumentedMethod,
                            assigner,
                            postProcessor,
                            offsetMappings,
                            methodVisitor,
                            implementationContext,
                            argumentHandler,
                            methodSizeHandler,
                            stackMapFrameHandler,
                            suppressionHandler,
                            relocationHandler,
                            exceptionHandler,
                            delegator);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public ArgumentHandler.Factory getArgumentHandlerFactory() {
                        return backupArguments
                            ? ArgumentHandler.Factory.COPYING
                            : ArgumentHandler.Factory.SIMPLE;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public TypeDefinition getAdviceType() {
                        return adviceMethod.getReturnType();
                    }

                    /**
                     * Implementation of exit advice that handles exceptions.
                     */
                    @HashCodeAndEqualsPlugin.Enhance
                    protected static class WithExceptionHandler extends Delegating.Resolved.ForMethodExit {

                        /**
                         * The type of the handled throwable type for which this advice is invoked.
                         */
                        private final TypeDescription throwable;

                        /**
                         * Creates a new resolved dispatcher for implementing method exit advice that handles exceptions.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor factory to apply.
                         * @param namedTypes    A mapping of all available local variables by their name to their type.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param enterType     The type of the value supplied by the enter advice method or
                         *                      a description of {@code void} if no such value exists.
                         * @param throwable     The type of the handled throwable type for which this advice is invoked.
                         * @param delegator     The delegator to use.
                         */
                        protected WithExceptionHandler(MethodDescription.InDefinedShape adviceMethod,
                                                       PostProcessor postProcessor,
                                                       Map<String, TypeDefinition> namedTypes,
                                                       List<? extends OffsetMapping.Factory<?>> userFactories,
                                                       TypeDefinition enterType,
                                                       TypeDescription throwable,
                                                       Delegator delegator) {
                            super(adviceMethod, postProcessor, namedTypes, userFactories, enterType, delegator);
                            this.throwable = throwable;
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDescription getThrowable() {
                            return throwable;
                        }
                    }

                    /**
                     * Implementation of exit advice that ignores exceptions.
                     */
                    protected static class WithoutExceptionHandler extends Delegating.Resolved.ForMethodExit {

                        /**
                         * Creates a new resolved dispatcher for implementing method exit advice that does not handle exceptions.
                         *
                         * @param adviceMethod  The represented advice method.
                         * @param postProcessor The post processor factory to apply.
                         * @param namedTypes    A mapping of all available local variables by their name to their type.
                         * @param userFactories A list of user-defined factories for offset mappings.
                         * @param enterType     The type of the value supplied by the enter advice method or
                         *                      a description of {@code void} if no such value exists.
                         * @param delegator     The delegator to use.
                         */
                        protected WithoutExceptionHandler(MethodDescription.InDefinedShape adviceMethod,
                                                          PostProcessor postProcessor,
                                                          Map<String, TypeDefinition> namedTypes,
                                                          List<? extends OffsetMapping.Factory<?>> userFactories,
                                                          TypeDefinition enterType,
                                                          Delegator delegator) {
                            super(adviceMethod, postProcessor, namedTypes, userFactories, enterType, delegator);
                        }

                        /**
                         * {@inheritDoc}
                         */
                        public TypeDescription getThrowable() {
                            return NoExceptionHandler.DESCRIPTION;
                        }
                    }
                }
            }
        }
    }


    /**
     * A handler for computing the instrumented method's size.
     */
    protected interface MethodSizeHandler {

        /**
         * Indicates that a size is not computed but handled directly by ASM.
         */
        int UNDEFINED_SIZE = Short.MAX_VALUE;

        /**
         * Records a minimum stack size required by the represented advice method.
         *
         * @param stackSize The minimum size required by the represented advice method.
         */
        void requireStackSize(int stackSize);

        /**
         * Requires a minimum length of the local variable array.
         *
         * @param localVariableLength The minimal required length of the local variable array.
         */
        void requireLocalVariableLength(int localVariableLength);

        /**
         * A method size handler for the instrumented method.
         */
        interface ForInstrumentedMethod extends MethodSizeHandler {

            /**
             * Binds a method size handler for the enter advice.
             *
             * @param adviceMethod The method representing the enter advice.
             * @return A method size handler for the enter advice.
             */
            ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod);

            /**
             * Binds the method size handler for the exit advice.
             *
             * @param adviceMethod The method representing the exit advice.
             * @return A method size handler for the exit advice.
             */
            ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod);

            /**
             * Computes a compound stack size for the advice and the translated instrumented method.
             *
             * @param stackSize The required stack size of the instrumented method before translation.
             * @return The stack size required by the instrumented method and its advice methods.
             */
            int compoundStackSize(int stackSize);

            /**
             * Computes a compound local variable array length for the advice and the translated instrumented method.
             *
             * @param localVariableLength The required local variable array length of the instrumented method before translation.
             * @return The local variable length required by the instrumented method and its advice methods.
             */
            int compoundLocalVariableLength(int localVariableLength);
        }

        /**
         * A method size handler for an advice method.
         */
        interface ForAdvice extends MethodSizeHandler {

            /**
             * Requires additional padding for the operand stack that is required for this advice's execution.
             *
             * @param stackSizePadding The required padding.
             */
            void requireStackSizePadding(int stackSizePadding);

            /**
             * Requires additional padding for the local variable array that is required for this advice's execution.
             *
             * @param localVariableLengthPadding The required padding.
             */
            void requireLocalVariableLengthPadding(int localVariableLengthPadding);

            /**
             * Records the maximum values for stack size and local variable array which are required by the advice method
             * for its individual execution without translation.
             *
             * @param stackSize           The minimum required stack size.
             * @param localVariableLength The minimum required length of the local variable array.
             */
            void recordMaxima(int stackSize, int localVariableLength);
        }

        /**
         * A non-operational method size handler.
         */
        enum NoOp implements ForInstrumentedMethod, ForAdvice {

            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * {@inheritDoc}
             */
            public ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public int compoundStackSize(int stackSize) {
                return UNDEFINED_SIZE;
            }

            /**
             * {@inheritDoc}
             */
            public int compoundLocalVariableLength(int localVariableLength) {
                return UNDEFINED_SIZE;
            }

            /**
             * {@inheritDoc}
             */
            public void requireStackSize(int stackSize) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void requireLocalVariableLength(int localVariableLength) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void requireStackSizePadding(int stackSizePadding) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void requireLocalVariableLengthPadding(int localVariableLengthPadding) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void recordMaxima(int stackSize, int localVariableLength) {
                /* do nothing */
            }
        }

        /**
         * A default implementation for a method size handler.
         */
        abstract class Default implements MethodSizeHandler.ForInstrumentedMethod {

            /**
             * The instrumented method.
             */
            protected final MethodDescription instrumentedMethod;

            /**
             * A list of virtual method arguments that are explicitly added before any code execution.
             */
            protected final List<? extends TypeDescription> initialTypes;

            /**
             * A list of virtual method arguments that are available before the instrumented method is executed.
             */
            protected final List<? extends TypeDescription> preMethodTypes;

            /**
             * A list of virtual method arguments that are available after the instrumented method has completed.
             */
            protected final List<? extends TypeDescription> postMethodTypes;

            /**
             * The maximum stack size required by a visited advice method.
             */
            protected int stackSize;

            /**
             * The maximum length of the local variable array required by a visited advice method.
             */
            protected int localVariableLength;

            /**
             * Creates a new default meta data handler that recomputes the space requirements of an instrumented method.
             *
             * @param instrumentedMethod The instrumented method.
             * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
             * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
             * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
             */
            protected Default(MethodDescription instrumentedMethod,
                              List<? extends TypeDescription> initialTypes,
                              List<? extends TypeDescription> preMethodTypes,
                              List<? extends TypeDescription> postMethodTypes) {
                this.instrumentedMethod = instrumentedMethod;
                this.initialTypes = initialTypes;
                this.preMethodTypes = preMethodTypes;
                this.postMethodTypes = postMethodTypes;
            }

            /**
             * Creates a method size handler applicable for the given instrumented method.
             *
             * @param instrumentedMethod The instrumented method.
             * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
             * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
             * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
             * @param copyArguments      {@code true} if the original arguments are copied before invoking the instrumented method.
             * @param writerFlags        The flags supplied to the ASM class writer.
             * @return An appropriate method size handler.
             */
            protected static MethodSizeHandler.ForInstrumentedMethod of(MethodDescription instrumentedMethod,
                                                                        List<? extends TypeDescription> initialTypes,
                                                                        List<? extends TypeDescription> preMethodTypes,
                                                                        List<? extends TypeDescription> postMethodTypes,
                                                                        boolean copyArguments,
                                                                        int writerFlags) {
                if ((writerFlags & (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)) != 0) {
                    return NoOp.INSTANCE;
                } else if (copyArguments) {
                    return new WithCopiedArguments(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
                } else {
                    return new WithRetainedArguments(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
                }
            }

            /**
             * {@inheritDoc}
             */
            public MethodSizeHandler.ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
                return new ForAdvice(adviceMethod, instrumentedMethod.getStackSize() + StackSize.of(initialTypes));
            }

            /**
             * {@inheritDoc}
             */
            public void requireStackSize(int stackSize) {
                Default.this.stackSize = Math.max(this.stackSize, stackSize);
            }

            /**
             * {@inheritDoc}
             */
            public void requireLocalVariableLength(int localVariableLength) {
                this.localVariableLength = Math.max(this.localVariableLength, localVariableLength);
            }

            /**
             * {@inheritDoc}
             */
            public int compoundStackSize(int stackSize) {
                return Math.max(this.stackSize, stackSize);
            }

            /**
             * {@inheritDoc}
             */
            public int compoundLocalVariableLength(int localVariableLength) {
                return Math.max(this.localVariableLength, localVariableLength
                        + StackSize.of(postMethodTypes)
                        + StackSize.of(initialTypes)
                        + StackSize.of(preMethodTypes));
            }

            /**
             * A method size handler that expects that the original arguments are retained.
             */
            protected static class WithRetainedArguments extends Default {

                /**
                 * Creates a new default method size handler that expects that the original arguments are retained.
                 *
                 * @param instrumentedMethod The instrumented method.
                 * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
                 * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
                 * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
                 */
                protected WithRetainedArguments(MethodDescription instrumentedMethod,
                                                List<? extends TypeDescription> initialTypes,
                                                List<? extends TypeDescription> preMethodTypes,
                                                List<? extends TypeDescription> postMethodTypes) {
                    super(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
                }

                /**
                 * {@inheritDoc}
                 */
                public MethodSizeHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                    return new ForAdvice(adviceMethod, instrumentedMethod.getStackSize()
                            + StackSize.of(postMethodTypes)
                            + StackSize.of(initialTypes)
                            + StackSize.of(preMethodTypes));
                }

                /**
                 * {@inheritDoc}
                 */
                public int compoundLocalVariableLength(int localVariableLength) {
                    return Math.max(this.localVariableLength, localVariableLength
                            + StackSize.of(postMethodTypes)
                            + StackSize.of(initialTypes)
                            + StackSize.of(preMethodTypes));
                }
            }

            /**
             * A method size handler that expects that the original arguments were copied.
             */
            protected static class WithCopiedArguments extends Default {

                /**
                 * Creates a new default method size handler that expects the original arguments to be copied.
                 *
                 * @param instrumentedMethod The instrumented method.
                 * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
                 * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
                 * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
                 */
                protected WithCopiedArguments(MethodDescription instrumentedMethod,
                                              List<? extends TypeDescription> initialTypes,
                                              List<? extends TypeDescription> preMethodTypes,
                                              List<? extends TypeDescription> postMethodTypes) {
                    super(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
                }

                /**
                 * {@inheritDoc}
                 */
                public MethodSizeHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                    return new ForAdvice(adviceMethod, 2 * instrumentedMethod.getStackSize()
                            + StackSize.of(initialTypes)
                            + StackSize.of(preMethodTypes)
                            + StackSize.of(postMethodTypes));
                }

                /**
                 * {@inheritDoc}
                 */
                public int compoundLocalVariableLength(int localVariableLength) {
                    return Math.max(this.localVariableLength, localVariableLength
                            + instrumentedMethod.getStackSize()
                            + StackSize.of(postMethodTypes)
                            + StackSize.of(initialTypes)
                            + StackSize.of(preMethodTypes));
                }
            }

            /**
             * A method size handler for an advice method.
             */
            protected class ForAdvice implements MethodSizeHandler.ForAdvice {

                /**
                 * The advice method.
                 */
                private final MethodDescription.InDefinedShape adviceMethod;

                /**
                 * The base of the local variable length that is implied by the method instrumentation prior to applying this advice method.
                 */
                private final int baseLocalVariableLength;

                /**
                 * The additional padding to apply to the operand stack.
                 */
                private int stackSizePadding;

                /**
                 * The additional padding to apply to the local variable array.
                 */
                private int localVariableLengthPadding;

                /**
                 * Creates a default method size handler for an advice method.
                 *
                 * @param adviceMethod            The advice method.
                 * @param baseLocalVariableLength The base of the local variable length that is implied by the method instrumentation
                 *                                prior to applying this advice method.
                 */
                protected ForAdvice(MethodDescription.InDefinedShape adviceMethod, int baseLocalVariableLength) {
                    this.adviceMethod = adviceMethod;
                    this.baseLocalVariableLength = baseLocalVariableLength;
                }

                /**
                 * {@inheritDoc}
                 */
                public void requireStackSize(int stackSize) {
                    Default.this.requireStackSize(stackSize);
                }

                /**
                 * {@inheritDoc}
                 */
                public void requireLocalVariableLength(int localVariableLength) {
                    Default.this.requireLocalVariableLength(localVariableLength);
                }

                /**
                 * {@inheritDoc}
                 */
                public void requireStackSizePadding(int stackSizePadding) {
                    this.stackSizePadding = Math.max(this.stackSizePadding, stackSizePadding);
                }

                /**
                 * {@inheritDoc}
                 */
                public void requireLocalVariableLengthPadding(int localVariableLengthPadding) {
                    this.localVariableLengthPadding = Math.max(this.localVariableLengthPadding, localVariableLengthPadding);
                }

                /**
                 * {@inheritDoc}
                 */
                public void recordMaxima(int stackSize, int localVariableLength) {
                    Default.this.requireStackSize(stackSize + stackSizePadding);
                    Default.this.requireLocalVariableLength(localVariableLength
                            - adviceMethod.getStackSize()
                            + baseLocalVariableLength
                            + localVariableLengthPadding);
                }
            }
        }
    }

    /**
     * A handler for computing and translating stack map frames.
     */
    public interface StackMapFrameHandler {

        /**
         * Translates a frame.
         *
         * @param methodVisitor       The method visitor to write the frame to.
         * @param type                The frame's type.
         * @param localVariableLength The local variable length.
         * @param localVariable       An array containing the types of the current local variables.
         * @param stackSize           The size of the operand stack.
         * @param stack               An array containing the types of the current operand stack.
         */
        void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack);

        /**
         * Injects a frame indicating the beginning of a return value handler for the currently handled method.
         *
         * @param methodVisitor The method visitor onto which to apply the stack map frame.
         */
        void injectReturnFrame(MethodVisitor methodVisitor);

        /**
         * Injects a frame indicating the beginning of an exception handler for the currently handled method.
         *
         * @param methodVisitor The method visitor onto which to apply the stack map frame.
         */
        void injectExceptionFrame(MethodVisitor methodVisitor);

        /**
         * Injects a frame indicating the completion of the currently handled method, i.e. all yielded types were added.
         *
         * @param methodVisitor The method visitor onto which to apply the stack map frame.
         */
        void injectCompletionFrame(MethodVisitor methodVisitor);

        /**
         * A stack map frame handler that can be used within a post processor. Emitting frames via this
         * handler is the only legal way for a post processor to produce frames.
         */
        interface ForPostProcessor {

            /**
             * Injects a frame that represents the current state.
             *
             * @param methodVisitor The method visitor onto which to apply the stack map frame.
             * @param stack         A list of types that are currently on the stack.
             */
            void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack);
        }

        /**
         * A stack map frame handler for an instrumented method.
         */
        interface ForInstrumentedMethod extends StackMapFrameHandler {

            /**
             * Binds this meta data handler for the enter advice.
             *
             * @param adviceMethod The enter advice method.
             * @return An appropriate meta data handler for the enter method.
             */
            ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod);

            /**
             * Binds this meta data handler for the exit advice.
             *
             * @param adviceMethod The exit advice method.
             * @return An appropriate meta data handler for the enter method.
             */
            ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod);

            /**
             * Returns a hint to supply to a {@link ClassReader} when parsing an advice method.
             *
             * @return The reader hint to supply to an ASM class reader.
             */
            int getReaderHint();

            /**
             * Injects a frame after initialization if any initialization is performed.
             *
             * @param methodVisitor The method visitor to write any frames to.
             */
            void injectInitializationFrame(MethodVisitor methodVisitor);

            /**
             * Injects a frame before executing the instrumented method.
             *
             * @param methodVisitor The method visitor to write any frames to.
             */
            void injectStartFrame(MethodVisitor methodVisitor);

            /**
             * Injects a frame indicating the completion of the currently handled method, i.e. all yielded types were added.
             *
             * @param methodVisitor The method visitor onto which to apply the stack map frame.
             */
            void injectPostCompletionFrame(MethodVisitor methodVisitor);
        }

        /**
         * A stack map frame handler for an advice method.
         */
        interface ForAdvice extends StackMapFrameHandler, ForPostProcessor, Advice.StackMapFrameHandler.ForPostProcessor {
            /* marker interface */
        }

        /**
         * A non-operational stack map frame handler.
         */
        enum NoOp implements ForInstrumentedMethod, ForAdvice {

            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * {@inheritDoc}
             */
            public StackMapFrameHandler.ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public StackMapFrameHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                return this;
            }

            /**
             * {@inheritDoc}
             */
            public int getReaderHint() {
                return ClassReader.SKIP_FRAMES;
            }

            /**
             * {@inheritDoc}
             */
            public void translateFrame(MethodVisitor methodVisitor,
                                       int type,
                                       int localVariableLength,
                                       Object[] localVariable,
                                       int stackSize,
                                       Object[] stack) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectReturnFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectExceptionFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectCompletionFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectInitializationFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectStartFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
                /* do nothing */
            }

            /**
             * {@inheritDoc}
             */
            public void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack) {
                /* do nothing */
            }
        }

        /**
         * A default implementation of a stack map frame handler for an instrumented method.
         */
        abstract class Default implements ForInstrumentedMethod {

            /**
             * An empty array indicating an empty frame.
             */
            protected static final Object[] EMPTY = new Object[0];

            /**
             * The instrumented type.
             */
            protected final TypeDescription instrumentedType;

            /**
             * The instrumented method.
             */
            protected final MethodDescription instrumentedMethod;

            /**
             * A list of virtual method arguments that are explicitly added before any code execution.
             */
            protected final List<? extends TypeDescription> initialTypes;

            /**
             * A list of virtual arguments that are available after the enter advice method is executed.
             */
            protected final List<? extends TypeDescription> latentTypes;

            /**
             * A list of virtual method arguments that are available before the instrumented method is executed.
             */
            protected final List<? extends TypeDescription> preMethodTypes;

            /**
             * A list of virtual method arguments that are available after the instrumented method has completed.
             */
            protected final List<? extends TypeDescription> postMethodTypes;

            /**
             * {@code true} if the meta data handler is expected to expand its frames.
             */
            protected final boolean expandFrames;

            /**
             * The current frame's size divergence from the original local variable array.
             */
            protected int currentFrameDivergence;

            /**
             * Creates a new default stack map frame handler.
             *
             * @param instrumentedType   The instrumented type.
             * @param instrumentedMethod The instrumented method.
             * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
             * @param latentTypes        A list of virtual arguments that are available after the enter advice method is executed.
             * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
             * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
             * @param expandFrames       {@code true} if the meta data handler is expected to expand its frames.
             */
            protected Default(TypeDescription instrumentedType,
                              MethodDescription instrumentedMethod,
                              List<? extends TypeDescription> initialTypes,
                              List<? extends TypeDescription> latentTypes,
                              List<? extends TypeDescription> preMethodTypes,
                              List<? extends TypeDescription> postMethodTypes,
                              boolean expandFrames) {
                this.instrumentedType = instrumentedType;
                this.instrumentedMethod = instrumentedMethod;
                this.initialTypes = initialTypes;
                this.latentTypes = latentTypes;
                this.preMethodTypes = preMethodTypes;
                this.postMethodTypes = postMethodTypes;
                this.expandFrames = expandFrames;
            }

            /**
             * Creates an appropriate stack map frame handler for an instrumented method.
             *
             * @param instrumentedType   The instrumented type.
             * @param instrumentedMethod The instrumented method.
             * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
             * @param latentTypes        A list of virtual arguments that are available after the enter advice method is executed.
             * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
             * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
             * @param exitAdvice         {@code true} if the current advice implies exit advice.
             * @param copyArguments      {@code true} if the original arguments are copied before invoking the instrumented method.
             * @param classFileVersion   The instrumented type's class file version.
             * @param writerFlags        The flags supplied to the ASM writer.
             * @param readerFlags        The reader flags supplied to the ASM reader.
             * @return An appropriate stack map frame handler for an instrumented method.
             */
            protected static ForInstrumentedMethod of(TypeDescription instrumentedType,
                                                      MethodDescription instrumentedMethod,
                                                      List<? extends TypeDescription> initialTypes,
                                                      List<? extends TypeDescription> latentTypes,
                                                      List<? extends TypeDescription> preMethodTypes,
                                                      List<? extends TypeDescription> postMethodTypes,
                                                      boolean exitAdvice,
                                                      boolean copyArguments,
                                                      ClassFileVersion classFileVersion,
                                                      int writerFlags,
                                                      int readerFlags) {
                if ((writerFlags & ClassWriter.COMPUTE_FRAMES) != 0
                    || classFileVersion.isLessThan(ClassFileVersion.JAVA_V6)) {
                    return NoOp.INSTANCE;
                } else if (!exitAdvice && initialTypes.isEmpty()) {
                    return new Trivial(instrumentedType,
                            instrumentedMethod,
                            latentTypes,
                            (readerFlags & ClassReader.EXPAND_FRAMES) != 0);
                } else if (copyArguments) {
                    return new WithPreservedArguments.WithArgumentCopy(instrumentedType,
                            instrumentedMethod,
                            initialTypes,
                            latentTypes,
                            preMethodTypes,
                            postMethodTypes,
                            (readerFlags & ClassReader.EXPAND_FRAMES) != 0);
                } else {
                    return new WithPreservedArguments.WithoutArgumentCopy(instrumentedType,
                            instrumentedMethod,
                            initialTypes,
                            latentTypes,
                            preMethodTypes,
                            postMethodTypes,
                            (readerFlags & ClassReader.EXPAND_FRAMES) != 0,
                            !instrumentedMethod.isConstructor());
                }
            }

            /**
             * {@inheritDoc}
             */
            public StackMapFrameHandler.ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
                return new ForAdvice(adviceMethod, initialTypes, latentTypes, preMethodTypes, TranslationMode.ENTER, instrumentedMethod.isConstructor()
                        ? Initialization.UNITIALIZED
                        : Initialization.INITIALIZED);
            }

            /**
             * {@inheritDoc}
             */
            public int getReaderHint() {
                return expandFrames
                        ? ClassReader.EXPAND_FRAMES
                        : AsmVisitorWrapper.NO_FLAGS;
            }

            /**
             * Translates a frame.
             *
             * @param methodVisitor       The method visitor to write the frame to.
             * @param translationMode     The translation mode to apply.
             * @param methodDescription   The method description for which the frame is written.
             * @param additionalTypes     The additional types to consider part of the instrumented method's parameters.
             * @param type                The frame's type.
             * @param localVariableLength The local variable length.
             * @param localVariable       An array containing the types of the current local variables.
             * @param stackSize           The size of the operand stack.
             * @param stack               An array containing the types of the current operand stack.
             */
            protected void translateFrame(MethodVisitor methodVisitor,
                                          TranslationMode translationMode,
                                          MethodDescription methodDescription,
                                          List<? extends TypeDescription> additionalTypes,
                                          int type,
                                          int localVariableLength,
                                          Object[] localVariable,
                                          int stackSize,
                                          Object[] stack) {
                switch (type) {
                    case Opcodes.F_SAME:
                    case Opcodes.F_SAME1:
                        break;
                    case Opcodes.F_APPEND:
                        currentFrameDivergence += localVariableLength;
                        break;
                    case Opcodes.F_CHOP:
                        currentFrameDivergence -= localVariableLength;
                        if (currentFrameDivergence < 0) {
                            throw new IllegalStateException(methodDescription + " dropped " + Math.abs(currentFrameDivergence) + " implicit frames");
                        }
                        break;
                    case Opcodes.F_FULL:
                    case Opcodes.F_NEW:
                        if (methodDescription.getParameters().size() + (methodDescription.isStatic() ? 0 : 1) > localVariableLength) {
                            throw new IllegalStateException("Inconsistent frame length for " + methodDescription + ": " + localVariableLength);
                        }
                        int offset;
                        if (methodDescription.isStatic()) {
                            offset = 0;
                        } else {
                            if (!translationMode.isPossibleThisFrameValue(instrumentedType, instrumentedMethod, localVariable[0])) {
                                throw new IllegalStateException(methodDescription + " is inconsistent for 'this' reference: " + localVariable[0]);
                            }
                            offset = 1;
                        }
                        for (int index = 0; index < methodDescription.getParameters().size(); index++) {
                            if (!Initialization.INITIALIZED.toFrame(methodDescription.getParameters().get(index).getType().asErasure()).equals(localVariable[index + offset])) {
                                throw new IllegalStateException(methodDescription + " is inconsistent at " + index + ": " + localVariable[index + offset]);
                            }
                        }
                        Object[] translated = new Object[localVariableLength
                                - (methodDescription.isStatic() ? 0 : 1)
                                - methodDescription.getParameters().size()
                                + (instrumentedMethod.isStatic() ? 0 : 1)
                                + instrumentedMethod.getParameters().size()
                                + additionalTypes.size()];
                        int index = translationMode.copy(instrumentedType, instrumentedMethod, methodDescription, localVariable, translated);
                        for (TypeDescription typeDescription : additionalTypes) {
                            translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                        }
                        System.arraycopy(localVariable,
                                methodDescription.getParameters().size() + (methodDescription.isStatic() ? 0 : 1),
                                translated,
                                index,
                                translated.length - index);
                        localVariableLength = translated.length;
                        localVariable = translated;
                        currentFrameDivergence = translated.length - index;
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected frame type: " + type);
                }
                methodVisitor.visitFrame(type, localVariableLength, localVariable, stackSize, stack);
            }

            /**
             * Injects a full stack map frame after the instrumented method has completed.
             *
             * @param methodVisitor  The method visitor onto which to write the stack map frame.
             * @param initialization The initialization to apply when resolving a reference to the instance on which a non-static method is invoked.
             * @param typesInArray   The types that were added to the local variable array additionally to the values of the instrumented method.
             * @param typesOnStack   The types currently on the operand stack.
             */
            protected void injectFullFrame(MethodVisitor methodVisitor,
                                           Initialization initialization,
                                           List<? extends TypeDescription> typesInArray,
                                           List<? extends TypeDescription> typesOnStack) {
                Object[] localVariable = new Object[instrumentedMethod.getParameters().size()
                        + (instrumentedMethod.isStatic() ? 0 : 1)
                        + typesInArray.size()];
                int index = 0;
                if (!instrumentedMethod.isStatic()) {
                    localVariable[index++] = initialization.toFrame(instrumentedType);
                }
                for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                }
                for (TypeDescription typeDescription : typesInArray) {
                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                }
                index = 0;
                Object[] stackType = new Object[typesOnStack.size()];
                for (TypeDescription typeDescription : typesOnStack) {
                    stackType[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                }
                methodVisitor.visitFrame(expandFrames ? Opcodes.F_NEW : Opcodes.F_FULL, localVariable.length, localVariable, stackType.length, stackType);
                currentFrameDivergence = 0;
            }

            /**
             * A translation mode that determines how the fixed frames of the instrumented method are written.
             */
            protected enum TranslationMode {

                /**
                 * A translation mode that simply copies the original frames which are available when translating frames of the instrumented method.
                 */
                COPY {
                    @Override
                    protected int copy(TypeDescription instrumentedType,
                                       MethodDescription instrumentedMethod,
                                       MethodDescription methodDescription,
                                       Object[] localVariable,
                                       Object[] translated) {
                        int length = instrumentedMethod.getParameters().size() + (instrumentedMethod.isStatic() ? 0 : 1);
                        System.arraycopy(localVariable, 0, translated, 0, length);
                        return length;
                    }

                    @Override
                    protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
                        return instrumentedMethod.isConstructor() && Opcodes.UNINITIALIZED_THIS.equals(frame) || Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);
                    }
                },

                /**
                 * A translation mode for the enter advice that considers that the {@code this} reference might not be initialized for a constructor.
                 */
                ENTER {
                    @Override
                    protected int copy(TypeDescription instrumentedType,
                                       MethodDescription instrumentedMethod,
                                       MethodDescription methodDescription,
                                       Object[] localVariable,
                                       Object[] translated) {
                        int index = 0;
                        if (!instrumentedMethod.isStatic()) {
                            translated[index++] = instrumentedMethod.isConstructor()
                                    ? Opcodes.UNINITIALIZED_THIS
                                    : Initialization.INITIALIZED.toFrame(instrumentedType);
                        }
                        for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                            translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                        }
                        return index;
                    }

                    @Override
                    protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
                        return instrumentedMethod.isConstructor()
                                ? Opcodes.UNINITIALIZED_THIS.equals(frame)
                                : Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);
                    }
                },

                /**
                 * A translation mode for an exit advice where the {@code this} reference is always initialized.
                 */
                EXIT {
                    @Override
                    protected int copy(TypeDescription instrumentedType,
                                       MethodDescription instrumentedMethod,
                                       MethodDescription methodDescription,
                                       Object[] localVariable,
                                       Object[] translated) {
                        int index = 0;
                        if (!instrumentedMethod.isStatic()) {
                            translated[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                        }
                        for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                            translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                        }
                        return index;
                    }

                    @Override
                    protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
                        return Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);
                    }
                };

                /**
                 * Copies the fixed parameters of the instrumented method onto the operand stack.
                 *
                 * @param instrumentedType   The instrumented type.
                 * @param instrumentedMethod The instrumented method.
                 * @param methodDescription  The method for which a frame is created.
                 * @param localVariable      The original local variable array.
                 * @param translated         The array containing the translated frames.
                 * @return The amount of frames added to the translated frame array.
                 */
                protected abstract int copy(TypeDescription instrumentedType,
                                            MethodDescription instrumentedMethod,
                                            MethodDescription methodDescription,
                                            Object[] localVariable,
                                            Object[] translated);

                /**
                 * Checks if a variable value in a stack map frame is a legal value for describing a {@code this} reference.
                 *
                 * @param instrumentedType   The instrumented type.
                 * @param instrumentedMethod The instrumented method.
                 * @param frame              The frame value representing the {@code this} reference.
                 * @return {@code true} if the value is a legal representation of the {@code this} reference.
                 */
                protected abstract boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame);
            }

            /**
             * Represents the initialization state of a stack value that can either be initialized or uninitialized.
             */
            protected enum Initialization {

                /**
                 * Represents an uninitialized frame value within a constructor before invoking the super constructor.
                 */
                UNITIALIZED {
                    /**
                     * {@inheritDoc}
                     */
                    protected Object toFrame(TypeDescription typeDescription) {
                        if (typeDescription.isPrimitive()) {
                            throw new IllegalArgumentException("Cannot assume primitive uninitialized value: " + typeDescription);
                        }
                        return Opcodes.UNINITIALIZED_THIS;
                    }
                },

                /**
                 * Represents an initialized frame value.
                 */
                INITIALIZED {
                    /**
                     * {@inheritDoc}
                     */
                    protected Object toFrame(TypeDescription typeDescription) {
                        if (typeDescription.represents(boolean.class)
                                || typeDescription.represents(byte.class)
                                || typeDescription.represents(short.class)
                                || typeDescription.represents(char.class)
                                || typeDescription.represents(int.class)) {
                            return Opcodes.INTEGER;
                        } else if (typeDescription.represents(long.class)) {
                            return Opcodes.LONG;
                        } else if (typeDescription.represents(float.class)) {
                            return Opcodes.FLOAT;
                        } else if (typeDescription.represents(double.class)) {
                            return Opcodes.DOUBLE;
                        } else {
                            return typeDescription.getInternalName();
                        }
                    }
                };

                /**
                 * Initializes a frame value to its frame type.
                 *
                 * @param typeDescription The type being resolved.
                 * @return The frame value.
                 */
                protected abstract Object toFrame(TypeDescription typeDescription);
            }

            /**
             * A trivial stack map frame handler that applies a trivial translation for the instrumented method's stack map frames.
             */
            protected static class Trivial extends Default {

                /**
                 * Creates a new stack map frame handler that applies a trivial translation for the instrumented method's stack map frames.
                 *
                 * @param instrumentedType   The instrumented type.
                 * @param instrumentedMethod The instrumented method.
                 * @param latentTypes        A list of virtual arguments that are available after the enter advice method is executed.
                 * @param expandFrames       {@code true} if the meta data handler is expected to expand its frames.
                 */
                protected Trivial(TypeDescription instrumentedType, MethodDescription instrumentedMethod, List<? extends TypeDescription> latentTypes, boolean expandFrames) {
                    super(instrumentedType,
                            instrumentedMethod,
                            Collections.emptyList(),
                            latentTypes,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            expandFrames);
                }

                /**
                 * {@inheritDoc}
                 */
                public void translateFrame(MethodVisitor methodVisitor,
                                           int type,
                                           int localVariableLength,
                                           Object[] localVariable,
                                           int stackSize,
                                           Object[] stack) {
                    methodVisitor.visitFrame(type, localVariableLength, localVariable, stackSize, stack);
                }

                /**
                 * {@inheritDoc}
                 */
                public StackMapFrameHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                    throw new IllegalStateException("Did not expect exit advice " + adviceMethod + " for " + instrumentedMethod);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectReturnFrame(MethodVisitor methodVisitor) {
                    throw new IllegalStateException("Did not expect return frame for " + instrumentedMethod);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectExceptionFrame(MethodVisitor methodVisitor) {
                    throw new IllegalStateException("Did not expect exception frame for " + instrumentedMethod);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectCompletionFrame(MethodVisitor methodVisitor) {
                    throw new IllegalStateException("Did not expect completion frame for " + instrumentedMethod);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
                    throw new IllegalStateException("Did not expect post completion frame for " + instrumentedMethod);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectInitializationFrame(MethodVisitor methodVisitor) {
                    /* do nothing */
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectStartFrame(MethodVisitor methodVisitor) {
                    /* do nothing */
                }
            }

            /**
             * A stack map frame handler that requires the original arguments of the instrumented method to be preserved in their original form.
             */
            protected abstract static class WithPreservedArguments extends Default {

                /**
                 * {@code true} if a completion frame for the method bust be a full frame to reflect an initialization change.
                 */
                protected boolean allowCompactCompletionFrame;

                /**
                 * Creates a new stack map frame handler that requires the stack map frames of the original arguments to be preserved.
                 *
                 * @param instrumentedType            The instrumented type.
                 * @param instrumentedMethod          The instrumented method.
                 * @param initialTypes                A list of virtual method arguments that are explicitly added before any code execution.
                 * @param latentTypes                 A list of virtual arguments that are available after the enter advice method is executed.
                 * @param preMethodTypes              A list of virtual method arguments that are available before the instrumented method is executed.
                 * @param postMethodTypes             A list of virtual method arguments that are available after the instrumented method has completed.
                 * @param expandFrames                {@code true} if the meta data handler is expected to expand its frames.
                 * @param allowCompactCompletionFrame {@code true} if a completion frame for the method bust be a full frame to reflect an initialization change.
                 */
                protected WithPreservedArguments(TypeDescription instrumentedType,
                                                 MethodDescription instrumentedMethod,
                                                 List<? extends TypeDescription> initialTypes,
                                                 List<? extends TypeDescription> latentTypes,
                                                 List<? extends TypeDescription> preMethodTypes,
                                                 List<? extends TypeDescription> postMethodTypes,
                                                 boolean expandFrames,
                                                 boolean allowCompactCompletionFrame) {
                    super(instrumentedType, instrumentedMethod, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames);
                    this.allowCompactCompletionFrame = allowCompactCompletionFrame;
                }

                @Override
                //@SuppressFBWarnings(value = "RC_REF_COMPARISON_BAD_PRACTICE", justification = "ASM models frames by reference comparison.")
                protected void translateFrame(MethodVisitor methodVisitor,
                                              TranslationMode translationMode,
                                              MethodDescription methodDescription,
                                              List<? extends TypeDescription> additionalTypes,
                                              int type,
                                              int localVariableLength,
                                              Object[] localVariable,
                                              int stackSize,
                                              Object[] stack) {
                    if (type == Opcodes.F_FULL && localVariableLength > 0 && localVariable[0] != Opcodes.UNINITIALIZED_THIS) {
                        allowCompactCompletionFrame = true;
                    }
                    super.translateFrame(methodVisitor, translationMode, methodDescription, additionalTypes, type, localVariableLength, localVariable, stackSize, stack);
                }

                /**
                 * {@inheritDoc}
                 */
                public StackMapFrameHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
                    return new ForAdvice(adviceMethod,
                            CompoundList.of(initialTypes, preMethodTypes, postMethodTypes),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            TranslationMode.EXIT,
                            Initialization.INITIALIZED);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectReturnFrame(MethodVisitor methodVisitor) {
                    if (!expandFrames && currentFrameDivergence == 0) {
                        if (instrumentedMethod.getReturnType().represents(void.class)) {
                            methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                        } else {
                            methodVisitor.visitFrame(Opcodes.F_SAME1,
                                    EMPTY.length,
                                    EMPTY,
                                    1,
                                    new Object[]{Initialization.INITIALIZED.toFrame(instrumentedMethod.getReturnType().asErasure())});
                        }
                    } else {
                        injectFullFrame(methodVisitor, Initialization.INITIALIZED, CompoundList.of(initialTypes, preMethodTypes), instrumentedMethod.getReturnType().represents(void.class)
                                ? Collections.emptyList()
                                : Collections.singletonList(instrumentedMethod.getReturnType().asErasure()));
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectExceptionFrame(MethodVisitor methodVisitor) {
                    if (!expandFrames && currentFrameDivergence == 0) {
                        methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[]{Type.getInternalName(Throwable.class)});
                    } else {
                        injectFullFrame(methodVisitor, Initialization.INITIALIZED, CompoundList.of(initialTypes, preMethodTypes), Collections.singletonList(TypeDescription.THROWABLE));
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectCompletionFrame(MethodVisitor methodVisitor) {
                    if (allowCompactCompletionFrame && !expandFrames && currentFrameDivergence == 0 && postMethodTypes.size() < 4) {
                        if (postMethodTypes.isEmpty()) {
                            methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                        } else {
                            Object[] local = new Object[postMethodTypes.size()];
                            int index = 0;
                            for (TypeDescription typeDescription : postMethodTypes) {
                                local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
                        }
                    } else {
                        injectFullFrame(methodVisitor, Initialization.INITIALIZED,
                            CompoundList.of(initialTypes, preMethodTypes, postMethodTypes), Collections.emptyList());
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
                    if (!expandFrames && currentFrameDivergence == 0) {
                        methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                    } else {
                        injectFullFrame(methodVisitor, Initialization.INITIALIZED,
                            CompoundList.of(initialTypes, preMethodTypes, postMethodTypes), Collections.emptyList());
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectInitializationFrame(MethodVisitor methodVisitor) {
                    if (!initialTypes.isEmpty()) {
                        if (!expandFrames && initialTypes.size() < 4) {
                            Object[] localVariable = new Object[initialTypes.size()];
                            int index = 0;
                            for (TypeDescription typeDescription : initialTypes) {
                                localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            methodVisitor.visitFrame(Opcodes.F_APPEND, localVariable.length, localVariable, EMPTY.length, EMPTY);
                        } else {
                            Object[] localVariable = new Object[(instrumentedMethod.isStatic() ? 0 : 1)
                                    + instrumentedMethod.getParameters().size()
                                    + initialTypes.size()];
                            int index = 0;
                            if (instrumentedMethod.isConstructor()) {
                                localVariable[index++] = Opcodes.UNINITIALIZED_THIS;
                            } else if (!instrumentedMethod.isStatic()) {
                                localVariable[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                            }
                            for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                                localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            for (TypeDescription typeDescription : initialTypes) {
                                localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            methodVisitor.visitFrame(expandFrames ? Opcodes.F_NEW : Opcodes.F_FULL, localVariable.length, localVariable, EMPTY.length, EMPTY);
                        }
                    }
                }

                /**
                 * A stack map frame handler that expects that the original argument frames remain preserved throughout the original invocation.
                 */
                protected static class WithoutArgumentCopy extends WithPreservedArguments {

                    /**
                     * Creates a new stack map frame handler that expects the original frames to be preserved.
                     *
                     * @param instrumentedType            The instrumented type.
                     * @param instrumentedMethod          The instrumented method.
                     * @param initialTypes                A list of virtual method arguments that are explicitly added before any code execution.
                     * @param latentTypes                 A list of virtual arguments that are available after the enter advice method is executed.
                     * @param preMethodTypes              A list of virtual method arguments that are available before the instrumented method is executed.
                     * @param postMethodTypes             A list of virtual method arguments that are available after the instrumented method has completed.
                     * @param expandFrames                {@code true} if the meta data handler is expected to expand its frames.
                     * @param allowCompactCompletionFrame {@code true} if a completion frame for the method bust be a full frame to reflect an initialization change.
                     */
                    protected WithoutArgumentCopy(TypeDescription instrumentedType,
                                                  MethodDescription instrumentedMethod,
                                                  List<? extends TypeDescription> initialTypes,
                                                  List<? extends TypeDescription> latentTypes,
                                                  List<? extends TypeDescription> preMethodTypes,
                                                  List<? extends TypeDescription> postMethodTypes,
                                                  boolean expandFrames,
                                                  boolean allowCompactCompletionFrame) {
                        super(instrumentedType, instrumentedMethod, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames, allowCompactCompletionFrame);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void injectStartFrame(MethodVisitor methodVisitor) {
                        /* do nothing */
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void translateFrame(MethodVisitor methodVisitor,
                                               int type,
                                               int localVariableLength,
                                               Object[] localVariable,
                                               int stackSize,
                                               Object[] stack) {
                        translateFrame(methodVisitor,
                                TranslationMode.COPY,
                                instrumentedMethod,
                                CompoundList.of(initialTypes, preMethodTypes),
                                type,
                                localVariableLength,
                                localVariable,
                                stackSize,
                                stack);
                    }
                }

                /**
                 * A stack map frame handler that expects that an argument copy of the original method arguments was made.
                 */
                protected static class WithArgumentCopy extends WithPreservedArguments {

                    /**
                     * Creates a new stack map frame handler that expects an argument copy.
                     *
                     * @param instrumentedType   The instrumented type.
                     * @param instrumentedMethod The instrumented method.
                     * @param initialTypes       A list of virtual method arguments that are explicitly added before any code execution.
                     * @param latentTypes        The types that are given post execution of a possible enter advice.
                     * @param preMethodTypes     A list of virtual method arguments that are available before the instrumented method is executed.
                     * @param postMethodTypes    A list of virtual method arguments that are available after the instrumented method has completed.
                     * @param expandFrames       {@code true} if the meta data handler is expected to expand its frames.
                     */
                    protected WithArgumentCopy(TypeDescription instrumentedType,
                                               MethodDescription instrumentedMethod,
                                               List<? extends TypeDescription> initialTypes,
                                               List<? extends TypeDescription> latentTypes,
                                               List<? extends TypeDescription> preMethodTypes,
                                               List<? extends TypeDescription> postMethodTypes,
                                               boolean expandFrames) {
                        super(instrumentedType, instrumentedMethod, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames, true);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public void injectStartFrame(MethodVisitor methodVisitor) {
                        if (!instrumentedMethod.isStatic() || !instrumentedMethod.getParameters().isEmpty()) {
                            if (!expandFrames && (instrumentedMethod.isStatic() ? 0 : 1) + instrumentedMethod.getParameters().size() < 4) {
                                Object[] localVariable = new Object[(instrumentedMethod.isStatic() ? 0 : 1) + instrumentedMethod.getParameters().size()];
                                int index = 0;
                                if (instrumentedMethod.isConstructor()) {
                                    localVariable[index++] = Opcodes.UNINITIALIZED_THIS;
                                } else if (!instrumentedMethod.isStatic()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                                }
                                for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                methodVisitor.visitFrame(Opcodes.F_APPEND, localVariable.length, localVariable, EMPTY.length, EMPTY);
                            } else {
                                Object[] localVariable = new Object[(instrumentedMethod.isStatic() ? 0 : 2)
                                        + instrumentedMethod.getParameters().size() * 2
                                        + initialTypes.size()
                                        + preMethodTypes.size()];
                                int index = 0;
                                if (instrumentedMethod.isConstructor()) {
                                    localVariable[index++] = Opcodes.UNINITIALIZED_THIS;
                                } else if (!instrumentedMethod.isStatic()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                                }
                                for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                for (TypeDescription typeDescription : initialTypes) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                for (TypeDescription typeDescription : preMethodTypes) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                if (instrumentedMethod.isConstructor()) {
                                    localVariable[index++] = Opcodes.UNINITIALIZED_THIS;
                                } else if (!instrumentedMethod.isStatic()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                                }
                                for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                                    localVariable[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                methodVisitor.visitFrame(expandFrames ? Opcodes.F_NEW : Opcodes.F_FULL, localVariable.length, localVariable, EMPTY.length, EMPTY);
                            }
                        }
                        currentFrameDivergence = (instrumentedMethod.isStatic() ? 0 : 1) + instrumentedMethod.getParameters().size();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    // @SuppressFBWarnings(value = "RC_REF_COMPARISON_BAD_PRACTICE",
                    // justification = "Reference equality is required by ASM")
                    public void translateFrame(MethodVisitor methodVisitor,
                                               int type,
                                               int localVariableLength,
                                               Object[] localVariable,
                                               int stackSize,
                                               Object[] stack) {
                        switch (type) {
                            case Opcodes.F_SAME:
                            case Opcodes.F_SAME1:
                                break;
                            case Opcodes.F_APPEND:
                                currentFrameDivergence += localVariableLength;
                                break;
                            case Opcodes.F_CHOP:
                                currentFrameDivergence -= localVariableLength;
                                break;
                            case Opcodes.F_FULL:
                            case Opcodes.F_NEW:
                                Object[] translated = new Object[localVariableLength
                                        + (instrumentedMethod.isStatic() ? 0 : 1)
                                        + instrumentedMethod.getParameters().size()
                                        + initialTypes.size()
                                        + preMethodTypes.size()];
                                int index = 0;
                                if (instrumentedMethod.isConstructor()) {
                                    Initialization initialization = Initialization.INITIALIZED;
                                    for (int variableIndex = 0; variableIndex < localVariableLength; variableIndex++) {
                                        if (localVariable[variableIndex] == Opcodes.UNINITIALIZED_THIS) {
                                            initialization = Initialization.UNITIALIZED;
                                            break;
                                        }
                                    }
                                    translated[index++] = initialization.toFrame(instrumentedType);
                                } else if (!instrumentedMethod.isStatic()) {
                                    translated[index++] = Initialization.INITIALIZED.toFrame(instrumentedType);
                                }
                                for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                                    translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                for (TypeDescription typeDescription : initialTypes) {
                                    translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                for (TypeDescription typeDescription : preMethodTypes) {
                                    translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                                }
                                if (localVariableLength > 0) {
                                    System.arraycopy(localVariable, 0, translated, index, localVariableLength);
                                }
                                localVariableLength = translated.length;
                                localVariable = translated;
                                currentFrameDivergence = localVariableLength;
                                break;
                            default:
                                throw new IllegalArgumentException("Unexpected frame type: " + type);
                        }
                        methodVisitor.visitFrame(type, localVariableLength, localVariable, stackSize, stack);
                    }
                }
            }

            /**
             * A stack map frame handler for an advice method.
             */
            protected class ForAdvice implements StackMapFrameHandler.ForAdvice {

                /**
                 * The method description for which frames are translated.
                 */
                protected final MethodDescription.InDefinedShape adviceMethod;

                /**
                 * The types provided before execution of the advice code.
                 */
                protected final List<? extends TypeDescription> startTypes;

                /**
                 * The types that are given post execution of the advice.
                 */
                private final List<? extends TypeDescription> intermediateTypes;

                /**
                 * The types provided after execution of the advice code.
                 */
                protected final List<? extends TypeDescription> endTypes;

                /**
                 * The translation mode to apply for this advice method. Should be either {@link TranslationMode#ENTER} or {@link TranslationMode#EXIT}.
                 */
                protected final TranslationMode translationMode;

                /**
                 * The initialization to apply when resolving a reference to the instance on which a non-static method is invoked.
                 */
                private final Initialization initialization;

                /**
                 * {@code true} if an intermediate frame was yielded.
                 */
                private boolean intermedate;

                /**
                 * Creates a new meta data handler for an advice method.
                 *
                 * @param adviceMethod      The method description for which frames are translated.
                 * @param startTypes        The types provided before execution of the advice code.
                 * @param intermediateTypes The types that are given post execution of the advice.
                 * @param endTypes          The types provided after execution of the advice code.
                 * @param translationMode   The translation mode to apply for this advice method. Should be
                 *                          either {@link TranslationMode#ENTER} or {@link TranslationMode#EXIT}.
                 * @param initialization    The initialization to apply when resolving a reference to the instance on which a non-static method is invoked.
                 */
                protected ForAdvice(MethodDescription.InDefinedShape adviceMethod,
                                    List<? extends TypeDescription> startTypes,
                                    List<? extends TypeDescription> intermediateTypes,
                                    List<? extends TypeDescription> endTypes,
                                    TranslationMode translationMode,
                                    Initialization initialization) {
                    this.adviceMethod = adviceMethod;
                    this.startTypes = startTypes;
                    this.intermediateTypes = intermediateTypes;
                    this.endTypes = endTypes;
                    this.translationMode = translationMode;
                    this.initialization = initialization;
                    intermedate = false;
                }

                /**
                 * {@inheritDoc}
                 */
                public void translateFrame(MethodVisitor methodVisitor,
                                           int type,
                                           int localVariableLength,
                                           Object[] localVariable,
                                           int stackSize,
                                           Object[] stack) {
                    Default.this.translateFrame(methodVisitor,
                            translationMode,
                            adviceMethod,
                            startTypes,
                            type,
                            localVariableLength,
                            localVariable,
                            stackSize,
                            stack);
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectReturnFrame(MethodVisitor methodVisitor) {
                    if (!expandFrames && currentFrameDivergence == 0) {
                        if (adviceMethod.getReturnType().represents(void.class)) {
                            methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                        } else {
                            methodVisitor.visitFrame(Opcodes.F_SAME1,
                                    EMPTY.length,
                                    EMPTY,
                                    1,
                                    new Object[]{Initialization.INITIALIZED.toFrame(adviceMethod.getReturnType().asErasure())});
                        }
                    } else {
                        injectFullFrame(methodVisitor, initialization, startTypes, adviceMethod.getReturnType().represents(void.class)
                                ? Collections.emptyList()
                                : Collections.singletonList(adviceMethod.getReturnType().asErasure()));
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectExceptionFrame(MethodVisitor methodVisitor) {
                    if (!expandFrames && currentFrameDivergence == 0) {
                        methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[]{Type.getInternalName(Throwable.class)});
                    } else {
                        injectFullFrame(methodVisitor, initialization, startTypes, Collections.singletonList(TypeDescription.THROWABLE));
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectCompletionFrame(MethodVisitor methodVisitor) {
                    if (expandFrames) {
                        injectFullFrame(methodVisitor, initialization,
                            CompoundList.of(startTypes, endTypes), Collections.emptyList());
                    } else if (currentFrameDivergence == 0 && (intermedate || endTypes.size() < 4)) {
                        if (intermedate || endTypes.isEmpty()) {
                            methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                        } else {
                            Object[] local = new Object[endTypes.size()];
                            int index = 0;
                            for (TypeDescription typeDescription : endTypes) {
                                local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
                        }
                    } else if (currentFrameDivergence < 3 && endTypes.isEmpty()) {
                        methodVisitor.visitFrame(Opcodes.F_CHOP, currentFrameDivergence, EMPTY, EMPTY.length, EMPTY);
                        currentFrameDivergence = 0;
                    } else {
                        injectFullFrame(methodVisitor, initialization,
                            CompoundList.of(startTypes, endTypes), Collections.emptyList());
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack) {
                    if (expandFrames) {
                        injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, intermediateTypes), stack);
                    } else if (intermedate && stack.size() < 2) {
                        if (stack.isEmpty()) {
                            methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                        } else {
                            methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[]{Initialization.INITIALIZED.toFrame(stack.get(0))});
                        }
                    } else if (currentFrameDivergence == 0
                            && intermediateTypes.size() < 4
                            && (stack.isEmpty() || stack.size() < 2 && intermediateTypes.isEmpty())) {
                        if (intermediateTypes.isEmpty()) {
                            if (stack.isEmpty()) {
                                methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
                            } else {
                                methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[]{Initialization.INITIALIZED.toFrame(stack.get(0))});
                            }
                        } else {
                            Object[] local = new Object[intermediateTypes.size()];
                            int index = 0;
                            for (TypeDescription typeDescription : intermediateTypes) {
                                local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
                            }
                            methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
                        }
                    } else if (currentFrameDivergence < 3 && intermediateTypes.isEmpty() && stack.isEmpty()) {
                        methodVisitor.visitFrame(Opcodes.F_CHOP, currentFrameDivergence, EMPTY, EMPTY.length, EMPTY);
                    } else {
                        injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, intermediateTypes), stack);
                    }
                    currentFrameDivergence = intermediateTypes.size() - endTypes.size();
                    intermedate = true;
                }
            }
        }
    }


    /**
     * An argument handler is responsible for resolving offsets of the local variable array in the context of the applied instrumentation.
     */
    public interface ArgumentHandler {

        /**
         * The offset of the {@code this} reference.
         */
        int THIS_REFERENCE = 0;

        /**
         * Resolves an offset relative to an offset of the instrumented method.
         *
         * @param offset The offset to resolve.
         * @return The resolved offset.
         */
        int argument(int offset);

        /**
         * Resolves the offset of the exit value of the exit advice.
         *
         * @return The offset of the exit value.
         */
        int exit();

        /**
         * Resolves the offset of the enter value of the enter advice.
         *
         * @return The offset of the enter value.
         */
        int enter();

        /**
         * Returns the offset of the local variable with the given name.
         *
         * @param name The name of the local variable being accessed.
         * @return The named variable's offset.
         */
        int named(String name);

        /**
         * Resolves the offset of the returned value of the instrumented method.
         *
         * @return The offset of the returned value of the instrumented method.
         */
        int returned();

        /**
         * Resolves the offset of the thrown exception of the instrumented method.
         *
         * @return The offset of the thrown exception of the instrumented method.
         */
        int thrown();

        /**
         * An argument handler that is used for resolving the instrumented method.
         */
        interface ForInstrumentedMethod extends ArgumentHandler {

            /**
             * Resolves a local variable index.
             *
             * @param index The index to resolve.
             * @return The resolved local variable index.
             */
            int variable(int index);

            /**
             * Prepares this argument handler for future offset access.
             *
             * @param methodVisitor The method visitor to which to write any potential byte code.
             * @return The minimum stack size that is required to apply this manipulation.
             */
            int prepare(MethodVisitor methodVisitor);

            /**
             * Binds an advice method as enter advice for this handler.
             *
             * @param adviceMethod The resolved enter advice handler.
             * @return The resolved argument handler for enter advice.
             */
            ForAdvice bindEnter(MethodDescription adviceMethod);

            /**
             * Binds an advice method as exit advice for this handler.
             *
             * @param adviceMethod  The resolved exit advice handler.
             * @param skipThrowable {@code true} if no throwable is stored.
             * @return The resolved argument handler for enter advice.
             */
            ForAdvice bindExit(MethodDescription adviceMethod, boolean skipThrowable);

            /**
             * Returns {@code true} if the original arguments are copied before invoking the instrumented method.
             *
             * @return {@code true} if the original arguments are copied before invoking the instrumented method.
             */
            boolean isCopyingArguments();

            /**
             * Returns a list of the named types in their declared order.
             *
             * @return A list of the named types in their declared order.
             */
            List<TypeDescription> getNamedTypes();

            /**
             * A default implementation of an argument handler for an instrumented method.
             */
            abstract class Default implements ForInstrumentedMethod {

                /**
                 * The instrumented method.
                 */
                protected final MethodDescription instrumentedMethod;

                /**
                 * The exit type or {@code void} if no exit type is defined.
                 */
                protected final TypeDefinition exitType;

                /**
                 * A mapping of all available local variables by their name to their type.
                 */
                protected final SortedMap<String, TypeDefinition> namedTypes;

                /**
                 * The enter type or {@code void} if no enter type is defined.
                 */
                protected final TypeDefinition enterType;

                /**
                 * Creates a new default argument handler for an instrumented method.
                 *
                 * @param instrumentedMethod The instrumented method.
                 * @param exitType           The exit type or {@code void} if no exit type is defined.
                 * @param namedTypes         A mapping of all available local variables by their name to their type.
                 * @param enterType          The enter type or {@code void} if no enter type is defined.
                 */
                protected Default(MethodDescription instrumentedMethod,
                                  TypeDefinition exitType,
                                  SortedMap<String, TypeDefinition> namedTypes,
                                  TypeDefinition enterType) {
                    this.instrumentedMethod = instrumentedMethod;
                    this.namedTypes = namedTypes;
                    this.exitType = exitType;
                    this.enterType = enterType;
                }

                /**
                 * {@inheritDoc}
                 */
                public int exit() {
                    return instrumentedMethod.getStackSize();
                }

                /**
                 * {@inheritDoc}
                 */
                public int named(String name) {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.headMap(name).values());
                }

                /**
                 * {@inheritDoc}
                 */
                public int enter() {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.values());
                }

                /**
                 * {@inheritDoc}
                 */
                public int returned() {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.values())
                            + enterType.getStackSize().getSize();
                }

                /**
                 * {@inheritDoc}
                 */
                public int thrown() {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.values())
                            + enterType.getStackSize().getSize()
                            + instrumentedMethod.getReturnType().getStackSize().getSize();
                }

                /**
                 * {@inheritDoc}
                 */
                public ForAdvice bindEnter(MethodDescription adviceMethod) {
                    return new ForAdvice.Default.ForMethodEnter(instrumentedMethod, adviceMethod, exitType, namedTypes);
                }

                /**
                 * {@inheritDoc}
                 */
                public ForAdvice bindExit(MethodDescription adviceMethod, boolean skipThrowable) {
                    return new ForAdvice.Default.ForMethodExit(instrumentedMethod,
                            adviceMethod,
                            exitType,
                            namedTypes,
                            enterType,
                            skipThrowable ? StackSize.ZERO : StackSize.SINGLE);
                }

                /**
                 * {@inheritDoc}
                 */
                public List<TypeDescription> getNamedTypes() {
                    List<TypeDescription> namedTypes = new ArrayList<>(this.namedTypes.size());
                    for (TypeDefinition typeDefinition : this.namedTypes.values()) {
                        namedTypes.add(typeDefinition.asErasure());
                    }
                    return namedTypes;
                }

                /**
                 * A simple argument handler for an instrumented method.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected static class Simple extends Default {

                    /**
                     * Creates a new simple argument handler for an instrumented method.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param exitType           The exit type or {@code void} if no exit type is defined.
                     * @param namedTypes         A mapping of all available local variables by their name to their type.
                     * @param enterType          The enter type or {@code void} if no enter type is defined.
                     */
                    protected Simple(MethodDescription instrumentedMethod,
                                     TypeDefinition exitType,
                                     SortedMap<String, TypeDefinition> namedTypes,
                                     TypeDefinition enterType) {
                        super(instrumentedMethod, exitType, namedTypes, enterType);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int argument(int offset) {
                        return offset < instrumentedMethod.getStackSize()
                                ? offset
                                : offset + exitType.getStackSize().getSize() + StackSize.of(namedTypes.values()) + enterType.getStackSize().getSize();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int variable(int index) {
                        return index < (instrumentedMethod.isStatic() ? 0 : 1) + instrumentedMethod.getParameters().size()
                                ? index
                                : index + (exitType.represents(void.class) ? 0 : 1) + namedTypes.size() + (enterType.represents(void.class) ? 0 : 1);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public boolean isCopyingArguments() {
                        return false;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int prepare(MethodVisitor methodVisitor) {
                        return 0;
                    }
                }

                /**
                 * An argument handler for an instrumented method that copies all arguments before executing the instrumented method.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected static class Copying extends Default {

                    /**
                     * Creates a new copying argument handler for an instrumented method.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param exitType           The exit type or {@code void} if no exit type is defined.
                     * @param namedTypes         A mapping of all available local variables by their name to their type.
                     * @param enterType          The enter type or {@code void} if no enter type is defined.
                     */
                    protected Copying(MethodDescription instrumentedMethod,
                                      TypeDefinition exitType,
                                      SortedMap<String, TypeDefinition> namedTypes,
                                      TypeDefinition enterType) {
                        super(instrumentedMethod, exitType, namedTypes, enterType);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int argument(int offset) {
                        return instrumentedMethod.getStackSize()
                                + exitType.getStackSize().getSize()
                                + StackSize.of(namedTypes.values())
                                + enterType.getStackSize().getSize()
                                + offset;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int variable(int index) {
                        return (instrumentedMethod.isStatic() ? 0 : 1)
                                + instrumentedMethod.getParameters().size()
                                + (exitType.represents(void.class) ? 0 : 1)
                                + namedTypes.size()
                                + (enterType.represents(void.class) ? 0 : 1)
                                + index;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public boolean isCopyingArguments() {
                        return true;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int prepare(MethodVisitor methodVisitor) {
                        StackSize stackSize;
                        if (!instrumentedMethod.isStatic()) {
                            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                            methodVisitor.visitVarInsn(Opcodes.ASTORE, instrumentedMethod.getStackSize()
                                    + exitType.getStackSize().getSize()
                                    + StackSize.of(namedTypes.values())
                                    + enterType.getStackSize().getSize());
                            stackSize = StackSize.SINGLE;
                        } else {
                            stackSize = StackSize.ZERO;
                        }
                        for (ParameterDescription parameterDescription : instrumentedMethod.getParameters()) {
                            Type type = Type.getType(parameterDescription.getType().asErasure().getDescriptor());
                            methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ILOAD), parameterDescription.getOffset());
                            methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), instrumentedMethod.getStackSize()
                                    + exitType.getStackSize().getSize()
                                    + StackSize.of(namedTypes.values())
                                    + enterType.getStackSize().getSize()
                                    + parameterDescription.getOffset());
                            stackSize = stackSize.maximum(parameterDescription.getType().getStackSize());
                        }
                        return stackSize.getSize();
                    }
                }
            }
        }

        /**
         * An argument handler that is used for resolving an advice method.
         */
        interface ForAdvice extends ArgumentHandler, Advice.ArgumentHandler {

            /**
             * Resolves an offset of the advice method.
             *
             * @param offset The offset to resolve.
             * @return The resolved offset.
             */
            int mapped(int offset);

            /**
             * A default implementation for an argument handler for an advice method.
             */
            abstract class Default implements ArgumentHandler.ForAdvice {

                /**
                 * The instrumented method.
                 */
                protected final MethodDescription instrumentedMethod;

                /**
                 * The advice method.
                 */
                protected final MethodDescription adviceMethod;

                /**
                 * The enter type or {@code void} if no enter type is defined.
                 */
                protected final TypeDefinition exitType;

                /**
                 * A mapping of all available local variables by their name to their type.
                 */
                protected final SortedMap<String, TypeDefinition> namedTypes;

                /**
                 * Creates a new argument handler for an enter advice.
                 *
                 * @param instrumentedMethod The instrumented method.
                 * @param adviceMethod       The advice method.
                 * @param exitType           The exit type or {@code void} if no exit type is defined.
                 * @param namedTypes         A mapping of all available local variables by their name to their type.
                 */
                protected Default(MethodDescription instrumentedMethod,
                                  MethodDescription adviceMethod,
                                  TypeDefinition exitType,
                                  SortedMap<String, TypeDefinition> namedTypes) {
                    this.instrumentedMethod = instrumentedMethod;
                    this.adviceMethod = adviceMethod;
                    this.exitType = exitType;
                    this.namedTypes = namedTypes;
                }

                /**
                 * {@inheritDoc}
                 */
                public int argument(int offset) {
                    return offset;
                }

                /**
                 * {@inheritDoc}
                 */
                public int exit() {
                    return instrumentedMethod.getStackSize();
                }

                /**
                 * {@inheritDoc}
                 */
                public int named(String name) {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.headMap(name).values());
                }

                /**
                 * {@inheritDoc}
                 */
                public int enter() {
                    return instrumentedMethod.getStackSize()
                            + exitType.getStackSize().getSize()
                            + StackSize.of(namedTypes.values());
                }

                /**
                 * An argument handler for an enter advice method.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected static class ForMethodEnter extends Default {

                    /**
                     * Creates a new argument handler for an enter advice method.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param adviceMethod       The advice method.
                     * @param exitType           The exit type or {@code void} if no exit type is defined.
                     * @param namedTypes         A mapping of all available local variables by their name to their type.
                     */
                    protected ForMethodEnter(MethodDescription instrumentedMethod,
                                             MethodDescription adviceMethod,
                                             TypeDefinition exitType,
                                             SortedMap<String, TypeDefinition> namedTypes) {
                        super(instrumentedMethod, adviceMethod, exitType, namedTypes);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int returned() {
                        throw new IllegalStateException("Cannot resolve the return value offset during enter advice");
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int thrown() {
                        throw new IllegalStateException("Cannot resolve the thrown value offset during enter advice");
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int mapped(int offset) {
                        return instrumentedMethod.getStackSize()
                                + exitType.getStackSize().getSize()
                                + StackSize.of(namedTypes.values())
                                - adviceMethod.getStackSize() + offset;
                    }
                }

                /**
                 * An argument handler for an exit advice method.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                protected static class ForMethodExit extends Default {

                    /**
                     * The enter type or {@code void} if no enter type is defined.
                     */
                    private final TypeDefinition enterType;

                    /**
                     * The stack size of a possibly stored throwable.
                     */
                    private final StackSize throwableSize;

                    /**
                     * Creates a new argument handler for an exit advice method.
                     *
                     * @param instrumentedMethod The instrumented method.
                     * @param adviceMethod       The advice method.
                     * @param exitType           The exit type or {@code void} if no exit type is defined.
                     * @param namedTypes         A mapping of all available local variables by their name to their type.
                     * @param enterType          The enter type or {@code void} if no enter type is defined.
                     * @param throwableSize      The stack size of a possibly stored throwable.
                     */
                    protected ForMethodExit(MethodDescription instrumentedMethod,
                                            MethodDescription adviceMethod,
                                            TypeDefinition exitType,
                                            SortedMap<String, TypeDefinition> namedTypes,
                                            TypeDefinition enterType,
                                            StackSize throwableSize) {
                        super(instrumentedMethod, adviceMethod, exitType, namedTypes);
                        this.enterType = enterType;
                        this.throwableSize = throwableSize;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int returned() {
                        return instrumentedMethod.getStackSize()
                                + exitType.getStackSize().getSize()
                                + StackSize.of(namedTypes.values())
                                + enterType.getStackSize().getSize();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int thrown() {
                        return instrumentedMethod.getStackSize()
                                + exitType.getStackSize().getSize()
                                + StackSize.of(namedTypes.values())
                                + enterType.getStackSize().getSize()
                                + instrumentedMethod.getReturnType().getStackSize().getSize();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public int mapped(int offset) {
                        return instrumentedMethod.getStackSize()
                                + exitType.getStackSize().getSize()
                                + StackSize.of(namedTypes.values())
                                + enterType.getStackSize().getSize()
                                + instrumentedMethod.getReturnType().getStackSize().getSize()
                                + throwableSize.getSize()
                                - adviceMethod.getStackSize()
                                + offset;
                    }
                }
            }
        }

        /**
         * A factory for creating an argument handler.
         */
        enum Factory {

            /**
             * A factory for creating a simple argument handler.
             */
            SIMPLE {
                @Override
                protected ForInstrumentedMethod resolve(MethodDescription instrumentedMethod,
                                                        TypeDefinition enterType,
                                                        TypeDefinition exitType,
                                                        SortedMap<String, TypeDefinition> namedTypes) {
                    return new ForInstrumentedMethod.Default.Simple(instrumentedMethod,
                            exitType,
                            namedTypes,
                            enterType);
                }
            },

            /**
             * A factory for creating an argument handler that copies all arguments before executing the instrumented method.
             */
            COPYING {
                @Override
                protected ForInstrumentedMethod resolve(MethodDescription instrumentedMethod,
                                                        TypeDefinition enterType,
                                                        TypeDefinition exitType,
                                                        SortedMap<String, TypeDefinition> namedTypes) {
                    return new ForInstrumentedMethod.Default.Copying(instrumentedMethod,
                            exitType,
                            namedTypes,
                            enterType);
                }
            };

            /**
             * Creates an argument handler.
             *
             * @param instrumentedMethod The instrumented method.
             * @param enterType          The enter type or {@code void} if no such type is defined.
             * @param exitType           The exit type or {@code void} if no exit type is defined.
             * @param namedTypes         A mapping of all available local variables by their name to their type.
             * @return An argument handler for the instrumented method.
             */
            protected abstract ForInstrumentedMethod resolve(MethodDescription instrumentedMethod,
                                                             TypeDefinition enterType,
                                                             TypeDefinition exitType,
                                                             SortedMap<String, TypeDefinition> namedTypes);
        }
    }


    /**
     * Represents an offset mapping for an advice method to an alternative offset.
     */
    public interface OffsetMapping {

        /**
         * Resolves an offset mapping to a given target offset.
         *
         * @param instrumentedType   The instrumented type.
         * @param instrumentedMethod The instrumented method for which the mapping is to be resolved.
         * @param assigner           The assigner to use.
         * @param argumentHandler    The argument handler to use for resolving offsets of the local variable array of the instrumented method.
         * @param sort               The sort of the advice method being resolved.
         * @return A suitable target mapping.
         */
        Target resolve(TypeDescription instrumentedType,
                       MethodDescription instrumentedMethod,
                       Assigner assigner,
                       ArgumentHandler argumentHandler,
                       Sort sort);

        /**
         * A target offset of an offset mapping.
         */
        interface Target {

            /**
             * Resolves a read instruction.
             *
             * @return A stack manipulation that represents a reading of an advice parameter.
             */
            StackManipulation resolveRead();

            /**
             * Resolves a write instruction.
             *
             * @return A stack manipulation that represents a writing to an advice parameter.
             */
            StackManipulation resolveWrite();

            /**
             * Resolves an increment instruction.
             *
             * @param value The incrementation value.
             * @return A stack manipulation that represents a writing to an advice parameter.
             */
            StackManipulation resolveIncrement(int value);

            /**
             * An adapter class for a target that only can be read.
             */
            abstract class AbstractReadOnlyAdapter implements Target {

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveWrite() {
                    throw new IllegalStateException("Cannot write to read-only value");
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveIncrement(int value) {
                    throw new IllegalStateException("Cannot write to read-only value");
                }
            }

            /**
             * A target for an offset mapping that represents a non-operational value. All writes are discarded and a value's
             * default value is returned upon every read.
             */
            @HashCodeAndEqualsPlugin.Enhance
            abstract class ForDefaultValue implements Target {

                /**
                 * The represented type.
                 */
                protected final TypeDefinition typeDefinition;

                /**
                 * A stack manipulation to apply after a read instruction.
                 */
                protected final StackManipulation readAssignment;

                /**
                 * Creates a new target for a default value.
                 *
                 * @param typeDefinition The represented type.
                 * @param readAssignment A stack manipulation to apply after a read instruction.
                 */
                protected ForDefaultValue(TypeDefinition typeDefinition, StackManipulation readAssignment) {
                    this.typeDefinition = typeDefinition;
                    this.readAssignment = readAssignment;
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveRead() {
                    return new StackManipulation.Compound(DefaultValue.of(typeDefinition), readAssignment);
                }

                /**
                 * A read-only target for a default value.
                 */
                public static class ReadOnly extends ForDefaultValue {

                    /**
                     * Creates a new writable target for a default value.
                     *
                     * @param typeDefinition The represented type.
                     */
                    public ReadOnly(TypeDefinition typeDefinition) {
                        this(typeDefinition, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a new -writable target for a default value.
                     *
                     * @param typeDefinition The represented type.
                     * @param readAssignment A stack manipulation to apply after a read instruction.
                     */
                    public ReadOnly(TypeDefinition typeDefinition, StackManipulation readAssignment) {
                        super(typeDefinition, readAssignment);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        throw new IllegalStateException("Cannot write to read-only default value");
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        throw new IllegalStateException("Cannot write to read-only default value");
                    }
                }

                /**
                 * A read-write target for a default value.
                 */
                public static class ReadWrite extends ForDefaultValue {

                    /**
                     * Creates a new read-only target for a default value.
                     *
                     * @param typeDefinition The represented type.
                     */
                    public ReadWrite(TypeDefinition typeDefinition) {
                        this(typeDefinition, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a new read-only target for a default value.
                     *
                     * @param typeDefinition The represented type.
                     * @param readAssignment A stack manipulation to apply after a read instruction.
                     */
                    public ReadWrite(TypeDefinition typeDefinition, StackManipulation readAssignment) {
                        super(typeDefinition, readAssignment);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        return Removal.of(typeDefinition);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        return StackManipulation.Trivial.INSTANCE;
                    }
                }
            }

            /**
             * A target for an offset mapping that represents a local variable.
             */
            @HashCodeAndEqualsPlugin.Enhance
            abstract class ForVariable implements Target {

                /**
                 * The represented type.
                 */
                protected final TypeDefinition typeDefinition;

                /**
                 * The value's offset.
                 */
                protected final int offset;

                /**
                 * An assignment to execute upon reading a value.
                 */
                protected final StackManipulation readAssignment;

                /**
                 * Creates a new target for a local variable mapping.
                 *
                 * @param typeDefinition The represented type.
                 * @param offset         The value's offset.
                 * @param readAssignment An assignment to execute upon reading a value.
                 */
                protected ForVariable(TypeDefinition typeDefinition, int offset, StackManipulation readAssignment) {
                    this.typeDefinition = typeDefinition;
                    this.offset = offset;
                    this.readAssignment = readAssignment;
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveRead() {
                    return new StackManipulation.Compound(MethodVariableAccess.of(typeDefinition).loadFrom(offset), readAssignment);
                }

                /**
                 * A target for a read-only mapping of a local variable.
                 */
                public static class ReadOnly extends ForVariable {

                    /**
                     * Creates a read-only mapping for a local variable.
                     *
                     * @param typeDefinition The represented type.
                     * @param offset         The value's offset.
                     */
                    public ReadOnly(TypeDefinition typeDefinition, int offset) {
                        this(typeDefinition, offset, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a read-only mapping for a local variable.
                     *
                     * @param typeDefinition The represented type.
                     * @param offset         The value's offset.
                     * @param readAssignment An assignment to execute upon reading a value.
                     */
                    public ReadOnly(TypeDefinition typeDefinition, int offset, StackManipulation readAssignment) {
                        super(typeDefinition, offset, readAssignment);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        throw new IllegalStateException("Cannot write to read-only parameter " + typeDefinition + " at " + offset);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        throw new IllegalStateException("Cannot write to read-only variable " + typeDefinition + " at " + offset);
                    }
                }

                /**
                 * A target for a writable mapping of a local variable.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class ReadWrite extends ForVariable {

                    /**
                     * A stack manipulation to apply upon a write to the variable.
                     */
                    private final StackManipulation writeAssignment;

                    /**
                     * Creates a new target mapping for a writable local variable.
                     *
                     * @param typeDefinition The represented type.
                     * @param offset         The value's offset.
                     */
                    public ReadWrite(TypeDefinition typeDefinition, int offset) {
                        this(typeDefinition, offset, StackManipulation.Trivial.INSTANCE, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a new target mapping for a writable local variable.
                     *
                     * @param typeDefinition  The represented type.
                     * @param offset          The value's offset.
                     * @param readAssignment  An assignment to execute upon reading a value.
                     * @param writeAssignment A stack manipulation to apply upon a write to the variable.
                     */
                    public ReadWrite(TypeDefinition typeDefinition, int offset, StackManipulation readAssignment, StackManipulation writeAssignment) {
                        super(typeDefinition, offset, readAssignment);
                        this.writeAssignment = writeAssignment;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        return new StackManipulation.Compound(writeAssignment, MethodVariableAccess.of(typeDefinition).storeAt(offset));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        return typeDefinition.represents(int.class)
                                ? MethodVariableAccess.of(typeDefinition).increment(offset, value)
                                : new StackManipulation.Compound(resolveRead(), IntegerConstant.forValue(1), Addition.INTEGER, resolveWrite());
                    }
                }
            }

            /**
             * A target mapping for an array of all local variables.
             */
            @HashCodeAndEqualsPlugin.Enhance
            abstract class ForArray implements Target {

                /**
                 * The compound target type.
                 */
                protected final TypeDescription.Generic target;

                /**
                 * The stack manipulations to apply upon reading a variable array.
                 */
                protected final List<? extends StackManipulation> valueReads;

                /**
                 * Creates a new target mapping for an array of all local variables.
                 *
                 * @param target     The compound target type.
                 * @param valueReads The stack manipulations to apply upon reading a variable array.
                 */
                protected ForArray(TypeDescription.Generic target, List<? extends StackManipulation> valueReads) {
                    this.target = target;
                    this.valueReads = valueReads;
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveRead() {
                    return ArrayFactory.forType(target).withValues(valueReads);
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveIncrement(int value) {
                    throw new IllegalStateException("Cannot increment read-only array value");
                }

                /**
                 * A target mapping for a read-only target mapping for an array of local variables.
                 */
                public static class ReadOnly extends ForArray {

                    /**
                     * Creates a read-only target mapping for an array of all local variables.
                     *
                     * @param target     The compound target type.
                     * @param valueReads The stack manipulations to apply upon reading a variable array.
                     */
                    public ReadOnly(TypeDescription.Generic target, List<? extends StackManipulation> valueReads) {
                        super(target, valueReads);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        throw new IllegalStateException("Cannot write to read-only array value");
                    }
                }

                /**
                 * A target mapping for a writable target mapping for an array of local variables.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class ReadWrite extends ForArray {

                    /**
                     * The stack manipulations to apply upon writing to a variable array.
                     */
                    private final List<? extends StackManipulation> valueWrites;

                    /**
                     * Creates a writable target mapping for an array of all local variables.
                     *
                     * @param target      The compound target type.
                     * @param valueReads  The stack manipulations to apply upon reading a variable array.
                     * @param valueWrites The stack manipulations to apply upon writing to a variable array.
                     */
                    public ReadWrite(TypeDescription.Generic target,
                                     List<? extends StackManipulation> valueReads,
                                     List<? extends StackManipulation> valueWrites) {
                        super(target, valueReads);
                        this.valueWrites = valueWrites;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        return new StackManipulation.Compound(ArrayAccess.of(target).forEach(valueWrites), Removal.SINGLE);
                    }
                }
            }

            /**
             * A target for an offset mapping that loads a field value.
             */
            @HashCodeAndEqualsPlugin.Enhance
            abstract class ForField implements Target {

                /**
                 * The field value to load.
                 */
                protected final FieldDescription fieldDescription;

                /**
                 * The stack manipulation to apply upon a read.
                 */
                protected final StackManipulation readAssignment;

                /**
                 * Creates a new target for a field value mapping.
                 *
                 * @param fieldDescription The field value to load.
                 * @param readAssignment   The stack manipulation to apply upon a read.
                 */
                protected ForField(FieldDescription fieldDescription, StackManipulation readAssignment) {
                    this.fieldDescription = fieldDescription;
                    this.readAssignment = readAssignment;
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveRead() {
                    return new StackManipulation.Compound(fieldDescription.isStatic()
                            ? StackManipulation.Trivial.INSTANCE
                            : MethodVariableAccess.loadThis(), FieldAccess.forField(fieldDescription).read(), readAssignment);
                }

                /**
                 * A read-only mapping for a field value.
                 */
                public static class ReadOnly extends ForField {

                    /**
                     * Creates a new read-only mapping for a field.
                     *
                     * @param fieldDescription The field value to load.
                     */
                    public ReadOnly(FieldDescription fieldDescription) {
                        this(fieldDescription, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a new read-only mapping for a field.
                     *
                     * @param fieldDescription The field value to load.
                     * @param readAssignment   The stack manipulation to apply upon a read.
                     */
                    public ReadOnly(FieldDescription fieldDescription, StackManipulation readAssignment) {
                        super(fieldDescription, readAssignment);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        throw new IllegalStateException("Cannot write to read-only field value");
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        throw new IllegalStateException("Cannot write to read-only field value");
                    }
                }

                /**
                 * A mapping for a writable field.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class ReadWrite extends ForField {

                    /**
                     * An assignment to apply prior to a field write.
                     */
                    private final StackManipulation writeAssignment;

                    /**
                     * Creates a new target for a writable field.
                     *
                     * @param fieldDescription The field value to load.
                     */
                    public ReadWrite(FieldDescription fieldDescription) {
                        this(fieldDescription, StackManipulation.Trivial.INSTANCE, StackManipulation.Trivial.INSTANCE);
                    }

                    /**
                     * Creates a new target for a writable field.
                     *
                     * @param fieldDescription The field value to load.
                     * @param readAssignment   The stack manipulation to apply upon a read.
                     * @param writeAssignment  An assignment to apply prior to a field write.
                     */
                    public ReadWrite(FieldDescription fieldDescription,
                                     StackManipulation readAssignment, StackManipulation writeAssignment) {
                        super(fieldDescription, readAssignment);
                        this.writeAssignment = writeAssignment;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        StackManipulation preparation;
                        if (fieldDescription.isStatic()) {
                            preparation = StackManipulation.Trivial.INSTANCE;
                        } else {
                            preparation = new StackManipulation.Compound(
                                    MethodVariableAccess.loadThis(),
                                    Duplication.SINGLE.flipOver(fieldDescription.getType()),
                                    Removal.SINGLE
                            );
                        }
                        return new StackManipulation.Compound(writeAssignment, preparation, FieldAccess.forField(fieldDescription).write());
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        return new StackManipulation.Compound(
                                resolveRead(),
                                IntegerConstant.forValue(value),
                                Addition.INTEGER,
                                resolveWrite()
                        );
                    }
                }
            }

            /**
             * A target for an offset mapping that represents a read-only stack manipulation.
             */
            @HashCodeAndEqualsPlugin.Enhance
            class ForStackManipulation implements Target {

                /**
                 * The represented stack manipulation.
                 */
                private final StackManipulation stackManipulation;

                /**
                 * Creates a new target for an offset mapping for a stack manipulation.
                 *
                 * @param stackManipulation The represented stack manipulation.
                 */
                public ForStackManipulation(StackManipulation stackManipulation) {
                    this.stackManipulation = stackManipulation;
                }

                /**
                 * Creates a target for a {@link Method} or {@link Constructor} constant.
                 *
                 * @param methodDescription The method or constructor to represent.
                 * @return A mapping for a method or constructor constant.
                 */
                public static Target of(MethodDescription.InDefinedShape methodDescription) {
                    return new ForStackManipulation(MethodConstant.of(methodDescription));
                }

                /**
                 * Creates a target for an offset mapping for a type constant.
                 *
                 * @param typeDescription The type constant to represent.
                 * @return A mapping for a type constant.
                 */
                public static Target of(TypeDescription typeDescription) {
                    return new ForStackManipulation(ClassConstant.of(typeDescription));
                }

                /**
                 * Creates a target for an offset mapping for a constant value or {@code null}.
                 *
                 * @param value The constant value to represent or {@code null}.
                 * @return An appropriate target for an offset mapping.
                 */
                public static Target of(Object value) {
                    if (value == null) {
                        return new ForStackManipulation(NullConstant.INSTANCE);
                    } else if (value instanceof Boolean) {
                        return new ForStackManipulation(IntegerConstant.forValue((Boolean) value));
                    } else if (value instanceof Byte) {
                        return new ForStackManipulation(IntegerConstant.forValue((Byte) value));
                    } else if (value instanceof Short) {
                        return new ForStackManipulation(IntegerConstant.forValue((Short) value));
                    } else if (value instanceof Character) {
                        return new ForStackManipulation(IntegerConstant.forValue((Character) value));
                    } else if (value instanceof Integer) {
                        return new ForStackManipulation(IntegerConstant.forValue((Integer) value));
                    } else if (value instanceof Long) {
                        return new ForStackManipulation(LongConstant.forValue((Long) value));
                    } else if (value instanceof Float) {
                        return new ForStackManipulation(FloatConstant.forValue((Float) value));
                    } else if (value instanceof Double) {
                        return new ForStackManipulation(DoubleConstant.forValue((Double) value));
                    } else if (value instanceof String) {
                        return new ForStackManipulation(new TextConstant((String) value));
                    } else if (value instanceof Enum<?>) {
                        return new ForStackManipulation(FieldAccess
                            .forEnumeration(new EnumerationDescription.ForLoadedEnumeration((Enum<?>) value)));
                    } else if (value instanceof EnumerationDescription) {
                        return new ForStackManipulation(FieldAccess.forEnumeration((EnumerationDescription) value));
                    } else if (value instanceof Class<?>) {
                        return new ForStackManipulation(ClassConstant.of(TypeDescription.ForLoadedType.of((Class<?>) value)));
                    } else if (value instanceof TypeDescription) {
                        return new ForStackManipulation(ClassConstant.of((TypeDescription) value));
                    } else if (JavaType.METHOD_HANDLE.isInstance(value)) {
                        return new ForStackManipulation(new JavaConstantValue(JavaConstant.MethodHandle.ofLoaded(value)));
                    } else if (JavaType.METHOD_TYPE.isInstance(value)) {
                        return new ForStackManipulation(new JavaConstantValue(JavaConstant.MethodType.ofLoaded(value)));
                    } else if (value instanceof JavaConstant) {
                        return new ForStackManipulation(new JavaConstantValue((JavaConstant) value));
                    } else {
                        throw new IllegalArgumentException("Not a constant value: " + value);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveRead() {
                    return stackManipulation;
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveWrite() {
                    throw new IllegalStateException("Cannot write to constant value: " + stackManipulation);
                }

                /**
                 * {@inheritDoc}
                 */
                public StackManipulation resolveIncrement(int value) {
                    throw new IllegalStateException("Cannot write to constant value: " + stackManipulation);
                }

                /**
                 * A constant value that can be written to.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class Writable implements Target {

                    /**
                     * The reading stack manipulation.
                     */
                    private final StackManipulation read;

                    /**
                     * The writing stack manipulation.
                     */
                    private final StackManipulation write;

                    /**
                     * Creates a writable target.
                     *
                     * @param read  The reading stack manipulation.
                     * @param write The writing stack manipulation.
                     */
                    public Writable(StackManipulation read, StackManipulation write) {
                        this.read = read;
                        this.write = write;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveRead() {
                        return read;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveWrite() {
                        return write;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public StackManipulation resolveIncrement(int value) {
                        throw new IllegalStateException("Cannot increment mutable constant value: " + write);
                    }
                }
            }
        }

        /**
         * Represents a factory for creating a {@link OffsetMapping} for a given parameter for a given annotation.
         *
         * @param <T> The annotation type that triggers this factory.
         */
        interface Factory<T extends Annotation> {

            /**
             * Returns the annotation type of this factory.
             *
             * @return The factory's annotation type.
             */
            Class<T> getAnnotationType();

            /**
             * Creates a new offset mapping for the supplied parameter if possible.
             *
             * @param target     The parameter description for which to resolve an offset mapping.
             * @param annotation The annotation that triggered this factory.
             * @param adviceType {@code true} if the binding is applied using advice method delegation.
             * @return A resolved offset mapping or {@code null} if no mapping can be resolved for this parameter.
             */
            OffsetMapping make(ParameterDescription.InDefinedShape target,
                               AnnotationDescription.Loadable<T> annotation, AdviceType adviceType);

            /**
             * Describes the type of advice being applied.
             */
            enum AdviceType {

                /**
                 * Indicates advice where the invocation is delegated.
                 */
                DELEGATION(true),

                /**
                 * Indicates advice where the invocation's code is copied into the target method.
                 */
                INLINING(false);

                /**
                 * {@code true} if delegation is used.
                 */
                private final boolean delegation;

                /**
                 * Creates a new advice type.
                 *
                 * @param delegation {@code true} if delegation is used.
                 */
                AdviceType(boolean delegation) {
                    this.delegation = delegation;
                }

                /**
                 * Returns {@code true} if delegation is used.
                 *
                 * @return {@code true} if delegation is used.
                 */
                public boolean isDelegation() {
                    return delegation;
                }
            }

            /**
             * A simple factory that binds a constant offset mapping.
             *
             * @param <T> The annotation type that represents this offset mapping.
             */
            @HashCodeAndEqualsPlugin.Enhance
            class Simple<T extends Annotation> implements Factory<T> {

                /**
                 * The annotation type being bound.
                 */
                private final Class<T> annotationType;

                /**
                 * The fixed offset mapping.
                 */
                private final OffsetMapping offsetMapping;

                /**
                 * Creates a simple factory for a simple binding for an offset mapping.
                 *
                 * @param annotationType The annotation type being bound.
                 * @param offsetMapping  The fixed offset mapping.
                 */
                public Simple(Class<T> annotationType, OffsetMapping offsetMapping) {
                    this.annotationType = annotationType;
                    this.offsetMapping = offsetMapping;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    return offsetMapping;
                }
            }

            /**
             * A factory for an annotation whose use is not permitted.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            class Illegal<T extends Annotation> implements Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * Creates a factory that does not permit the usage of the represented annotation.
                 *
                 * @param annotationType The annotation type.
                 */
                public Illegal(Class<T> annotationType) {
                    this.annotationType = annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    throw new IllegalStateException("Usage of " + annotationType + " is not allowed on " + target);
                }
            }
        }

        /**
         * Describes the sort of the executed advice.
         */
        enum Sort {

            /**
             * Indicates that an offset is mapped for an enter advice.
             */
            ENTER {
                @Override
                public boolean isPremature(MethodDescription methodDescription) {
                    return methodDescription.isConstructor();
                }
            },

            /**
             * Indicates that an offset is mapped for an exit advice.
             */
            EXIT {
                @Override
                public boolean isPremature(MethodDescription methodDescription) {
                    return false;
                }
            };

            /**
             * Checks if an advice is executed in a premature state, i.e. the instrumented method is a constructor where the super constructor is not
             * yet invoked. In this case, the {@code this} reference is not yet initialized and therefore not available.
             *
             * @param methodDescription The instrumented method.
             * @return {@code true} if the advice is executed premature for the instrumented method.
             */
            public abstract boolean isPremature(MethodDescription methodDescription);
        }

        /**
         * An offset mapping for a given parameter of the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        abstract class ForArgument implements OffsetMapping {

            /**
             * The type expected by the advice method.
             */
            protected final TypeDescription.Generic target;

            /**
             * Determines if the parameter is to be treated as read-only.
             */
            protected final boolean readOnly;

            /**
             * The typing to apply when assigning values.
             */
            private final Assigner.Typing typing;

            /**
             * Creates a new offset mapping for a parameter of the instrumented method.
             *
             * @param target   The type expected by the advice method.
             * @param readOnly Determines if the parameter is to be treated as read-only.
             * @param typing   The typing to apply.
             */
            protected ForArgument(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                ParameterDescription parameterDescription = resolve(instrumentedMethod);
                StackManipulation readAssignment = assigner.assign(parameterDescription.getType(), target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + parameterDescription + " to " + target);
                } else if (readOnly) {
                    return new Target.ForVariable.ReadOnly(parameterDescription.getType(), argumentHandler.argument(parameterDescription.getOffset()), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, parameterDescription.getType(), typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + parameterDescription + " to " + target);
                    }
                    return new Target.ForVariable.ReadWrite(parameterDescription.getType(), argumentHandler.argument(parameterDescription.getOffset()), readAssignment, writeAssignment);
                }
            }

            /**
             * Resolves the bound parameter.
             *
             * @param instrumentedMethod The instrumented method.
             * @return The bound parameter.
             */
            protected abstract ParameterDescription resolve(MethodDescription instrumentedMethod);

            /**
             * An offset mapping for a parameter of the instrumented method with a specific index.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class Unresolved extends ForArgument {

                /**
                 * The index of the parameter.
                 */
                private final int index;

                /**
                 * {@code true} if the parameter binding is optional.
                 */
                private final boolean optional;

                /**
                 * Creates a new offset binding for a parameter with a given index.
                 *
                 * @param target     The target type.
                 * @param annotation The annotation that triggers this binding.
                 */
                protected Unresolved(TypeDescription.Generic target, AnnotationDescription.Loadable<Argument> annotation) {
                    this(target, annotation.getValue(Factory.ARGUMENT_READ_ONLY).resolve(Boolean.class),
                            annotation.getValue(Factory.ARGUMENT_TYPING)
                                .load(Argument.class.getClassLoader()).resolve(Assigner.Typing.class),
                            annotation.getValue(Factory.ARGUMENT_VALUE).resolve(Integer.class),
                            annotation.getValue(Factory.ARGUMENT_OPTIONAL).resolve(Boolean.class));
                }

                /**
                 * Creates a new offset binding for a parameter with a given index.
                 *
                 * @param parameterDescription The parameter triggering this binding.
                 */
                protected Unresolved(ParameterDescription parameterDescription) {
                    this(parameterDescription.getType(),
                        true, Assigner.Typing.STATIC, parameterDescription.getIndex());
                }

                /**
                 * Creates a non-optional offset binding for a parameter with a given index.
                 *
                 * @param target   The type expected by the advice method.
                 * @param readOnly Determines if the parameter is to be treated as read-only.
                 * @param typing   The typing to apply.
                 * @param index    The index of the parameter.
                 */
                public Unresolved(TypeDescription.Generic target,
                                  boolean readOnly, Assigner.Typing typing, int index) {
                    this(target, readOnly, typing, index, false);
                }

                /**
                 * Creates a new offset binding for a parameter with a given index.
                 *
                 * @param target   The type expected by the advice method.
                 * @param readOnly Determines if the parameter is to be treated as read-only.
                 * @param typing   The typing to apply.
                 * @param index    The index of the parameter.
                 * @param optional {@code true} if the parameter binding is optional.
                 */
                public Unresolved(TypeDescription.Generic target,
                                  boolean readOnly, Assigner.Typing typing, int index, boolean optional) {
                    super(target, readOnly, typing);
                    this.index = index;
                    this.optional = optional;
                }

                @Override
                protected ParameterDescription resolve(MethodDescription instrumentedMethod) {
                    ParameterList<?> parameters = instrumentedMethod.getParameters();
                    if (parameters.size() <= index) {
                        throw new IllegalStateException(instrumentedMethod + " does not define an index " + index);
                    } else {
                        return parameters.get(index);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public Target resolve(TypeDescription instrumentedType,
                                      MethodDescription instrumentedMethod,
                                      Assigner assigner,
                                      ArgumentHandler argumentHandler,
                                      Sort sort) {
                    if (optional && instrumentedMethod.getParameters().size() <= index) {
                        return readOnly
                                ? new Target.ForDefaultValue.ReadOnly(target)
                                : new Target.ForDefaultValue.ReadWrite(target);
                    }
                    return super.resolve(instrumentedType, instrumentedMethod, assigner, argumentHandler, sort);
                }

                /**
                 * A factory for a mapping of a parameter of the instrumented method.
                 */
                protected enum Factory implements OffsetMapping.Factory<Argument> {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * A description of the {@link Argument#value()} method.
                     */
                    private static final MethodDescription.InDefinedShape ARGUMENT_VALUE;

                    /**
                     * A description of the {@link Argument#readOnly()} method.
                     */
                    private static final MethodDescription.InDefinedShape ARGUMENT_READ_ONLY;

                    /**
                     * A description of the {@link Argument#typing()} method.
                     */
                    private static final MethodDescription.InDefinedShape ARGUMENT_TYPING;

                    /**
                     * A description of the {@link Argument#optional()} method.
                     */
                    private static final MethodDescription.InDefinedShape ARGUMENT_OPTIONAL;

                    /*
                     * Resolves annotation properties.
                     */
                    static {
                        MethodList<MethodDescription.InDefinedShape> methods = TypeDescription
                            .ForLoadedType.of(Argument.class).getDeclaredMethods();
                        ARGUMENT_VALUE = methods.filter(named("value")).getOnly();
                        ARGUMENT_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                        ARGUMENT_TYPING = methods.filter(named("typing")).getOnly();
                        ARGUMENT_OPTIONAL = methods.filter(named("optional")).getOnly();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Class<Argument> getAnnotationType() {
                        return Argument.class;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                              AnnotationDescription.Loadable<Argument> annotation,
                                              AdviceType adviceType) {
                        if (adviceType.isDelegation()
                            && !annotation.getValue(ARGUMENT_READ_ONLY).resolve(Boolean.class)) {
                            throw new IllegalStateException("Cannot define writable field access for "
                                + target + " when using delegation");
                        } else {
                            return new ForArgument.Unresolved(target.getType(), annotation);
                        }
                    }
                }
            }

            /**
             * An offset mapping for a specific parameter of the instrumented method.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class Resolved extends ForArgument {

                /**
                 * The parameter being bound.
                 */
                private final ParameterDescription parameterDescription;

                /**
                 * Creates an offset mapping that binds a parameter of the instrumented method.
                 *
                 * @param target               The type expected by the advice method.
                 * @param readOnly             Determines if the parameter is to be treated as read-only.
                 * @param typing               The typing to apply.
                 * @param parameterDescription The parameter being bound.
                 */
                public Resolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, ParameterDescription parameterDescription) {
                    super(target, readOnly, typing);
                    this.parameterDescription = parameterDescription;
                }

                @Override
                protected ParameterDescription resolve(MethodDescription instrumentedMethod) {
                    if (!parameterDescription.getDeclaringMethod().equals(instrumentedMethod)) {
                        throw new IllegalStateException(parameterDescription + " is not a parameter of " + instrumentedMethod);
                    }
                    return parameterDescription;
                }

                /**
                 * A factory for a parameter argument of the instrumented method.
                 *
                 * @param <T> The type of the bound annotation.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {

                    /**
                     * The annotation type.
                     */
                    private final Class<T> annotationType;

                    /**
                     * The bound parameter.
                     */
                    private final ParameterDescription parameterDescription;

                    /**
                     * {@code true} if the factory should create a read-only binding.
                     */
                    private final boolean readOnly;

                    /**
                     * The typing to use.
                     */
                    private final Assigner.Typing typing;

                    /**
                     * Creates a new factory for binding a parameter of the instrumented method with read-only semantics and static typing.
                     *
                     * @param annotationType       The annotation type.
                     * @param parameterDescription The bound parameter.
                     */
                    public Factory(Class<T> annotationType, ParameterDescription parameterDescription) {
                        this(annotationType, parameterDescription, true, Assigner.Typing.STATIC);
                    }

                    /**
                     * Creates a new factory for binding a parameter of the instrumented method.
                     *
                     * @param annotationType       The annotation type.
                     * @param parameterDescription The bound parameter.
                     * @param readOnly             {@code true} if the factory should create a read-only binding.
                     * @param typing               The typing to use.
                     */
                    public Factory(Class<T> annotationType, ParameterDescription parameterDescription,
                                   boolean readOnly, Assigner.Typing typing) {
                        this.annotationType = annotationType;
                        this.parameterDescription = parameterDescription;
                        this.readOnly = readOnly;
                        this.typing = typing;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Class<T> getAnnotationType() {
                        return annotationType;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                              AnnotationDescription.Loadable<T> annotation,
                                              AdviceType adviceType) {
                        return new Resolved(target.getType(), readOnly, typing, parameterDescription);
                    }
                }
            }
        }

        /**
         * An offset mapping that provides access to the {@code this} reference of the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForThisReference implements OffsetMapping {

            /**
             * The type that the advice method expects for the {@code this} reference.
             */
            private final TypeDescription.Generic target;

            /**
             * Determines if the parameter is to be treated as read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * {@code true} if the parameter should be bound to {@code null} if the instrumented method is static.
             */
            private final boolean optional;

            /**
             * Creates a new offset mapping for a {@code this} reference.
             *
             * @param target     The type that the advice method expects for the {@code this} reference.
             * @param annotation The mapped annotation.
             */
            protected ForThisReference(TypeDescription.Generic target, AnnotationDescription.Loadable<This> annotation) {
                this(target,
                        annotation.getValue(Factory.THIS_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.THIS_TYPING).load(This.class.getClassLoader()).resolve(Assigner.Typing.class),
                        annotation.getValue(Factory.THIS_OPTIONAL).resolve(Boolean.class));
            }

            /**
             * Creates a new offset mapping for a {@code this} reference.
             *
             * @param target   The type that the advice method expects for the {@code this} reference.
             * @param readOnly Determines if the parameter is to be treated as read-only.
             * @param typing   The typing to apply.
             * @param optional {@code true} if the parameter should be bound to {@code null} if the instrumented method is static.
             */
            public ForThisReference(TypeDescription.Generic target,
                                    boolean readOnly, Assigner.Typing typing, boolean optional) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
                this.optional = optional;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                if (instrumentedMethod.isStatic() || sort.isPremature(instrumentedMethod)) {
                    if (optional) {
                        return readOnly
                                ? new Target.ForDefaultValue.ReadOnly(instrumentedType)
                                : new Target.ForDefaultValue.ReadWrite(instrumentedType);
                    } else {
                        throw new IllegalStateException(
                            "Cannot map this reference for static method or constructor start: " + instrumentedMethod);
                    }
                }
                StackManipulation readAssignment = assigner.assign(instrumentedType.asGenericType(), target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + instrumentedType + " to " + target);
                } else if (readOnly) {
                    return new Target.ForVariable.ReadOnly(instrumentedType.asGenericType(),
                        argumentHandler.argument(ArgumentHandler.THIS_REFERENCE), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, instrumentedType.asGenericType(), typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to " + instrumentedType);
                    }
                    return new Target.ForVariable.ReadWrite(instrumentedType.asGenericType(),
                        argumentHandler.argument(ArgumentHandler.THIS_REFERENCE), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for creating a {@link ForThisReference} offset mapping.
             */
            protected enum Factory implements OffsetMapping.Factory<This> {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * A description of the {@link This#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape THIS_READ_ONLY;

                /**
                 * A description of the {@link This#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape THIS_TYPING;

                /**
                 * A description of the {@link This#optional()} method.
                 */
                private static final MethodDescription.InDefinedShape THIS_OPTIONAL;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(This.class).getDeclaredMethods();
                    THIS_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    THIS_TYPING = methods.filter(named("typing")).getOnly();
                    THIS_OPTIONAL = methods.filter(named("optional")).getOnly();
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<This> getAnnotationType() {
                    return This.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<This> annotation,
                                          AdviceType adviceType) {
                    if (adviceType.isDelegation() && !annotation.getValue(THIS_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot write to this reference for " + target + " in read-only context");
                    } else {
                        return new ForThisReference(target.getType(), annotation);
                    }
                }
            }
        }

        /**
         * An offset mapping that maps an array containing all arguments of the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForAllArguments implements OffsetMapping {

            /**
             * The component target type.
             */
            private final TypeDescription.Generic target;

            /**
             * {@code true} if the array is read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * {@code true} if a {@code null} value should be assigned if the
             * instrumented method does not declare any parameters.
             */
            private final boolean nullIfEmpty;

            /**
             * Creates a new offset mapping for an array containing all arguments.
             *
             * @param target     The component target type.
             * @param annotation The mapped annotation.
             */
            protected ForAllArguments(TypeDescription.Generic target,
                                      AnnotationDescription.Loadable<AllArguments> annotation) {
                this(target, annotation.getValue(Factory.ALL_ARGUMENTS_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.ALL_ARGUMENTS_TYPING)
                            .load(AllArguments.class.getClassLoader()).resolve(Assigner.Typing.class),
                        annotation.getValue(Factory.ALL_ARGUMENTS_NULL_IF_EMPTY).resolve(Boolean.class));
            }

            /**
             * Creates a new offset mapping for an array containing all arguments.
             *
             * @param target      The component target type.
             * @param readOnly    {@code true} if the array is read-only.
             * @param typing      The typing to apply.
             * @param nullIfEmpty {@code true} if a {@code null} value should be assigned if the
             *                    instrumented method does not declare any parameters.
             */
            public ForAllArguments(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, boolean nullIfEmpty) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
                this.nullIfEmpty = nullIfEmpty;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                if (nullIfEmpty && instrumentedMethod.getParameters().isEmpty()) {
                    return readOnly
                            ? new Target.ForStackManipulation(NullConstant.INSTANCE)
                            : new Target.ForStackManipulation.Writable(NullConstant.INSTANCE, Removal.SINGLE);
                }
                List<StackManipulation> valueReads = new ArrayList(instrumentedMethod.getParameters().size());
                for (ParameterDescription parameterDescription : instrumentedMethod.getParameters()) {
                    StackManipulation readAssignment = assigner.assign(parameterDescription.getType(), target, typing);
                    if (!readAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + parameterDescription + " to " + target);
                    }
                    valueReads.add(new StackManipulation.Compound(MethodVariableAccess.of(parameterDescription.getType())
                            .loadFrom(argumentHandler.argument(parameterDescription.getOffset())), readAssignment));
                }
                if (readOnly) {
                    return new Target.ForArray.ReadOnly(target, valueReads);
                } else {
                    List<StackManipulation> valueWrites = new ArrayList(instrumentedMethod.getParameters().size());
                    for (ParameterDescription parameterDescription : instrumentedMethod.getParameters()) {
                        StackManipulation writeAssignment = assigner.assign(target, parameterDescription.getType(), typing);
                        if (!writeAssignment.isValid()) {
                            throw new IllegalStateException("Cannot assign " + target + " to " + parameterDescription);
                        }
                        valueWrites.add(new StackManipulation.Compound(writeAssignment,
                            MethodVariableAccess.of(parameterDescription.getType())
                                .storeAt(argumentHandler.argument(parameterDescription.getOffset()))));
                    }
                    return new Target.ForArray.ReadWrite(target, valueReads, valueWrites);
                }
            }

            /**
             * A factory for an offset mapping that maps all arguments values of the instrumented method.
             */
            protected enum Factory implements OffsetMapping.Factory<AllArguments> {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * A description of the {@link AllArguments#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_READ_ONLY;

                /**
                 * A description of the {@link AllArguments#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_TYPING;

                /**
                 * A description of the {@link AllArguments#nullIfEmpty()} method.
                 */
                private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_NULL_IF_EMPTY;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(AllArguments.class).getDeclaredMethods();
                    ALL_ARGUMENTS_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    ALL_ARGUMENTS_TYPING = methods.filter(named("typing")).getOnly();
                    ALL_ARGUMENTS_NULL_IF_EMPTY = methods.filter(named("nullIfEmpty")).getOnly();
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<AllArguments> getAnnotationType() {
                    return AllArguments.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<AllArguments> annotation,
                                          AdviceType adviceType) {
                    if (!target.getType().represents(Object.class) && !target.getType().isArray()) {
                        throw new IllegalStateException("Cannot use AllArguments annotation on a non-array type");
                    } else if (adviceType.isDelegation() && !annotation.getValue(ALL_ARGUMENTS_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot define writable field access for " + target);
                    } else {
                        return new ForAllArguments(target.getType().represents(Object.class)
                                ? TypeDescription.Generic.OBJECT
                                : target.getType().getComponentType(), annotation);
                    }
                }
            }
        }

        /**
         * Maps the declaring type of the instrumented method.
         */
        enum ForInstrumentedType implements OffsetMapping {

            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                return Target.ForStackManipulation.of(instrumentedType);
            }
        }

        /**
         * Maps a constant representing the instrumented method.
         */
        enum ForInstrumentedMethod implements OffsetMapping {

            /**
             * A constant that must be a {@link Method} instance.
             */
            METHOD {
                @Override
                protected boolean isRepresentable(MethodDescription instrumentedMethod) {
                    return instrumentedMethod.isMethod();
                }
            },

            /**
             * A constant that must be a {@link Constructor} instance.
             */
            CONSTRUCTOR {
                @Override
                protected boolean isRepresentable(MethodDescription instrumentedMethod) {
                    return instrumentedMethod.isConstructor();
                }
            },

            /**
             * A constant that must be a {@code java.lang.reflect.Executable} instance.
             */
            EXECUTABLE {
                @Override
                protected boolean isRepresentable(MethodDescription instrumentedMethod) {
                    return true;
                }
            };

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                if (!isRepresentable(instrumentedMethod)) {
                    throw new IllegalStateException("Cannot represent " + instrumentedMethod + " as given method constant");
                }
                return Target.ForStackManipulation.of(instrumentedMethod.asDefined());
            }

            /**
             * Checks if the supplied method is representable for the assigned offset mapping.
             *
             * @param instrumentedMethod The instrumented method to represent.
             * @return {@code true} if this method is representable.
             */
            protected abstract boolean isRepresentable(MethodDescription instrumentedMethod);
        }

        /**
         * An offset mapping for a field.
         */
        @HashCodeAndEqualsPlugin.Enhance
        abstract class ForField implements OffsetMapping {

            /**
             * The {@link FieldValue#value()} method.
             */
            private static final MethodDescription.InDefinedShape VALUE;

            /**
             * The {@link FieldValue#declaringType()}} method.
             */
            private static final MethodDescription.InDefinedShape DECLARING_TYPE;

            /**
             * The {@link FieldValue#readOnly()}} method.
             */
            private static final MethodDescription.InDefinedShape READ_ONLY;

            /**
             * The {@link FieldValue#typing()}} method.
             */
            private static final MethodDescription.InDefinedShape TYPING;

            /*
             * Looks up all annotation properties to avoid loading of the declaring field type.
             */
            static {
                MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType
                    .of(FieldValue.class).getDeclaredMethods();
                VALUE = methods.filter(named("value")).getOnly();
                DECLARING_TYPE = methods.filter(named("declaringType")).getOnly();
                READ_ONLY = methods.filter(named("readOnly")).getOnly();
                TYPING = methods.filter(named("typing")).getOnly();
            }

            /**
             * The expected type that the field can be assigned to.
             */
            private final TypeDescription.Generic target;

            /**
             * {@code true} if this mapping is read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates an offset mapping for a field.
             *
             * @param target   The target type.
             * @param readOnly {@code true} if this mapping is read-only.
             * @param typing   The typing to apply.
             */
            public ForField(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                FieldDescription fieldDescription = resolve(instrumentedType, instrumentedMethod);
                if (!fieldDescription.isStatic() && instrumentedMethod.isStatic()) {
                    throw new IllegalStateException("Cannot read non-static field " + fieldDescription + " from static method " + instrumentedMethod);
                } else if (sort.isPremature(instrumentedMethod) && !fieldDescription.isStatic()) {
                    throw new IllegalStateException("Cannot access non-static field before calling constructor: " + instrumentedMethod);
                }
                StackManipulation readAssignment = assigner.assign(fieldDescription.getType(), target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + fieldDescription + " to " + target);
                } else if (readOnly) {
                    return new Target.ForField.ReadOnly(fieldDescription, readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, fieldDescription.getType(), typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to " + fieldDescription);
                    }
                    return new Target.ForField.ReadWrite(fieldDescription.asDefined(), readAssignment, writeAssignment);
                }
            }

            /**
             * Resolves the field being bound.
             *
             * @param instrumentedType   The instrumented type.
             * @param instrumentedMethod The instrumented method.
             * @return The field being bound.
             */
            protected abstract FieldDescription resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod);

            /**
             * An offset mapping for a field that is resolved from the instrumented type by its name.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public abstract static class Unresolved extends ForField {

                /**
                 * Indicates that a name should be extracted from an accessor method.
                 */
                protected static final String BEAN_PROPERTY = "";

                /**
                 * The name of the field.
                 */
                private final String name;

                /**
                 * Creates an offset mapping for a field that is not yet resolved.
                 *
                 * @param target   The target type.
                 * @param readOnly {@code true} if this mapping is read-only.
                 * @param typing   The typing to apply.
                 * @param name     The name of the field.
                 */
                public Unresolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, String name) {
                    super(target, readOnly, typing);
                    this.name = name;
                }

                @Override
                protected FieldDescription resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                    FieldLocator locator = fieldLocator(instrumentedType);
                    FieldLocator.Resolution resolution = name.equals(BEAN_PROPERTY)
                            ? resolveAccessor(locator, instrumentedMethod)
                            : locator.locate(name);
                    if (!resolution.isResolved()) {
                        throw new IllegalStateException("Cannot locate field named " + name + " for " + instrumentedType);
                    } else {
                        return resolution.getField();
                    }
                }

                /**
                 * Resolves a field locator for a potential accessor method.
                 *
                 * @param fieldLocator      The field locator to use.
                 * @param methodDescription The method description that is the potential accessor.
                 * @return A resolution for a field locator.
                 */
                private static FieldLocator.Resolution resolveAccessor(FieldLocator fieldLocator, MethodDescription methodDescription) {
                    String fieldName;
                    if (isSetter().matches(methodDescription)) {
                        fieldName = methodDescription.getInternalName().substring(3);
                    } else if (isGetter().matches(methodDescription)) {
                        fieldName = methodDescription.getInternalName().substring(methodDescription.getInternalName().startsWith("is") ? 2 : 3);
                    } else {
                        return FieldLocator.Resolution.Illegal.INSTANCE;
                    }
                    return fieldLocator.locate(Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1));
                }

                /**
                 * Returns a field locator for this instance.
                 *
                 * @param instrumentedType The instrumented type.
                 * @return An appropriate field locator.
                 */
                protected abstract FieldLocator fieldLocator(TypeDescription instrumentedType);

                /**
                 * An offset mapping for a field with an implicit declaring type.
                 */
                public static class WithImplicitType extends Unresolved {

                    /**
                     * Creates an offset mapping for a field with an implicit declaring type.
                     *
                     * @param target     The target type.
                     * @param annotation The annotation to represent.
                     */
                    protected WithImplicitType(TypeDescription.Generic target, AnnotationDescription.Loadable<FieldValue> annotation) {
                        this(target,
                                annotation.getValue(READ_ONLY).resolve(Boolean.class),
                                annotation.getValue(TYPING).load(Assigner.Typing.class.getClassLoader()).resolve(Assigner.Typing.class),
                                annotation.getValue(VALUE).resolve(String.class));
                    }

                    /**
                     * Creates an offset mapping for a field with an implicit declaring type.
                     *
                     * @param target   The target type.
                     * @param name     The name of the field.
                     * @param readOnly {@code true} if the field is read-only.
                     * @param typing   The typing to apply.
                     */
                    public WithImplicitType(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, String name) {
                        super(target, readOnly, typing, name);
                    }

                    @Override
                    protected FieldLocator fieldLocator(TypeDescription instrumentedType) {
                        return new FieldLocator.ForClassHierarchy(instrumentedType);
                    }
                }

                /**
                 * An offset mapping for a field with an explicit declaring type.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class WithExplicitType extends Unresolved {

                    /**
                     * The type declaring the field.
                     */
                    private final TypeDescription declaringType;

                    /**
                     * Creates an offset mapping for a field with an explicit declaring type.
                     *
                     * @param target        The target type.
                     * @param annotation    The annotation to represent.
                     * @param declaringType The field's declaring type.
                     */
                    protected WithExplicitType(TypeDescription.Generic target,
                                               AnnotationDescription.Loadable<FieldValue> annotation,
                                               TypeDescription declaringType) {
                        this(target,
                                annotation.getValue(READ_ONLY).resolve(Boolean.class),
                                annotation.getValue(TYPING).load(Assigner.Typing.class.getClassLoader()).resolve(Assigner.Typing.class),
                                annotation.getValue(VALUE).resolve(String.class),
                                declaringType);
                    }

                    /**
                     * Creates an offset mapping for a field with an explicit declaring type.
                     *
                     * @param target        The target type.
                     * @param name          The name of the field.
                     * @param readOnly      {@code true} if the field is read-only.
                     * @param typing        The typing to apply.
                     * @param declaringType The field's declaring type.
                     */
                    public WithExplicitType(TypeDescription.Generic target,
                                            boolean readOnly,
                                            Assigner.Typing typing,
                                            String name,
                                            TypeDescription declaringType) {
                        super(target, readOnly, typing, name);
                        this.declaringType = declaringType;
                    }

                    @Override
                    protected FieldLocator fieldLocator(TypeDescription instrumentedType) {
                        if (!declaringType.represents(TargetType.class) && !instrumentedType.isAssignableTo(declaringType)) {
                            throw new IllegalStateException(declaringType + " is no super type of " + instrumentedType);
                        }
                        return new FieldLocator.ForExactType(TargetType.resolve(declaringType, instrumentedType));
                    }
                }

                /**
                 * A factory for a {@link Unresolved} offset mapping.
                 */
                protected enum Factory implements OffsetMapping.Factory<FieldValue> {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * {@inheritDoc}
                     */
                    public Class<FieldValue> getAnnotationType() {
                        return FieldValue.class;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                              AnnotationDescription.Loadable<FieldValue> annotation,
                                              AdviceType adviceType) {
                        if (adviceType.isDelegation() && !annotation.getValue(ForField.READ_ONLY).resolve(Boolean.class)) {
                            throw new IllegalStateException("Cannot write to field for " + target + " in read-only context");
                        } else {
                            TypeDescription declaringType = annotation.getValue(DECLARING_TYPE).resolve(TypeDescription.class);
                            return declaringType.represents(void.class)
                                    ? new WithImplicitType(target.getType(), annotation)
                                    : new WithExplicitType(target.getType(), annotation, declaringType);
                        }
                    }
                }
            }

            /**
             * A binding for an offset mapping that represents a specific field.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class Resolved extends ForField {

                /**
                 * The accessed field.
                 */
                private final FieldDescription fieldDescription;

                /**
                 * Creates a resolved offset mapping for a field.
                 *
                 * @param target           The target type.
                 * @param readOnly         {@code true} if this mapping is read-only.
                 * @param typing           The typing to apply.
                 * @param fieldDescription The accessed field.
                 */
                public Resolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, FieldDescription fieldDescription) {
                    super(target, readOnly, typing);
                    this.fieldDescription = fieldDescription;
                }

                @Override
                protected FieldDescription resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                    if (!fieldDescription.isStatic() && !fieldDescription.getDeclaringType().asErasure().isAssignableFrom(instrumentedType)) {
                        throw new IllegalStateException(fieldDescription + " is no member of " + instrumentedType);
                    } else if (!fieldDescription.isAccessibleTo(instrumentedType)) {
                        throw new IllegalStateException("Cannot access " + fieldDescription + " from " + instrumentedType);
                    }
                    return fieldDescription;
                }

                /**
                 * A factory that binds a field.
                 *
                 * @param <T> The annotation type this factory binds.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {

                    /**
                     * The annotation type.
                     */
                    private final Class<T> annotationType;

                    /**
                     * The field to be bound.
                     */
                    private final FieldDescription fieldDescription;

                    /**
                     * {@code true} if this factory should create a read-only binding.
                     */
                    private final boolean readOnly;

                    /**
                     * The typing to use.
                     */
                    private final Assigner.Typing typing;

                    /**
                     * Creates a new factory for binding a specific field with read-only semantics and static typing.
                     *
                     * @param annotationType   The annotation type.
                     * @param fieldDescription The field to bind.
                     */
                    public Factory(Class<T> annotationType, FieldDescription fieldDescription) {
                        this(annotationType, fieldDescription, true, Assigner.Typing.STATIC);
                    }

                    /**
                     * Creates a new factory for binding a specific field.
                     *
                     * @param annotationType   The annotation type.
                     * @param fieldDescription The field to bind.
                     * @param readOnly         {@code true} if this factory should create a read-only binding.
                     * @param typing           The typing to use.
                     */
                    public Factory(Class<T> annotationType, FieldDescription fieldDescription, boolean readOnly, Assigner.Typing typing) {
                        this.annotationType = annotationType;
                        this.fieldDescription = fieldDescription;
                        this.readOnly = readOnly;
                        this.typing = typing;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public Class<T> getAnnotationType() {
                        return annotationType;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                              AnnotationDescription.Loadable<T> annotation,
                                              AdviceType adviceType) {
                        return new Resolved(target.getType(), readOnly, typing, fieldDescription);
                    }
                }
            }
        }

        /**
         * An offset mapping for the {@link Advice.Origin} annotation.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForOrigin implements OffsetMapping {

            /**
             * The delimiter character.
             */
            private static final char DELIMITER = '#';

            /**
             * The escape character.
             */
            private static final char ESCAPE = '\\';

            /**
             * The renderers to apply.
             */
            private final List<Renderer> renderers;

            /**
             * Creates a new offset mapping for an origin value.
             *
             * @param renderers The renderers to apply.
             */
            public ForOrigin(List<Renderer> renderers) {
                this.renderers = renderers;
            }

            /**
             * Parses a pattern of an origin annotation.
             *
             * @param pattern The supplied pattern.
             * @return An appropriate offset mapping.
             */
            public static OffsetMapping parse(String pattern) {
                if (pattern.equals(Origin.DEFAULT)) {
                    return new ForOrigin(Collections.singletonList(Renderer.ForStringRepresentation.INSTANCE));
                } else {
                    List<Renderer> renderers = new ArrayList<>(pattern.length());
                    int from = 0;
                    for (int to = pattern.indexOf(DELIMITER); to != -1; to = pattern.indexOf(DELIMITER, from)) {
                        if (to != 0 && pattern.charAt(to - 1) == ESCAPE && (to == 1 || pattern.charAt(to - 2) != ESCAPE)) {
                            renderers.add(new Renderer.ForConstantValue(pattern.substring(from, Math.max(0, to - 1)) + DELIMITER));
                            from = to + 1;
                            continue;
                        } else if (pattern.length() == to + 1) {
                            throw new IllegalStateException("Missing sort descriptor for " + pattern + " at index " + to);
                        }
                        renderers.add(new Renderer.ForConstantValue(pattern.substring(from, to).replace("" + ESCAPE + ESCAPE, "" + ESCAPE)));
                        switch (pattern.charAt(to + 1)) {
                            case Renderer.ForMethodName.SYMBOL:
                                renderers.add(Renderer.ForMethodName.INSTANCE);
                                break;
                            case Renderer.ForTypeName.SYMBOL:
                                renderers.add(Renderer.ForTypeName.INSTANCE);
                                break;
                            case Renderer.ForDescriptor.SYMBOL:
                                renderers.add(Renderer.ForDescriptor.INSTANCE);
                                break;
                            case Renderer.ForReturnTypeName.SYMBOL:
                                renderers.add(Renderer.ForReturnTypeName.INSTANCE);
                                break;
                            case Renderer.ForJavaSignature.SYMBOL:
                                renderers.add(Renderer.ForJavaSignature.INSTANCE);
                                break;
                            case Renderer.ForPropertyName.SYMBOL:
                                renderers.add(Renderer.ForPropertyName.INSTANCE);
                                break;
                            default:
                                throw new IllegalStateException("Illegal sort descriptor " + pattern.charAt(to + 1) + " for " + pattern);
                        }
                        from = to + 2;
                    }
                    renderers.add(new Renderer.ForConstantValue(pattern.substring(from)));
                    return new ForOrigin(renderers);
                }
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Renderer renderer : renderers) {
                    stringBuilder.append(renderer.apply(instrumentedType, instrumentedMethod));
                }
                return Target.ForStackManipulation.of(stringBuilder.toString());
            }

            /**
             * A renderer for an origin pattern element.
             */
            public interface Renderer {

                /**
                 * Returns a string representation for this renderer.
                 *
                 * @param instrumentedType   The instrumented type.
                 * @param instrumentedMethod The instrumented method.
                 * @return The string representation.
                 */
                String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod);

                /**
                 * A renderer for a method's internal name.
                 */
                enum ForMethodName implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The method name symbol.
                     */
                    public static final char SYMBOL = 'm';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return instrumentedMethod.getInternalName();
                    }
                }

                /**
                 * A renderer for a method declaring type's binary name.
                 */
                enum ForTypeName implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The type name symbol.
                     */
                    public static final char SYMBOL = 't';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return instrumentedType.getName();
                    }
                }

                /**
                 * A renderer for a method descriptor.
                 */
                enum ForDescriptor implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The descriptor symbol.
                     */
                    public static final char SYMBOL = 'd';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return instrumentedMethod.getDescriptor();
                    }
                }

                /**
                 * A renderer for a method's Java signature in binary form.
                 */
                enum ForJavaSignature implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The signature symbol.
                     */
                    public static final char SYMBOL = 's';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        StringBuilder stringBuilder = new StringBuilder("(");
                        boolean comma = false;
                        for (TypeDescription typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
                            if (comma) {
                                stringBuilder.append(',');
                            } else {
                                comma = true;
                            }
                            stringBuilder.append(typeDescription.getName());
                        }
                        return stringBuilder.append(')').toString();
                    }
                }

                /**
                 * A renderer for a method's return type in binary form.
                 */
                enum ForReturnTypeName implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The return type symbol.
                     */
                    public static final char SYMBOL = 'r';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return instrumentedMethod.getReturnType().asErasure().getName();
                    }
                }

                /**
                 * A renderer for a method's {@link Object#toString()} representation.
                 */
                enum ForStringRepresentation implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return instrumentedMethod.toString();
                    }
                }

                /**
                 * A renderer for a constant value.
                 */
                @HashCodeAndEqualsPlugin.Enhance
                class ForConstantValue implements Renderer {

                    /**
                     * The constant value.
                     */
                    private final String value;

                    /**
                     * Creates a new renderer for a constant value.
                     *
                     * @param value The constant value.
                     */
                    public ForConstantValue(String value) {
                        this.value = value;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return value;
                    }
                }

                /**
                 * A renderer for a property name.
                 */
                enum ForPropertyName implements Renderer {

                    /**
                     * The singleton instance.
                     */
                    INSTANCE;

                    /**
                     * The signature symbol.
                     */
                    public static final char SYMBOL = 'p';

                    /**
                     * {@inheritDoc}
                     */
                    public String apply(TypeDescription instrumentedType, MethodDescription instrumentedMethod) {
                        return FieldAccessor.FieldNameExtractor.ForBeanProperty.INSTANCE.resolve(instrumentedMethod);
                    }
                }
            }

            /**
             * A factory for a method origin.
             */
            protected enum Factory implements OffsetMapping.Factory<Origin> {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * A description of the {@link Origin#value()} method.
                 */
                private static final MethodDescription.InDefinedShape ORIGIN_VALUE = TypeDescription.ForLoadedType.of(Origin.class)
                        .getDeclaredMethods()
                        .filter(named("value"))
                        .getOnly();

                /**
                 * {@inheritDoc}
                 */
                public Class<Origin> getAnnotationType() {
                    return Origin.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Origin> annotation,
                                          AdviceType adviceType) {
                    if (target.getType().asErasure().represents(Class.class)) {
                        return OffsetMapping.ForInstrumentedType.INSTANCE;
                    } else if (target.getType().asErasure().represents(Method.class)) {
                        return OffsetMapping.ForInstrumentedMethod.METHOD;
                    } else if (target.getType().asErasure().represents(Constructor.class)) {
                        return OffsetMapping.ForInstrumentedMethod.CONSTRUCTOR;
                    } else if (JavaType.EXECUTABLE.getTypeStub().equals(target.getType().asErasure())) {
                        return OffsetMapping.ForInstrumentedMethod.EXECUTABLE;
                    } else if (target.getType().asErasure().isAssignableFrom(String.class)) {
                        return ForOrigin.parse(annotation.getValue(ORIGIN_VALUE).resolve(String.class));
                    } else {
                        throw new IllegalStateException("Non-supported type " + target.getType() + " for @Origin annotation");
                    }
                }
            }
        }

        /**
         * An offset mapping for a parameter where assignments are fully ignored and that always return the parameter type's default value.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForUnusedValue implements OffsetMapping {

            /**
             * The unused type.
             */
            private final TypeDefinition target;

            /**
             * Creates a new offset mapping for an unused type.
             *
             * @param target The unused type.
             */
            public ForUnusedValue(TypeDefinition target) {
                this.target = target;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                return new Target.ForDefaultValue.ReadWrite(target);
            }

            /**
             * A factory for an offset mapping for an unused value.
             */
            protected enum Factory implements OffsetMapping.Factory<Unused> {

                /**
                 * A factory for representing an unused value.
                 */
                INSTANCE;

                /**
                 * {@inheritDoc}
                 */
                public Class<Unused> getAnnotationType() {
                    return Unused.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Unused> annotation,
                                          AdviceType adviceType) {
                    return new ForUnusedValue(target.getType());
                }
            }
        }

        /**
         * An offset mapping for a parameter where assignments are fully ignored and that is assigned a boxed version of the instrumented
         * method's return value or {@code null} if the return type is not primitive or {@code void}.
         */
        enum ForStubValue implements OffsetMapping, Factory<StubValue> {

            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                return new Target.ForDefaultValue.ReadOnly(instrumentedMethod.getReturnType(), assigner.assign(instrumentedMethod.getReturnType(),
                        TypeDescription.Generic.OBJECT,
                        Assigner.Typing.DYNAMIC));
            }

            /**
             * {@inheritDoc}
             */
            public Class<StubValue> getAnnotationType() {
                return StubValue.class;
            }

            /**
             * {@inheritDoc}
             */
            public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                      AnnotationDescription.Loadable<StubValue> annotation,
                                      AdviceType adviceType) {
                if (!target.getType().represents(Object.class)) {
                    throw new IllegalStateException("Cannot use StubValue on non-Object parameter type " + target);
                } else {
                    return this;
                }
            }
        }

        /**
         * An offset mapping that provides access to the value that is returned by the enter advice.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForEnterValue implements OffsetMapping {

            /**
             * The represented target type.
             */
            private final TypeDescription.Generic target;

            /**
             * The enter type.
             */
            private final TypeDescription.Generic enterType;

            /**
             * {@code true} if the annotated value is read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates a new offset mapping for the enter type.
             *
             * @param target     The represented target type.
             * @param enterType  The enter type.
             * @param annotation The represented annotation.
             */
            protected ForEnterValue(TypeDescription.Generic target, TypeDescription.Generic enterType, AnnotationDescription.Loadable<Enter> annotation) {
                this(target,
                        enterType,
                        annotation.getValue(Factory.ENTER_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.ENTER_TYPING).load(Enter.class.getClassLoader()).resolve(Assigner.Typing.class));
            }

            /**
             * Creates a new offset mapping for the enter type.
             *
             * @param target    The represented target type.
             * @param enterType The enter type.
             * @param readOnly  {@code true} if the annotated value is read-only.
             * @param typing    The typing to apply.
             */
            public ForEnterValue(TypeDescription.Generic target, TypeDescription.Generic enterType, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.enterType = enterType;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation readAssignment = assigner.assign(enterType, target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + enterType + " to " + target);
                } else if (readOnly) {
                    return new Target.ForVariable.ReadOnly(target, argumentHandler.enter(), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, enterType, typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to " + enterType);
                    }
                    return new Target.ForVariable.ReadWrite(target, argumentHandler.enter(), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for creating a {@link ForEnterValue} offset mapping.
             */
            @HashCodeAndEqualsPlugin.Enhance
            protected static class Factory implements OffsetMapping.Factory<Enter> {

                /**
                 * A description of the {@link Argument#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape ENTER_READ_ONLY;

                /**
                 * A description of the {@link Argument#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape ENTER_TYPING;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Enter.class).getDeclaredMethods();
                    ENTER_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    ENTER_TYPING = methods.filter(named("typing")).getOnly();
                }

                /**
                 * The supplied type of the enter advice.
                 */
                private final TypeDefinition enterType;

                /**
                 * Creates a new factory for creating a {@link ForEnterValue} offset mapping.
                 *
                 * @param enterType The supplied type of the enter method.
                 */
                protected Factory(TypeDefinition enterType) {
                    this.enterType = enterType;
                }

                /**
                 * Creates a new factory for creating a {@link ForEnterValue} offset mapping.
                 *
                 * @param typeDefinition The supplied type of the enter advice.
                 * @return An appropriate offset mapping factory.
                 */
                protected static OffsetMapping.Factory<Enter> of(TypeDefinition typeDefinition) {
                    return typeDefinition.represents(void.class)
                            ? new Illegal(Enter.class)
                            : new Factory(typeDefinition);
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<Enter> getAnnotationType() {
                    return Enter.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Enter> annotation,
                                          AdviceType adviceType) {
                    if (adviceType.isDelegation() && !annotation.getValue(ENTER_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
                    } else {
                        return new ForEnterValue(target.getType(), enterType.asGenericType(), annotation);
                    }
                }
            }
        }

        /**
         * An offset mapping that provides access to the value that is returned by the exit advice.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForExitValue implements OffsetMapping {

            /**
             * The represented target type.
             */
            private final TypeDescription.Generic target;

            /**
             * The exit type.
             */
            private final TypeDescription.Generic exitType;

            /**
             * {@code true} if the annotated value is read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates a new offset mapping for the exit type.
             *
             * @param target     The represented target type.
             * @param exitType   The exit type.
             * @param annotation The represented annotation.
             */
            protected ForExitValue(TypeDescription.Generic target, TypeDescription.Generic exitType, AnnotationDescription.Loadable<Exit> annotation) {
                this(target,
                        exitType,
                        annotation.getValue(Factory.EXIT_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.EXIT_TYPING).load(Exit.class.getClassLoader()).resolve(Assigner.Typing.class));
            }

            /**
             * Creates a new offset mapping for the enter type.
             *
             * @param target   The represented target type.
             * @param exitType The exit type.
             * @param readOnly {@code true} if the annotated value is read-only.
             * @param typing   The typing to apply.
             */
            public ForExitValue(TypeDescription.Generic target, TypeDescription.Generic exitType, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.exitType = exitType;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation readAssignment = assigner.assign(exitType, target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + exitType + " to " + target);
                } else if (readOnly) {
                    return new Target.ForVariable.ReadOnly(target, argumentHandler.exit(), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, exitType, typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to " + exitType);
                    }
                    return new Target.ForVariable.ReadWrite(target, argumentHandler.exit(), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for creating a {@link ForExitValue} offset mapping.
             */
            @HashCodeAndEqualsPlugin.Enhance
            protected static class Factory implements OffsetMapping.Factory<Exit> {

                /**
                 * A description of the {@link Exit#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape EXIT_READ_ONLY;

                /**
                 * A description of the {@link Exit#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape EXIT_TYPING;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Exit.class).getDeclaredMethods();
                    EXIT_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    EXIT_TYPING = methods.filter(named("typing")).getOnly();
                }

                /**
                 * The supplied type of the exit advice.
                 */
                private final TypeDefinition exitType;

                /**
                 * Creates a new factory for creating a {@link ForExitValue} offset mapping.
                 *
                 * @param exitType The supplied type of the exit advice.
                 */
                protected Factory(TypeDefinition exitType) {
                    this.exitType = exitType;
                }

                /**
                 * Creates a new factory for creating a {@link ForExitValue} offset mapping.
                 *
                 * @param typeDefinition The supplied type of the enter method.
                 * @return An appropriate offset mapping factory.
                 */
                protected static OffsetMapping.Factory<Exit> of(TypeDefinition typeDefinition) {
                    return typeDefinition.represents(void.class)
                            ? new Illegal(Exit.class)
                            : new Factory(typeDefinition);
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<Exit> getAnnotationType() {
                    return Exit.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Exit> annotation,
                                          AdviceType adviceType) {
                    if (adviceType.isDelegation() && !annotation.getValue(EXIT_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
                    } else {
                        return new ForExitValue(target.getType(), exitType.asGenericType(), annotation);
                    }
                }
            }
        }

        /**
         * An offset mapping that provides access to a named local variable that is declared by the advice methods via {@link Local}.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForLocalValue implements OffsetMapping {

            /**
             * The variable's target type.
             */
            private final TypeDescription.Generic target;

            /**
             * The local variable's type.
             */
            private final TypeDescription.Generic localType;

            /**
             * The local variable's name.
             */
            private final String name;

            /**
             * Creates an offset mapping for a local variable that is declared by the advice methods via {@link Local}.
             *
             * @param target    The variable's target type.
             * @param localType The local variable's type.
             * @param name      The local variable's name.
             */
            public ForLocalValue(TypeDescription.Generic target, TypeDescription.Generic localType, String name) {
                this.target = target;
                this.localType = localType;
                this.name = name;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation readAssignment = assigner.assign(localType, target, Assigner.Typing.STATIC);
                StackManipulation writeAssignment = assigner.assign(target, localType, Assigner.Typing.STATIC);
                if (!readAssignment.isValid() || !writeAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + localType + " to " + target);
                } else {
                    return new Target.ForVariable.ReadWrite(target, argumentHandler.named(name), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for an offset mapping for a local variable that is declared by the advice methods via {@link Local}.
             */
            @HashCodeAndEqualsPlugin.Enhance
            protected static class Factory implements OffsetMapping.Factory<Local> {

                /**
                 * A description of the {@link Local#value()} method.
                 */
                protected static final MethodDescription.InDefinedShape LOCAL_VALUE = TypeDescription.ForLoadedType.of(Local.class)
                        .getDeclaredMethods()
                        .filter(named("value"))
                        .getOnly();

                /**
                 * The mapping of type names to their type that are available.
                 */
                private final Map<String, TypeDefinition> namedTypes;

                /**
                 * Creates a factory for a {@link Local} variable mapping.
                 *
                 * @param namedTypes The mapping of type names to their type that are available.
                 */
                protected Factory(Map<String, TypeDefinition> namedTypes) {
                    this.namedTypes = namedTypes;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<Local> getAnnotationType() {
                    return Local.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Local> annotation,
                                          AdviceType adviceType) {
                    String name = annotation.getValue(LOCAL_VALUE).resolve(String.class);
                    TypeDefinition namedType = namedTypes.get(name);
                    if (namedType == null) {
                        throw new IllegalStateException("Named local variable is unknown: " + name);
                    }
                    return new ForLocalValue(target.getType(), namedType.asGenericType(), name);
                }
            }
        }

        /**
         * An offset mapping that provides access to the value that is returned by the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForReturnValue implements OffsetMapping {

            /**
             * The type that the advice method expects for the return value.
             */
            private final TypeDescription.Generic target;

            /**
             * Determines if the parameter is to be treated as read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates a new offset mapping for a return value.
             *
             * @param target     The type that the advice method expects for the return value.
             * @param annotation The annotation being bound.
             */
            protected ForReturnValue(TypeDescription.Generic target, AnnotationDescription.Loadable<Return> annotation) {
                this(target,
                        annotation.getValue(Factory.RETURN_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.RETURN_TYPING).load(Return.class.getClassLoader()).resolve(Assigner.Typing.class));
            }

            /**
             * Creates a new offset mapping for a return value.
             *
             * @param target   The type that the advice method expects for the return value.
             * @param readOnly Determines if the parameter is to be treated as read-only.
             * @param typing   The typing to apply.
             */
            public ForReturnValue(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation readAssignment = assigner.assign(instrumentedMethod.getReturnType(), target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + instrumentedMethod.getReturnType() + " to " + target);
                } else if (readOnly) {
                    return instrumentedMethod.getReturnType().represents(void.class)
                            ? new Target.ForDefaultValue.ReadOnly(target)
                            : new Target.ForVariable.ReadOnly(instrumentedMethod.getReturnType(), argumentHandler.returned(), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, instrumentedMethod.getReturnType(), typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to " + instrumentedMethod.getReturnType());
                    }
                    return instrumentedMethod.getReturnType().represents(void.class)
                            ? new Target.ForDefaultValue.ReadWrite(target)
                            : new Target.ForVariable.ReadWrite(instrumentedMethod.getReturnType(), argumentHandler.returned(), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for creating a {@link ForReturnValue} offset mapping.
             */
            protected enum Factory implements OffsetMapping.Factory<Return> {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * A description of the {@link Return#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape RETURN_READ_ONLY;

                /**
                 * A description of the {@link Return#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape RETURN_TYPING;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Return.class).getDeclaredMethods();
                    RETURN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    RETURN_TYPING = methods.filter(named("typing")).getOnly();
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<Return> getAnnotationType() {
                    return Return.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Return> annotation,
                                          AdviceType adviceType) {
                    if (adviceType.isDelegation() && !annotation.getValue(RETURN_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot write return value for " + target + " in read-only context");
                    } else {
                        return new ForReturnValue(target.getType(), annotation);
                    }
                }
            }
        }

        /**
         * An offset mapping for accessing a {@link Throwable} of the instrumented method.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForThrowable implements OffsetMapping {

            /**
             * The type of parameter that is being accessed.
             */
            private final TypeDescription.Generic target;

            /**
             * {@code true} if the parameter is read-only.
             */
            private final boolean readOnly;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates a new offset mapping for access of the exception that is thrown by the instrumented method..
             *
             * @param target     The type of parameter that is being accessed.
             * @param annotation The annotation to bind.
             */
            protected ForThrowable(TypeDescription.Generic target, AnnotationDescription.Loadable<Thrown> annotation) {
                this(target,
                        annotation.getValue(Factory.THROWN_READ_ONLY).resolve(Boolean.class),
                        annotation.getValue(Factory.THROWN_TYPING).load(Thrown.class.getClassLoader()).resolve(Assigner.Typing.class));
            }

            /**
             * Creates a new offset mapping for access of the exception that is thrown by the instrumented method..
             *
             * @param target   The type of parameter that is being accessed.
             * @param readOnly {@code true} if the parameter is read-only.
             * @param typing   The typing to apply.
             */
            public ForThrowable(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing) {
                this.target = target;
                this.readOnly = readOnly;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation readAssignment = assigner.assign(TypeDescription.THROWABLE.asGenericType(), target, typing);
                if (!readAssignment.isValid()) {
                    throw new IllegalStateException("Cannot assign Throwable to " + target);
                } else if (readOnly) {
                    return new Target.ForVariable.ReadOnly(TypeDescription.THROWABLE, argumentHandler.thrown(), readAssignment);
                } else {
                    StackManipulation writeAssignment = assigner.assign(target, TypeDescription.THROWABLE.asGenericType(), typing);
                    if (!writeAssignment.isValid()) {
                        throw new IllegalStateException("Cannot assign " + target + " to Throwable");
                    }
                    return new Target.ForVariable.ReadWrite(TypeDescription.THROWABLE, argumentHandler.thrown(), readAssignment, writeAssignment);
                }
            }

            /**
             * A factory for accessing an exception that was thrown by the instrumented method.
             */
            protected enum Factory implements OffsetMapping.Factory<Thrown> {

                /**
                 * The singleton instance.
                 */
                INSTANCE;

                /**
                 * A description of the {@link Thrown#readOnly()} method.
                 */
                private static final MethodDescription.InDefinedShape THROWN_READ_ONLY;

                /**
                 * A description of the {@link Thrown#typing()} method.
                 */
                private static final MethodDescription.InDefinedShape THROWN_TYPING;

                /*
                 * Resolves annotation properties.
                 */
                static {
                    MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Thrown.class).getDeclaredMethods();
                    THROWN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
                    THROWN_TYPING = methods.filter(named("typing")).getOnly();
                }

                /**
                 * Resolves an appropriate offset mapping factory for the {@link Thrown} parameter annotation.
                 *
                 * @param adviceMethod The exit advice method, annotated with {@link OnMethodExit}.
                 * @return An appropriate offset mapping factory.
                 */
                @SuppressWarnings("unchecked") // In absence of @SafeVarargs
                protected static OffsetMapping.Factory<?> of(MethodDescription.InDefinedShape adviceMethod) {
                    return isNoExceptionHandler(adviceMethod.getDeclaredAnnotations()
                            .ofType(OnMethodExit.class)
                            .getValue(ON_THROWABLE)
                            .resolve(TypeDescription.class))
                        ? new OffsetMapping.Factory.Illegal(Thrown.class) : Factory.INSTANCE;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<Thrown> getAnnotationType() {
                    return Thrown.class;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<Thrown> annotation,
                                          AdviceType adviceType) {
                    if (adviceType.isDelegation() && !annotation.getValue(THROWN_READ_ONLY).resolve(Boolean.class)) {
                        throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
                    } else {
                        return new ForThrowable(target.getType(), annotation);
                    }
                }
            }
        }

        /**
         * An offset mapping for binding a stack manipulation.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForStackManipulation implements OffsetMapping {

            /**
             * The stack manipulation that loads the bound value.
             */
            private final StackManipulation stackManipulation;

            /**
             * The type of the loaded value.
             */
            private final TypeDescription.Generic typeDescription;

            /**
             * The target type of the annotated parameter.
             */
            private final TypeDescription.Generic targetType;

            /**
             * The typing to apply.
             */
            private final Assigner.Typing typing;

            /**
             * Creates an offset mapping that binds a stack manipulation.
             *
             * @param stackManipulation The stack manipulation that loads the bound value.
             * @param typeDescription   The type of the loaded value.
             * @param targetType        The target type of the annotated parameter.
             * @param typing            The typing to apply.
             */
            public ForStackManipulation(StackManipulation stackManipulation,
                                        TypeDescription.Generic typeDescription,
                                        TypeDescription.Generic targetType,
                                        Assigner.Typing typing) {
                this.stackManipulation = stackManipulation;
                this.typeDescription = typeDescription;
                this.targetType = targetType;
                this.typing = typing;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation assignment = assigner.assign(typeDescription, targetType, typing);
                if (!assignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + typeDescription + " to " + targetType);
                }
                return new Target.ForStackManipulation(new StackManipulation.Compound(stackManipulation, assignment));
            }

            public ForStackManipulation with(StackManipulation stackManipulation) {
                return new ForStackManipulation(stackManipulation, this.typeDescription, this.targetType, this.typing);
            }

            public StackManipulation getStackManipulation() {
                return this.stackManipulation;
            }

            /**
             * A factory that binds a stack manipulation.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * The stack manipulation that loads the bound value.
                 */
                private final StackManipulation stackManipulation;

                /**
                 * The type of the loaded value.
                 */
                private final TypeDescription.Generic typeDescription;

                /**
                 * Creates a new factory for binding a type description.
                 *
                 * @param annotationType  The annotation type.
                 * @param typeDescription The type to bind.
                 */
                public Factory(Class<T> annotationType, TypeDescription typeDescription) {
                    this(annotationType, ClassConstant.of(typeDescription), TypeDescription.CLASS.asGenericType());
                }

                /**
                 * Creates a new factory for binding an enumeration.
                 *
                 * @param annotationType         The annotation type.
                 * @param enumerationDescription The enumeration to bind.
                 */
                public Factory(Class<T> annotationType, EnumerationDescription enumerationDescription) {
                    this(annotationType, FieldAccess.forEnumeration(enumerationDescription), enumerationDescription.getEnumerationType().asGenericType());
                }

                /**
                 * Creates a new factory for binding a stack manipulation.
                 *
                 * @param annotationType    The annotation type.
                 * @param stackManipulation The stack manipulation that loads the bound value.
                 * @param typeDescription   The type of the loaded value.
                 */
                public Factory(Class<T> annotationType, StackManipulation stackManipulation, TypeDescription.Generic typeDescription) {
                    this.annotationType = annotationType;
                    this.stackManipulation = stackManipulation;
                    this.typeDescription = typeDescription;
                }

                /**
                 * Creates a binding for a fixed {@link String}, a primitive value or a method handle or type.
                 *
                 * @param annotationType The annotation type.
                 * @param value          The primitive (wrapper) value, {@link String} value, method handle or type to bind.
                 * @param <S>            The annotation type.
                 * @return A factory for creating an offset mapping that binds the supplied value.
                 */
                public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType, Object value) {
                    StackManipulation stackManipulation;
                    TypeDescription typeDescription;
                    if (value == null) {
                        return new OfDefaultValue(annotationType);
                    } else if (value instanceof Boolean) {
                        stackManipulation = IntegerConstant.forValue((Boolean) value);
                        typeDescription = TypeDescription.ForLoadedType.of(boolean.class);
                    } else if (value instanceof Byte) {
                        stackManipulation = IntegerConstant.forValue((Byte) value);
                        typeDescription = TypeDescription.ForLoadedType.of(byte.class);
                    } else if (value instanceof Short) {
                        stackManipulation = IntegerConstant.forValue((Short) value);
                        typeDescription = TypeDescription.ForLoadedType.of(short.class);
                    } else if (value instanceof Character) {
                        stackManipulation = IntegerConstant.forValue((Character) value);
                        typeDescription = TypeDescription.ForLoadedType.of(char.class);
                    } else if (value instanceof Integer) {
                        stackManipulation = IntegerConstant.forValue((Integer) value);
                        typeDescription = TypeDescription.ForLoadedType.of(int.class);
                    } else if (value instanceof Long) {
                        stackManipulation = LongConstant.forValue((Long) value);
                        typeDescription = TypeDescription.ForLoadedType.of(long.class);
                    } else if (value instanceof Float) {
                        stackManipulation = FloatConstant.forValue((Float) value);
                        typeDescription = TypeDescription.ForLoadedType.of(float.class);
                    } else if (value instanceof Double) {
                        stackManipulation = DoubleConstant.forValue((Double) value);
                        typeDescription = TypeDescription.ForLoadedType.of(double.class);
                    } else if (value instanceof String) {
                        stackManipulation = new TextConstant((String) value);
                        typeDescription = TypeDescription.STRING;
                    } else if (value instanceof Class<?>) {
                        stackManipulation = ClassConstant.of(TypeDescription.ForLoadedType.of((Class<?>) value));
                        typeDescription = TypeDescription.CLASS;
                    } else if (value instanceof TypeDescription) {
                        stackManipulation = ClassConstant.of((TypeDescription) value);
                        typeDescription = TypeDescription.CLASS;
                    } else if (value instanceof Enum<?>) {
                        stackManipulation = FieldAccess.forEnumeration(new EnumerationDescription.ForLoadedEnumeration((Enum<?>) value));
                        typeDescription = TypeDescription.ForLoadedType.of(((Enum<?>) value).getDeclaringClass());
                    } else if (value instanceof EnumerationDescription) {
                        stackManipulation = FieldAccess.forEnumeration((EnumerationDescription) value);
                        typeDescription = ((EnumerationDescription) value).getEnumerationType();
                    } else if (JavaType.METHOD_HANDLE.isInstance(value)) {
                        JavaConstant constant = JavaConstant.MethodHandle.ofLoaded(value);
                        stackManipulation = new JavaConstantValue(constant);
                        typeDescription = constant.getTypeDescription();
                    } else if (JavaType.METHOD_TYPE.isInstance(value)) {
                        JavaConstant constant = JavaConstant.MethodType.ofLoaded(value);
                        stackManipulation = new JavaConstantValue(constant);
                        typeDescription = constant.getTypeDescription();
                    } else if (value instanceof JavaConstant) {
                        stackManipulation = new JavaConstantValue((JavaConstant) value);
                        typeDescription = ((JavaConstant) value).getTypeDescription();
                    } else {
                        throw new IllegalStateException("Not a constant value: " + value);
                    }
                    return new Factory(annotationType, stackManipulation, typeDescription.asGenericType());
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<T> annotation,
                                          AdviceType adviceType) {
                    return new ForStackManipulation(stackManipulation, typeDescription, target.getType(), Assigner.Typing.STATIC);
                }
            }

            /**
             * A factory for binding the annotated parameter's default value.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class OfDefaultValue<T extends Annotation> implements OffsetMapping.Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * Creates a factory for an offset mapping tat binds the parameter's default value.
                 *
                 * @param annotationType The annotation type.
                 */
                public OfDefaultValue(Class<T> annotationType) {
                    this.annotationType = annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    return new ForStackManipulation(DefaultValue.of(target.getType()), target.getType(), target.getType(), Assigner.Typing.STATIC);
                }
            }

            /**
             * A factory for binding an annotation's property.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class OfAnnotationProperty<T extends Annotation> implements OffsetMapping.Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * The annotation property.
                 */
                private final MethodDescription.InDefinedShape property;

                /**
                 * Creates a factory for binding an annotation property.
                 *
                 * @param annotationType The annotation type.
                 * @param property       The annotation property.
                 */
                protected OfAnnotationProperty(Class<T> annotationType, MethodDescription.InDefinedShape property) {
                    this.annotationType = annotationType;
                    this.property = property;
                }

                /**
                 * Creates a factory for an offset mapping that binds an annotation property.
                 *
                 * @param annotationType The annotation type to bind.
                 * @param property       The property to bind.
                 * @param <S>            The annotation type.
                 * @return A factory for binding a property of the annotation type.
                 */
                public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType, String property) {
                    if (!annotationType.isAnnotation()) {
                        throw new IllegalArgumentException("Not an annotation type: " + annotationType);
                    }
                    try {
                        return new OfAnnotationProperty(annotationType, new MethodDescription.ForLoadedMethod(annotationType.getMethod(property)));
                    } catch (NoSuchMethodException exception) {
                        throw new IllegalArgumentException("Cannot find a property " + property + " on " + annotationType, exception);
                    }
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    Object value = annotation.getValue(property).resolve();
                    OffsetMapping.Factory<T> factory;
                    if (value instanceof TypeDescription) {
                        factory = new Factory(annotationType, (TypeDescription) value);
                    } else if (value instanceof EnumerationDescription) {
                        factory = new Factory(annotationType, (EnumerationDescription) value);
                    } else if (value instanceof AnnotationDescription) {
                        throw new IllegalStateException("Cannot bind annotation as fixed value for " + property);
                    } else {
                        factory = Factory.of(annotationType, value);
                    }
                    return factory.make(target, annotation, adviceType);
                }
            }

            /**
             * Uses dynamic method invocation for binding an annotated parameter to a value.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class OfDynamicInvocation<T extends Annotation> implements OffsetMapping.Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * The bootstrap method or constructor.
                 */
                private final MethodDescription.InDefinedShape bootstrapMethod;

                /**
                 * The arguments to the bootstrap method.
                 */
                private final List<? extends JavaConstant> arguments;

                /**
                 * Creates a new factory for a dynamic invocation.
                 *
                 * @param annotationType  The annotation type.
                 * @param bootstrapMethod The bootstrap method or constructor.
                 * @param arguments       The arguments to the bootstrap method.
                 */
                public OfDynamicInvocation(Class<T> annotationType,
                                           MethodDescription.InDefinedShape bootstrapMethod,
                                           List<? extends JavaConstant> arguments) {
                    this.annotationType = annotationType;
                    this.bootstrapMethod = bootstrapMethod;
                    this.arguments = arguments;
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    if (!target.getType().isInterface()) {
                        throw new IllegalArgumentException(target.getType() + " is not an interface");
                    } else if (!target.getType().getInterfaces().isEmpty()) {
                        throw new IllegalArgumentException(target.getType() + " must not extend other interfaces");
                    } else if (!target.getType().isPublic()) {
                        throw new IllegalArgumentException(target.getType() + " is mot public");
                    }
                    MethodList<?> methodCandidates = target.getType().getDeclaredMethods().filter(isAbstract());
                    if (methodCandidates.size() != 1) {
                        throw new IllegalArgumentException(target.getType() + " must declare exactly one abstract method");
                    }
                    return new ForStackManipulation(MethodInvocation.invoke(bootstrapMethod).dynamic(methodCandidates.getOnly().getInternalName(),
                            target.getType().asErasure(),
                            methodCandidates.getOnly().getParameters().asTypeList().asErasures(),
                            arguments), target.getType(), target.getType(), Assigner.Typing.STATIC);
                }
            }
        }

        /**
         * An offset mapping that loads a serialized value.
         */
        @HashCodeAndEqualsPlugin.Enhance
        class ForSerializedValue implements OffsetMapping {

            /**
             * The type of the serialized value as it is used.
             */
            private final TypeDescription.Generic target;

            /**
             * The class type of the serialized value.
             */
            private final TypeDescription typeDescription;

            /**
             * The stack manipulation deserializing the represented value.
             */
            private final StackManipulation deserialization;

            /**
             * Creates a new offset mapping for a serialized value.
             *
             * @param target          The type of the serialized value as it is used.
             * @param typeDescription The class type of the serialized value.
             * @param deserialization The stack manipulation deserializing the represented value.
             */
            public ForSerializedValue(TypeDescription.Generic target, TypeDescription typeDescription, StackManipulation deserialization) {
                this.target = target;
                this.typeDescription = typeDescription;
                this.deserialization = deserialization;
            }

            /**
             * {@inheritDoc}
             */
            public Target resolve(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  Assigner assigner,
                                  ArgumentHandler argumentHandler,
                                  Sort sort) {
                StackManipulation assignment = assigner.assign(typeDescription.asGenericType(), target, Assigner.Typing.DYNAMIC);
                if (!assignment.isValid()) {
                    throw new IllegalStateException("Cannot assign " + typeDescription + " to " + target);
                }
                return new Target.ForStackManipulation(new StackManipulation.Compound(deserialization, assignment));
            }

            /**
             * A factory for loading a deserialized value.
             *
             * @param <T> The annotation type this factory binds.
             */
            @HashCodeAndEqualsPlugin.Enhance
            public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {

                /**
                 * The annotation type.
                 */
                private final Class<T> annotationType;

                /**
                 * The type description as which to treat the deserialized value.
                 */
                private final TypeDescription typeDescription;

                /**
                 * The stack manipulation that loads the represented value.
                 */
                private final StackManipulation deserialization;

                /**
                 * Creates a factory for loading a deserialized value.
                 *
                 * @param annotationType  The annotation type.
                 * @param typeDescription The type description as which to treat the deserialized value.
                 * @param deserialization The stack manipulation that loads the represented value.
                 */
                protected Factory(Class<T> annotationType,
                                  TypeDescription typeDescription, StackManipulation deserialization) {
                    this.annotationType = annotationType;
                    this.typeDescription = typeDescription;
                    this.deserialization = deserialization;
                }

                /**
                 * Creates a factory for an offset mapping that loads the provided value.
                 *
                 * @param annotationType The annotation type to be bound.
                 * @param target         The instance representing the value to be deserialized.
                 * @param targetType     The target type as which to use the target value.
                 * @param <S>            The annotation type the created factory binds.
                 * @return An appropriate offset mapping factory.
                 */
                public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType,
                                                                                 Serializable target,
                                                                                 Class<?> targetType) {
                    if (!targetType.isInstance(target)) {
                        throw new IllegalArgumentException(target + " is no instance of " + targetType);
                    }
                    return new Factory(annotationType, TypeDescription.ForLoadedType.of(targetType),
                        SerializedConstant.of(target));
                }

                /**
                 * {@inheritDoc}
                 */
                public Class<T> getAnnotationType() {
                    return annotationType;
                }

                /**
                 * {@inheritDoc}
                 */
                public OffsetMapping make(ParameterDescription.InDefinedShape target,
                                          AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
                    return new ForSerializedValue(target.getType(), typeDescription, deserialization);
                }
            }
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.METHOD)
    public @interface OnMethodExitNoException {

        /**
         * <p>
         * Determines if the execution of the instrumented method should be repeated. This does not include any enter advice.
         * </p>
         * <p>
         * When specifying a non-primitive type, this method's return value that is subject to an {@code instanceof} check where
         * the instrumented method is only executed, if the returned instance is {@code not} an instance of the specified class.
         * Alternatively, it is possible to specify either {@link OnDefaultValue} or {@link OnNonDefaultValue} where the instrumented
         * method is only repeated if the advice method returns a default or non-default value of the advice method's return type.
         * It is illegal to specify a primitive type as an argument whereas setting the value to {@code void} indicates that the
         * instrumented method should never be repeated.
         * </p>
         * <p>
         * <b>Important</b>: Constructors cannot be repeated.
         * </p>
         *
         * @return A value defining what return values of the advice method indicate that the instrumented method
         * should be repeated or {@code void} if the instrumented method should never be repeated.
         */
        Class<?> repeatOn() default void.class;

        /**
         * Indicates a {@link Throwable} super type for which this exit advice is invoked if it was thrown from the instrumented method.
         * If an exception is thrown, it is available via the {@link Thrown} parameter annotation. If a method returns exceptionally,
         * any parameter annotated with {@link Return} is assigned the parameter type's default value.
         *
         * @return The type of {@link Throwable} for which this exit advice handler is invoked.
         */
        Class<? extends Throwable> onThrowable() default NoExceptionHandler.class;

        /**
         * <p>
         * If {@code true}, all arguments of the instrumented method are copied before execution. Doing so, parameter reassignments applied
         * by the instrumented are not effective during the execution of the annotated exit advice.
         * </p>
         * <p>
         * Disabling this option can cause problems with the translation of stack map frames (meta data that is embedded in a Java class) if these
         * frames become inconsistent with the original arguments of the instrumented method. In this case, the original arguments are no longer
         * available to the exit advice such that Byte Buddy must abort the instrumentation with an error. If the instrumented method does not issue
         * a stack map frame due to a lack of branching instructions, Byte Buddy might not be able to discover such an inconsistency what can cause
         * a {@link VerifyError} instead of a Byte Buddy-issued exception as those inconsistencies are not discovered.
         * </p>
         *
         * @return {@code true} if a backup of all method arguments should be made.
         */
        boolean backupArguments() default true;

        /**
         * Determines if the annotated method should be inlined into the instrumented method or invoked from it. When a method
         * is inlined, its byte code is copied into the body of the target method. this makes it is possible to execute code
         * with the visibility privileges of the instrumented method while loosing the privileges of the declared method methods.
         * When a method is not inlined, it is invoked similarly to a common Java method call. Note that it is not possible to
         * set breakpoints within a method when it is inlined as no debugging information is copied from the advice method into
         * the instrumented method.
         *
         * @return {@code true} if the annotated method should be inlined into the instrumented method.
         */
        boolean inline() default true;

        /**
         * Indicates that this advice should suppress any {@link Throwable} type being thrown during the advice's execution. By default,
         * any such exception is silently suppressed. Custom behavior can be configured by using {@link Advice#withExceptionHandler(StackManipulation)}.
         *
         * @return The type of {@link Throwable} to suppress.
         * @see Advice#withExceptionPrinting()
         */
        Class<? extends Throwable> suppress() default NoExceptionHandler.class;
    }

    /**
     * A marker class that indicates that an advice method does not suppress any {@link Throwable}.
     */
    public static class NoExceptionHandler extends Throwable {

        /**
         * The class's serial version UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * A description of the {@link NoExceptionHandler} type.
         */
        private static final TypeDescription DESCRIPTION = TypeDescription.ForLoadedType.of(NoExceptionHandler.class);

        /**
         * A private constructor as this class is not supposed to be invoked.
         */
        private NoExceptionHandler() {
            throw new UnsupportedOperationException("This class only serves as a marker type and should not be instantiated");
        }
    }
}
