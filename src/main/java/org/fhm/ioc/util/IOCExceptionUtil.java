package org.fhm.ioc.util;

import org.fhm.ioc.exception.AutoSetupException;
import org.fhm.ioc.exception.ConfigurationException;
import org.fhm.ioc.exception.NormalException;
import org.fhm.ioc.exception.ResourceScannerException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @Classname ClazzUtil
 * @Description TODO Exception deal util
 * @Date 2023/10/14 16:27
 * @Created by 月光叶
 */
public class IOCExceptionUtil {

    @Contract("_ -> new")
    public static @NotNull NormalException generateNormalException(String msg) {
        return new NormalException(msg);
    }

    @Contract("_, _ -> new")
    public static @NotNull NormalException generateNormalException(String msg, Exception e) {
        return new NormalException(msg, e);
    }

    @Contract("_ -> new")
    public static @NotNull NormalException generateNormalException(Exception e) {
        return new NormalException(e.getMessage(), e);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceScannerException generateResourceScannerException(Exception e) {
        return new ResourceScannerException(e.getMessage(), e);
    }


    @Contract("_, _ -> new")
    public static @NotNull ConfigurationException generateConfigurationException(String msg, Exception e) {
        return new ConfigurationException(msg, e);
    }

    @Contract("_ -> new")
    public static @NotNull ConfigurationException generateConfigurationException(Exception e) {
        return new ConfigurationException(e.getMessage(), e);
    }

    @Contract("_ -> new")
    public static @NotNull ConfigurationException generateConfigurationException(String msg) {
        return new ConfigurationException(msg);
    }


    @Contract("_ -> new")
    public static @NotNull AutoSetupException generateAutoSetupException(Exception e) {
        return new AutoSetupException(e.getMessage(), e);
    }

    @Contract("_ -> new")
    public static @NotNull AutoSetupException generateAutoSetupException(String message) {
        return new AutoSetupException(message);
    }


}
