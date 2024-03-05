package org.fhm.substrate.standard;

/**
 * @since 2023/12/19-11:34 AM
 * @author Somberguy
 */
public interface ILogger {

    void info(String message, Object... parameters);

    void info(Object message, Object... parameters);

    void warn(String message, Object... parameters);

    void warn(Object message, Object... parameters);

    void debug(String message, Object... parameters);

    void debug(Object message, Object... parameters);

    void error(Exception e, String message);

    void error(String message, Object... parameters);


}
