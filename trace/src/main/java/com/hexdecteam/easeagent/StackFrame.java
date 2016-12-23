package com.hexdecteam.easeagent;

import java.util.LinkedList;
import java.util.List;

public final class StackFrame {
    private final static ThreadLocal<StackFrame> CURRENT = new ThreadLocal<>();

    private final StackFrame       parent;
    private final String           invocation;
    private final long             begin;
    private final int              level;
    private final List<StackFrame> children;

    private volatile long end;

    private StackFrame(String invocation) {
        this(invocation, null, 0);
    }

    private StackFrame(String invocation, StackFrame parent, int level) {
        this.invocation = invocation;
        this.parent = parent;
        this.level = level;
        begin = System.nanoTime();
        children = new LinkedList<>();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append((end - begin) / 1000 / 1000).append("ms]\t")
          .append(invocation)
        ;
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

    public static boolean setIfAbsent(String invocation) {
        if (CURRENT.get() != null) return false;

        CURRENT.set(new StackFrame(invocation));
        return true;
    }

    public static void fork(String invocation) {
        final StackFrame frame = CURRENT.get();
        if (frame != null) {
            CURRENT.set(fork(frame, invocation));
        }
    }

    public static void print() {
        System.out.println(join());
    }

    public static StackFrame join() {
        final StackFrame frame = CURRENT.get();
        if (frame == null) return null;
        CURRENT.set(join(frame));
        return frame;
    }

    private static StackFrame fork(StackFrame parent, String name) {
        final StackFrame frame = new StackFrame(name, parent, parent.level + 1);
        parent.children.add(frame);
        return frame;
    }

    private static StackFrame join(StackFrame frame) {
        frame.end = System.nanoTime();
        if (frame.end - frame.begin < 1000 * 1000) {
            frame.parent.children.remove(frame);
        }
        return frame.parent;
    }

}
