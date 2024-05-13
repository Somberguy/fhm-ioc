package org.fhm.substrate.exception;

import org.fhm.substrate.service.LoggerHandler;
import org.fhm.substrate.standard.ILogger;

/**
 * @since 2023/10/14 10:24
 * @author 谭波
 */
public class NormalException extends RuntimeException {

    private static final long serialVersionUID = -7034897190745766933L;
    private final ILogger logger = LoggerHandler.getLogger(NormalException.class);


    public NormalException(String msg) {
        super(msg);
        logger.error(msg);
    }

    public NormalException(String msg, Exception e) {
        super(msg, e);
        logger.error(msg, e);
    }
}
