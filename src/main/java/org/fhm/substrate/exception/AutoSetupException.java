package org.fhm.substrate.exception;

/**
 * @since 2023/10/28 12:23
 * @author 谭波
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
