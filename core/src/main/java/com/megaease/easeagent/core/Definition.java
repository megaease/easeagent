package com.megaease.easeagent.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.Map;

public interface Definition<T extends Definition> {
    Transforming<T> type(ElementMatcher<? super TypeDescription> matcher);

    interface Transforming<T extends Definition> {
        Fork<T> transform(Transformer t);
    }

    interface Fork<T extends Definition> extends Transforming<T>, Definition<T> {
        T end();
    }

    final class Transformer {
        public final String inlineAdviceClassName;
        public final String adviceFactoryClassName;
        public final ElementMatcher<? super MethodDescription> matcher;

        public Transformer(String inlineAdviceClassName, String adviceFactoryClassName, ElementMatcher<? super MethodDescription> matcher) {
            this.inlineAdviceClassName = inlineAdviceClassName;
            this.adviceFactoryClassName = adviceFactoryClassName;
            this.matcher = matcher;
        }
    }

    class Default implements Definition<Default>, Fork<Default> {

        public static final Default EMPTY = new Default(null, null, null);

        private final ElementMatcher<? super TypeDescription> matcher;
        private final Iterable<Transformer> transformers;
        private final Default next;

        private Default(ElementMatcher<? super TypeDescription> matcher,
                        Iterable<Transformer> transformers,
                        Default aDefault) {
            this.matcher = matcher;
            this.transformers = transformers;
            next = aDefault;
        }

        @Override
        public Transforming<Default> type(ElementMatcher<? super TypeDescription> matcher) {
            return new Default(matcher, Collections.<Transformer>emptySet(), this);
        }


        @Override
        public Fork<Default> transform(Transformer t) {
            return new Default(matcher, Iterables.concat(transformers, Collections.singleton(t)), next);
        }

        @Override
        public Default end() {
            return this;
        }

        public Map<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> asMap() {
            final ImmutableMap.Builder<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> builder = ImmutableMap.builder();

            for (Default fab = this; fab != EMPTY; fab = fab.next) {
                builder.put(fab.matcher, fab.transformers);
            }

            return builder.build();
        }

    }

}
