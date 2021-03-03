package com.megaease.easeagent.core.utils;

import com.google.common.base.Charsets;

public class TextUtils {

    public static String cutStrByDataSize(String str, DataSize size) {
        byte[] now = str.getBytes(Charsets.UTF_8);
        if (now.length <= size.toBytes()) {
            return str;
        }
        String tmp = new String(now, 0, (int) size.toBytes(), Charsets.UTF_8);
        char unstable = tmp.charAt(tmp.length() - 1);
        char old = str.charAt(tmp.length() - 1);
        if (unstable == old) {
            return tmp;
        }
        return new String(tmp.toCharArray(), 0, tmp.length() - 1);
    }
}
