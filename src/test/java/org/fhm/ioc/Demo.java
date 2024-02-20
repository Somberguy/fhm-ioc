package org.fhm.ioc;

import org.fhm.ioc.annotation.BeanEnable;
import org.fhm.ioc.annotation.BeanInitial;
import org.fhm.ioc.service.LoggerHandler;
import org.fhm.ioc.standard.ILoggerHandler;

/**
 * @Classname Demo
 * @Description TODO
 * @Date 2024/2/20-2:53 PM
 * @Author tanbo
 */
@DemoComponent("Demo")
public class Demo {

    private final ILoggerHandler logger = LoggerHandler.getLogger(Demo.class);

    public void test(){
        logger.info("demo test successful");
    }


    @BeanInitial
    private void beanInitial(){
        // The bean to do initial
        logger.info("demo start initialize");
    }

    @BeanEnable
    private void beanEnable(){
        // The bean to do enable
        logger.info("demo start enable");
    }

}
