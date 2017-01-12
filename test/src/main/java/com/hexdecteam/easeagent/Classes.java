package com.hexdecteam.easeagent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;

import java.net.URL;
import java.net.URLClassLoader;

public class Classes {

    public static Class<?> transform(Class<?> type,
                                     Transformation.Feature feature,
                                     ClassLoader loader,
                                     TypeDescription td) {
        return feature.transformer().transform(new ByteBuddy().redefine(type), td, null)
                      .make().load(loader).getLoaded();
    }

    public static By transform(Class<?> type) {
        return new By(type);
    }

    public static class By {
        private final Class<?> type;

        By(Class<?> type) {
            this.type = type;
        }

        public With by(Transformation.Feature feature) {
            return new With(type, feature);
        }
    }

    public static class With {

        private final Class<?>               type;
        private final Transformation.Feature feature;

        With(Class<?> type, Transformation.Feature feature) {
            this.type = type;
            this.feature = feature;
        }

        Loading with(ClassLoader loader) {
            return new Loading(type, feature, null, loader);
        }

        Loading with(TypeDescription td) {
            return new Loading(type, feature, td, null);
        }

        Class<?> load() {
            return new Loading(type, feature, new TypeDescription.ForLoadedType(type), null).load();
        }
        Class<?> load(ClassLoader target) {
            return new Loading(type, feature, new TypeDescription.ForLoadedType(type), null).load(target);
        }
    }

    public static class Loading {

        private final Class<?>               type;
        private final Transformation.Feature feature;
        private final TypeDescription        td;
        private final ClassLoader            loader;

        Loading(Class<?> type, Transformation.Feature feature, TypeDescription td, ClassLoader loader) {
            this.type = type;
            this.feature = feature;
            this.td = td;
            this.loader = loader;
        }

        public Class<?> load(){
            return load(new URLClassLoader(new URL[0]));
        }

        public Class<?> load(ClassLoader target) {
            return feature.transformer().transform(new ByteBuddy().redefine(type), td, loader)
                                     .make().load(target).getLoaded();
        }
    }


}
