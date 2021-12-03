package com.megaease.easeagent.plugin.redis.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class RedisPropertiesAdvice implements Points {
    //return def
    //            .type(named("org.springframework.boot.autoconfigure.data.redis.RedisProperties"))
    //            .transform(setProperty(nameStartsWith("set")))
    //            .end()
    //            ;
    @Override
    public IClassMatcher getClassMatcher() {
        return name("org.springframework.boot.autoconfigure.data.redis.RedisProperties");
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(
                MethodMatcher
                    .builder()
                    .nameStartWith("set")
                    .build()
            )
            .build();
    }
}
