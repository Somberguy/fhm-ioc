package org.fhm.ioc.service;

import org.fhm.ioc.standard.ILoggerHandler;

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

    private static final Map<String, ILoggerHandler> loggerHandlerContainer = new HashMap<>();

    public static ILoggerHandler getLogger(Class<?> clazz) {
        ILoggerHandler handler;
        String clazzName = clazz.getName();
        if (Objects.isNull((handler = loggerHandlerContainer.get(clazzName)))) {
            synchronized(LoggerHandler.class){
                return loggerHandlerContainer.computeIfAbsent(clazzName, k -> new CommonHandler(clazz));
            }
        }
        return handler;
    }
}
