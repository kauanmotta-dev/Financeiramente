package com.financeiramente.desktop.logging;

import com.financeiramente.core.db.AppLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaUtilAppLogger implements AppLogger {

    private final Logger logger;

    public JavaUtilAppLogger(Class<?> source) {
        this.logger = Logger.getLogger(source.getName());
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}