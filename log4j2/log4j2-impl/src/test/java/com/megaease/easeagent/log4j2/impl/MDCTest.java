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

package com.megaease.easeagent.log4j2.impl;

import com.megaease.easeagent.log4j2.MDC;
import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import org.junit.Test;

import static org.junit.Assert.*;

public class MDCTest {
    static {
        AllUrlsSupplier.ENABLED = true;
    }

    @Test
    public void put() {
        MDC.put("testA", "testB");
        assertNull(org.slf4j.MDC.get("testA"));
        assertNotNull(MDC.get("testA"));
    }

    @Test
    public void remove() {
        MDC.put("testA", "testB");
        assertNotNull(MDC.get("testA"));
        MDC.remove("testA");
        assertNull(MDC.get("testA"));
    }

    @Test
    public void get() {
        MDC.put("testA", "testB");
        assertNull(org.slf4j.MDC.get("testA"));
        assertNotNull(MDC.get("testA"));
        org.slf4j.MDC.put("testB", "testB");
        assertNotNull(org.slf4j.MDC.get("testB"));
        assertNull(MDC.get("testB"));
    }
}
