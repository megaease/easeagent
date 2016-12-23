package com.hexdecteam.easeagent;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.ServiceLoader.load;
import static net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target.BOOTSTRAP;

public class Bootstrap {

    private static final PrivilegedAction<String> GET_TMP_DIR_ACTION = () -> System.getProperty("java.io.tmpdir");

    private static final File TMP_DIR = new File(AccessController.doPrivileged(GET_TMP_DIR_ACTION));

    public static void premain(String args, Instrumentation inst) {
        final ServiceLoader<Transformation> transformations = load(Transformation.class, Bootstrap.class.getClassLoader());
        injectClassesOf(transformations, inst);
        apply(transformations, args, inst);
    }

    private static void apply(Iterable<Transformation> transformations, String args, Instrumentation inst) {
        final Configuration c = Configuration.load(args);
        for (Transformation t : transformations) {
            c.configure(t).apply(inst);
        }
    }

    private static void injectClassesOf(Iterable<Transformation> transformations, Instrumentation inst) {
        Map<TypeDescription, byte[]> map = new HashMap<>();
        for (Transformation t : transformations) {
            final InjectClass ann = t.getClass().getAnnotation(InjectClass.class);
            final Class<?>[] classes = ann.value();
            for (Class<?> c : classes) {
                map.put(desc(c), bytesOf(c));
            }
        }
        if (map.isEmpty()) return;
        ClassInjector.UsingInstrumentation.of(TMP_DIR, BOOTSTRAP, inst).inject(map);
    }

    private static TypeDescription desc(Class<?> type) {
        return new TypeDescription.ForLoadedType(type);
    }

    private static byte[] bytesOf(Class<?> type) {
        try {
            final ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(type.getClassLoader());
            return locator.locate(type.getName()).resolve();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
