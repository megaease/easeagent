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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.transform;

abstract class ProcessUtils {

    static ProcessUtils of(final ProcessingEnvironment pe) {
        return new ProcessUtils(pe) {};
    }

    private final Elements elements;
    private final Types types;

    private ProcessUtils(ProcessingEnvironment pe) {
        elements = pe.getElementUtils();
        types = pe.getTypeUtils();
    }

    boolean isSameType(TypeMirror t, Class<?> c, Class<?>... typeParams) {
        if(typeParams.length == 0) return isSameType(t, c.getCanonicalName());

        if (!isSameType(asElement(t).asType(), c.getCanonicalName())) return false;

        final List<? extends TypeMirror> arguments = ((DeclaredType) t).getTypeArguments();

        for (int i = 0; i < typeParams.length; i++) {
            if (!isSameType(arguments.get(i), typeParams[i].getCanonicalName())) return false;
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
        return transform(ams, ANNOTATION_MIRROR_TO_SPEC);
    }

    Iterable<ParameterSpec> asParameterSpecs(List<? extends VariableElement> params) {
        return transform(params, PARAMETER_TO_SPEC);
    }

    FluentIterable<TypeElement> asTypeElements(Supplier<Class<?>[]> supplier) {
        try {
            return from(supplier.get()).transform(new Function<Class<?>, TypeElement>() {
                @Override
                public TypeElement apply(Class<?> input) {
                    return getTypeElement(input.getCanonicalName());
                }
            });
        } catch (MirroredTypesException e) {
            return from(e.getTypeMirrors()).transform(new Function<TypeMirror, TypeElement>() {
                @Override
                public TypeElement apply(TypeMirror input) {
                    return (TypeElement) asElement(input);
                }
            });
        }
    }

    TypeElement asTypeElement(Supplier<Class<?>> supplier) {
        try {
            return getTypeElement(supplier.get().getCanonicalName());
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


    private static final Function<AnnotationMirror, AnnotationSpec> ANNOTATION_MIRROR_TO_SPEC = new Function<AnnotationMirror, AnnotationSpec>() {
        @Override
        public AnnotationSpec apply(AnnotationMirror input) {
            return AnnotationSpec.get(input);
        }
    };

    private static final Function<VariableElement, ParameterSpec> PARAMETER_TO_SPEC = new Function<VariableElement, ParameterSpec>() {
        @Override
        public ParameterSpec apply(VariableElement input) {
            return ParameterSpec.builder(TypeName.get(input.asType()), input.getSimpleName().toString())
                                .addModifiers(Modifier.FINAL)
                                .addAnnotations(transform(input.getAnnotationMirrors(), ANNOTATION_MIRROR_TO_SPEC))
                                .build();

        }
    };
}
