package org.fhm.ioc.service;


import org.fhm.ioc.constant.Common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname ASMClassLoader
 * @Description TODO ASM class loader
 * @Date 2023/10/12 10:56
 * @Author by 月光叶
 */
public class IOCClassLoader extends ClassLoader {

    private static final Map<URL[], URLClassLoader> classLoaderContainer = new HashMap<>();

    public static Class<?> loadClass(URL url, String className) throws ClassNotFoundException, MalformedURLException {
        URL[] urls = {url};
        return classLoaderContainer.computeIfAbsent(
                urls, k -> new URLClassLoader(urls)
        ).loadClass(className);
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

    public static IOCClassLoader getInstance(){
        return Instance.instance;
    }

    private static final class Instance {
        private static final IOCClassLoader instance = new IOCClassLoader();
    }
}
