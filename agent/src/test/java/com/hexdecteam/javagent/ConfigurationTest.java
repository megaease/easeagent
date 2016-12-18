package com.hexdecteam.javagent;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConfigurationTest {
    @Test
    public void should_config_a() throws Exception {
        final Optional<A> result = Configuration.load(null).configure(new A());
        if (result.isPresent()) {
            final A a = result.get();
            assertThat(a.s, is("text"));
            assertThat(a.i, is(1));
            assertThat(a.b, is(true));

            assertThat(a.ss, is(Arrays.asList("a", "b")));
            assertThat(a.is, is(Arrays.asList(1, 2)));
            assertThat(a.bs, is(Arrays.asList(true, false)));

            assertThat(a.m, is(Collections.singletonMap("k", "v")));
        } else {
            fail();
        }
    }

    @Configurable(prefix = "a")
    static class A {
        String  s;
        int     i;
        boolean b;

        List<String>  ss;
        List<Integer> is;
        List<Boolean> bs;

        Map<String, ?> m;
    }

}