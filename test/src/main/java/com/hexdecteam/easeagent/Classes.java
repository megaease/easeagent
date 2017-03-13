package com.hexdecteam.easeagent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.pool.TypePool;

import java.net.URL;
import java.net.URLClassLoader;

public class Classes {

    public static <T> By<T> transform(Class<? extends T> type) {
        return new By<T>(new ByteBuddy().redefine(type), new TypeDescription.ForLoadedType(type), new URLClassLoader(new URL[0]));
    }

    public static <T> By<T> transform(String name, ClassLoader loader) {
        final ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
        final TypeDescription type = TypePool.Default.of(locator).describe(name).resolve();
        final DynamicType.Builder<? extends T> builder = new ByteBuddy().redefine(type, locator);
        return new By<T>(builder, type, loader);
    }

    public static class By<T> {
        private final DynamicType.Builder<? extends T> builder;
        private final TypeDescription td;
        private final ClassLoader loader;

        By(DynamicType.Builder<? extends T> builder, TypeDescription td, ClassLoader loader) {
            this.builder = builder;
            this.td = td;
            this.loader = loader;
        }

        public Loading<T> by(Transformation.Feature feature) {
            return new Loading<T>(builder, feature, td, loader);
        }
    }

    public static class Loading<T> {

        private final DynamicType.Builder<? extends T> builder;
        private final Transformation.Feature feature;
        private final TypeDescription td;
        private final ClassLoader loader;

        Loading(DynamicType.Builder<? extends T> builder, Transformation.Feature feature, TypeDescription td, ClassLoader loader) {
            this.builder = builder;
            this.feature = feature;
            this.td = td;
            this.loader = loader;
        }

        public Class<T> load() {
            return load(loader);
        }

        @SuppressWarnings("unchecked")
        public Class<T> load(ClassLoader target) {
            return (Class<T>) feature.transformer().transform(builder, td, target, null).make().load(target).getLoaded();
        }
    }


}
