package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import io.lettuce.core.resource.DefaultClientResources;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

public class RedisConnectionAdviceTest {

    @Test
    public void success() throws Exception {
        Definition.Default def = new GenLettuceInjectAgentFieldAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        List<Class<?>> classList = Classes.transform(this.getClass().getName() + "$MyClientResources")
                .with(def)
                .load(loader);

        Field field = AgentDynamicFieldAccessor.getDynamicFieldFromClass(classList.get(0));
        Assert.assertNotNull(field);

    }

    static class MyClientResources extends DefaultClientResources {

        protected MyClientResources(Builder builder) {
            super(builder);
        }
    }
}
