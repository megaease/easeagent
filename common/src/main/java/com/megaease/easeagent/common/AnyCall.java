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
            new Function<String, Junction<TypeDescription>>() {
        @Override
        public Junction<TypeDescription> apply(String input) {
            return nameStartsWith(input);
        }
    };

    private static <T> Junction<T> anyOf(Iterable<Junction<T>> matchers) {
        Junction<T> fold = none();
        for (Junction<T> matcher : matchers) {
            fold = fold.or(matcher);
        }
        return fold;
    }

}
