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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepeatedAnnotationVisitor
    implements AnnotationValueVisitor<Set<AnnotationMirror>, Class<? extends Annotation>>  {

    @Override
    public Set<AnnotationMirror> visit(AnnotationValue av, Class<? extends Annotation> p) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visit(AnnotationValue av) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitBoolean(boolean b, Class<? extends Annotation> p) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitByte(byte b, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitChar(char c, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitDouble(double d, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitFloat(float f, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitInt(int i, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitLong(long i, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitShort(short s, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitString(String s, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitType(TypeMirror t, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitEnumConstant(VariableElement c, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<AnnotationMirror> visitAnnotation(AnnotationMirror a, Class<? extends Annotation> aClass) {
        return Collections.singleton(a);
    }

    @Override
    public Set<AnnotationMirror> visitArray(List<? extends AnnotationValue> values, Class<? extends Annotation> p) {
        final Set<AnnotationMirror> accept = new HashSet<>();

        for (AnnotationValue v : values) {
            accept.addAll(v.accept(this, p));
        }

        return accept;
    }

    @Override
    public Set<AnnotationMirror> visitUnknown(AnnotationValue av, Class<? extends Annotation> aClass) {
        return Collections.emptySet();
    }
}
