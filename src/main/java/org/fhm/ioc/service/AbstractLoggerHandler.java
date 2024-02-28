package org.fhm.ioc.service;

import org.fhm.ioc.standard.ILoggerHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @Classname LoggerHandler
 * @Description TODO
 * @Date 2023/12/19-11:39 AM
 * @Author tanbo
 */
public abstract class AbstractLoggerHandler {

    private static final Map<String, ILoggerHandler> loggerHandlerContainer = new HashMap<>();
    protected static Function<Class<?>, ILoggerHandler> create;
    public abstract void initializeLoggerHandler();
    public static ILoggerHandler getLogger(Class<?> clazz) {
        ILoggerHandler handler;
        String clazzName = clazz.getName();
        if (Objects.isNull((handler = loggerHandlerContainer.get(clazzName)))) {
            synchronized(AbstractLoggerHandler.class){
                return loggerHandlerContainer.computeIfAbsent(clazzName, k -> create.apply(clazz));
            }
        }
        return handler;
    }
}
