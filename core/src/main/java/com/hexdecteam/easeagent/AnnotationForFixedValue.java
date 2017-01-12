package com.hexdecteam.easeagent;

import net.bytebuddy.asm.Advice.DynamicValue.ForFixedValue;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.annotation.Annotation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public abstract class AnnotationForFixedValue<T extends Annotation> extends ForFixedValue<T> {
    protected final Class<? extends Annotation> type;

    public AnnotationForFixedValue(Class<? extends Annotation> type) {this.type = type;}

    protected <V> V property(AnnotationDescription.Loadable<? extends Annotation> ad, String name, Class<V> vClass) {
        return ad.getValue(ad.getAnnotationType()
                             .getDeclaredMethods()
                             .filter(named(name))
                             .getOnly()).resolve(vClass);
    }

    protected Object doResolve(TypeDescription it, MethodDescription im,
                               ParameterDescription.InDefinedShape target,
                               AnnotationDescription.Loadable<T> annotation,
                               Assigner assigner, boolean initialized) {
        return doResolve(it, im, im.getDeclaredAnnotations().ofType(type));
    }

    protected abstract Object doResolve(TypeDescription it, MethodDescription im,
                                        AnnotationDescription.Loadable<? extends Annotation> ad);
}
