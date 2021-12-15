package com.megaease.easeagent.plugin.api.dispatcher;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;

public interface IDispatcher {
    void enter(int chainIndex, MethodInfo info);

    Object exit(int chainIndex, MethodInfo methodInfo,
                Context context, Object result, Throwable e);
}
