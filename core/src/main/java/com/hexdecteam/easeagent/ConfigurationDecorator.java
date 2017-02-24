package com.hexdecteam.easeagent;

import com.typesafe.config.Config;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.typesafe.config.ConfigFactory.load;
import static com.typesafe.config.ConfigFactory.parseFile;
import static com.typesafe.config.ConfigRenderOptions.defaults;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

class ConfigurationDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDecorator.class);

    private final Config config;

    ConfigurationDecorator(Config config) {
        this.config = config;
    }

    static ConfigurationDecorator configurationDecorator(String args) {
        final Config config = isNullOrEmpty(args) ? load(ConfigurationDecorator.class.getClassLoader())
                                                  : parseFile(new File(args)).resolve();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(config.root().render(defaults().setOriginComments(false).setJson(false)));

        return new ConfigurationDecorator(config);
    }

    static Class<?> configurationClassDeclaredBy(Class<?> aClass) {
        final Type superclass = aClass.getGenericSuperclass();

        if (superclass != null && superclass instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) superclass;
            if (pt.getRawType() == Transformation.class) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
        }

        final Type[] interfaces = aClass.getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType() == Plugin.class) {
                    return (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
        }
        throw new MayBeABug(aClass + " implements Plugin without generic type.");
    }

    <T> T newInstance(Class<T> t) {
        try {
            return new ByteBuddy().subclass(t)
                                  .method(isDeclaredBy(t)).intercept(to(new Interceptor()))
                                  .make().load(t.getClassLoader())
                                  .getLoaded().newInstance();
        } catch (Exception e) {
            throw new MayBeABug(e);
        }
    }

    class Interceptor {

        @RuntimeType
        public Object get(@Origin Method method, @SuperCall(nullIfImpossible = true) Callable<Object> defaultValue)
                throws Exception {
            final String prefix = method.getDeclaringClass().getAnnotation(Binding.class).value();
            final Binding ann = method.getAnnotation(Binding.class);
            final String key = prefix + '.' + (ann == null ? method.getName() : ann.value());

            if (!config.hasPath(key) && defaultValue != null) {
                return defaultValue.call();
            }

            final Class<?> type = method.getReturnType();

            if (type == List.class) {
                final ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
                return list(genericReturnType.getActualTypeArguments()[0], key);
            } else return value(type, key);
        }

        private Object value(Class<?> type, String key) {
            if (type == boolean.class || type == Boolean.class) return config.getBoolean(key);
            if (type == int.class || type == Integer.class) return config.getInt(key);
            if (type == long.class || type == Long.class) return config.getLong(key);
            if (type == double.class || type == Double.class) return config.getDouble(key);
            if (type == String.class) return config.getString(key);
            throw new MayBeABug("Unsupported return type: " + type);
        }

        private Object list(Type type, String key) {
            if (type == boolean.class || type == Boolean.class) return config.getBooleanList(key);
            if (type == int.class || type == Integer.class) return config.getIntList(key);
            if (type == long.class || type == Long.class) return config.getLongList(key);
            if (type == double.class || type == Double.class) return config.getDoubleList(key);
            if (type == String.class) return config.getStringList(key);
            throw new MayBeABug("Unsupported return list type: " + type);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Binding {
        String value();
    }

}
