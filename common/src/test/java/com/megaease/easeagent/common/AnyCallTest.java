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

 package com.megaease.easeagent.common;

import com.google.common.collect.Iterables;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Definition.Transformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AnyCallTest {

    @Test
    public void should_match_none_type() throws Exception {
        final Definition.Default def = new ForTest("", "").define(Definition.Default.EMPTY);
        for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> entry : def.asMap().entrySet()) {
            assertFalse(entry.getKey().matches(Descriptions.type(ForTest.class)));
        }
    }

    @Test
    public void should_match_for_test() throws Exception {
        final Definition.Default def = new ForTest(AnyCallTest.class.getPackage().getName(), "").define(Definition.Default.EMPTY);
        for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> entry : def.asMap().entrySet()) {
            assertTrue(entry.getKey().matches(Descriptions.type(ForTest.class)));
        }
    }

    @Test
    public void should_not_match_for_test() throws Exception {
        final Definition.Default def = new ForTest(AnyCallTest.class.getPackage().getName(), ForTest.class.getName())
                .define(Definition.Default.EMPTY);
        for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> entry : def.asMap().entrySet()) {
            assertFalse(entry.getKey().matches(Descriptions.type(ForTest.class)));
        }
    }

    @Test
    public void should_not_match_to_string() throws Exception {
        final Definition.Default def = new ForTest("", "").define(Definition.Default.EMPTY);
        for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> entry : def.asMap().entrySet()) {
            final Iterable<Transformer> transformers = entry.getValue();
            assertThat(Iterables.size(transformers), is(1));

            final Transformer transformer = Iterables.get(transformers, 0);
            assertFalse(transformer.matcher.matches(Descriptions.method(Object.class.getDeclaredMethod("toString"))));
        }
    }

    static class ForTest extends AnyCall {

        private final String include;
        private final String exclude;

        ForTest(String include, String exclude) {
            this.include = include;
            this.exclude = exclude;
        }

        @Override
        protected Transformer method(final ElementMatcher<? super MethodDescription> matcher) {
            return new Transformer("inline", "factory", matcher);
        }

        @Override
        protected List<String> include_class_prefix_list() {
            return include.isEmpty() ? Collections.<String>emptyList() : Collections.singletonList(include);
        }

        @Override
        protected List<String> exclude_class_prefix_list() {
            return exclude.isEmpty() ? Collections.<String>emptyList() : Collections.singletonList(exclude);
        }
    }
}