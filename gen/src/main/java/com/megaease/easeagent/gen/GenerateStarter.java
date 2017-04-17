package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.Transformation;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

class GenerateStarter {
    private final String packageName;
    private final String providerClasses;
    private final String transformationClasses;

    GenerateStarter(String packageName, String providerClasses, String transformationClasses) {
        this.packageName = packageName;
        this.providerClasses = providerClasses;
        this.transformationClasses = transformationClasses;
    }

    JavaFile apply() {
        final MethodSpec main = MethodSpec.methodBuilder("premain")
                                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                          .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
                                          .addParameter(String.class, "args")
                                          .addParameter(Instrumentation.class, "inst")
                                          .addException(Exception.class)
                                          .returns(TypeName.VOID)
                                          .addStatement("final $T<Class<?>> providers = $T.<Class<?>>asList(\n$L\n)",
                                                        Iterable.class, Arrays.class, providerClasses)
                                          .addStatement("final $T<Class<? extends $T>> transformations = $T.<Class<? extends $T>>asList(\n$L\n)",
                                                        Iterable.class, Transformation.class, Arrays.class, Transformation.class, transformationClasses)
                                          .addStatement("$T.start(args, inst, providers, transformations)", Bootstrap.class)
                                          .build();

        final TypeSpec spec = TypeSpec.classBuilder("StartBootstrap")
                                      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                      .addMethod(main).build();

        return JavaFile.builder(packageName, spec).build();

    }
}
