package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.hexdecteam.easeagent.Transformation.Feature.Compound;
import com.hexdecteam.easeagent.Transformation.Feature.Compoundable;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class TraceJDBC extends Transformation<Transformation.Noop> {

    @Override
    protected Feature feature(Noop conf) {
        return new Compound(Arrays.asList(
                new Compoundable(isSubTypeOf(Statement.class).and(not(isSubTypeOf(PreparedStatement.class)))) {
                    final String key = UUID.randomUUID().toString();

                    @Override
                    protected Builder<?> config(Builder<?> b) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(ForwardDetection.Key.class, key)
                                             .to(StatementAdvice.class)
                                             .on(isDeclaredBy(Statement.class).and(nameStartsWith("execute"))
                                                                              .and(takesArgument(0, String.class))));
                    }
                },
                new Compoundable(
                        ElementMatchers.isSubTypeOf(PreparedStatement.class)
                                       .and(declaresMethod(ElementMatchers.<MethodDescription>isPublic()
                                               .and(named("asSql"))
                                               .and(returns(String.class))
                                               .and(takesArguments(0))))
                ) {
                    final String key = UUID.randomUUID().toString();

                    @Override
                    protected Builder<?> config(Builder<?> b) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(ForwardDetection.Key.class, key)
                                             .to(MySQLAdvice.class).on(nameStartsWith("execute")));
                    }
                },
                new Compoundable(
                        ElementMatchers.<TypeDescription>named("org.h2.jdbc.JdbcPreparedStatement")
                                .and(declaresField(named("sqlStatement").and(fieldType(String.class))))
                ) {
                    @Override
                    protected Builder<?> config(Builder<?> b) {
                        return b.visit(Advice.to(H2Advice.class).on(nameStartsWith("execute")));
                    }
                }
        ));

    }

    static class StatementAdvice {
        @Advice.OnMethodEnter
        public static byte enter(@ForwardDetection.Key String key, @Advice.Argument(0) String sql) {
            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);
            // A union value for both marked and forked with bit operation.
            if (!marked) return 0;

            return (byte) (1 | (StackFrame.fork(sql, true) ? 2 : 0));
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key, @Advice.Enter byte enter) {
            if ((enter & 1) == 1) ForwardDetection.Mark.clear(key);
            if ((enter & 2) == 2) StackFrame.join();
        }
    }

    static class MySQLAdvice {
        @Advice.OnMethodEnter
        public static byte enter(@ForwardDetection.Key String key, @Advice.This Object self) {
            final boolean marked = ForwardDetection.Mark.markIfAbsent(key);
            // A union value for both marked and forked with bit operation.
            if (!marked) return 0;

            try {
                final Method method = self.getClass().getMethod("asSql");
                return (byte) (1 | (StackFrame.fork(method.invoke(self).toString(), true) ? 2 : 0));
            } catch (Exception e) {
                return 1;
            }
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@ForwardDetection.Key String key, @Advice.Enter byte enter) {
            if ((enter & 1) == 1) ForwardDetection.Mark.clear(key);
            if ((enter & 2) == 2) StackFrame.join();
        }

    }

    static class H2Advice {
        @Advice.OnMethodEnter
        public static boolean enter(@Advice.This Object self) {
            try {
                final Field field = self.getClass().getDeclaredField("sqlStatement");
                field.setAccessible(true);
                return StackFrame.fork(field.get(self).toString(), true);
            } catch (Exception e) {
                return false;
            }
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Enter boolean forked) {
            if (forked) StackFrame.join();
        }
    }
}
