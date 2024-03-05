package org.fhm.substrate.bean;

import org.fhm.substrate.annotation.DemoTestComponent;
import org.fhm.substrate.service.LoggerHandler;
import org.fhm.substrate.standard.ILogger;

/**
 * @since 2024/2/20-4:22 PM
 * @author Somberguy
 */
@DemoTestComponent("DemoAttach")
public class DemoAttach implements IDemoTest {

    private final ILogger logger = LoggerHandler.getLogger(DemoAttach.class);

    @Override
    public void test() {
        logger.info("demoAttach test successful");
    }


}
