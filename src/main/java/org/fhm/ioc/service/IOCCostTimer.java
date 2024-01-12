package org.fhm.ioc.service;

import org.fhm.ioc.standard.ILoggerHandler;

/**
 * @Classname IOCCostTimer
 * @Description TODO
 * @Date 2023/12/12 20:28
 * @Created by 月光叶
 */
public class IOCCostTimer {

    private final ILoggerHandler logger = LoggerHandler.getLogger(IOCCostTimer.class);

    private long start;

    public static IOCCostTimer getInstance() {
        return IOCCostTimer.Instance.instance;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void endAndPrint() {
        long costTime = System.currentTimeMillis() - start;
        logger.info("enable project cost: {}s {}ms", costTime / 1000, costTime % 1000);
    }

    private static final class Instance {
        private static final IOCCostTimer instance = new IOCCostTimer();
    }

}
