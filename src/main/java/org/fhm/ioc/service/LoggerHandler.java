package org.fhm.ioc.service;

import org.fhm.ioc.standard.ILoggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname LoggerHandler
 * @Description TODO
 * @Date 2023/12/19-11:39 AM
 * @Author tanbo
 */
public class LoggerHandler {

    private static final Map<String, ILoggerHandler> loggerContainer = new HashMap<>();

    public static synchronized ILoggerHandler getLogger(Class<?> clazz) {
        ILoggerHandler handler = loggerContainer.get(clazz.getName());
        if (!Objects.nonNull(handler)) {
            handler = new Handler(clazz);
            loggerContainer.put(clazz.getName(), handler);
        }
        return handler;
    }

    private static final class Handler implements ILoggerHandler {

        private final Logger log;

        public Handler(Class<?> clazz) {
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
}
