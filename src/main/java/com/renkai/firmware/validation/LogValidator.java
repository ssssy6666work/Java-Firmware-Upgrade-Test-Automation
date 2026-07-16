package com.renkai.firmware.validation;

import java.util.Arrays;
import java.util.List;

public class LogValidator {
    private static final List<String> ERROR_KEYWORDS = Arrays.asList(
            "ERROR",
            "FAILED",
            "PANIC",
            "CRITICAL",
            "SEGMENTATION FAULT",
            "BOOT FAILURE"
    );

    public ValidationResult validate(List<String> logs) {
        for (String log : logs) {
            String upper = log.toUpperCase();
            for (String keyword : ERROR_KEYWORDS) {
                if (upper.contains(keyword)) {
                    return ValidationResult.failure(
                            "Error keyword found in log: " + keyword + " -> " + log);
                }
            }
        }
        return ValidationResult.success("No critical error keyword found");
    }
}
