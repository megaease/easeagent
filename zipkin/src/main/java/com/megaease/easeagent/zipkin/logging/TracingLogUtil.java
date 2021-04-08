//package com.megaease.easeagent.zipkin.logging;
//
//import brave.Span;
//import com.megaease.easeagent.common.logging.LogUtils;
//import org.springframework.util.ReflectionUtils;
//
//import java.lang.reflect.Method;
//
//public class TracingLogUtil {
//
//    public static String mdcGet(String name, ClassLoader classLoader) {
//        Class<?> log4JMDCClass = LogUtils.loadLog4JMDC(classLoader);
//        if (log4JMDCClass != null) {
//            Method getMethod = ReflectionUtils.findMethod(log4JMDCClass, "get", String.class);
//            if (getMethod != null) {
//                return (String) ReflectionUtils.invokeMethod(getMethod, null, name);
//            }
//        }
//        Class<?> loadLogBackMDCClass = LogUtils.loadLogBackMDC(classLoader);
//        if (loadLogBackMDCClass != null) {
//            Method getMethod = ReflectionUtils.findMethod(loadLogBackMDCClass, "get", String.class);
//            if (getMethod != null) {
//                return (String) ReflectionUtils.invokeMethod(getMethod, null, name);
//            }
//        }
//        return null;
//    }
//
//    public static void mdcPut(Span span, ClassLoader classLoader) {
//        Class<?> log4JMDCClass = LogUtils.loadLog4JMDC(classLoader);
//        if (log4JMDCClass != null) {
//            Method putMethod = ReflectionUtils.findMethod(log4JMDCClass, "put", String.class, String.class);
//            if (putMethod != null) {
////            ThreadContext.put("traceId", span.context().traceIdString());
////            ThreadContext.put("spanId", span.context().spanIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "traceId", span.context().traceIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "spanId", span.context().spanIdString());
//                return;
//            }
//        }
//        Class<?> loadLogBackMDCClass = LogUtils.loadLogBackMDC(classLoader);
//        if (loadLogBackMDCClass != null) {
//            Method putMethod = ReflectionUtils.findMethod(loadLogBackMDCClass, "put", String.class, String.class);
//            if (putMethod != null) {
//                ReflectionUtils.invokeMethod(putMethod, null, "traceId", span.context().traceIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "spanId", span.context().spanIdString());
//            }
////            MDC.put("traceId", span.context().traceIdString());
////            MDC.put("spanId", span.context().spanIdString());
//        }
//    }
//
//    public static void mdcPut(Method method, String name, String value) {
//        ReflectionUtils.invokeMethod(method, null, name, value);
//
//        Class<?> log4JMDCClass = LogUtils.loadLog4JMDC(classLoader);
//        if (log4JMDCClass != null) {
//            Method putMethod = ReflectionUtils.findMethod(log4JMDCClass, "put", String.class, String.class);
//            if (putMethod != null) {
////            ThreadContext.put("traceId", span.context().traceIdString());
////            ThreadContext.put("spanId", span.context().spanIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "traceId", span.context().traceIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "spanId", span.context().spanIdString());
//                return;
//            }
//        }
//        Class<?> loadLogBackMDCClass = LogUtils.loadLogBackMDC(classLoader);
//        if (loadLogBackMDCClass != null) {
//            Method putMethod = ReflectionUtils.findMethod(loadLogBackMDCClass, "put", String.class, String.class);
//            if (putMethod != null) {
//                ReflectionUtils.invokeMethod(putMethod, null, "traceId", span.context().traceIdString());
//                ReflectionUtils.invokeMethod(putMethod, null, "spanId", span.context().spanIdString());
//            }
////            MDC.put("traceId", span.context().traceIdString());
////            MDC.put("spanId", span.context().spanIdString());
//        }
//    }
//
//    public static void mdcRemove(ClassLoader classLoader) {
//        Class<?> log4JMDCClass = LogUtils.loadLog4JMDC(classLoader);
//        if (log4JMDCClass != null) {
//            Method removeMethod = ReflectionUtils.findMethod(log4JMDCClass, "remove", String.class);
//            if (removeMethod != null) {
//                ReflectionUtils.invokeMethod(removeMethod, null, "tractId");
//                ReflectionUtils.invokeMethod(removeMethod, null, "spanId");
//            }
////            ThreadContext.remove("tractId");
////            ThreadContext.remove("spanId");
//            return;
//        }
//        Class<?> loadLogBackMDCClass = LogUtils.loadLogBackMDC(classLoader);
//        if (loadLogBackMDCClass != null) {
//            Method removeMethod = ReflectionUtils.findMethod(loadLogBackMDCClass, "remove", String.class);
//            if (removeMethod != null) {
//                ReflectionUtils.invokeMethod(removeMethod, null, "tractId");
//                ReflectionUtils.invokeMethod(removeMethod, null, "spanId");
//            }
////            MDC.remove("traceId");
////            MDC.remove("spanId");
//        }
//    }
//}
