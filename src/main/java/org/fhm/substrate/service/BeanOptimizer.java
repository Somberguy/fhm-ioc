package org.fhm.substrate.service;

import org.fhm.substrate.annotation.BeanEnable;
import org.fhm.substrate.annotation.BeanInitial;
import org.fhm.substrate.annotation.Component;
import org.fhm.substrate.annotation.Setup;
import org.fhm.substrate.config.AbstractConfiguration;
import org.fhm.substrate.constant.Common;
import org.fhm.substrate.standard.ILogger;
import org.fhm.substrate.standard.IStarter;
import org.fhm.substrate.util.ClazzUtil;
import org.fhm.substrate.util.IOCExceptionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @since 2023/10/24-11:26 AM
 * @author Somberguy
 */
public class BeanOptimizer {

    private final ILogger logger = LoggerHandler.getLogger(BeanOptimizer.class);

    private final Map<String, Object> beans = new HashMap<>();

    public static BeanOptimizer getInstance() {
        return Instance.instance;
    }

    public void clearNotNecessaryObj() {
        AutoSetupExecutor.getInstance().setupInterfaceAndAbstractObjs
                .forEach((bean, m)
                        -> m.forEach((field, fieldTypeClass) ->
                        findFieldAndSetValue(field, fieldTypeClass, bean)
                ));
    }

    private void findFieldAndSetValue(Field field, Class<?> fieldTypeClass, Object bean) {
        List<? extends Class<?>> requireClasses = filterRequireBeanClazz(fieldTypeClass);
        if (requireClasses.isEmpty()) {
            logger.warn("the field {} of the bean {} does not have a suitable object mounted to the IOC", field.getName(), bean);
            return;
        }
        setupMember(field, bean, requireClasses, getSetupName(field, requireClasses.size()));
    }

    private List<? extends Class<?>> filterRequireBeanClazz(Class<?> fieldTypeClass) {
        return beans.values()
                .stream()
                .map(Object::getClass)
                .filter(fieldTypeClass::isAssignableFrom)
                .collect(Collectors.toList());
    }

    private String getSetupName(Field field, int size) {
        String setupName = field.getAnnotation(Setup.class).value();
        if ((Objects.isNull(setupName) || setupName.isEmpty()) && size > 1)
            throw IOCExceptionUtil
                    .generateNormalException("there are multiple implementations " +
                            "of interfaces or abstract " +
                            "injection objects, please specify the target");
        String configIdentified;
        if (setupName.contains((configIdentified = Common.CONFIG_IDENTIFIED.getName()))) {
            setupName = setupName.replace(configIdentified, "");
            Object temp;
            Object o
                    = (temp = AbstractConfiguration.configContainer.get(setupName))
                    == null ?
                    AbstractConfiguration.defaultConfigContainer.get(setupName)
                    : temp;
            if (o == null || (setupName = o.toString()).isEmpty()) {
                throw IOCExceptionUtil
                        .generateNormalException("there are multiple implementations " +
                                "of interfaces or abstract " +
                                "injection objects, please specify " +
                                "correct configuration attributes");
            }
        }
        return setupName;
    }

    private void setupMember(Field field, Object bean, List<? extends Class<?>> collect, String setupName) {
        if (collect.size() == 1) {
            String requireBeanName;
            Class<?> requireBeanClazz = collect.get(0);
            if (!setupName.isEmpty()
                    && !(requireBeanName = getBeanInjectName(requireBeanClazz)).isEmpty()
                    && !requireBeanName.equals(setupName)
            )
                throw IOCExceptionUtil
                        .generateAutoSetupException("the load name " + setupName + " of the bean is inconsistent with " +
                                "the injection name " + requireBeanName);
            ClazzUtil.setClazzValue(bean, beans.get(requireBeanClazz.getName()), field);
        } else {
            List<String> targetObj = new ArrayList<>();
            for (Class<?> c : collect) {
                String beanName = getBeanInjectName(c);
                if (setupName.equals(beanName))
                    targetObj.add(c.getName());
            }
            if (targetObj.isEmpty())
                throw IOCExceptionUtil.generateAutoSetupException("fail to setup who the field " +
                        field.getName() + " of the object " + bean.getClass().getName());
            if (targetObj.size() > 1)
                throw IOCExceptionUtil
                        .generateNormalException("there are multiple implementations of " +
                                "interfaces or abstract injection objects, " +
                                "indicating multiple targets. Please check and try again");
            String targetClazzName = targetObj.get(0);
            ClazzUtil.setClazzValue(bean, beans.get(targetClazzName), field);
        }
    }


