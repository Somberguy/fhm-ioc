package org.fhm.substrate.manager;

import org.fhm.substrate.ability.IInitializeConfigurationObject;
import org.fhm.substrate.config.AbstractConfiguration;
import org.fhm.substrate.service.*;
import org.fhm.substrate.standard.ILogger;
import org.fhm.substrate.standard.IStarter;

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
 * <b>JSubstrate</b>'s bootstrap object.
 * @since 2023/10/14 10:02
 * @author 谭波
 */
public class Bootstrap {

    @SuppressWarnings("unused")
    public static final Charset charset = StandardCharsets.UTF_8;

    private static final String BANNER_FILE_NAME = "banner.txt";

    static {
        LoggerHandler.getInstance().initializeLoggerHandler();
    }

    private static final ILogger logger = LoggerHandler.getLogger(Bootstrap.class);

    private static List<Class<? extends Annotation>> newManageAnnotations;

    /**
     * Open <b>JSubstrate</b>.
     * @param args The entrance parameters of java.
     * @param starterClazz Customized an implementation of <b>IStarter</b>.
     */
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
        logger.info("start initialize configuration");
        initializeConfiguration();
        logger.info("start optimizing the beans");
        enableBeanOptimizer(args, starterClazz);
    }

    /**
     * Print banner.
     */
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
                Charset defaultCharset = Charset.defaultCharset();
                System.out.println(new String(text.getBytes(defaultCharset), defaultCharset));
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

    /**
     * Scan files and collect resources according to the configuration requirements of <b>JSubstrate</b>
     * @param starterClazz Customized an implementation of <b>IStarter</b>.
     */
    private static void collectConfigAndClassResource(
            Class<? extends IStarter> starterClazz
    ) {
        ResourceScanner scanner = ResourceScanner.getInstance();
        logger.info("start initialize resource scanner");
        newManageAnnotations = scanner.initialize(starterClazz);
        logger.info("start filter out the required CP");
        scanner.filterClassPath();
        logger.info("start fixed-point scanning");
        scanner.scanRequiredSystem();
        logger.info("start scan the path to obtain the required resources and class files");
        scanner.scanRequiredFileAndSetupObj();
        logger.info("start clear cache and create beans");
        scanner.clearCacheAndCreateBeans();
    }

    /**
     * Automatically assemble dependent objects for beans.
     */
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

    /**
     * Initialize configuration object.
     */
    private static void initializeConfiguration() {
        AbstractConfiguration.configObj.forEach((k, obj) -> {
            if (obj instanceof IInitializeConfigurationObject) {
                IInitializeConfigurationObject actuator = (IInitializeConfigurationObject) obj;
                actuator.initializeConfigurationObject(obj);
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

    /**
     * Enable bean optimizer.
     * @param args The entrance parameters of java.
     * @param starterClazz Customized an implementation of <b>IStarter</b>.
     */
    private static void enableBeanOptimizer(String[] args, Class<? extends IStarter> starterClazz) {
        BeanOptimizer beanOptimizer = BeanOptimizer.getInstance();
        logger.info("clear not necessary implement and cache");
        beanOptimizer.clearNotNecessaryObj();
        logger.info("start bean initial");
        beanOptimizer.beansInitial();
        logger.info("start bean enable");
        beanOptimizer.beansEnable();
        logger.info("clear cache data");
        AbstractConfiguration.clearMemory();
        printMachineCurrentStatus();
        IOCCostTimer.getInstance().endAndPrint();
        logger.info("enable project complete");
        beanOptimizer.start(args, newManageAnnotations, starterClazz);
    }

    /**
     * Print <b>JSubstrate</b>'s run details
     */
    private static void printMachineCurrentStatus() {
        Runtime runtime = Runtime.getRuntime();
        logger.info("current the number of available processors : {}", runtime.availableProcessors());
        logger.info("current maximum heap memory: {}MB", runtime.maxMemory() / 1024 / 1024);
        long costMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
        logger.info("current cost memory: {}MB {}KB", costMemory / 1024, costMemory % 1024);
    }

    public static <T> T getBean(Class<T> clazz){
        return BeanOptimizer.getInstance().getBean(clazz);
    }

}
