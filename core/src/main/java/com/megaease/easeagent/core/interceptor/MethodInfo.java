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
     * Throwable is existed if method throws exception. Otherwise, it is null.
     */
    private Throwable throwable;

    /**
     * The return value of instrumented method
     */
    private Object retValue;

    public boolean isSuccess() {
        return this.throwable == null;
    }

}
