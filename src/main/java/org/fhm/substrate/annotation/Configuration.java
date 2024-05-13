package org.fhm.substrate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The annotation <b>Configuration</b> is used to mark a configuration object that needs to be injected into the <b>IOC</b>.
 * @since 2023/10/14 23:03
 * @author 谭波
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {

    /**
     * @return Configuration object name.
     */
    String value();

}
