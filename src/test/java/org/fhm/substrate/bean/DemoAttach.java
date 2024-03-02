package org.fhm.substrate.bean;

import org.fhm.substrate.annotation.DemoTestComponent;
import org.fhm.substrate.service.LoggerHandler;
import org.fhm.substrate.standard.ILogger;

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
