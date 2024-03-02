package org.fhm.substrate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname DemoTestComponent
 * @Description TODO
 * @Date 2024/2/20-5:13 PM
 * @Author tanbo
 */
@Component // Specify a custom annotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DemoTestComponent {

    String value();
}
