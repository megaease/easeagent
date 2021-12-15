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

package com.megaease.easeagent.core.plugin;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.dispatcher.IDispatcher;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

public class BridgeDispatcher implements IDispatcher {
    @Override
    public void enter(int chainIndex, MethodInfo info) {
        InitializeContext context = EaseAgent.initializeContextSupplier.get();
        if (context.isNoop()) {
            return;
        }
        Dispatcher.enter(chainIndex, info, context);
    }

    @Override
    public Object exit(int chainIndex, MethodInfo methodInfo,
                     Context context, Object result, Throwable e) {
        if (context.isNoop() || !(context instanceof InitializeContext)) {
            return result;
        }
        InitializeContext iContext = (InitializeContext)context;
        methodInfo.throwable(e);
        methodInfo.retValue(result);
        Dispatcher.exit(chainIndex, methodInfo, iContext);
        if (methodInfo.isChanged()) {
            result = methodInfo.getRetValue();
        }

        return result;
    }
}
