package com.megaease.easeagent.common;

import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class HttpServletService implements Transformation {

    static final String HTTP_SERVLET = "javax.servlet.http.HttpServlet";
    static final String HTTP_SERVLET_REQUEST = "javax.servlet.http.HttpServletRequest";
    static final String HTTP_SERVLET_RESPONSE = "javax.servlet.http.HttpServletResponse";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named(HTTP_SERVLET)))
                  .transform(service(
                          takesArguments(2)
                                  .and(takesArgument(0, named(HTTP_SERVLET_REQUEST)))
                                  .and(takesArgument(1, named(HTTP_SERVLET_RESPONSE))))
                  ).end();

    }

    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

}
