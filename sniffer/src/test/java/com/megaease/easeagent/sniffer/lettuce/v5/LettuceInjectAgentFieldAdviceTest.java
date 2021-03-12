package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import io.lettuce.core.resource.DefaultClientResources;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class LettuceInjectAgentFieldAdviceTest {

    @Test
    public void success() throws Exception {
        Definition.Default def = new GenLettuceInjectAgentFieldAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        Class cls = Classes.transform(this.getClass().getName() + "$MyClientResources")
                .with(def)
                .load(loader).get(0);
        Field field = AgentDynamicFieldAccessor.getDynamicFieldFromClass(cls);
        Assert.assertNotNull(field);
    }

    static class MyClientResources extends DefaultClientResources {

        protected MyClientResources(Builder builder) {
            super(builder);
        }
    }
}
