package org.fhm.substrate.service;

import org.fhm.substrate.standard.ILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2023/10/24-11:26 AM
 * @author 谭波
 */
public class CommonLogger implements ILogger {

    private final Logger log;

    public CommonLogger(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void info(String message, Object... parameters) {
        log.info(message, parameters);
    }

    @Override
    public void info(Object message, Object... parameters) {
        log.info(message.toString(), parameters);
    }

    @Override
    public void warn(String message, Object... parameters) {
        log.warn(message, parameters);
    }

    @Override
    public void warn(Object message, Object... parameters) {
        log.warn(message.toString(), parameters);
    }

    @Override
    public void debug(String message, Object... parameters) {
        log.debug(message, parameters);
    }

    @Override
    public void debug(Object message, Object... parameters) {
        log.debug(message.toString(), parameters);
    }

    @Override
    public void error(Exception e, String message) {
        log.error(message, e);
    }

    @Override
    public void error(String message, Object... parameters) {
        log.error(message, parameters);
    }
}