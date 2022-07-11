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

package com.megaease.easeagent.plugin.processor;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.interceptor.InterceptorProvider;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.annotation.AdvicesTo;
import com.squareup.javapoet.ClassName;
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
    TreeSet<String> processAnnotations = new TreeSet<>();

    public PluginProcessor() {
        super();
        processAnnotations.add(AdviceTo.class.getCanonicalName());
        processAnnotations.add(AdvicesTo.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return processAnnotations;
    }

    private Set<TypeElement> process(Set<Class<? extends Annotation>> annotationClasses,
                            Elements elements,
                            RoundEnvironment roundEnv) {
        TreeSet<String> services = new TreeSet<>();
        Set<TypeElement> types = new HashSet<>();
        Class<?> dstClass = Interceptor.class;

        Set<Element> roundElements = new HashSet<>();
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            Set<? extends Element> es = roundEnv.getElementsAnnotatedWith(annotationClass);
            roundElements.addAll(es);
        }

        for (Element e : roundElements) {
            if (!e.getKind().isClass() || e.getModifiers().contains(ABSTRACT)) {
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

    private void writeToMetaInf(Class<?> dstClass, Collection<String> services) {
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
        LinkedHashMap<String, TypeElement> plugins = searchPluginClass(roundEnv.getRootElements(), utils);
        if (plugins == null || plugins.isEmpty()) {
            processingEnv.getMessager().printMessage(Kind.WARNING, "Can't find AgentPlugin class!");
            return false;
        }
        Set<Class<? extends Annotation>> classes = new HashSet<>();
        classes.add(AdvicesTo.class);
        classes.add(AdviceTo.class);
        Set<TypeElement> interceptors = process(classes, elements, roundEnv);
        // generate providerBean
        generateProviderBeans(plugins, interceptors, utils);

        return false;
    }

    LinkedHashMap<String, TypeElement> searchPluginClass(Set<? extends Element> elements, BeanUtils utils) {
        TypeElement findInterface = utils.getTypeElement(AgentPlugin.class.getCanonicalName());
        TypeElement found;

        ArrayList<TypeElement> plugins = new ArrayList<>();
        ElementVisitor8 visitor = new ElementVisitor8(utils);
        for (Element e : elements) {
            found = e.accept(visitor, findInterface);
            if (found != null) {
                plugins.add(found);
            }
        }
        LinkedHashMap<String, TypeElement> pluginNames = new LinkedHashMap<>();
        for (TypeElement p : plugins) {
            ClassName className = utils.classNameOf(p);
            pluginNames.put(className.canonicalName(), p);
        }
        writeToMetaInf(AgentPlugin.class, pluginNames.keySet());

        return pluginNames;
    }

    private void generateProviderBeans(LinkedHashMap<String, TypeElement> plugins,
                                       Set<TypeElement> interceptors, BeanUtils utils) {
        TreeSet<String> providers = new TreeSet<>();
        TreeSet<String> points = new TreeSet<>();
        for (TypeElement type : interceptors) {
            if(isNull(type.getAnnotation(AdviceTo.class))
                && isNull(type.getAnnotation(AdvicesTo.class))) {
                continue;
            }
            List<? extends AnnotationMirror> annotations = type.getAnnotationMirrors();
            Set<AnnotationMirror> adviceToAnnotations = new HashSet<>();
            for (AnnotationMirror annotation : annotations) {
                if (utils.isSameType(annotation.getAnnotationType(), AdviceTo.class.getCanonicalName())) {
                    adviceToAnnotations.add(annotation);
                    continue;
                }
                if (!utils.isSameType(annotation.getAnnotationType(), AdvicesTo.class.getCanonicalName())) {
                    continue;
                }
                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                RepeatedAnnotationVisitor visitor = new RepeatedAnnotationVisitor();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : values.entrySet()) {
                    String key = e.getKey().getSimpleName().toString();
                    if (key.equals("value")) {
                        AnnotationValue av = e.getValue();
                        Set<AnnotationMirror> as = av.accept(visitor, AdvicesTo.class);
                        adviceToAnnotations.addAll(as);
                        break;
                    }
                }
            }

            int seq = 0;
            TypeElement plugin = plugins.values().toArray(new TypeElement[0])[0];
            for (AnnotationMirror annotation : adviceToAnnotations) {
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
                    } else if (key.equals("plugin") && plugins.get(value) != null) {
                        plugin = plugins.get(value);
                    }
                }
                to.put("seq", Integer.toString(seq));
                GenerateProviderBean gb = new GenerateProviderBean(plugin, type, to, utils);
                JavaFile file = gb.apply();
                try {
                    file.toBuilder().indent("    ")
                        .addFileComment("This ia a generated file.")
                        .build().writeTo(processingEnv.getFiler());
                    providers.add(gb.getProviderClass());
                    seq += 1;
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
                }
            }
        }
        writeToMetaInf(Points.class, points);
        writeToMetaInf(InterceptorProvider.class, providers);
    }
}
