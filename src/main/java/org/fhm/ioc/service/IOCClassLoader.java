package org.fhm.ioc.service;


import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @Classname ASMClassLoader
 * @Description TODO ASM class loader
 * @Date 2023/10/12 10:56
 * @Author by 月光叶
 */
public class IOCClassLoader {

    private List<URL> urlContainer = new ArrayList<>();

    private Set<String> clazzNamesContainer = new HashSet<>();

    private Map<String, Class<?>> abstractAndInterface = new HashMap<>();

    public static IOCClassLoader getInstance() {
        return Instance.instance;
    }

    public void loadClass(Map<String, Object> objContainer) {
        URL[] urls = urlContainer.toArray(new URL[]{});
        try (URLClassLoader loader = new URLClassLoader(urls)) {
            for (String clazzName : clazzNamesContainer) {
                Constructor<?> constructor = loader.loadClass(clazzName).getDeclaredConstructor();
                constructor.setAccessible(true);
                objContainer.put(clazzName, constructor.newInstance());
            }
            for (String className : abstractAndInterface.keySet()) {
                abstractAndInterface.put(className, loader.loadClass(className));
            }
        } catch (Exception ignored) {
        }
    }

    public void put(URL url, String className) {
        urlContainer.add(url);
        clazzNamesContainer.add(className);
    }

    public void putAbstractAndInterface(String className) {
        abstractAndInterface.put(className, null);
    }

    public Class<?> getAbstractAndInterface(String className) {
        return abstractAndInterface.get(className);
    }

    public boolean isAddedClazz(String clazzName) {
        return clazzNamesContainer.contains(clazzName);
    }

    public void clearCache() {
        urlContainer = null;
        clazzNamesContainer = null;
        abstractAndInterface = null;
    }

    private static final class Instance {
        private static final IOCClassLoader instance = new IOCClassLoader();
    }
}
