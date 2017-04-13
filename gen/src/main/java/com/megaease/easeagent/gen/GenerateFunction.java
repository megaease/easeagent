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
        if (!e.getModifiers().contains(ABSTRACT)) throw new ElementException(e, "should be abstract");

        if (e.getNestingKind() == NestingKind.MEMBER) throw new ElementException(e, "should not be nest");

        final String packageName = utils.packageNameOf(e);
        final String simpleName = Generated.PREFIX + utils.simpleNameOf(e);
        final TypeSpec.Builder builder = classBuilder(simpleName)
                .addAnnotations(utils.asAnnotationSpecs(e.getAnnotationMirrors()))
                .addModifiers(PUBLIC).superclass(utils.typeNameOf(e.asType()));

        for (AssemblyProcessor.GenerateSpecFactory factory : factories)
            e.accept(factory.create(builder, packageName + '.' + simpleName), utils);

        return JavaFile.builder(packageName, builder.build()).build();
    }
}
