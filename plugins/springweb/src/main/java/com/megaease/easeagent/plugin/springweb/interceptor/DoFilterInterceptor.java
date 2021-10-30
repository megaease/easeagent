package com.megaease.easeagent.plugin.springweb.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.springweb.advice.DoFilterAdvice;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletRequest;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Object context) {
        // OncePerRequestFilter oncePerRequestFilter = (OncePerRequestFilter) methodInfo.getInvoker();
        ServletRequest request = (ServletRequest) methodInfo.getArgs()[0];
        System.out.println("run before http server do filter");
    }

    @Override
    public Object after(MethodInfo methodInfo, Object context) {
        System.out.println("run after http server do filter");
        return null;
    }
}
