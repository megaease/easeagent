package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.dispatcher.IDispatcher;

public class NoOpDispatcher implements IDispatcher {
    @Override
    public void enter(int chainIndex, MethodInfo info) {
    }

    @Override
    public Object exit(int chainIndex, MethodInfo methodInfo, Context context, Object result, Throwable e) {
        return result;
    }
}
