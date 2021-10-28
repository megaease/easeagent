package com.megaease.easeagent.plugin.jdkhttpserver.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.jdkhttpserver.advice.DoFilterAdvice;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default")
public class DoFilterInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Object context) {
        System.out.println("run before http server");
    }

    @Override
    public Object after(MethodInfo methodInfo, Object context) {
        return null;
    }
}
