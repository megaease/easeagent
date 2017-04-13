package com.megaease.easeagent.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class LocalhostAddress {
    private static final InetAddress LOCALHOST_ADDR;
    private static final String      LOCALHOST_NAME;

    static {
        try {
            LOCALHOST_ADDR = InetAddress.getLocalHost();
            LOCALHOST_NAME = LOCALHOST_ADDR.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    public static InetAddress getLocalhostAddr() {
        return LOCALHOST_ADDR;
    }

    public static String getLocalhostName() {
        return LOCALHOST_NAME;
    }


    private LocalhostAddress() { }
}
