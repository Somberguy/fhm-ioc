package org.fhm.ioc.util;

import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.service.LoggerHandler;

import java.lang.reflect.Field;

/**
 * @Classname ClazzUtil
 * @Description TODO
 * @Date 2023/11/9-11:10 AM
 * @Author tanbo
 */
public class ClazzUtil {

    private static final ILoggerHandler logger = LoggerHandler.getLogger(ClazzUtil.class);

    public static void setClazzValue(Object o, Object obj, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            if (field.get(o) == null) {
                field.set(o, obj);
            }
        } catch (IllegalAccessException e) {
            logger.error("the field" + field.getName() + " of class " + o.getClass().getName() + "set fail");
            throw IOCExceptionUtil.generateNormalException(e.getMessage(), e);
        }
    }
}
