package com.megaease.easeagent.sniffer;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionTool {
    public static Object invokeMethod(Object own, String method, Object... args) throws ReflectiveOperationException {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getClass();
        }
        Method md = ReflectionUtils.findMethod(own.getClass(), method, types);
        ReflectionUtils.makeAccessible(md);
        return ReflectionUtils.invokeMethod(md, own, args);
    }

    public static Object extractField(Object own, String field) throws ReflectiveOperationException {
        Field fd = ReflectionUtils.findField(own.getClass(), field);
        ReflectionUtils.makeAccessible(fd);
        return ReflectionUtils.getField(fd, own);
    }
}
