package org.fhm.substrate.service;

import org.fhm.substrate.standard.ILogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @since 2023/12/19-11:39 AM
 * @author 谭波
 */
public abstract class AbstractLoggerHandler {

    private static final Map<String, ILogger> loggerHandlerContainer = new HashMap<>();
    protected static Function<Class<?>, ILogger> create;
    public abstract void initializeLoggerHandler();
    public static ILogger getLogger(Class<?> clazz) {
        ILogger handler;
        String clazzName = clazz.getName();
        if (Objects.isNull((handler = loggerHandlerContainer.get(clazzName)))) {
            return loggerHandlerContainer.computeIfAbsent(clazzName, k -> create.apply(clazz));
        }
        return handler;
    }
}
