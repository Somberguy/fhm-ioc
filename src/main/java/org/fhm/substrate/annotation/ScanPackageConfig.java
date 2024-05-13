package org.fhm.substrate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation <b>ScanPackageConfig</b> is used to configure a required Scan-Package-Path.
 * @since 2024/1/8-1:43 PM
 * @author 谭波
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanPackageConfig {

    /**
     * @return A required Scan-Package-Path.
     */
    String[] value() default "org.fhm.**";

}
