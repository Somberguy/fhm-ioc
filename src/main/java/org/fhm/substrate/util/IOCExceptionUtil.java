package org.fhm.substrate.util;

import org.fhm.substrate.exception.AutoSetupException;
import org.fhm.substrate.exception.ConfigurationException;
import org.fhm.substrate.exception.NormalException;
import org.fhm.substrate.exception.ResourceScannerException;

/**
 * @since 2023/10/14 16:27
 * @author Somberguy
 */
public class IOCExceptionUtil {

    public static NormalException generateNormalException(String msg) {
        return new NormalException(msg);
    }

    public static NormalException generateNormalException(String msg, Exception e) {
        return new NormalException(msg, e);
    }

    public static NormalException generateNormalException(Exception e) {
        return new NormalException(e.getMessage(), e);
    }

    public static ResourceScannerException generateResourceScannerException(Exception e) {
        return new ResourceScannerException(e.getMessage(), e);
    }

    public static ResourceScannerException generateResourceScannerException(String message, Exception e) {
        return new ResourceScannerException(message, e);
    }


    public static ConfigurationException generateConfigurationException(String msg, Exception e) {
        return new ConfigurationException(msg, e);
    }

    public static ConfigurationException generateConfigurationException(Exception e) {
        return new ConfigurationException(e.getMessage(), e);
    }

    public static ConfigurationException generateConfigurationException(String msg) {
        return new ConfigurationException(msg);
    }


    public static AutoSetupException generateAutoSetupException(Exception e) {
        return new AutoSetupException(e.getMessage(), e);
    }

    public static AutoSetupException generateAutoSetupException(String message) {
        return new AutoSetupException(message);
    }


}
