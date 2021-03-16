package com.megaease.easeagent.core.interceptor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MethodInfo {

    /**
     * The this reference of the instrumented method
     */
    private Object invoker;

    /**
     * instrumented method name
     */
    private String method;

    /**
     * The arguments of instrumented method. If no args exist,args=null
     */
    private Object[] args;

    /**
     * The return value of instrumented method
     */
    private Throwable throwable;

    /**
     * Throwable is exist if method throws exception. Otherwise it is null.
     */
    private Object retValue;

    public boolean isSuccess() {
        return this.throwable == null;
    }

}
