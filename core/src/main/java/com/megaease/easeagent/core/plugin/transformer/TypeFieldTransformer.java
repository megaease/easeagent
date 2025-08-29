package com.megaease.easeagent.core.plugin.transformer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.field.TypeFieldGetter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.utility.JavaModule;

import java.util.concurrent.ConcurrentHashMap;

public class TypeFieldTransformer implements AgentBuilder.Transformer {
    private static final Logger log = LoggerFactory.getLogger(TypeFieldTransformer.class);

    private static final ConcurrentHashMap<String, Cache<ClassLoader, Boolean>> FIELD_MAP = new ConcurrentHashMap<>();
    private final String fieldName;
    private final Class<?> accessor;
    private final AgentBuilder.Transformer.ForAdvice transformer;
    public TypeFieldTransformer(String fieldName) {
        this.fieldName = fieldName;
        this.accessor = TypeFieldGetter.class;
        this.transformer = new AgentBuilder.Transformer
            .ForAdvice(Advice.withCustomMapping())
            .include(getClass().getClassLoader());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                            TypeDescription td, ClassLoader cl, JavaModule m) {
        if (check(td, this.accessor, cl) && this.fieldName != null) {
            try {
                b = b.implement(this.accessor)
                    .intercept(FieldAccessor.ofField(this.fieldName));
            } catch (Exception e) {
                log.debug("Type:{} add extend field again!", td.getName());
            }
            return transformer.transform(b, td, cl, m);
        }
        return b;
    }

    /**
     * Avoiding add a accessor interface to a class repeatedly
     *
     * @param td       represent the class to be enhanced
     * @param accessor access interface class
     * @param cl       current classloader
     * @return return true when it is the first time
     */
    private static boolean check(TypeDescription td, Class<?> accessor, ClassLoader cl) {
        String key = td.getCanonicalName() + accessor.getCanonicalName();

        Cache<ClassLoader, Boolean> checkCache = FIELD_MAP.get(key);
        if (checkCache == null) {
            Cache<ClassLoader, Boolean> cache = CacheBuilder.newBuilder().weakKeys().build();
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            cache.put(cl, true);
            checkCache = FIELD_MAP.putIfAbsent(key, cache);
            if (checkCache == null) {
                return true;
            }
        }

        return checkCache.getIfPresent(cl) == null;
    }
}
