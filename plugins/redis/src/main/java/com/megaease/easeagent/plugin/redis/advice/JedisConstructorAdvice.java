package com.megaease.easeagent.plugin.redis.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;
import static com.megaease.easeagent.plugin.tools.matcher.MethodMatcherUtils.constructor;

public class JedisConstructorAdvice implements Points {

    @Override
    public IClassMatcher getClassMatcher() {
        return name("redis.clients.jedis.Jedis");

    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(constructor())
            .build();
    }
}
