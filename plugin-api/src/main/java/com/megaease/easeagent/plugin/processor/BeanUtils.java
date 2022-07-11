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

import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.function.Supplier;

abstract class BeanUtils {
    static BeanUtils of(final ProcessingEnvironment pe) {
        return new BeanUtils(pe) {
        };
    }

    private final Elements elements;
    private final Types types;

    private BeanUtils(ProcessingEnvironment pe) {
        elements = pe.getElementUtils();
        types = pe.getTypeUtils();
    }

    Element asElement(TypeMirror t) {
        return types.asElement(t);
    }

    ClassName classNameOf(TypeElement e) {
        return ClassName.get(e);
    }

    String packageNameOf(TypeElement e) {
        return ClassName.get(e).packageName();
    }

    TypeElement asTypeElement(Supplier<Class<?>> supplier) {
        try {
            Class<?> clazz = supplier.get();
            return getTypeElement(clazz.getCanonicalName());
        } catch (MirroredTypeException e) {
            return (TypeElement) asElement(e.getTypeMirror());
        }
    }

    boolean isSameType(TypeMirror t, String canonical) {
        return isSameType(t, getTypeElement(canonical).asType());
    }

    boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    boolean isAssignable(TypeElement t1, TypeElement t2) {
        TypeMirror tm1 = t1.asType();
        TypeMirror tm2 = t2.asType();
        return types.isAssignable(tm1, tm2);
    }

    public TypeElement getTypeElement(CharSequence name) {
        return elements.getTypeElement(name);
    }
}
