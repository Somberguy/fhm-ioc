package org.fhm.ioc.bean;

import org.fhm.ioc.annotation.DemoTestComponent;
import org.fhm.ioc.service.LoggerHandler;
import org.fhm.ioc.standard.ILogger;

/**
 * @Classname Demo2
 * @Description TODO
 * @Date 2024/2/20-4:22 PM
 * @Author tanbo
 */
@DemoTestComponent("DemoAttach")
public class DemoAttach implements IDemoTest {

    private final ILogger logger = LoggerHandler.getLogger(DemoAttach.class);

    @Override
    public void test() {
        logger.info("demoAttach test successful");
    }


}
