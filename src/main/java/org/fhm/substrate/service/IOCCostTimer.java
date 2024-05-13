package org.fhm.substrate.service;

import org.fhm.substrate.standard.ILogger;

/**
 * @since 2023/12/12 20:28
 * @author 谭波
 */
public class IOCCostTimer {

    private final ILogger logger = LoggerHandler.getLogger(IOCCostTimer.class);

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
