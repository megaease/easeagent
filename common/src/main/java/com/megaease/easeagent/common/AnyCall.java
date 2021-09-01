/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.megaease.easeagent.common;

import com.google.common.base.Function;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class AnyCall implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        final Iterable<Junction<TypeDescription>> includes = transform(include_class_prefix_list(), NAME_STARTS_WITH);
        final Iterable<Junction<TypeDescription>> excludes = transform(exclude_class_prefix_list(), NAME_STARTS_WITH);
        return def.type(anyOf(includes).and(not(anyOf(excludes))).and(not(nameContains("CGLIB$$"))))
                  .transform(method(not(isTypeInitializer().or(isSetter())
                                                           .or(isGetter())
                                                           .or(isConstructor())
                                                           .or(isClone())
                                                           .or(isEquals())
                                                           .or(isHashCode())
                                                           .or(isToString())
                                                           .or(ElementMatchers.<MethodDescription>isSynthetic())
                                                           .or(ElementMatchers.<MethodDescription>isBridge())
                                                           .or(ElementMatchers.<MethodDescription>isAbstract())
                                                           .or(ElementMatchers.<MethodDescription>isNative())
                                                           .or(ElementMatchers.<MethodDescription>isStrict()))))
                  .end();

    }

    protected abstract Definition.Transformer method(ElementMatcher<? super MethodDescription> matcher);

    protected abstract List<String> include_class_prefix_list();

    protected abstract List<String> exclude_class_prefix_list();

    private static final Function<String, Junction<TypeDescription>> NAME_STARTS_WITH =
        ElementMatchers::nameStartsWith;

    private static <T> Junction<T> anyOf(Iterable<Junction<T>> matchers) {
        Junction<T> fold = none();
        for (Junction<T> matcher : matchers) {
            fold = fold.or(matcher);
        }
        return fold;
    }

}
