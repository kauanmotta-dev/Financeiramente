package com.financeiramente.core.util;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public static String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(message);
        }
        return value.trim();
    }
}