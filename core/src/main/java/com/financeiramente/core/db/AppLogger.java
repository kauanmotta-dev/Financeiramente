package com.financeiramente.core.db;

public interface AppLogger {

    default void info(String message) {
    }

    default void warn(String message) {
    }

    default void error(String message) {
    }

    default void error(String message, Throwable throwable) {
        error(message);
    }
}