package com.hexdecteam.easeagent;

import com.google.common.base.Function;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * A basic {@link Plugin} for transformation of class byte code, like:
 * <pre><code>
 * {@literal @AutoService(Plugin.class)}
 *  public class Foo extends{@literal Transformation<Foo.Configuration>} {
 *    protected Features feature(Configuration conf) { ... }
 *
 *    static abstract class Configuration { ... }
 *  }
 * </code></pre>
 *
 * @see Plugin
 * @see ConfigurationDecorator
 */
public abstract class Transformation<Configuration> implements Plugin<Configuration> {
    private static final Logger                LOGGER   = LoggerFactory.getLogger(Transformation.class);
    private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {

        @Override
        public void onTransformation(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded, DynamicType dt) {
            LOGGER.debug("Transform {} from {}", td, ld);
        }

        @Override
        public void onIgnored(TypeDescription td, ClassLoader ld, JavaModule m, boolean loaded) {
            LOGGER.trace("Ignored {} from {}", td, ld);
        }

        @Override
        public void onError(String name, ClassLoader ld, JavaModule m, boolean loaded, Throwable error) {
            LOGGER.error(name, error);
        }

        @Override
        public void onComplete(String name, ClassLoader ld, JavaModule m, boolean loaded) {
            LOGGER.trace("Complete {} from {}", name, ld);
        }
    };

    @Override
    public final void hook(Configuration conf, Instrumentation inst, Subscription subs) {
        final Feature feature = feature(conf);
        if (feature.transformer() == Transformer.NoOp.INSTANCE) return;
        new AgentBuilder.Default()
                .with(LISTENER)
                .ignore(any(), isBootstrapClassLoader().or(is(selfClassLoader())))
                .or(isInterface())
                .or(isSynthetic())
                .or(nameStartsWith("sun.reflect."))
                .type(feature.type()).transform(feature.transformer())
                .installOn(inst);
    }

    protected abstract Feature feature(Configuration conf);

    private ClassLoader selfClassLoader() {
        return getClass().getClassLoader();
    }

    public interface Feature {
        Junction<TypeDescription> type();

        Transformer transformer();

        Feature NO_OP = new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                return none();
            }

            @Override
            public Transformer transformer() {
                return Transformer.NoOp.INSTANCE;
            }
        };

        class Compound implements Feature {

            private final Junction<TypeDescription> type;
            private final Transformer.Compound      transformer;

            public Compound(List<? extends Feature> features) {
                type = ReduceF.reduce(transform(features, new Function<Feature, Junction<TypeDescription>>() {
                    @Override
                    public Junction<TypeDescription> apply(Feature input) {
                        return input.type();
                    }
                }).iterator(), new ReduceF.BiFunction<Junction<TypeDescription>>() {
                    @Override
                    public Junction<TypeDescription> apply(Junction<TypeDescription> l, Junction<TypeDescription> r) {
                        return l.or(r);
                    }
                });

                transformer = new Transformer.Compound(transform(features, new Function<Feature, Transformer>() {
                    @Override
                    public Transformer apply(Feature input) {
                        return input.transformer();
                    }
                }));

            }

            @Override
            public Junction<TypeDescription> type() {
                return type;
            }

            @Override
            public Transformer transformer() {
                return transformer;
            }
        }
    }

}
