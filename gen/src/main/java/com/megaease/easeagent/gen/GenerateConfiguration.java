package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Configurable.Item;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.typesafe.config.Config;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.overriding;
import static javax.lang.model.element.Modifier.*;

class GenerateConfiguration extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {


    GenerateConfiguration(TypeSpec.Builder builder) {
        super(builder);
    }

    @Override
    public TypeSpec.Builder visitTypeAsClass(TypeElement e, final ProcessUtils utils) {
        if (e.getAnnotation(Configurable.class) == null) return super.visitTypeAsClass(e, utils);

        final TypeSpec.Builder builder = super.visitTypeAsClass(e, utils);

        for (Element member : e.getEnclosedElements()) {
            member.accept(new GenerateMethods(builder), utils);
        }

        return builder.addField(Config.class, "conf", FINAL)
                      .addMethod(constructor(Config.class));
    }

    private static String methodNameOf(TypeMirror type, ProcessUtils utils) {
        switch (type.getKind()) {
            case BOOLEAN:
                return "Boolean";
            case INT:
                return "Int";
            case DOUBLE:
                return "Double";
            case LONG:
                return "Long";
            case DECLARED:
                if (utils.isSameType(type, String.class)) return "String";
                if (utils.isSameType(type, List.class, Boolean.class)) return "BooleanList";
                if (utils.isSameType(type, List.class, Integer.class)) return "IntList";
                if (utils.isSameType(type, List.class, Long.class)) return "LongList";
                if (utils.isSameType(type, List.class, Double.class)) return "DoubleList";
                if (utils.isSameType(type, List.class, String.class)) return "StringList";
            default:
                return null;

        }

    }

    private static MethodSpec constructor(Class<?> c) {
        return constructorBuilder().addParameter(c, "conf").addModifiers(PUBLIC).addStatement("this.conf = conf").build();
    }

    private static class GenerateMethods extends ElementKindVisitor6<TypeSpec.Builder, ProcessUtils> {
        GenerateMethods(TypeSpec.Builder builder) { super(builder); }

        @Override
        public TypeSpec.Builder visitExecutableAsMethod(ExecutableElement e, ProcessUtils utils) {
            if (e.getAnnotation(Item.class) == null) return super.visitExecutableAsMethod(e, utils);

            final String method = methodNameOf(e.getReturnType(), utils);

            if (method == null)
                throw new ElementException(e, "should be one type or list of those [boolean|int|long|double|String]");

            final String name = utils.simpleNameOf(e);

            final String format;
            final Object[] args;
            if (e.getModifiers().contains(ABSTRACT)) {
                format = "return conf.get$L($S)";
                args = new Object[]{method, name};
            } else {
                format = "return conf.hasPath($S) ? conf.get$L($S) : super.$L()";
                args = new Object[]{name, method, name, name};
            }
            return super.visitExecutableAsMethod(e, utils).addMethod(overriding(e).addStatement(format, args).build());
        }
    }
}
