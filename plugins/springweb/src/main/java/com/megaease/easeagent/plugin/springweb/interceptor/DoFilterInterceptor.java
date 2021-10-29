package com.megaease.easeagent.plugin.springweb.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.springweb.advice.DoFilterAdvice;

import javax.servlet.http.HttpServletRequest;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterInterceptor implements Interceptor {


    @Override
    public void before(MethodInfo methodInfo, Object context) {
        Config config = EaseAgent.configFactory.getConfig("observability", "springwebfilter", "trace");
        if (!config.enable()) {
            return;
        }
        Context sessionContext = EaseAgent.contextSupplier.get();
        if (sessionContext.enter(DoFilterInterceptor.class) != 1) {
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        Span span = sessionContext.importProgress(new HttpServerRequest(httpServletRequest));
        sessionContext.put(DoFilterInterceptor.class, span).start();
        System.out.println("run before http server do filter");
    }

    @Override
    public Object after(MethodInfo methodInfo, Object context) {
        Config config = EaseAgent.configFactory.getConfig("observability", "springwebfilter", "trace");
        if (!config.enable()) {
            return null;
        }
        Context sessionContext = EaseAgent.contextSupplier.get();
        if (sessionContext.out(DoFilterInterceptor.class) != 1) {
            return null;
        }
        sessionContext.<Span>remove(DoFilterInterceptor.class).finish();
        System.out.println("run after http server do filter");
        return null;
    }

    class HttpServerRequest implements Request {
        private final HttpServletRequest httpServletRequest;

        HttpServerRequest(HttpServletRequest httpServletRequest) {
            this.httpServletRequest = httpServletRequest;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.SERVER;
        }

        @Override
        public String name() {
            return "spring-web";
        }

        @Override
        public String header(String name) {
            return httpServletRequest.getHeader(name);
        }

        @Override
        public void setHeader(String name, String value) {
            httpServletRequest.setAttribute(name, value);
        }
    }
}
