package org.fhm.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname BeanEnable
 * @Description TODO
 * @Date 2023/11/29-5:10 PM
 * @Author tanbo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeanEnable {
}
