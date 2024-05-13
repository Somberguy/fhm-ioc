package org.fhm.substrate.service;

/**
 * @since 2024/2/28-4:08 PM
 * @author 谭波
 */
public class LoggerHandler extends AbstractLoggerHandler{

    @Override
    public void initializeLoggerHandler() {
        AbstractLoggerHandler.create = CommonLogger::new;
    }

    public static AbstractLoggerHandler getInstance(){
        return Instance.instance;
    }
    private static final class Instance{
        private static final AbstractLoggerHandler instance = new LoggerHandler();
    }
}
