package org.fhm.substrate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname Value
 * @Description TODO Configuration annotation
 * @Date 2023/10/14 11:11
 * @Author by 月光叶
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

    String value();

}
