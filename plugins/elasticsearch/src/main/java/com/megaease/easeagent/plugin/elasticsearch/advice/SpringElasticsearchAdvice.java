package com.megaease.easeagent.plugin.elasticsearch.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.hasSuperType;
import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class SpringElasticsearchAdvice implements Points {
    //return def.type(
    //                hasSuperType(named("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
    //                    .or(hasSuperType(named("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties")))
    //                    .or(named("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
    //                    .or(named("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"))
    //            )
    //            .transform(setProperty(nameStartsWith("set")))
    //            .end();
    @Override
    public IClassMatcher getClassMatcher() {
        return name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties")
            .or(hasSuperType("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"))
            .or(name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
            .or(name("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"));
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
