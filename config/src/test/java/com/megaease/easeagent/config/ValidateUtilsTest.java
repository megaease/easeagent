package com.megaease.easeagent.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.megaease.easeagent.config.ValidateUtils.*;

public class ValidateUtilsTest {
    @Test
    public void test_hasText() throws Exception {
        Configs configs = new Configs(Collections.emptyMap());
        try {
            ValidateUtils.validate(configs, "hello", HasText);
            Assert.fail("Never get here.");
        } catch (ValidateUtils.ValidException e) {
            Assert.assertTrue(e.getMessage().contains("has no non-empty value"));
        }
    }

    @Test
    public void test_numberInt() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "test"));
        try {
            ValidateUtils.validate(configs, "hello", HasText, NumberInt);
            Assert.fail("Never get here.");
        } catch (ValidateUtils.ValidException e) {
            Assert.assertTrue(e.getMessage().contains("has no integer value"));
        }
    }

    @Test
    public void test_numberInt2() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "100"));
        try {
            ValidateUtils.validate(configs, "hello", HasText, NumberInt);
        } catch (ValidateUtils.ValidException e) {
            Assert.fail("Never get here.");
        }
    }

    @Test
    public void test_bool() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "test"));
        try {
            ValidateUtils.validate(configs, "hello", HasText, Bool);
            Assert.fail("Never get here.");
        } catch (ValidateUtils.ValidException e) {
            Assert.assertTrue(e.getMessage().contains("has no boolean value"));
        }
    }

    @Test
    public void test_bool2() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "true"));
        try {
            ValidateUtils.validate(configs, "hello", HasText, Bool);
        } catch (ValidateUtils.ValidException e) {
            Assert.fail("Never get here.");
        }
    }

}