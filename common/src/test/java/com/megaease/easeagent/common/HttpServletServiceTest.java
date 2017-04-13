package com.megaease.easeagent.common;

import com.google.common.collect.Iterables;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Definition.Transformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class HttpServletServiceTest {

    @Test
    public void should_match_http_servlet_serve() throws Exception {
        final Definition.Default def = new ForTest().define(Definition.Default.EMPTY);
        for (Map.Entry<ElementMatcher<? super TypeDescription>, Iterable<Transformer>> entry : def.asMap().entrySet()) {
            assertTrue(entry.getKey().matches(Descriptions.type(HttpServlet.class)));

            final Iterable<Transformer> transformers = entry.getValue();
            assertThat(Iterables.size(transformers), is(1));

            final Transformer transformer = Iterables.get(transformers, 0);
            assertThat(transformer.inlineAdviceClassName, is("inline"));
            assertThat(transformer.adviceFactoryClassName, is("factory"));

            final Method service = HttpServlet.class.getDeclaredMethod("service", HttpServletRequest.class, HttpServletResponse.class);
            assertTrue(transformer.matcher.matches(Descriptions.method(service)));
        }
    }

    static class Foo extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }
    }

    private static class ForTest extends HttpServletService {
        @Override
        protected Transformer service(final ElementMatcher<? super MethodDescription> matcher) {
            return new Transformer("inline", "factory", matcher);
        }
    }
}