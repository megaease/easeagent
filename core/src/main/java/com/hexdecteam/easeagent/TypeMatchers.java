package com.hexdecteam.easeagent;

import com.google.common.base.Function;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Iterators.transform;
import static com.hexdecteam.easeagent.ReduceF.reduce;
import static net.bytebuddy.matcher.ElementMatchers.*;

public final class TypeMatchers {

    public static final Function<String, Junction<TypeDescription>> NAME_STARTS_WITH = new Function<String, Junction<TypeDescription>>() {
        @Override
        public Junction<TypeDescription> apply(String input) {
            return nameStartsWith(input);
        }
    };

    public static final Function<String, Junction<TypeDescription>> NAMED = new Function<String, Junction<TypeDescription>>() {
        @Override
        public Junction<TypeDescription> apply(String input) {
            return named(input);
        }
    };

    private static final Function<Junction<TypeDescription>, Junction<TypeDescription>> NOT =
            new Function<Junction<TypeDescription>, Junction<TypeDescription>>() {
                @Override
                public Junction<TypeDescription> apply(Junction<TypeDescription> input) {
                    return not(input);
                }
            };
    private static final ReduceF.BiFunction<Junction<TypeDescription>> OR =
            new ReduceF.BiFunction<Junction<TypeDescription>>() {
                @Override
                public Junction<TypeDescription> apply(Junction<TypeDescription> l, Junction<TypeDescription> r) {
                    return l.or(r);
                }
            };

    private TypeMatchers() {
        throw new UnsupportedOperationException();
    }

    public static Junction<TypeDescription> compound(Function<String, Junction<TypeDescription>> function,
                                                     List<String> includes, List<String> excludes) {
        final Junction<TypeDescription> type = any(function, includes);
        return excludes.isEmpty() ? type : type.and(any(compose(NOT, function), excludes));
    }

    public static Junction<TypeDescription> any(Function<String, Junction<TypeDescription>> function, List<String> iter) {
        return iter.isEmpty() ? ElementMatchers.<TypeDescription>none() : reduce(transform(iter.iterator(), function), OR);
    }
}
