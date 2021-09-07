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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class ProcessUtils {

    static ProcessUtils of(final ProcessingEnvironment pe) {
        return new ProcessUtils(pe) {
        };
    }

    private final Elements elements;
    private final Types types;

    private ProcessUtils(ProcessingEnvironment pe) {
        elements = pe.getElementUtils();
        types = pe.getTypeUtils();
    }

    boolean isSameType(TypeMirror t, Class<?> c, Class<?>... typeParams) {
        if (typeParams.length == 0) {
            return isSameType(t, c.getCanonicalName());
        }

        if (!isSameType(asElement(t).asType(), c.getCanonicalName())) {
            return false;
        }

        final List<? extends TypeMirror> arguments = ((DeclaredType) t).getTypeArguments();

        for (int i = 0; i < typeParams.length; i++) {
            if (!isSameType(arguments.get(i), typeParams[i].getCanonicalName())) {
                return false;
            }
        }

        return true;
    }

    Element asElement(TypeMirror t) {
        return types.asElement(t);
    }

    ClassName classNameOf(TypeElement e) {
        return ClassName.get(e);
    }

    String simpleNameOf(Element e) {
        return e.getSimpleName().toString();
    }

    String packageNameOf(TypeElement e) {
        return ClassName.get(e).packageName();
    }

    TypeName typeNameOf(TypeMirror t) {
        return TypeName.get(t);
    }

    Iterable<AnnotationSpec> asAnnotationSpecs(List<? extends AnnotationMirror> ams) {
        Iterator<AnnotationSpec> it = ams.stream().map(AnnotationSpec::get).iterator();
        return () -> it;
    }

    Iterable<ParameterSpec> asParameterSpecs(List<? extends VariableElement> params) {
        Iterator<ParameterSpec> it = params.stream().map(input -> ParameterSpec
            .builder(TypeName.get(input.asType()), input.getSimpleName().toString())
            .addAnnotations(asAnnotationSpecs(input.getAnnotationMirrors()))
            .build()).iterator();

        return () -> it;
    }

    Set<TypeElement> asTypeElements(Set<String> classNames) {
        return classNames.stream()
            .map(this::getTypeElement)
            .collect(Collectors.toSet());
    }

    Set<String> asClassNames(Supplier<Class<?>[]> supplier) {
        try {
            Class<?>[] classes = supplier.get();
            return Arrays.stream(classes)
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
        } catch (MirroredTypesException e) {
            return e.getTypeMirrors().stream()
                .map(TypeMirror::toString)
                .collect(Collectors.toSet());
        }
    }

    TypeElement asTypeElement(Supplier<Class<?>> supplier) {
        try {
            Class<?> clazz = supplier.get();
            return getTypeElement(clazz.getCanonicalName());
        } catch (MirroredTypeException e) {
            return (TypeElement) asElement(e.getTypeMirror());
        }
    }

    private boolean isSameType(TypeMirror t, String canonical) {
        return isSameType(t, getTypeElement(canonical).asType());
    }

    private boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    private TypeElement getTypeElement(CharSequence name) {
        return elements.getTypeElement(name);
    }
}
