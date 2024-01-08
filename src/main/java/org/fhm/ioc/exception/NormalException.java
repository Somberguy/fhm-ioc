package org.fhm.ioc.exception;

import org.fhm.ioc.ability.ILoggerHandler;
import org.fhm.ioc.service.LoggerHandler;

/**
 * @Classname FHMException
 * @Description TODO FOR HUMAN Exception
 * @Date 2023/10/14 10:24
 * @Created by 月光叶
 */
public class NormalException extends RuntimeException {

    private static final long serialVersionUID = -7034897190745766933L;
    private final ILoggerHandler logger = LoggerHandler.getLogger(NormalException.class);


    public NormalException(String msg) {
        super(msg);
        logger.error(msg);
    }

    public NormalException(String msg, Exception e) {
        super(msg, e);
        logger.error(msg, e);
    }
}
