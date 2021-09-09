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
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

class GenerateFunction implements Function<TypeElement, JavaFile> {

    private final ProcessUtils utils;
    private final Iterable<? extends AssemblyProcessor.GenerateSpecFactory> factories;

    GenerateFunction(ProcessUtils utils, Iterable<? extends AssemblyProcessor.GenerateSpecFactory> factories) {
        this.utils = utils;
        this.factories = factories;
    }

    @Override
    public JavaFile apply(TypeElement e) {
        assert e != null;
        if (!e.getModifiers().contains(ABSTRACT)) {
            throw new ElementException(e, "should be abstract");
        }
        if (e.getNestingKind() == NestingKind.MEMBER) {
            throw new ElementException(e, "should not be nest");
        }

        final String packageName = utils.packageNameOf(e);
        final String simpleName = Generate.PREFIX + utils.simpleNameOf(e);
        final TypeSpec.Builder builder = classBuilder(simpleName)
                .addAnnotations(utils.asAnnotationSpecs(e.getAnnotationMirrors()))
                .addModifiers(PUBLIC).superclass(utils.typeNameOf(e.asType()));

        for (AssemblyProcessor.GenerateSpecFactory factory : factories)
            e.accept(factory.create(builder, packageName + '.' + simpleName), utils);

        return JavaFile.builder(packageName, builder.build()).build();
    }
}
