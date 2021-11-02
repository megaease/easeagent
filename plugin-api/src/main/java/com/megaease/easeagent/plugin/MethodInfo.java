/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin;

import lombok.Builder;

import java.beans.Transient;
import java.util.Objects;

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
     * Throwable is existed if method throws exception. Otherwise, it is null.
     */
    private Throwable throwable;

    /**
     * The return value of instrumented method
     */
    private Object retValue;

    private boolean changed;

    public boolean isChanged() {
        return changed;
    }

    public boolean isSuccess() {
        return this.throwable == null;
    }

    public Object getInvoker() {
        return this.invoker;
    }

    public String getMethod() {
        return this.method;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public Object getRetValue() {
        return this.retValue;
    }

    public void setInvoker(Object invoker) {
        this.invoker = invoker;
        this.changed = true;
    }

    public void setMethod(String method) {
        this.method = method;
        this.changed = true;
    }

    public void setArgs(Object[] args) {
        this.args = args;
        this.changed = true;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        this.changed = true;
    }

    public void setRetValue(Object retValue) {
        this.retValue = retValue;
        this.changed = true;
    }

    public boolean equals(final MethodInfo o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        final MethodInfo other = (MethodInfo) o;
        if (!Objects.equals(this.getInvoker(), other.getInvoker())) {
            return false;
        }
        if (!Objects.equals(this.getMethod(), other.getMethod())) {
            return false;
        }
        if (!java.util.Arrays.deepEquals(this.getArgs(), other.getArgs())) {
            return false;
        }
        if (!Objects.equals(this.getThrowable(), other.getThrowable())) {
            return false;
        }
        if (!Objects.equals(this.getRetValue(), other.getRetValue())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object invoker = this.getInvoker();
        result = result * PRIME + (invoker == null ? 43 : invoker.hashCode());
        final Object method = this.getMethod();
        result = result * PRIME + (method == null ? 43 : method.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getArgs());
        final Object throwable = this.getThrowable();
        result = result * PRIME + (throwable == null ? 43 : throwable.hashCode());
        final Object retValue = this.getRetValue();
        result = result * PRIME + (retValue == null ? 43 : retValue.hashCode());
        return result;
    }

    public String toString() {
        return "MethodInfo(invoker=" + this.getInvoker() + ", method=" + this.getMethod()
            + ", args=" + java.util.Arrays.deepToString(this.getArgs())
            + ", throwable=" + this.getThrowable() + ", retValue=" + this.getRetValue() + ")";
    }
}
