package org.fhm.ioc.service;

import org.fhm.ioc.ability.ILoggerHandler;
import org.fhm.ioc.asm.OptimizeASMTransformer;
import org.fhm.ioc.asm.TransformerNode;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.constant.Common;
import org.fhm.ioc.util.IOCExceptionUtil;
import org.fhm.ioc.util.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

/**
 * @Classname ClassFileScanner
 * @Description TODO
 * @Date 2023/10/22 14:49
 * @Created by 月光叶
 */
public class ResourceScanner {

    private final ILoggerHandler logger = LoggerHandler.getLogger(ResourceScanner.class);

    private final Set<String> classLoaderRecord = new HashSet<>();
    public Set<String> urls = new HashSet<>();

    public Set<String> scanPackage = new HashSet<>();

    public Set<Class<? extends Annotation>> annotationClazzContainer = new HashSet<>(2);

    public Set<String> jarNames = new HashSet<>(1);

    public static ResourceScanner getInstance() {
        return Instance.instance;
    }

    public void filterRequiredPath() {
        for (String url : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (
                    url.contains(Common.PROJECT_FILE_FLAG.getName())
            ) {
                urls.add(url);
            }
        }
    }

    public void scanRequiredFileAndSetupObj(Map<String, Object> objContainer) {
        logger.info("start to obtain the class files of CP");
        scanCPClassResource(objContainer);
        logger.info("start to obtain the class files in nested packages");
        scanJarResource(objContainer);
    }

