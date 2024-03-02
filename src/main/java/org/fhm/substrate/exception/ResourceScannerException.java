package org.fhm.substrate.exception;

/**
 * @Classname ResourceScannerException
 * @Description TODO
 * @Date 2023/10/28 11:01
 * @Author by 月光叶
 */
public class ResourceScannerException extends NormalException {

    private static final long serialVersionUID = -7034897190745766934L;

    public ResourceScannerException(String msg, Exception e) {
        super(msg, e);
    }

}
