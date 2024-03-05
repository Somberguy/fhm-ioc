package org.fhm.substrate.bean;

import org.fhm.substrate.annotation.BeanEnable;
import org.fhm.substrate.annotation.BeanInitial;
import org.fhm.substrate.annotation.DemoComponent;
import org.fhm.substrate.annotation.Setup;
import org.fhm.substrate.config.TestDemoConfiguration;
import org.fhm.substrate.service.LoggerHandler;
import org.fhm.substrate.standard.ILogger;

/**
 * @since 2024/2/20-2:53 PM
 * @author Somberguy
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
