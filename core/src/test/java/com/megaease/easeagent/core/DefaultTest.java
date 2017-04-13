package com.megaease.easeagent.core;

import com.megaease.easeagent.core.Definition.Default;
import org.junit.Test;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultTest {
    @Test
    public void should_work() throws Exception {
        final Map<?, ?> map = Default.EMPTY.type(named("t"))
                                           .transform(new Definition.Transformer("inline", "factory", named("m")))
                                           .end().asMap();
        assertThat(map.size(), is(1));
    }
}