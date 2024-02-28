package org.fhm.ioc.bean;

import org.fhm.ioc.annotation.BeanEnable;
import org.fhm.ioc.annotation.BeanInitial;
import org.fhm.ioc.annotation.DemoComponent;
import org.fhm.ioc.annotation.Setup;
import org.fhm.ioc.config.TestDemoConfiguration;
import org.fhm.ioc.service.LoggerHandler;
import org.fhm.ioc.standard.ILogger;

/**
 * @Classname Demo
 * @Description TODO
 * @Date 2024/2/20-2:53 PM
 * @Author tanbo
 */
@DemoComponent("Demo")
public class Demo implements IDemoTest {

    private final ILogger logger = LoggerHandler.getLogger(Demo.class);

    @Setup
    private TestDemoConfiguration testDemoConfiguration;

    @Override
    public void test() {
        logger.info("demo test successful");
        logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
    }


    @BeanInitial
    private void beanInitial() throws Exception {
        // The bean to do initial
        logger.info("demo start initialize");
        logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
    }

    @BeanEnable
    private void beanEnable() throws Exception {
        // The bean to do enable
        logger.info("demo start enable");
        logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
    }

}
