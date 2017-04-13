package com.megaease.easeagent.common;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Method;

public abstract class Descriptions {
    public static TypeDescription type(Class<?> aClass) {
        return new TypeDescription.ForLoadedType(aClass);
    }

    public static MethodDescription method(Method method) {
        return new MethodDescription.ForLoadedMethod(method);
    }

    private Descriptions() { }
}
