package com.hexdecteam.easeagent;

public class MayBeABug extends IllegalStateException {
    public MayBeABug(String msg) {
        super(msg);
    }

    public MayBeABug(Throwable cause) {
        super(cause);
    }
}
