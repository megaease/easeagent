package com.hexdecteam.easeagent;

import com.codahale.metrics.annotation.*;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class MetricAnnotationsTest {

    @Test
    public void should_matches_type() throws Exception {
        final Transformation.Feature feature = featureOf(Counted.class);
        final TypeDescription.ForLoadedType td = new TypeDescription.ForLoadedType(Foo.class);
        assertTrue(feature.type().matches(td));
    }

    @Test
    public void should_publish_event_inc() throws Exception {
        final Class<?> loaded = Classes.transform(Foo.class).by(featureOf(Counted.class)).load();
        final Method inc = loaded.getMethod("inc");
        final Object o = loaded.newInstance();

        inc.invoke(o);

        final Object poll = EventBus.queue.poll(1, TimeUnit.SECONDS);
        assertTrue(poll instanceof MetricEvents.Inc);
        assertThat(((MetricEvents.Inc) poll).name, is("inc"));
    }

    @Test
    public void should_publish_event_mark() throws Exception {
        final Class<?> loaded = Classes.transform(Foo.class).by(featureOf(Metered.class)).load();
        final Method inc = loaded.getMethod("mark");
        final Object o = loaded.newInstance();

        inc.invoke(o);

        final Object poll = EventBus.queue.poll(1, TimeUnit.SECONDS);
        assertTrue(poll instanceof MetricEvents.Mark);
        assertThat(((MetricEvents.Mark) poll).name, is(Foo.class.getName() + "#meter"));
    }

    @Test
    public void should_publish_event_mark_exception() throws Exception {
        final Class<?> loaded = Classes.transform(Foo.class).by(featureOf(ExceptionMetered.class)).load();
        final Method inc = loaded.getMethod("error");
        final Object o = loaded.newInstance();

        try {
            inc.invoke(o);
            fail("No complaint");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        final Object poll = EventBus.queue.poll(1, TimeUnit.SECONDS);
        assertTrue(poll instanceof MetricEvents.Mark);
        assertThat(((MetricEvents.Mark) poll).name, is(Foo.class.getName() + "#error.exceptions"));
    }

    @Test
    public void should_publish_event_update() throws Exception {
        final Class<?> loaded = Classes.transform(Foo.class).by(featureOf(Timed.class)).load();
        final Method inc = loaded.getMethod("update");
        final Object o = loaded.newInstance();

        inc.invoke(o);

        final Object poll = EventBus.queue.poll(1, TimeUnit.SECONDS);
        assertTrue(poll instanceof MetricEvents.Update);
    }

    @Test
    public void should_publish_event_register() throws Exception {
        final Class<?> loaded = Classes.transform(Foo.class).by(featureOf(Gauge.class)).load();
        loaded.newInstance();
        final Object poll = EventBus.queue.poll(1, TimeUnit.SECONDS);
        assertTrue(poll instanceof MetricEvents.Register);
    }

    private Transformation.Feature featureOf(final Class<? extends Annotation> c) {
        MetricAnnotations.Configuration conf = new MetricAnnotations.Configuration() {
            @Override
            List<String> supported_annotations() {
                return Collections.singletonList(c.getSimpleName());
            }
        };
        return new MetricAnnotations().feature(conf);
    }


    public static class Foo {

        @Counted(name = "inc", absolute = true)
        public void inc() {}

        @Metered(name = "meter")
        public void mark() {}

        @ExceptionMetered(cause = RuntimeException.class)
        public void error() {throw new RuntimeException();}

        @Timed
        public void update() {}

        @Gauge
        public int value() {return 1;}
    }
}