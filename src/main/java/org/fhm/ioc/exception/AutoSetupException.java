package org.fhm.ioc.exception;

/**
 * @Classname AutoSetupException
 * @Description TODO
 * @Date 2023/10/28 12:23
 * @Author by 月光叶
 */
public class AutoSetupException extends NormalException {

    private static final long serialVersionUID = -1034897190745766133L;

    public AutoSetupException(String msg, Exception e) {
        super(msg, e);
    }

    public AutoSetupException(String msg) {
        super(msg);
    }

}
