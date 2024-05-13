package org.fhm.substrate.util;

import org.fhm.substrate.service.LoggerHandler;
import org.fhm.substrate.standard.ILogger;

import java.lang.reflect.Field;

/**
 * @since 2023/11/9-11:10 AM
 * @author 谭波
 */
public class ClazzUtil {

    private static final ILogger logger = LoggerHandler.getLogger(ClazzUtil.class);

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
