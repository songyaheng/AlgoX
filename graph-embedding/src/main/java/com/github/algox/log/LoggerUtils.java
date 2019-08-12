package com.github.algox.log;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    public static void info(String format, Object... args) {
        logger.info(String.format(format, args));
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void error(String error) {
        logger.error(error);
    }

    public static void error(Exception e) {
        logger.error(ExceptionUtils.getMessage(e));
    }
}
