package com.megaease.easeagent.gen;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Dispatcher;
import com.megaease.easeagent.core.Injection;
import com.squareup.javapoet.*;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

class GenerateTransformation extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {
    private final String generatedClassName;

    GenerateTransformation(TypeSpec.Builder builder, String generatedClassName) {
        super(builder);
        this.generatedClassName = generatedClassName;
    }

    @Override
    public TypeSpec.Builder visitTypeAsClass(TypeElement e, ProcessUtils utils) {
        final TypeSpec.Builder builder = super.visitTypeAsClass(e, utils);
        for (Element member : e.getEnclosedElements()) {
            member.accept(new GenerateTransformerFactoryMethod(builder), utils);
        }
        return builder;
    }

    private class GenerateTransformerFactoryMethod extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {
        GenerateTransformerFactoryMethod(TypeSpec.Builder builder) {super(builder);}

        @Override
        public TypeSpec.Builder visitExecutableAsMethod(final ExecutableElement e, ProcessUtils utils) {
            final List<? extends VariableElement> parameters = e.getParameters();
            final TypeMirror returnType = e.getReturnType();

            if (!e.getModifiers().contains(Modifier.ABSTRACT)
                    || !utils.isSameType(returnType, Definition.Transformer.class)
                    || parameters.size() != 1
                    || isNotMethodElementMatcher(parameters.get(0).asType(), utils))
                return super.visitExecutableAsMethod(e, utils);

            if (e.getAnnotation(AdviceTo.class) == null)
                throw new ElementException(e, "should be annotated with " + AdviceTo.class);

            final TypeSpec.Builder builder = super.visitExecutableAsMethod(e, utils);

            final TypeElement adviceElement = utils.asTypeElement(new Supplier<Class<?>>() {
                @Override
                public Class<?> get() {
                    return e.getAnnotation(AdviceTo.class).value();
                }
            });

            if (adviceElement.getNestingKind() == NestingKind.MEMBER && !adviceElement.getModifiers().contains(Modifier.STATIC))
                throw new ElementException(e, "should be top level or static");


            final String adviceClassName = generatedClassName + '$' + utils.simpleNameOf(adviceElement);
            final String inlineAdviceClassName = adviceClassName + "_inline";
            final String adviceFactoryClassName = adviceClassName + "_factory";

            final String format = "return new $T($S, $S, $L)";
            final Object[] args = new Object[]{Definition.Transformer.class, inlineAdviceClassName, adviceFactoryClassName, join(parameters)};
            builder.addMethod(MethodSpec.overriding(e).addStatement(format, args).build());

            adviceElement.accept(new GenerateInlineAdviceClass(builder, adviceFactoryClassName), utils);
            adviceElement.accept(new GenerateAdviceFactoryClass(builder), utils);

            return builder;
        }

        private boolean isNotMethodElementMatcher(TypeMirror tm, ProcessUtils utils) {
            final WildcardTypeName wtn = WildcardTypeName.supertypeOf(MethodDescription.class);
            return !utils.typeNameOf(tm).equals(ParameterizedTypeName.get(ClassName.get(ElementMatcher.class), wtn));
        }

        private class GenerateInlineAdviceClass extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {
            private final String generateClassName;

            GenerateInlineAdviceClass(TypeSpec.Builder builder, String generateClassName) {
                super(builder);
                this.generateClassName = generateClassName;
            }

            @Override
            public TypeSpec.Builder visitTypeAsClass(TypeElement e, ProcessUtils utils) {

                final TypeSpec.Builder builder = TypeSpec.classBuilder(utils.simpleNameOf(e) + "_inline")
                                                         .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                for (Element member : e.getEnclosedElements()) {
                    member.accept(new GenerateMethods(builder), utils);
                }
                return super.visitTypeAsClass(e, utils).addType(builder.build());
            }

            private class GenerateMethods extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {

                GenerateMethods(TypeSpec.Builder builder) { super(builder); }

                @Override
                public TypeSpec.Builder visitExecutableAsMethod(ExecutableElement e, ProcessUtils utils) {
                    final boolean noEnter = e.getAnnotation(Advice.OnMethodEnter.class) == null;
                    final boolean noExit = e.getAnnotation(Advice.OnMethodExit.class) == null;

                    if (noEnter && noExit) return super.visitExecutableAsMethod(e, utils);

                    return super.visitExecutableAsMethod(e, utils).addMethod(inlineAdviceMethod(e, utils));

                }

