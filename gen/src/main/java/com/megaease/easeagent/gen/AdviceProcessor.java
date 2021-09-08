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
package com.megaease.easeagent.gen;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.singleton;
import static javax.lang.model.element.Modifier.ABSTRACT;

@AutoService(Processor.class)
public class AdviceProcessor extends AbstractProcessor {
    Class<Generate.Advice> annotationClass = Generate.Advice.class;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(annotationClass.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Map<String,Set<String>> services = new HashMap<>();
        String cn = "com.megaease.easeagent.core.Transformation";
        Elements elements = processingEnv.getElementUtils();

        Set<? extends Element> roundElements = roundEnv.getElementsAnnotatedWith(annotationClass);
        for (Element e : roundElements) {
            if (!e.getKind().isClass()) {
                continue;
            }
            if (!e.getModifiers().contains(ABSTRACT)) {
                continue;
            }
            TypeElement type = (TypeElement)e;

            Set<String> v = services.computeIfAbsent(cn, k -> new TreeSet<>());
            v.add(elements.getBinaryName(type).toString());
        }

        Filer filer = processingEnv.getFiler();
        String prefix = "META-INF/services/";

        Set<String> providers = services.get(cn);
        if (providers == null || providers.isEmpty()) {
            return false;
        } else {
            try {
                String contract = prefix + cn;
                processingEnv.getMessager().printMessage(Kind.NOTE,"Writing " + contract);
                FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", contract);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), StandardCharsets.UTF_8));
                providers.forEach(pw::println);
                pw.close();
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write generated files: " + x);
            }
        }

        return false;
    }
}

