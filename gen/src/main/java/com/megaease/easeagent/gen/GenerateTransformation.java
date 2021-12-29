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

package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Dispatcher;
import com.megaease.easeagent.plugin.annotation.Injection;
import com.squareup.javapoet.*;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        GenerateTransformerFactoryMethod(TypeSpec.Builder builder) {
            super(builder);
        }

        @Override
        public TypeSpec.Builder visitExecutableAsMethod(final ExecutableElement e, ProcessUtils utils) {
            final List<? extends VariableElement> parameters = e.getParameters();
            final TypeMirror returnType = e.getReturnType();

            if (parameters.size() > 2) {
                return super.visitExecutableAsMethod(e, utils);
            }

            if (!e.getModifiers().contains(Modifier.ABSTRACT)
                    || !utils.isSameType(returnType, Definition.Transformer.class)
            ) {
                return super.visitExecutableAsMethod(e, utils);
            }

            if (parameters.size() == 1 && isNotMethodElementMatcher(parameters.get(0).asType(), utils)) {
                return super.visitExecutableAsMethod(e, utils);
            }

            if (parameters.size() == 2 && isNotMethodElementMatcher(parameters.get(0).asType(), utils)
                    && isNotString(parameters.get(1).asType(), utils)
            ) {
                return super.visitExecutableAsMethod(e, utils);
            }

            if (e.getAnnotation(AdviceTo.class) == null)
                throw new ElementException(e, "should be annotated with " + AdviceTo.class);

            final TypeSpec.Builder builder = super.visitExecutableAsMethod(e, utils);

            final TypeElement adviceElement = utils.asTypeElement(() -> e.getAnnotation(AdviceTo.class).value());

            if (adviceElement.getNestingKind() == NestingKind.MEMBER && !adviceElement.getModifiers().contains(Modifier.STATIC))
                throw new ElementException(e, "should be top level or static");


            final String adviceClassName = generatedClassName + '$' + utils.simpleNameOf(adviceElement);
            final String inlineAdviceClassName = adviceClassName + "_inline";
            final String adviceFactoryClassName = adviceClassName + "_factory";

            String format = "return new $T($S, $S, $L)";
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

        private boolean isNotString(TypeMirror tm, ProcessUtils utils) {
            final WildcardTypeName wtn = WildcardTypeName.supertypeOf(MethodDescription.class);
            return !utils.typeNameOf(tm).equals(ParameterizedTypeName.get(ClassName.get(String.class), wtn));
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

                GenerateMethods(TypeSpec.Builder builder) {
                    super(builder);
                }

                @Override
                public TypeSpec.Builder visitExecutableAsMethod(ExecutableElement e, ProcessUtils utils) {
                    final boolean noEnter = e.getAnnotation(Advice.OnMethodEnter.class) == null;
                    final boolean noExit = e.getAnnotation(Advice.OnMethodExit.class) == null;

                    if (noEnter && noExit) return super.visitExecutableAsMethod(e, utils);

                    return super.visitExecutableAsMethod(e, utils).addMethod(inlineAdviceMethod(e, utils));

                }

                public Map<VariableElement, String> getReplaceMap(List<? extends VariableElement> parameters) {
                    Map<VariableElement, String> result = new HashMap<>();
                    Optional<? extends VariableElement> changeAllArgs = parameters.stream().filter(p -> {
                        Advice.AllArguments aa = p.getAnnotation(Advice.AllArguments.class);
                        if (aa == null) {
                            return false;
                        }
                        return !aa.readOnly();
                    }).findFirst();


                    changeAllArgs.ifPresent(p -> result.put(p, "allArgsDump"));
                    return result;
                }

                public String buildBeforeExecute(Map<VariableElement, String> replaceMap) {
                    if (replaceMap.isEmpty()) {
                        return "";
                    }
                    StringBuilder sb = new StringBuilder();
                    replaceMap.forEach((k, v) -> {
                        final TypeName tn = TypeName.get(k.asType());
                        sb.append(String.format("%s %s = %s;\n", tn, v, k.getSimpleName()));
                    });
                    return sb.toString();
                }

                public String buildAfterExecute(Map<VariableElement, String> replaceMap) {
                    if (replaceMap.isEmpty()) {
                        return "";
                    }
                    StringBuilder sb = new StringBuilder();
                    replaceMap.forEach((k, v) -> sb.append(String.format("\n%s = %s;", k.getSimpleName(), v)));
                    return sb.toString();
                }

                private MethodSpec inlineAdviceMethod(ExecutableElement e, ProcessUtils utils) {
                    final String name = utils.simpleNameOf(e);
                    final TypeName returnType = utils.typeNameOf(e.getReturnType());

                    final String format;
                    final Object[] args;
                    final List<? extends VariableElement> parameters = e.getParameters();

                    Map<VariableElement, String> replaceMap = getReplaceMap(parameters);

                    final String join = parameters.isEmpty() ? "null" : join(parameters, replaceMap);

                    String beforeExecute = buildBeforeExecute(replaceMap);
                    String afterExecute = buildAfterExecute(replaceMap);

                    if (TypeName.VOID == returnType) {
                        format = beforeExecute + "$T.execute($S, $L);" + afterExecute;
                        args = new Object[]{Dispatcher.class, generateClassName + "#advice_" + name, join};
                    } else {
//                        format = "return ($T) $T.execute($S, $L)";
                        if (!parameters.isEmpty()) {
                            String retArg = null;
                            int i = 0;
                            for (VariableElement parameter : parameters) {
                                if (parameter.getAnnotation(Advice.Return.class) != null) {
                                    retArg = "arg" + i;
                                    break;
                                }
                                i++;
                            }
                            if (retArg == null) {
                                format = beforeExecute + "$T result = ($T) $T.execute($S, $L);\n" + afterExecute + "return result";
                            } else {
                                format = beforeExecute + "$T result = ($T) $T.execute($S, $L);\n" + retArg + " = result;\n" + afterExecute + "return " + retArg;
                            }

                        } else {
                            format = beforeExecute + "$T result = ($T) $T.execute($S, $L);\n" + afterExecute + "return result";
                        }
                        TypeName returnTypeName = returnType.isPrimitive() ? returnType.box() : returnType;
                        args = new Object[]{
                                returnTypeName, returnTypeName,
                                Dispatcher.class, generateClassName + "#advice_" + name, join
                        };
                    }
                    return MethodSpec.methodBuilder(name)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addAnnotations(utils.asAnnotationSpecs(e.getAnnotationMirrors()))
                            .addParameters(utils.asParameterSpecs(parameters))
                            .returns(returnType)
                            .addStatement(format, args)
                            .build();

                }

            }

        }

        private class GenerateAdviceFactoryClass extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {
            GenerateAdviceFactoryClass(TypeSpec.Builder builder) {
                super(builder);
            }

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

                GenerateMethods(TypeSpec.Builder builder) {
                    super(builder);
                }

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
                    final ExecutableElement execute = (ExecutableElement)utils
                        .asTypeElement(() -> Dispatcher.Advice.class)
                        .getEnclosedElements().get(0);

                    final String name = utils.simpleNameOf(e);
                    final TypeName returnType = utils.typeNameOf(e.getReturnType());
                    final MethodSpec.Builder builder = MethodSpec.overriding(execute);

                    final Function<VariableElement, String> mapFunction = new Function<VariableElement, String>() {
                        int i = 0;
                        @Override
                        public String apply(VariableElement input) {
                            final TypeName tn = utils.typeNameOf(input.asType());
                            return String.format("(%s) arg0[%d]", tn.isPrimitive() ? tn.box() : tn, i++);
                        }
                    };
                    final Object[] args = new Object[]{name,
                        e.getParameters().stream().map(mapFunction).collect(Collectors.joining(", "))
                    };

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
        return join(parameters, Collections.emptyMap());
    }

    private static String join(List<? extends VariableElement> parameters, Map<VariableElement, String> replaces) {
        return parameters.stream().map(e -> replaces.getOrDefault(e, e.toString())).collect(Collectors.joining(", "));
    }
}

