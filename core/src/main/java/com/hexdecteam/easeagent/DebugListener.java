package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugListener extends AgentBuilder.Listener.Adapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugListener.class);

    @Override
    public void onTransformation(TypeDescription td, ClassLoader cl, JavaModule m, DynamicType dt) {
        LOGGER.debug("Transform {} from {}", td, cl);
    }

    @Override
    public void onIgnored(TypeDescription td, ClassLoader cl, JavaModule m) {
        LOGGER.debug("Ignored {} from {}", td, cl);
    }

    @Override
    public void onError(String tn, ClassLoader cl, JavaModule m, Throwable t) {
        LOGGER.debug("Error {} from {}", tn, cl, t);
    }

}
