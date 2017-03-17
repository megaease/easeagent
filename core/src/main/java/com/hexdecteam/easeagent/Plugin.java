package com.hexdecteam.easeagent;

import java.lang.instrument.Instrumentation;

/**
 * A implementation of {@link Plugin} annotated with {@link com.google.auto.service.AutoService} would be loaded for extension, like:
 *
 * <pre><code>
 * {@literal @AutoService(Plugin.class)}
 *  public class Foo implements{@literal Plugin<Foo.Configuration>} {
 *    public void hook(Configuration conf, Instrumentation inst, Subscription subs){ ... }
 *
 *    static abstract class Configuration {
 *      String optional() {return "default";}
 *      abstract String required();
 *    }
 *  }
 * </code></pre>
 *
 * @param <Configuration> is a abstract class has methods that could be bound to config item or a interface without method.
 * @see ConfigurationDecorator
 */
public interface Plugin<Configuration> {
    void hook(Configuration conf, Instrumentation inst, Subscription subs);

    interface Noop {}
}
