package org.fhm.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @Classname Configuration
 * @Description TODO Configuration class sign
 * @Date 2023/10/14 23:03
 * @Created by 月光叶
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

    String value();

}