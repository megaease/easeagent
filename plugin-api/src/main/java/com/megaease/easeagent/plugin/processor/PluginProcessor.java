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

package com.megaease.easeagent.plugin.processor;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.Provider;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Objects.isNull;
import static javax.lang.model.element.Modifier.ABSTRACT;

@AutoService(Processor.class)
public class PluginProcessor extends AbstractProcessor {
    // Class<Plugin> annotationClass = Plugin.class;
    TreeSet<String>  annotations = new TreeSet<>();
    {
        annotations.add(AdviceTo.class.getCanonicalName());
        // for test temporarily
        // annotations.add(ProviderBean.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return annotations;
    }

    private Set<TypeElement> process(Class<? extends Annotation> annotationClass,
                            Class<?> dstClass,
                            Elements elements,
                            RoundEnvironment roundEnv) {
        TreeSet<String> services = new TreeSet<>();
        Set<TypeElement> types = new HashSet<>();

        Set<? extends Element> roundElements = roundEnv.getElementsAnnotatedWith(annotationClass);
        for (Element e : roundElements) {
            if (!e.getKind().isClass()) {
                continue;
            }
            if (e.getModifiers().contains(ABSTRACT)) {
                continue;
            }
            TypeElement type = (TypeElement)e;
            types.add(type);
            services.add(elements.getBinaryName(type).toString());
        }
        if (services.isEmpty()) {
            return types;
        }
        writeToMetaInf(dstClass, services);

        return types;
    }

    private void writeToMetaInf(Class<?> dstClass, Set<String> services) {
        String fileName = "META-INF/services/" + dstClass.getCanonicalName();

        if (services.isEmpty()) {
            return;
        }

        Filer filer = processingEnv.getFiler();
        PrintWriter pw = null;
        try {
            processingEnv.getMessager().printMessage(Kind.NOTE,"Writing " + fileName);
            FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
            pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), StandardCharsets.UTF_8));
            services.forEach(pw::println);
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write generated files: " + x);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        final BeanUtils utils = BeanUtils.of(processingEnv);
        Elements elements = processingEnv.getElementUtils();
        TypeElement plugin = searchPluginClass(roundEnv.getRootElements(), utils);
        if (plugin == null) {
            processingEnv.getMessager().printMessage(Kind.WARNING, "Can't find AgentPlugin class!");
            return false;
        }
        // process(Pointcut.class, Points.class, elements, roundEnv);
        Set<TypeElement> interceptors = process(AdviceTo.class, Interceptor.class, elements, roundEnv);
        // generate providerBean
        generateProviderBeans(plugin, interceptors, utils);
        // process(ProviderBean.class, Provider.class, elements, roundEnv);

        return false;
    }

    TypeElement searchPluginClass(Set<? extends Element> elements, BeanUtils utils) {
        TypeElement findInterface = utils.getTypeElement(AgentPlugin.class.getCanonicalName());
        TypeElement firstFound;

        ElementVisitor8 visitor = new ElementVisitor8(utils);
        for (Element e : elements) {
            firstFound = e.accept(visitor, findInterface);
            if (firstFound != null) {
                writeToMetaInf(AgentPlugin.class, Collections.singleton(utils.classNameOf(firstFound).canonicalName()));
                return firstFound;
            }
        }

        return null;
    }

    public void generateProviderBeans(TypeElement plugin, Set<TypeElement> interceptors, BeanUtils utils) {
        TreeSet<String> providers = new TreeSet<>();
        TreeSet<String> points = new TreeSet<>();
        for (TypeElement type : interceptors) {
            if(isNull(type.getAnnotation(AdviceTo.class))) {
                continue;
            }
            List<? extends AnnotationMirror> annotations = type.getAnnotationMirrors();
            for (AnnotationMirror annotation : annotations) {
                if (!utils.isSameType(annotation.getAnnotationType(), AdviceTo.class.getCanonicalName())) {
                    continue;
                }
                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                Map<String, String> to = new HashMap<>();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
                    String key = e.getKey().getSimpleName().toString();
                    AnnotationValue av = e.getValue();
                    String value;
                    if (av.getValue() == null) {
                        value = "default";
                    } else {
                        value = av.getValue().toString();
                    }
                    to.put(key, value);
                    if (key.equals("value")) {
                        points.add(value);
                    }
                }
                GenerateProviderBean gb = new GenerateProviderBean(plugin, type, to, utils);
                JavaFile file = gb.apply();
                try {
                    file.toBuilder().indent("    ")
                        .addFileComment("This ia a generated file.")
                        .build().writeTo(processingEnv.getFiler());
                    providers.add(gb.getProviderClass());
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
                }
            }
        }
        writeToMetaInf(Points.class, points);
        writeToMetaInf(Provider.class, providers);
    }
}
