package com.megaease.easeagent.plugin.redis.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class RedisPropertiesClusterAdvice implements Points {

    //return def
    //            .type(named("org.springframework.boot.autoconfigure.data.redis.RedisProperties$Cluster"))
    //            .transform(setNodes(named("setNodes")))
    //            .end()
    //            ;
    @Override
    public IClassMatcher getClassMatcher() {
        return name("org.springframework.boot.autoconfigure.data.redis.RedisProperties$Cluster");
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(
                MethodMatcher
                    .builder()
                    .named("setNodes")
                    .build()
            )
            .build();
    }
}
