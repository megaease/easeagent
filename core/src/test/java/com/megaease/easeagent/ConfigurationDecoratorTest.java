package com.megaease.easeagent;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigurationDecoratorTest {
    @Test
    public void should_implement_abstract_method() throws Exception {
        final Bar bar = new ConfigurationDecorator(config("bar.x = 10")).newInstance(Bar.class);
        assertThat(bar.x(), is(10));
    }

    @Test(expected = ConfigException.Missing.class)
    public void should_complaint_key_missing() throws Exception {
        final Bar bar = new ConfigurationDecorator(config("bar.y = 10")).newInstance(Bar.class);
        assertThat(bar.x(), is(10));
    }

    @Test
    public void should_override_default_value() throws Exception {
        final Bar bar = new ConfigurationDecorator(config("bar.y = 10")).newInstance(Bar.class);
        assertThat(bar.y(), is(10));
    }

    @Test
    public void should_get_default_value() throws Exception {
        final Bar bar = new ConfigurationDecorator(config("bar.x = 10")).newInstance(Bar.class);
        assertThat(bar.z(), is(3));
    }

    @Test
    public void should_get_by_binding_key() throws Exception {
        final Bar bar = new ConfigurationDecorator(config("bar.ww = 10")).newInstance(Bar.class);
        assertThat(bar.w(), is(10));
    }

    @Test
    public void should_get_all_kinds_type() throws Exception {
        final String present = "bar.b = yes\nbar.l = 1111\nbar.d = 1.0086\nbar.ld = [1.0,2.0]";
        final Bar bar = new ConfigurationDecorator(config(present)).newInstance(Bar.class);
        assertThat(bar.b(), is(true));
        assertThat(bar.l(), is(1111L));
        assertThat(bar.d(), is(1.0086));
        assertThat(bar.ld(), is(Arrays.asList(1.0, 2.0)));
    }

    @Test
    public void should_be_ok_with_no_configuration() throws Exception {
        new ConfigurationDecorator(config("")).newInstance(Plugin.Noop.class);
    }

    private static Config config(String s) {
        return ConfigFactory.parseString(s);
    }

    @ConfigurationDecorator.Binding("bar")
    static abstract class Bar {
        @ConfigurationDecorator.Binding("ww")
        abstract int w();

        abstract int x();

        int y() {return 2;}

        int z() {return 3;}

        abstract boolean b();

        abstract Long l();

        abstract double d();

        abstract List<Double> ld();
    }


}