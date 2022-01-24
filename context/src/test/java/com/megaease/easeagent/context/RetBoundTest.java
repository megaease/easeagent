/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.context;

import org.junit.Test;

import static org.junit.Assert.*;

public class RetBoundTest {

    @Test
    public void size() {
        assertEquals(100, new RetBound(100).size());
    }

    @Test
    public void get() {
        RetBound retBound = new RetBound(1);
        Object o = new Object();
        Object o2 = new Object();
        retBound.put("a", o);
        assertEquals(o, retBound.get("a"));
        assertNotEquals(o2, retBound.get("a"));
    }

    @Test
    public void put() {
        get();
    }
}
