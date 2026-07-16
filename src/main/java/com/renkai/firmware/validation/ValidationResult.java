package com.renkai.firmware.validation;

public class ValidationResult {
    private final boolean passed;
    private final String message;

    private ValidationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public static ValidationResult success(String message) {
        return new ValidationResult(true, message);
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }
}
