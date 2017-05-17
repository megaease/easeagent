package com.megaease.easeagent.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class HostAddress {
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

    public static InetAddress localaddr() {
        return LOCALHOST_ADDR;
    }

    public static String localhost() {
        return LOCALHOST_NAME;
    }

    public static String address(String host) {
        try {
            return Inet4Address.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return host;
        }
    }


    private HostAddress() { }
}
