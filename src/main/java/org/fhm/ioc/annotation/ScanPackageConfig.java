package org.fhm.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname ScanPackageConfig
 * @Description TODO
 * @Date 2024/1/8-1:43 PM
 * @Author tanbo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanPackageConfig {

    String[] value() default "org.fhm.**";

}
