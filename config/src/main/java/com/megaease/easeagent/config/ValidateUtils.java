package com.megaease.easeagent.config;

public class ValidateUtils {


    public static class ValidException extends RuntimeException {
        public ValidException(String message) {
            super(message);
        }
    }

    public interface Validator {
        void validate(String name, String value) throws ValidException;
    }

    public static void validate(Configs configs, String name, Validator... vs) throws ValidException {
        String value = configs.getString(name);
        for (Validator one : vs) {
            one.validate(name, value);
        }
    }

    public static final Validator HasText = (name, value) -> {
        if (value == null || value.trim().length() == 0) {
            throw new ValidException(String.format("Property[%s] has no non-empty value", name));
        }
    };

    public static final Validator Bool = (name, value) -> {
        String upper = value.toUpperCase();
        if (upper.equals("TRUE") || upper.equals("FALSE")) {
            return;
        }
        throw new ValidException(String.format("Property[%s] has no boolean value", name));
    };

    public static final Validator NumberInt = (name, value) -> {
        try {
            int rst = Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new ValidException(String.format("Property[%s] has no integer value", name));
        }
    };
}