    private void scanCPClassResource(Map<String, Object> objContainer) {
        urls.forEach(url -> {
            File file = new File(url);
            if (file.isFile() && isRequiredJar(file.getName())) {
                dealJarFile(file, objContainer);
            }
            if (file.isDirectory()) {
                try {
                    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                            File pathFile = path.toFile();
                            String fileName = pathFile.getName();
                            if (pathFile.exists() && pathFile.isFile()) {
                                dealClassFile(pathFile, objContainer);
                                if (isRequiredFile(fileName)) {
                                    AbstractConfiguration.resource.put(
                                            fileName,
                                            Files.newInputStream(
                                                    Paths.get(pathFile.getAbsolutePath())
                                            )
                                    );
                                }
                                if (isRequiredJar(fileName)) {
                                    dealJarFile(pathFile, objContainer);
                                }
                            }
                            return super.visitFile(path, attrs);
                        }
                    });
                } catch (IOException e) {
                    logger.error("failed to obtain file stream {}", url);
                    throw IOCExceptionUtil.generateResourceScannerException(e);
                }
            }
        });
    }

    private void scanJarResource(Map<String, Object> objContainer) {
        if (scanPackage.isEmpty())
            scanPackage.add(Common.PROJECT_PACKAGE_NAME.getName());
        scanPackage.forEach(packageName -> {
            Enumeration<URL> systemResources;
            try {
                systemResources = ClassLoader.getSystemResources(packageName);
            } catch (IOException e) {
                throw IOCExceptionUtil.generateResourceScannerException(e);
            }
            while (systemResources.hasMoreElements()) {
                URL url = systemResources.nextElement();
                findClassJar(url, objContainer);
            }
        });
    }

    private void findClassJar(URL url, Map<String, Object> objContainer) {
        JarFile jarFile = null;
        URLConnection urlConnection = null;
        try {
            if (Objects.nonNull(url)) {
                urlConnection = url.openConnection();
            }
            if (urlConnection instanceof JarURLConnection) {
                JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                jarFile = jarURLConnection.getJarFile();
            }
        } catch (IOException e) {
            logger.error("failed to obtain package {} .jar resource", url, e);
            throw IOCExceptionUtil.generateResourceScannerException(e);
        }
        if (Objects.nonNull(jarFile)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (jarEntryName.endsWith(Common.CLASS_FILE_SUFFIX.getName())) {
                    dealInnerJarClassFile(objContainer, jarEntry, jarFile);
                }
                if (isRequiredFile(jarEntryName) && AbstractConfiguration.resource.get(jarEntryName) == null) {
                    try {
                        AbstractConfiguration.resource.put(
                                jarEntryName,
                                jarFile.getInputStream(jarEntry)
                        );
                    } catch (IOException e) {
                        logger.error("failed to obtain current resource {} stream", jarEntry.getName(), e);
                        throw IOCExceptionUtil.generateResourceScannerException(e);
                    }
                }
            }
        }
    }

    private void dealJarFile(File pathFile, Map<String, Object> objContainer) {
        try (JarFile jarFile = new JarFile(pathFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String jarEntryName = jarEntry.getName();
                dealInnerJarClassFile(objContainer, jarEntry, jarFile);
                if (isRequiredFile(jarEntryName)) {
                    AbstractConfiguration.resource.put(
                            pathFile.getName(),
                            jarFile.getInputStream(jarEntry)
                    );
                }
            }
        } catch (IOException e) {
            logger.error("failed to obtain the jar stream of path {}", pathFile, e);
            throw IOCExceptionUtil.generateResourceScannerException(e);
        }
    }

    private void dealInnerJarClassFile(Map<String, Object> objContainer, @NotNull JarEntry jarEntry, JarFile jarFile) {
        if (jarEntry.getName().endsWith(Common.CLASS_FILE_SUFFIX.getName())) {
            try {
                collectManagementObjects(
                        IOUtil.inStreamToByte(jarFile.getInputStream(jarEntry)),
                        annotationClazzContainer,
                        objContainer);
            } catch (IOException e) {
                logger.error("failed to obtain current resource {} stream", jarEntry.getName(), e);
                throw IOCExceptionUtil.generateResourceScannerException(e);
            }
        }
    }

    private void dealClassFile(@NotNull File pathFile, Map<String, Object> objContainer) {
        if (
                pathFile.isFile() &&
                        pathFile.getName()
                                .endsWith(Common.CLASS_FILE_SUFFIX.getName())
        ) {
            try {
                collectManagementObjects(
                        IOUtil.file2Bytes(pathFile.getAbsolutePath()),
                        annotationClazzContainer,
                        objContainer
                );
            } catch (Exception e) {
                logger.error("failed to obtain current resource {} stream", pathFile, e);
                throw IOCExceptionUtil.generateResourceScannerException(e);
            }
        }
    }

    private boolean isRequiredJar(@NotNull String fileName) {
        return fileName.endsWith(Common.JAR_FILE_SUFFIX.getName())
                &&
                jarNames.stream().anyMatch(fileName::contains);
    }

    private boolean isRequiredFile(@NotNull String fileName) {
        return fileName.endsWith(".properties") && !fileName.contains("pom.properties");
    }

    public void collectManagementObjects(
            byte[] bytes,
            Set<Class<? extends Annotation>> annotations,
            Map<String, Object> objContainer) {
        AtomicReference<String> clazzName = new AtomicReference<>("");
        OptimizeASMTransformer.getInstance().init(bytes).create(
                new TransformerNode(
                        ASM9,
                        null,
                        cn -> {
                            int access = cn.access;
                            if ((access & ACC_INTERFACE) != 0 && (access & ACC_ABSTRACT) != 0) {
                                return;
                            }
                            if (Objects.nonNull(annotations) && !annotations.isEmpty()) {
                                List<AnnotationNode> visibleAnnotations;
                                if ((visibleAnnotations = cn.visibleAnnotations) == null) {
                                    return;
                                }
                                if (
                                        annotations.stream()
                                                .map(Class::getName)
                                                .allMatch(
                                                        name ->
                                                                visibleAnnotations
                                                                        .stream()
                                                                        .map(an -> an.desc.replace("/", "."))
                                                                        .map(an -> an.substring(1, an.length() - 1))
                                                                        .noneMatch(name::equals)
                                                )
                                ) {
                                    return;
                                }
                            }
                            if (cn.methods.stream().noneMatch(m -> m.name.equals("<init>")
                                    && m.desc.equals("()V") && m.access == ACC_PUBLIC)) {
                                logger.warn("class {} has no unmanaged parameterless constructor", cn.name);
                                return;
                            }
                            clazzName.set(Type.getObjectType(cn.name).getClassName());
                        }
                )
        );
        String clazz;
        if ((clazz = clazzName.get()).isEmpty() || classLoaderRecord.contains(clazz)) {
            return;
        }
        try {
            Class<?> srcClass = Class.forName(clazz);
            Class<?> aClass = IOCClassLoader
                    .getInstance()
                    .loadByteArr(
                            clazz,
                            bytes,
                            srcClass.getProtectionDomain()
                    );
            if (Objects.nonNull(aClass)) {
                objContainer
                        .put(
                                clazz,
                                aClass
                                        .getConstructor().newInstance()
                        );
                classLoaderRecord.add(clazz);
            } else {
                logger.warn(clazz + " has been loaded");
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | ClassNotFoundException ignore) {
        }
    }

    private static final class Instance {
        private static final ResourceScanner instance = new ResourceScanner();
    }

}
