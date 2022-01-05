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

package com.megaease.easeagent.core.utils;

import com.megaease.easeagent.plugin.utils.common.DataSize;

import java.nio.charset.StandardCharsets;

public class TextUtils {

    private TextUtils() {
    }

    public static String cutStrByDataSize(String str, DataSize size) {
        byte[] now = str.getBytes(StandardCharsets.UTF_8);
        if (now.length <= size.toBytes()) {
            return str;
        }
        String tmp = new String(now, 0, (int) size.toBytes(), StandardCharsets.UTF_8);
        char unstable = tmp.charAt(tmp.length() - 1);
        char old = str.charAt(tmp.length() - 1);
        if (unstable == old) {
            return tmp;
        }
        return new String(tmp.toCharArray(), 0, tmp.length() - 1);
    }

    public static boolean hasText(String val) {
        return val != null && val.trim().length() > 0;
    }
}
