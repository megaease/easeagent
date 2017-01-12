package com.hexdecteam.easeagent;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation;
import net.bytebuddy.pool.TypePool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Collections.list;
import static net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target.BOOTSTRAP;

/**
 * Inject classes annotated with {@link com.google.auto.service.AutoService} to bootstrap class loader,
 * like:
 * <pre><code>
 * {@literal @AutoService(AppendBootstrapClassLoaderSearch.class)}
 *  public class Foo {}
 * </code></pre>
 *
 * @see EventBus
 */
final class AppendBootstrapClassLoaderSearch {
    private static final File TMP_FILE = new File(AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                @Override
                public String run() {return System.getProperty("java.io.tmpdir");}
            })
    );

    static Set<String> by(Instrumentation inst) throws IOException {
        final Set<String> names = findClassAnnotatedAutoService(AppendBootstrapClassLoaderSearch.class);
        UsingInstrumentation.of(TMP_FILE, BOOTSTRAP, inst).inject(types(names));
        return names;
    }

    private static Map<TypeDescription, byte[]> types(Set<String> names) {
        final ClassLoader loader = AppendBootstrapClassLoaderSearch.class.getClassLoader();
        final ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
        final TypePool pool = TypePool.Default.of(locator);

        Function<String, TypeDescription> asTDesc = new Function<String, TypeDescription>() {
            @Override
            public TypeDescription apply(String input) {
                return pool.describe(input).resolve();
            }
        };

        Function<String, byte[]> asBytes = new Function<String, byte[]>() {
            @Override
            public byte[] apply(String input) {
                try {
                    return locator.locate(input).resolve();
                } catch (IOException e) {
                    throw new MayBeABug(e);
                }
            }
        };

        return Maps.transformValues(uniqueIndex(names, asTDesc), asBytes);
    }

    private static Set<String> findClassAnnotatedAutoService(Class<?> cls) throws IOException {
        final ClassLoader loader = cls.getClassLoader();
        return from(list(loader.getResources("META-INF/services/" + cls.getName())))
                .transform(new Function<URL, InputStreamReader>() {
                    @Override
                    public InputStreamReader apply(URL input) {
                        try {
                            final URLConnection connection = input.openConnection();
                            final InputStream stream = connection.getInputStream();
                            return new InputStreamReader(stream, Charsets.UTF_8);
                        } catch (IOException e) {
                            throw new MayBeABug(e);
                        }
                    }
                })
                .transformAndConcat(new Function<InputStreamReader, Iterable<String>>() {
                    @Override
                    public Iterable<String> apply(InputStreamReader input) {
                        try {
                            return CharStreams.readLines(input);
                        } catch (IOException e) {
                            throw new MayBeABug(e);
                        } finally {
                            Closeables.closeQuietly(input);
                        }

                    }
                })
                .toSet();
    }

    private AppendBootstrapClassLoaderSearch() { }
}
