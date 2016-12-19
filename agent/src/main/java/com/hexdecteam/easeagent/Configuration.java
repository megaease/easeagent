package com.hexdecteam.easeagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public abstract class Configuration {

    private static final Logger LOGGER  = LoggerFactory.getLogger(Configuration.class);
    public static final  String DEFAULT = "easeagent.yml";

    @SuppressWarnings("unchecked")
    public static Configuration load(String args) {
        final Yaml yaml = new Yaml();
        final Map<String, Object> map = (Map<String, Object>) yaml.load(inputStream(args));
        return new Configuration() {
            @Override
            public <T> Optional<T> configure(T bean) {
                final Configurable ann = bean.getClass().getAnnotation(Configurable.class);
                if (ann == null) {
                    LOGGER.error("{} should annotate with {}", bean.getClass(), Configurable.class);
                    return Optional.empty();
                }
                return Optional.of(bind(bean, (Map<String, Object>) map.get(ann.prefix())));
            }
        };
    }

    private static <T> T bind(T bean, Map<String, Object> map) {
        map.forEach((k, v) -> {
            final Class<?> c = bean.getClass();
            try {
                final Field field = c.getDeclaredField(k);
                field.setAccessible(true);
                field.set(bean, v);
            } catch (NoSuchFieldException e) {
                LOGGER.warn("Skip setting unknown field {} of {}", k, c);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Skip setting field {} of {} cause by illegal access", k, c);
            }
        });
        return bean;
    }

    private static InputStream inputStream(String args) {
        String name = args == null ? DEFAULT : args;
        try {
            return new URL(name).openConnection().getInputStream();
        } catch (MalformedURLException e) {
            return inputStreamFromFile(name);
        } catch (IOException e) {
            LOGGER.warn("Fallback to default configuration", e);
            return inputStreamFromFile(DEFAULT);
        }
    }

    private static InputStream inputStreamFromFile(String name) {
        final File file = new File(name);
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return inputStreamFromResource(name);
        }
    }

    private static InputStream inputStreamFromResource(String name) {
        return Configuration.class.getClassLoader().getResourceAsStream(name);
    }

    private Configuration() {}

    public abstract <T> Optional<T> configure(T bean);
}
