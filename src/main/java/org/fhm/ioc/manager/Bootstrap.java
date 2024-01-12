package org.fhm.ioc.manager;

import org.fhm.ioc.ability.IActuator;
import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.standard.IStarter;
import org.fhm.ioc.annotation.Component;
import org.fhm.ioc.annotation.Configuration;
import org.fhm.ioc.annotation.ScanPackageConfig;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.constant.Common;
import org.fhm.ioc.service.*;
import org.fhm.ioc.util.IOCExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname AbstractBootstrap
 * @Description TODO Abstract startup class
 * @Date 2023/10/14 10:02
 * @Created by 月光叶
 */
public class Bootstrap {

    public static final Charset charset = StandardCharsets.UTF_8;

    private static final ILoggerHandler logger = LoggerHandler.getLogger(Bootstrap.class);

    private static List<Class<? extends Annotation>> newManageAnnotations;

    @SuppressWarnings("unused")
    public static void open(String[] args, Class<? extends IStarter> starterClazz) {
        IOCCostTimer.getInstance().start();
        logger.info("start initial class and resource container");
        initialClazzAndResourceContainer(starterClazz);
        logger.info("start auto setup bean");
        autoSetupObj();
        logger.info("start initial configuration");
        initialConfiguration();
        logger.info("start optimize bean");
        executeBeanOptimizer(args, starterClazz);
    }


    private static void initialClazzAndResourceContainer(
            Class<? extends IStarter> starterClazz
    ) {
        ResourceScanner scanner = ResourceScanner.getInstance();
        Class<?> mainClazz;
        String jarNameByClazz;
        if (
            Objects.nonNull((mainClazz = getMainClazz()))
            && !(jarNameByClazz = getJarNameByClazz(mainClazz)).isEmpty()
        )
            scanner.jarNames.add(jarNameByClazz);
        logger.info("start configure resource scanner");
        newManageAnnotations = obtainManageAnnotation(starterClazz);
        if (Objects.nonNull(newManageAnnotations))
            scanner.annotationClazzContainer.addAll(newManageAnnotations);
        scanner.annotationClazzContainer.add(Component.class);
        scanner.annotationClazzContainer.add(Configuration.class);
        ScanPackageConfig config;
        if (Objects.nonNull(mainClazz) && Objects.nonNull((config = mainClazz.getAnnotation(ScanPackageConfig.class))))
            scanner.scanPackage.addAll(Arrays.asList(config.value()));
        logger.info("start filter out the required resource path");
        scanner.filterRequiredPath();
        logger.info("scan the path to obtain the required resources and class files");
        scanner.scanRequiredFileAndSetupObj(AutoSetupExecutor.getInstance().getObjContainer());
    }

    private static List<Class<? extends Annotation>> obtainManageAnnotation(Class<? extends IStarter> starterClazz) {
        try {
            return starterClazz.newInstance().newManageMembers();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.warn(e);
            return null;
        }
    }

    private static void autoSetupObj() {
        AutoSetupExecutor executor = AutoSetupExecutor.getInstance();
        logger.info("initial auto setup container");
        executor.initialAutoSetupContainer();
        logger.info("auto setup obj");
        executor.autoSetup();
        logger.info("auto setup map obj");
        executor.autoSetupMapObj();
        logger.info("distribute bean");
        executor.beanDistribute();
    }

    private static void executeBeanOptimizer(String[] args, Class<? extends IStarter> starterClazz) {
        BeanOptimizer beanOptimizer = BeanOptimizer.getInstance();
        logger.info("clear not necessary implement and cache");
        beanOptimizer.clearNotNecessaryObj();
        logger.info("start bean initial");
        beanOptimizer.beansInitial();
        logger.info("start bean enable");
        beanOptimizer.beansEnable();
        logger.info("clear cache data");
        AbstractConfiguration.clearMemory();
        printMemory();
        IOCCostTimer.getInstance().endAndPrint();
        logger.info("enable project complete");
        beanOptimizer.start(args, newManageAnnotations, starterClazz);
    }

    private static void printMemory() {
        Runtime runtime = Runtime.getRuntime();
        logger.info("current maximum heap memory: {}MB", runtime.maxMemory() / 1024 / 1024);
        long costMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
        logger.info("current cost memory: {}MB {}KB", costMemory / 1024, costMemory % 1024);
    }

    private static void initialConfiguration() {
        AbstractConfiguration.configObj.forEach((k, obj) -> {
            if (obj instanceof IActuator) {
                IActuator actuator = (IActuator) obj;
                actuator.action(obj);
            }
        });
        AbstractConfiguration.resource.values().forEach(is -> {
            try {
                if (Objects.nonNull(is)) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error("failed to close stream");
            }
        });
    }

    private static Class<?> getMainClazz() {
        try {
            for (StackTraceElement element : new RuntimeException().getStackTrace()) {
                if ("main".equals(element.getMethodName())) {
                    return Class.forName(element.getClassName());
                }
            }
        } catch (ClassNotFoundException e) {
            throw IOCExceptionUtil.generateNormalException(e);
        }
        return null;
    }


    private static String getJarNameByClazz(Class<?> clazz) {
        String clazzPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (clazzPath.endsWith(Common.JAR_FILE_SUFFIX.getName())) {
            return clazzPath.substring(clazzPath.lastIndexOf(File.separator) + 1);
        } else {
            return "";
        }
    }

}
