package org.fhm.substrate.exception;

/**
 * @since 2023/10/28 11:40
 * @author Somberguy
 */
public class ConfigurationException extends NormalException {

    private static final long serialVersionUID = -7034897190745766133L;

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String msg, Exception e) {
        super(msg, e);
    }
}
