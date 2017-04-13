package com.megaease.easeagent.gen;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.lang.model.element.TypeElement;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class GenerateSpecTestBase {
    private final When when;
    private final String generated;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    GenerateSpecTestBase(When when, String generated) {
        this.when = when;
        this.generated = generated;
    }

    @Test
    public void run() throws Exception {
        final TypeElement te = mock(TypeElement.class);
        final ProcessUtils utils = mock(ProcessUtils.class);
        assertThat(when.given(te, utils, thrown), FileMatchers.be(generated));
    }

    interface When {
        String given(TypeElement te, ProcessUtils utils, ExpectedException thrown);
    }
}
