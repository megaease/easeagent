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
import com.megaease.easeagent.plugin.annotation.Plugin;
import com.megaease.easeagent.plugin.annotation.Pointcut;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

import static javax.lang.model.element.Modifier.ABSTRACT;

@AutoService(AgentPlugin.class)
public class PluginProcessor extends AbstractProcessor {
    Class<Plugin> annotationClass = Plugin.class;
    TreeSet<String>  annotations = new TreeSet<>();
    {
        annotations.add(Plugin.class.getCanonicalName());
        annotations.add(Pointcut.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return annotations;
    }

    private boolean process(Class<? extends Annotation> annotationClass,
                            Elements elements,
                            RoundEnvironment roundEnv) {
        TreeSet<String> services = new TreeSet<>();

        Set<? extends Element> roundElements = roundEnv.getElementsAnnotatedWith(annotationClass);
        for (Element e : roundElements) {
            if (!e.getKind().isClass()) {
                continue;
            }
            if (!e.getModifiers().contains(ABSTRACT)) {
                continue;
            }
            TypeElement type = (TypeElement)e;
            services.add(elements.getBinaryName(type).toString());
        }

        String fileName = "META-INF/services/" + annotationClass.getCanonicalName();

        if (services.isEmpty()) {
            return false;
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

        return false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Elements elements = processingEnv.getElementUtils();
        process(Plugin.class, elements, roundEnv);
        process(Pointcut.class, elements, roundEnv);

        return false;
    }
}


