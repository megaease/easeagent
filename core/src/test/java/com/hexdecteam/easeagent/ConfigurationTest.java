package com.hexdecteam.easeagent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {
    @Test
    public void should_config_obj_with_configurable() throws Exception {
        final Configured o = Configuration.load(null).configure(new Configured());
        assertThat(o.s, is("text"));
        assertThat(o.i, is(1));
        assertThat(o.b, is(true));

        assertThat(o.ss, is(Arrays.asList("a", "b")));
        assertThat(o.is, is(Arrays.asList(1, 2)));
        assertThat(o.bs, is(Arrays.asList(true, false)));

        assertThat(o.m, is(Collections.singletonMap("k", "v")));
    }

    @Test
    public void should_ignore_obj_without_configurable() throws Exception {
        final Ignored1 o = Configuration.load(null).configure(new Ignored1());
        assertThat(o.i, is(0));
    }

    @Test
    public void should_ignore_obj_cause_by_missing_prefix() throws Exception {
        final Ignored2 o = Configuration.load(null).configure(new Ignored2());
        assertThat(o.i, is(0));
    }

    @Configurable(prefix = "a")
    static class Configured {
        String  s;
        int     i;
        boolean b;

        List<String>  ss;
        List<Integer> is;
        List<Boolean> bs;

        Map<String, ?> m;
    }

    static class Ignored1 {
        int     i;
    }

    @Configurable(prefix = "b")
    static class Ignored2 {
        int     i;
    }

}