    private String getBeanInjectName(Class<?> c) {
        Component annotation = c.getAnnotation(Component.class);
        if (Objects.nonNull(annotation)) {
            return annotation.value();
        } else {
            List<Annotation> annotations = Arrays
                    .stream(c.getAnnotations())
                    .filter(
                            anno -> anno.annotationType()
                                    .isAnnotationPresent(Component.class))
                    .collect(Collectors.toList());
            if (annotations.size() > 1)
                throw IOCExceptionUtil.generateAutoSetupException(c.getName() + " duplicate custom injection annotations");
            Annotation requireAnnotation = annotations.get(0);
            try {
                Method value = requireAnnotation.getClass().getMethod("value");
                Object invoke = value.invoke(requireAnnotation);
                if (Objects.isNull(invoke))
                    return "";
                if (!(invoke instanceof String))
                    throw IOCExceptionUtil.generateAutoSetupException("the custom injection note value type is set incorrectly");
                return invoke.toString();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void beansInitial() {
        beans.values().forEach(o -> invokeOptimizeMethod(o.getClass(), o, BeanInitial.class));
    }

    public void beansEnable() {
        beans.values().forEach(o -> invokeOptimizeMethod(o.getClass(), o, BeanEnable.class));
    }

    private void invokeOptimizeMethod(Class<?> clazz, Object bean, Class<? extends Annotation> annotation) {
        List<Method> requireMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .collect(Collectors.toList());
        if (requireMethods.isEmpty())
            return;
        if (requireMethods.size() > 1)
            throw IOCExceptionUtil.generateAutoSetupException("beans prohibit repetition of lifecycle methods");
        Method method = requireMethods.get(0);
        if (method.getParameterCount() != 0)
            throw IOCExceptionUtil.generateAutoSetupException("lifecycle methods are forbidden to have parameters");
        if (!void.class.isAssignableFrom(method.getReturnType()))
            throw IOCExceptionUtil.generateAutoSetupException("lifecycle methods are forbidden to have returns");
        method.setAccessible(true);
        try {
            method.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("An error is reported for the {} method of class {}", method.getName(), clazz.getName());
            throw new RuntimeException(e);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (Objects.nonNull(superclass)) {
            invokeOptimizeMethod(superclass, bean, annotation);
        }
    }

    public void start(
            String[] args,
            List<Class<? extends Annotation>> newManageAnnotations,
            Class<? extends IStarter> starter
    ) {
        Object object;
        String name = starter.getName();
        if ((object = beans.get(name)) instanceof IStarter) {
            IStarter start = (IStarter) object;
            if (Objects.nonNull(newManageAnnotations)) {
                newManageAnnotations.forEach(anno -> {
                    List<Object> beanObjs = new ArrayList<>();
                    beans.values().forEach(bean -> {
                        if (bean.getClass().isAnnotationPresent(anno)) {
                            beanObjs.add(bean);
                        }
                    });
                    if (!beanObjs.isEmpty())
                        start.manageNotify(beanObjs, anno);
                });
            }
            try {
                start.start(args);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                try {
                    start.close();
                } catch (Exception ex) {
                    logger.warn("starter close error", ex);
                }
            }
        } else {
            String tip = name + " starter is not managed";
            logger.error(IOCExceptionUtil.generateNormalException(tip), tip);
            System.exit(0);
        }
    }


    public void addBean(String key, Object o) {
        beans.put(key, o);
    }


    private static final class Instance {
        private static final BeanOptimizer instance = new BeanOptimizer();
    }


}
