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
