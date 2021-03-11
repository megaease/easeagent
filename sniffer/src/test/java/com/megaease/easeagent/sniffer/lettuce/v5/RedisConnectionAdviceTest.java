package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import io.lettuce.core.StatefulRedisConnectionImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class RedisConnectionAdviceTest {

    @Test
    public void success() throws Exception {
        Definition.Default def = new GenRedisConnectionAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        Classes.transform("io.lettuce.core.StatefulRedisConnectionImpl")
                .with(def)
                .load(loader).get(0);
        Field field = AgentFieldAccessor.getFieldFromClass(StatefulRedisConnectionImpl.class);
        Assert.assertNotNull(field);
    }
}
