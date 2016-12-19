package com.hexdecteam.easeagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public interface StackTrace {

    @Configurable(prefix = "stack.trace")
    class Transformation extends AbstractTransformation {
        List<String> classes = Collections.singletonList(".+");

        @Override
        protected ElementMatcher.Junction<TypeDescription> matcher() {
            return classes.stream()
                          .map(ElementMatchers::<TypeDescription>nameMatches)
                          .reduce(ElementMatcher.Junction::or)
                          .orElse(nameMatches(classes.get(0)));
        }

        @Override
        protected AgentBuilder.Transformer transformer() {
            return (b, td, cl) -> b.visit(Advice.to(Delegation.class).on(methods()));
        }

        private ElementMatcher.Junction<MethodDescription.InDefinedShape> methods() {
            return not(isAbstract())
                    .and(not(isFinalizer()))
                    .and(not(isHashCode()))
                    .and(not(isEquals()))
                    .and(not(isToString()))
                    .and(not(isClone()))
                    .and(not(isDefaultConstructor()))
                    .and(not(isTypeInitializer()))
                    .and(not(isSetter()))
                    .and(not(isGetter()))
                    ;
        }
    }

    class Delegation {
        private final static ThreadLocal<Frame> CURRENT = new ThreadLocal<>();

        @Advice.OnMethodEnter(inline = false)
        public static long enter() {
            final Frame frame = CURRENT.get();
            if (frame == null) {
                CURRENT.set(new Frame());
            } else {
                CURRENT.set(frame.in());
            }
            return System.nanoTime();
        }

        @Advice.OnMethodExit(inline = false)
        public static void exit(@Advice.Origin String method, @Advice.Enter long begin) {
            final Frame frame = CURRENT.get();
            final Frame parent = frame.out(method, System.nanoTime() - begin);
            if (parent != null) {
                CURRENT.set(parent);
            } else {
                System.out.println(frame.toString());
            }
        }

    }

    class Frame {
        private final Frame       parent;
        private final List<Frame> children;
        private final int         level;

        String method;
        long   elapseNanos;

        Frame() {
            this(null, 1);
        }

        Frame(Frame parent, int level) {
            this.parent = parent;
            this.level = level;
            this.children = new LinkedList<>();
        }

        Frame in() {
            final Frame frame = new Frame(this, level + 1);
            children.add(frame);
            return frame;
        }

        Frame out(String method, long elapseNanos) {
            this.method = method;
            this.elapseNanos = elapseNanos;
            return parent;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append('[').append(elapseNanos / 1000 / 1000).append(" ms] ").append(method);
            final int size = children.size();
            for (int i = 0; i < size; i++) {
                sb.append('\n');
                for (int j = 0; j < level; j++) {
                    sb.append('\t');
                }
                if (i == size - 1) sb.append("└── ");
                else sb.append("├── ");
                sb.append(children.get(i));
            }
            return sb.toString();
        }
    }

}
