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
import com.megaease.easeagent.core.Injection;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singleton;

@AutoService(Processor.class)
public class AssemblyProcessor extends AbstractProcessor {
    Class<Injection.Provider> annotationClass = Injection.Provider.class;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final ProcessUtils utils = ProcessUtils.of(processingEnv);

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Assembly.class);
        for (final Element element : elements) {
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

    private void process(final ProcessUtils utils, final Assembly assembly, String packageName) {
        final Iterable<TypeElement> transformations = utils.asTypeElements(assembly::value);
        Iterable<JavaFile> files = process(packageName, utils, transformations);
        files.forEach(file -> {
            try {
                file.toBuilder().indent("    ")
                    .addFileComment("This ia a generated file.")
                    .build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
                                              Iterable<TypeElement> transformations) {
        Class<Injection.Provider> annotationClass = Injection.Provider.class;
        Stream<TypeElement> providers = StreamSupport.stream(transformations.spliterator(), false)
            .filter(element -> element.getAnnotation(annotationClass) != null)
            .map(element -> utils.asTypeElement(() -> element.getAnnotation(annotationClass).value()));

        Set<JavaFile> transformationFiles = StreamSupport.stream(transformations.spliterator(), false)
            .map(new GenerateFunction(utils, FOR_TRANSFORMATION))
            .collect(Collectors.toSet());

        Set<JavaFile> providerFiles = providers
            .map(new GenerateFunction(utils, FOR_PROVIDER))
            .collect(Collectors.toSet());

        JavaFile startBootstrap = new GenerateStarter(packageName,
            classesOf(providerFiles),
            classesOf(transformationFiles)).apply();

        providerFiles.addAll(transformationFiles);
        providerFiles.add(startBootstrap);

        return providerFiles;
    }

    private void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    private static String classesOf(Set<JavaFile> providers) {
        return providers.stream()
            .map(TO_CLASS_NAME)
            .map(APPEND_CLASS_SUFFIX)
            .collect(Collectors.joining(ON_COMMA_STR));
    }

    private static final Function<String, String> APPEND_CLASS_SUFFIX = input -> input + ".class";

    private static final Function<JavaFile, String> TO_CLASS_NAME = input -> input.packageName + "." + input.typeSpec.name;

    private static final String ON_COMMA_STR = ",\n";

    private static final Iterable<? extends GenerateSpecFactory> FOR_PROVIDER = singleton(
        (builder, generatedClassName) -> new GenerateConfiguration(builder));

    private static final Iterable<? extends GenerateSpecFactory> FOR_TRANSFORMATION = Arrays.asList(
        GenerateTransformation::new,
        (builder, generatedClassName) -> new GenerateConfiguration(builder)
    );

    interface GenerateSpecFactory {
        ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> create(TypeSpec.Builder builder, String generatedClassName);
    }
}
