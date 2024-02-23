package org.fhm.ioc.manager;

import org.fhm.ioc.ability.IActuator;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.service.*;
import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.standard.IStarter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @Classname AbstractBootstrap
 * @Description TODO Abstract startup class
 * @Date 2023/10/14 10:02
 * @Author by 月光叶
 */
public class Bootstrap {

    public static final Charset charset = StandardCharsets.UTF_8;

    private static final String BANNER_FILE_NAME = "banner.txt";

    private static final ILoggerHandler logger = LoggerHandler.getLogger(Bootstrap.class);

    private static List<Class<? extends Annotation>> newManageAnnotations;

    @SuppressWarnings("unused")
    public static void open(String[] args, Class<? extends IStarter> starterClazz) {
        IOCCostTimer.getInstance().start();
        printBanner();
        logger.info("read VM parameter");
        VMParametersManage.getInstance().readVMOptionsFileParameters(starterClazz);
        logger.info("start collect configuration file and class file");
        collectConfigAndClassResource(starterClazz);
        logger.info("start auto setup bean");
        autoSetupObj();
        logger.info("start initial configuration");
        initialConfiguration();
        logger.info("start optimize bean");
        executeBeanOptimizer(args, starterClazz);
    }

    private static void printBanner() {
        InputStream stream = ClassLoader.getSystemResourceAsStream(BANNER_FILE_NAME);
        if (Objects.isNull(stream)) {
            logger.warn("the banner file is missing");
            return;
        }
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            String text;
            while (Objects.nonNull((text = bufferedReader.readLine()))) {
                System.out.println(text);
            }
        } catch (IOException e) {
            logger.error("fail to print banner", e);
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }


    private static void collectConfigAndClassResource(
            Class<? extends IStarter> clazz
    ) {
        ResourceScanner scanner = ResourceScanner.getInstance();
        logger.info("start initialize resource scanner");
        newManageAnnotations = scanner.initialize(clazz);
        logger.info("start filter out the required class path");
        scanner.filterClassPath();
        logger.info("start fixed-point scanning");
        scanner.scanRequiredSystem();
        logger.info("start scan the path to obtain the required resources and class files");
        scanner.scanRequiredFileAndSetupObj();
        logger.info("start clear cache and create beans");
        scanner.clearCacheAndCreateBeans();
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


}
