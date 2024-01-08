package org.fhm.ioc.service;


import org.fhm.ioc.constant.Common;

import java.security.ProtectionDomain;

/**
 * @Classname ASMClassLoader
 * @Description TODO ASM class loader
 * @Date 2023/10/12 10:56
 * @Created by 月光叶
 */
public class IOCClassLoader extends ClassLoader {

    public static IOCClassLoader getInstance() {
        return Instance.instance;
    }

    public Class<?> loadByteArr(String clazz, byte[] bytes, ProtectionDomain domain) {
        clazz = clazz.replaceAll(Common.UNKNOWN_PARADIGM_SIGNS.getName(), "");
        try {
            findClass(clazz);
            return null;
        } catch (ClassNotFoundException e) {
            try {
                return loadClass(clazz);
            } catch (ClassNotFoundException ex) {
                return defineClass(
                        clazz, bytes, 0,
                        bytes.length,
                        domain
                );
            }
        }
    }

    private static final class Instance {

        public static IOCClassLoader instance = new IOCClassLoader();
    }
}
