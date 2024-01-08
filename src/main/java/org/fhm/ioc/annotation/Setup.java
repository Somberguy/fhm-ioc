package org.fhm.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Setup
 * @Description TODO
 * @Date 2023/10/24-3:33 PM
 * @Author tanbo
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Setup {
    String value() default "";
}
