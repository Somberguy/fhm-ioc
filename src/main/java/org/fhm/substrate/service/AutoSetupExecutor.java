package org.fhm.substrate.service;

import org.fhm.substrate.annotation.Component;
import org.fhm.substrate.annotation.Configuration;
import org.fhm.substrate.annotation.Setup;
import org.fhm.substrate.config.AbstractConfiguration;
import org.fhm.substrate.constant.Common;
import org.fhm.substrate.standard.ILogger;
import org.fhm.substrate.util.ClazzUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @since 2023/10/26 22:33
 * @author 谭波
 */
public class AutoSetupExecutor {

    private final ILogger logger = LoggerHandler.getLogger(AutoSetupExecutor.class);

    public Map<Object, Map<Field, Class<?>>> setupInterfaceAndAbstractObjs;

    private Map<Object, Map<Field, Class<?>>> setupMapObjs;

    private Map<String, Object> objContainer = new HashMap<>();

    public static AutoSetupExecutor getInstance() {
        return Instance.instance;
    }

    public void initialAutoSetupContainer() {
        int size = objContainer.size();
        setupMapObjs = new HashMap<>(size, 1);
        setupInterfaceAndAbstractObjs = new HashMap<>(size, 1);
    }

    public void autoSetup() {
        objContainer.forEach((k, v) -> setupValueAndFilter(v, v.getClass(), objContainer));
        IOCClassLoader.getInstance().clearCache();
    }

    private void setupValueAndFilter(Object v, Class<?> clazz, Map<String, Object> objContainer) {
        Field[] fields = clazz.getDeclaredFields();
        Stream.of(fields).forEach(field -> {
            if (!field.isAnnotationPresent(Setup.class))
                return;
            Class<?> fieldType;
            if (Map.class.isAssignableFrom((fieldType = field.getType()))) {
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                    if (
                            actualTypeArguments.length == 2 &&
                                    "java.lang.String".equals(actualTypeArguments[0].getTypeName())
                    ) {
                        Class<?> abstractAndInterface = IOCClassLoader
                                .getInstance()
                                .getAbstractAndInterface(
                                        actualTypeArguments[1]
                                                .getTypeName()
                                                .replaceAll(
                                                        Common.UNKNOWN_PARADIGM_SIGNS.getName(),
                                                        ""
                                                )
                                );
                        if (
                                objContainer.values().stream()
                                        .anyMatch(o -> abstractAndInterface.isAssignableFrom(o.getClass()))
                        ) {
                            setupMapObjs.computeIfAbsent(v, m -> new HashMap<>())
                                    .put(field, abstractAndInterface);
                        }
                    }
                }
            } else if (Modifier.isAbstract(fieldType.getModifiers()) || fieldType.isInterface()) {
                if (v.getClass().isAnnotationPresent(Configuration.class)) {
                    logger.warn("configuration obj can not inject class by interface or abstract");
                } else {
                    setupInterfaceAndAbstractObjs
                            .computeIfAbsent(
                                    v,
                                    map -> new HashMap<>()
                            )
                            .put(field, fieldType);
                }
            } else {
                ClazzUtil.setClazzValue(v, objContainer.get(field.getType().getName()), field);
            }
        });
        Class<?> superClazz;
        if (!(superClazz = clazz.getSuperclass()).getName().equals(Object.class.getName())) {
            setupValueAndFilter(v, superClazz, objContainer);
        }
    }

    public void autoSetupMapObj() {
        setupMapObjs.forEach((o, map) -> map.forEach((field, c) -> {
            Map<String, Object> objMap = new HashMap<>();
            objContainer.values()
                    .forEach(obj -> {
                        Class<?> objC;
                        if (c.isAssignableFrom((objC = obj.getClass()))) {
                            Component component;
                            String name;
                            if ((component = objC.getAnnotation(Component.class)) != null
                                    && !(name = component.value()).isEmpty()
                            ) {
                                objMap.put(name, obj);
                            } else {
                                objMap.put(objC.getName(), obj);
                            }
                        }
                    });
            ClazzUtil.setClazzValue(o, objMap, field);
        }));
    }


    public void beanDistribute() {
        objContainer.forEach((k, v) -> {
            if (v instanceof AbstractConfiguration) {
                AbstractConfiguration.configObj.put(k, v);
            } else {
                BeanOptimizer.getInstance().addBean(k, v);
            }
        });
        objContainer = null;
    }

    public Map<String, Object> getObjContainer() {
        return objContainer;
    }

    private static final class Instance {
        private static final AutoSetupExecutor instance = new AutoSetupExecutor();
    }
}
