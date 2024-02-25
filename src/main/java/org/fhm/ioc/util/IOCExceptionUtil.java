package org.fhm.ioc.util;

import org.fhm.ioc.exception.AutoSetupException;
import org.fhm.ioc.exception.ConfigurationException;
import org.fhm.ioc.exception.NormalException;
import org.fhm.ioc.exception.ResourceScannerException;

/**
 * @Classname ClazzUtil
 * @Description TODO Exception deal util
 * @Date 2023/10/14 16:27
 * @Author by 月光叶
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
