package org.fhm.substrate.service;

/**
 * @Classname LoggerHandler
 * @Description TODO
 * @Date 2024/2/28-4:08 PM
 * @Author tanbo
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
