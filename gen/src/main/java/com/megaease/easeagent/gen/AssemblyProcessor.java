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

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.megaease.easeagent.core.Injection;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.singleton;

@AutoService(Processor.class)
public class AssemblyProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final ProcessUtils utils = ProcessUtils.of(processingEnv);
        for (final Element element : roundEnv.getElementsAnnotatedWith(Assembly.class)) {
            try {
                process(utils, element.getAnnotation(Assembly.class), utils.packageNameOf((TypeElement) element));
            } catch (ElementException e) {
                error(e.element, e.getLocalizedMessage());
                return true;
            } catch (Exception e) {
                error(element, e.getLocalizedMessage());
                return true;
            }
        }
        return false;
    }

    private void process(final ProcessUtils utils, final Assembly assembly, String packageName) throws IOException {
        final Iterable<TypeElement> transformations = utils.asTypeElements(new Supplier<Class<?>[]>() {
            @Override
            public Class<?>[] get() {
                return assembly.value();
            }
        }).toList();

        final Iterable<TypeElement> providers = from(transformations).filter(new Predicate<TypeElement>() {
            @Override
            public boolean apply(TypeElement input) {
                return input.getAnnotation(Injection.Provider.class) != null;
            }
        }).transform(new Function<TypeElement, TypeElement>() {
            @Override
            public TypeElement apply(final TypeElement input) {
                return utils.asTypeElement(new Supplier<Class<?>>() {
                    @Override
                    public Class<?> get() {
                        return input.getAnnotation(Injection.Provider.class).value();
                    }
                });
            }
        }).toSet();

        for (JavaFile file : process(packageName, utils, providers, transformations)) {
            file.toBuilder().indent("    ").addFileComment("This ia a generated file.").build()
                .writeTo(processingEnv.getFiler());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(Assembly.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static Iterable<JavaFile> process(String packageName, ProcessUtils utils,
                                              Iterable<TypeElement> providers,
                                              Iterable<TypeElement> transformations) {

        final Iterable<JavaFile> providerFiles =
                from(providers).transform(new GenerateFunction(utils, FOR_PROVIDER)).toList();
        final Iterable<JavaFile> transformationFiles =
                from(transformations).transform(new GenerateFunction(utils, FOR_TRANSFORMATION)).toList();

        return Iterables.concat(providerFiles, transformationFiles, singleton(
                new GenerateStarter(packageName, classesOf(providerFiles), classesOf(transformationFiles)).apply())
        );

    }

    private void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    private static String classesOf(Iterable<JavaFile> providers) {
        return from(providers).transform(TO_CLASS_NAME).transform(APPEND_CLASS_SUFFIX).join(ON_COMMA);
    }

    private static final Function<String, String> APPEND_CLASS_SUFFIX = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input + ".class";
        }
    };

    private static final Function<JavaFile, String> TO_CLASS_NAME = new Function<JavaFile, String>() {
        @Override
        public String apply(JavaFile input) {
            return input.packageName + "." + input.typeSpec.name;
        }
    };

    private static final Joiner ON_COMMA = Joiner.on(",\n");

    private static final Iterable<? extends GenerateSpecFactory> FOR_PROVIDER = singleton(new GenerateSpecFactory() {
        @Override
        public ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> create(TypeSpec.Builder builder, String generatedClassName) {
            return new GenerateConfiguration(builder);
        }
    });

    private static final Iterable<? extends GenerateSpecFactory> FOR_TRANSFORMATION = Arrays.asList(
            new GenerateSpecFactory() {
                @Override
                public ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> create(TypeSpec.Builder builder, String generatedClassName) {
                    return new GenerateTransformation(builder, generatedClassName);
                }
            },
            new GenerateSpecFactory() {

                @Override
                public ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> create(TypeSpec.Builder builder, String generatedClassName) {
                    return new GenerateConfiguration(builder);
                }
            }
    );

    interface GenerateSpecFactory {
        ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> create(TypeSpec.Builder builder, String generatedClassName);
    }
}