                private MethodSpec inlineAdviceMethod(ExecutableElement e, ProcessUtils utils) {
                    final String name = utils.simpleNameOf(e);
                    final TypeName returnType = utils.typeNameOf(e.getReturnType());

                    final String format;
                    final Object[] args;
                    final List<? extends VariableElement> parameters = e.getParameters();

                    final String join = parameters.isEmpty() ? "null" : join(parameters);

                    if (TypeName.VOID == returnType) {
                        format = "$T.execute($S, $L)";
                        args = new Object[]{Dispatcher.class, generateClassName + "#advice_" + name, join};
                    } else {
                        format = "return ($T) $T.execute($S, $L)";
                        args = new Object[]{
                                returnType.isPrimitive() ? returnType.box() : returnType,
                                Dispatcher.class, generateClassName + "#advice_" + name, join
                        };
                    }
                    return MethodSpec.methodBuilder(name)
                                     .addModifiers(Modifier.STATIC)
                                     .addAnnotations(utils.asAnnotationSpecs(e.getAnnotationMirrors()))
                                     .addParameters(utils.asParameterSpecs(parameters))
                                     .returns(returnType)
                                     .addStatement(format, args)
                                     .build();

                }

            }

        }

        private class GenerateAdviceFactoryClass extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {

            GenerateAdviceFactoryClass(TypeSpec.Builder builder) { super(builder); }

            @Override
            public TypeSpec.Builder visitTypeAsClass(TypeElement e, ProcessUtils utils) {

                final TypeSpec.Builder builder = TypeSpec.classBuilder(utils.simpleNameOf(e) + "_factory")
                                                         .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                                         .superclass(utils.classNameOf(e));
                for (Element member : e.getEnclosedElements()) {
                    member.accept(new GenerateMethods(builder), utils);
                }

                return super.visitTypeAsClass(e, utils).addType(builder.build());
            }

            private class GenerateMethods extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {

                GenerateMethods(TypeSpec.Builder builder) {super(builder);}

                @Override
                public TypeSpec.Builder visitExecutableAsConstructor(ExecutableElement e, final ProcessUtils utils) {
                    if (e.getAnnotation(Injection.Autowire.class) == null)
                        return super.visitExecutableAsConstructor(e, utils);

                    final List<? extends VariableElement> parameters = e.getParameters();

                    if (parameters.isEmpty())
                        throw new ElementException(e, "should have parameters for autowire");

                    return super.visitExecutableAsConstructor(e, utils)
                                .addMethod(MethodSpec.constructorBuilder()
                                                     .addModifiers(Modifier.PUBLIC)
                                                     .addAnnotations(utils.asAnnotationSpecs(e.getAnnotationMirrors()))
                                                     .addParameters(utils.asParameterSpecs(parameters))
                                                     .addStatement("super($L)", join(parameters))
                                                     .build());
                }

                @Override
                public TypeSpec.Builder visitExecutableAsMethod(ExecutableElement e, ProcessUtils utils) {
                    final boolean noEnter = e.getAnnotation(Advice.OnMethodEnter.class) == null;
                    final boolean noExit = e.getAnnotation(Advice.OnMethodExit.class) == null;

                    if (noEnter && noExit) return super.visitExecutableAsMethod(e, utils);

                    return super.visitExecutableAsMethod(e, utils).addMethod(adviceFactoryMethod(e, utils));
                }

                private MethodSpec adviceFactoryMethod(ExecutableElement e, final ProcessUtils utils) {
                    final ExecutableElement execute = (ExecutableElement) utils.asTypeElement(ADVICE_CLASS).getEnclosedElements().get(0);

                    final String name = utils.simpleNameOf(e);
                    final TypeName returnType = utils.typeNameOf(e.getReturnType());
                    final MethodSpec.Builder builder = MethodSpec.overriding(execute);
                    final Object[] args = new Object[]{name, from(e.getParameters()).transform(new Function<VariableElement, String>() {
                        int i = 0;

                        @Override
                        public String apply(VariableElement input) {
                            final TypeName tn = utils.typeNameOf(input.asType());
                            return String.format("(%s) arg0[%d]", tn.isPrimitive() ? tn.box() : tn, i++);
                        }
                    }).join(Joiner.on(", "))};

                    if (TypeName.VOID == returnType) {
                        builder.addStatement("$L($L)", args);
                        builder.addStatement("return null");
                    } else {
                        builder.addStatement("return $L($L)", args);
                    }

                    return MethodSpec.methodBuilder("advice_" + name)
                                     .addModifiers(Modifier.PUBLIC)
                                     .returns(Dispatcher.Advice.class)
                                     .addStatement("return $L", TypeSpec.anonymousClassBuilder("")
                                                                        .addSuperinterface(Dispatcher.Advice.class)
                                                                        .addMethod(builder.build())
                                                                        .build()).build();
                }
            }
        }
    }

    private static String join(List<? extends VariableElement> parameters) {
        return from(parameters).transform(TO_STRING).join(JOINER);
    }

    private static final Supplier<Class<?>> ADVICE_CLASS = new Supplier<Class<?>>() {
        @Override
        public Class<?> get() {
            return Dispatcher.Advice.class;
        }
    };

    private static final Function<VariableElement, String> TO_STRING = new Function<VariableElement, String>() {
        @Override
        public String apply(VariableElement input) {
            return input.toString();
        }
    };
    private static final Joiner JOINER = Joiner.on(", ");

}
