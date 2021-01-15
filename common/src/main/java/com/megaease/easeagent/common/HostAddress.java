/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
