package org.fhm.ioc.service;

import org.fhm.ioc.annotation.BeanEnable;
import org.fhm.ioc.annotation.BeanInitial;
import org.fhm.ioc.annotation.Component;
import org.fhm.ioc.annotation.Setup;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.constant.Common;
import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.standard.IStarter;
import org.fhm.ioc.util.ClazzUtil;
import org.fhm.ioc.util.IOCExceptionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Classname AbstractFactory
 * @Description TODO
 * @Date 2023/10/24-11:26 AM
 * @Author tanbo
 */
public class BeanOptimizer {

    private final ILoggerHandler logger = LoggerHandler.getLogger(BeanOptimizer.class);

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
        List<? extends Class<?>> collect = beans.values()
                .stream()
                .map(Object::getClass)
                .filter(fieldTypeClass::isAssignableFrom)
                .collect(Collectors.toList());
        if (collect.size() == 1) {
            ClazzUtil.setClazzValue(bean, beans.get(collect.get(0).getName()), field);
            return;
        }
        if (collect.size() > 1) {
            String setupName = field.getAnnotation(Setup.class).value();
            if (setupName.isEmpty()) {
                throw IOCExceptionUtil
                        .generateNormalException("There are multiple implementations " +
                                "of interfaces or abstract " +
                                "injection objects, please specify the target");
            }
            String configIdentified;
            boolean isPadding = false;
            if (setupName.contains((configIdentified = Common.CONFIG_IDENTIFIED.getName()))) {
                isPadding = true;
                setupName = setupName.replace(configIdentified, "");
                Object temp;
                Object o
                        = (temp = AbstractConfiguration.configContainer.get(setupName))
                        == null ?
                        AbstractConfiguration.defaultConfigContainer.get(setupName)
                        : temp;
                if (o == null || (setupName = o.toString()).isEmpty()) {
                    throw IOCExceptionUtil
                            .generateNormalException("There are multiple implementations " +
                                    "of interfaces or abstract " +
                                    "injection objects, please specify " +
                                    "correct configuration attributes");
                }
            }
            List<String> targetObj = new ArrayList<>();
            for (Class<?> c : collect) {
                Component annotation = c.getAnnotation(Component.class);
                if (Objects.nonNull(annotation)){
                    if (annotation.value().equals(setupName))
                        targetObj.add(c.getName());
                }else {
                    List<Annotation> annotations = Arrays
                            .stream(c.getAnnotations())
                            .filter(
                                    anno -> anno.annotationType()
                                    .isAnnotationPresent(Component.class))
                            .collect(Collectors.toList());
                    if (annotations.size() > 1)
                        throw IOCExceptionUtil.generateAutoSetupException("duplicate custom injection annotations");
                    Annotation requireAnnotation = annotations.get(0);
                    try {
                        Method value = requireAnnotation.getClass().getMethod("value");
                        Object invoke = value.invoke(requireAnnotation);
                        if (!(invoke instanceof String))
                            throw IOCExceptionUtil.generateAutoSetupException("the custom injection note value type is set incorrectly");
                        if (setupName.equals(invoke))
                            targetObj.add(c.getName());
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (targetObj.size() != 1) {
                throw IOCExceptionUtil
                        .generateNormalException("There are multiple implementations of " +
                                "interfaces or abstract injection objects, " +
                                "indicating multiple targets. Please check and try again");
            } else {
                String targetClazzName = targetObj.get(0);
                ClazzUtil.setClazzValue(bean, beans.get(targetClazzName), field);
                if (isPadding) {
                    List<String> list
                            = collect.stream()
                            .map(Class::getName)
                            .collect(Collectors.toList());
                    list.remove(targetClazzName);
                    list.forEach(beans::remove);
                }
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
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (method.getParameterCount() != 0) {
                    logger.warn("the bean optimizer method is forbidden to have parameters");
                    continue;
                }
                method.setAccessible(true);
                try {
                    method.invoke(bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error("An error is reported for the {} method of class {}", method.getName(), clazz.getName());
                    throw new RuntimeException(e);
                }
            }
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
        if ((object = beans
                .get(name)) instanceof IStarter) {
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
