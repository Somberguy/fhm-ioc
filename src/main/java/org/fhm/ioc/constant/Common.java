package org.fhm.ioc.constant;

import java.io.File;

/**
 * @Classname Common
 * @Description TODO Public enumeration class
 * @Date 2023/10/14 10:24
 * @Author by 月光叶
 */
public enum Common {
    CLASS_FILE_SUFFIX(".class"),
    JAR_FILE_SUFFIX(".jar"),
    PROJECT_PACKAGE_NAME("org.fhm"),
    CONFIG_IDENTIFIED("->"),
    FILTER_CLASS_FILE_SEPARATOR("target" + File.separator + "classes"),
    FILTER_TEST_CLASS_FILE_SEPARATOR("target" + File.separator + "test-classes"),
    DEFAULT_KEYWORD("default-"),
    UNKNOWN_PARADIGM_SIGNS("<[^>]*>"),
    JAR_PATH_SYSTEM("fhm.ioc.jar.path");

    final String name;

    Common(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
