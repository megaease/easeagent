package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.Transformation;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
                                          .addParameter(String.class, "args")
                                          .addParameter(Instrumentation.class, "inst")
                                          .addException(Exception.class)
                                          .returns(TypeName.VOID)
                                          .addStatement("final $T<Class<?>> providers = $T.<Class<?>>asList($L)",
                                                        Iterable.class, Arrays.class, providerClasses)
                                          .addStatement("@SuppressWarnings(\"unchecked\") final $T<Class<? extends $T>> transformations = $T.<Class<? extends $T>>asList($L)",
                                                        Iterable.class, Transformation.class, Arrays.class, Transformation.class, transformationClasses)
                                          .addStatement("$T.start(args, inst, providers, transformations)", Bootstrap.class)
                                          .build();

        final TypeSpec spec = TypeSpec.classBuilder("StartBootstrap")
                                      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                      .addMethod(main).build();

        return JavaFile.builder(packageName, spec).build();

    }
}
