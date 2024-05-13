package org.fhm.substrate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation <b>BeanEnable</b> is used to mark Enable-Method of bean.
 * @since 2023/11/29-5:10 PM
 * @author 谭波
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeanEnable {
}
