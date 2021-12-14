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
