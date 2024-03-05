package org.fhm.substrate.constant;

import java.io.File;

/**
 * @since 2023/10/14 10:24
 * @author Somberguy
 */
public enum Common {
    CLASS_FILE_SUFFIX(".class"),
    JAR_FILE_SUFFIX(".jar"),
    PROJECT_PACKAGE_NAME("org.fhm.**"),
    CONFIG_IDENTIFIED("->"),
    FILTER_CLASS_FILE_SEPARATOR("target" + File.separator + "classes"),
    FILTER_TEST_CLASS_FILE_SEPARATOR("target" + File.separator + "test-classes"),
    DEFAULT_KEYWORD("default-"),
    UNKNOWN_PARADIGM_SIGNS("<[^>]*>"),
    JAR_PATH_SYSTEM("fhm.substrate.jar.path");

    final String name;

    Common(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
