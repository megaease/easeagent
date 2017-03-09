package com.hexdecteam.easeagent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.pool.TypePool;

import java.net.URL;
import java.net.URLClassLoader;

public class Classes {

    public static By transform(Class<?> type) {
        return new By(new ByteBuddy().redefine(type), new TypeDescription.ForLoadedType(type), new URLClassLoader(new URL[0]));
    }

    public static By transform(String name, ClassLoader loader) {
        final TypeDescription type = TypePool.Default.of(loader).describe(name).resolve();
        return new By(new ByteBuddy().redefine(type, ClassFileLocator.ForClassLoader.of(loader)), type, loader);
    }

    public static class By {
        private final DynamicType.Builder<?> builder;
        private final TypeDescription td;
        private final ClassLoader loader;

        By(DynamicType.Builder<?> builder, TypeDescription td, ClassLoader loader) {
            this.builder = builder;
            this.td = td;
            this.loader = loader;
        }

        public Loading by(Transformation.Feature feature) {
            return new Loading(builder, feature, td, loader);
        }
    }

    public static class Loading {

        private final DynamicType.Builder<?> builder;
        private final Transformation.Feature feature;
        private final TypeDescription td;
        private final ClassLoader loader;

        Loading(DynamicType.Builder<?> builder, Transformation.Feature feature, TypeDescription td, ClassLoader loader) {
            this.builder = builder;
            this.feature = feature;
            this.td = td;
            this.loader = loader;
        }

        public Class<?> load() {
            return load(loader);
        }

        public Class<?> load(ClassLoader target) {
            return feature.transformer().transform(builder, td, target, null).make().load(target).getLoaded();
        }
    }


}
