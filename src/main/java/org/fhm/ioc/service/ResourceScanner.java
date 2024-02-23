package org.fhm.ioc.service;

import org.fhm.ioc.annotation.Component;
import org.fhm.ioc.annotation.Configuration;
import org.fhm.ioc.annotation.ScanPackageConfig;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.constant.Common;
import org.fhm.ioc.constant.VMParameters;
import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.standard.IStarter;
import org.fhm.ioc.util.IOCExceptionUtil;
import org.fhm.ioc.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

/**
 * @Classname ClassFileScanner
 * @Description TODO
 * @Date 2023/10/22 14:49
 * @Author by 月光叶
 */
public class ResourceScanner {

    private final ILoggerHandler logger = LoggerHandler.getLogger(ResourceScanner.class);

    private Set<String> urls = new HashSet<>();

    private Set<String> requirePackageNames = new HashSet<>();

    private Set<String> rangePackageNames = new HashSet<>();

    private Set<Class<? extends Annotation>> annotationClazzContainer = new HashSet<>(2);

    private String ownerJarName;

    {
        this.annotationClazzContainer.add(Component.class);
        this.annotationClazzContainer.add(Configuration.class);
    }

    public static ResourceScanner getInstance() {
        return Instance.instance;
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

    private void getJarNameByClazz(Class<?> clazz) {
        String clazzPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (clazzPath.endsWith(Common.JAR_FILE_SUFFIX.getName())) {
            ownerJarName = clazzPath.substring(clazzPath.lastIndexOf(File.separator) + 1);
        }
    }

    public List<Class<? extends Annotation>> initialize(Class<? extends IStarter> starterClazz) {
        Class<?> mainClazz;
        if (
            Objects.nonNull((mainClazz = getMainClazz()))
        )
            getJarNameByClazz(mainClazz);
        logger.info("start configure resource scanner");
        List<Class<? extends Annotation>> newManageAnnotations = obtainManageAnnotation(starterClazz);
        if (Objects.nonNull(newManageAnnotations))
            this.annotationClazzContainer.addAll(newManageAnnotations);
        if (Objects.nonNull(mainClazz)){
            ScanPackageConfig packageConfig;
            String[] packageNames;
            if (Objects.isNull((packageConfig = mainClazz.getAnnotation(ScanPackageConfig.class)))
                    || (packageNames = packageConfig.value()).length == 0){
                String defaultPackageName;
                this.rangePackageNames.add((defaultPackageName = Common.PROJECT_PACKAGE_NAME.getName())
                        .replace(".", "/"));
                this.requirePackageNames.add(defaultPackageName.replace(".", File.separator));
            } else {

            }
        }
        return newManageAnnotations;
    }

    public void filterClassPath() {
        for (String url : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (
                url.contains(Common.FILTER_CLASS_FILE_SEPARATOR.getName())
                    || url.contains(Common.FILTER_TEST_CLASS_FILE_SEPARATOR.getName())
                    || (
                        url.endsWith(Common.JAR_FILE_SUFFIX.getName())
                            && Objects.nonNull(ownerJarName)
                            && !ownerJarName.isEmpty()
                            && url.contains(ownerJarName)
                    )
            ) {
                urls.add(url);
            }
        }
    }

    public void scanRequiredSystem() {
        VMParameters.REGISTRY_BEAN_DIR_PATH.use((name, dirPath) -> {
            File file = new File(dirPath);
            if (file.exists() && file.isDirectory()) {
                logger.info("start obtain the class file {} of VM system", dirPath);
                File[] files = file.listFiles();
                if (Objects.nonNull(files))
                    for (File jarFile : files) {
                        if (jarFile.getAbsolutePath().endsWith(Common.JAR_FILE_SUFFIX.getName()))
                            dealJarFile(jarFile, VMParameters.REGISTRY_PACKAGE_NAME.getValue());
                    }
            }
        });
    }

    public void scanRequiredFileAndSetupObj() {
        collectResourceByClassPath();
        collectResourceByURL();
    }

    private void collectResourceByClassPath() {
        urls.forEach(
                url -> {
                    File file = new File(url);
                    if (file.exists()) {
                        if (file.isDirectory())
                            dealDirectory(file);
                        if (file.isFile()) {
                            if (isRequiredClazzFile(file))
                                dealClassFile(file);
                            if (isRequiredJar(file.getName()))
                                dealJarFile(file, "");
                        }
                    }
                }
        );
    }

    private List<Class<? extends Annotation>> obtainManageAnnotation(Class<? extends IStarter> starterClazz) {
        try {
            return starterClazz.newInstance().newManageMembers();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.warn(e);
            return null;
        }
    }

    private void collectResourceByURL() {
        rangePackageNames.forEach(packageName -> {
                            Enumeration<URL> systemResources;
                            try {
                                systemResources = ClassLoader.getSystemResources(packageName);
                            } catch (IOException e) {
                                throw IOCExceptionUtil.generateResourceScannerException(e);
                            }
                            while (systemResources.hasMoreElements()) {
                                URL url = systemResources.nextElement();
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
                                        InputStream jarInputStream;
                                        try {
                                            jarInputStream = jarFile.getInputStream(jarEntry);
                                        } catch (IOException e) {
                                            logger.error("failed to obtain current resource {} stream", jarEntry.getName(), e);
                                            throw IOCExceptionUtil.generateResourceScannerException(e);
                                        }
                                        if (jarEntry.getName().endsWith(Common.CLASS_FILE_SUFFIX.getName())) {
                                            collectManagementObjects(
                                                    url,
                                                    IOUtil.inStreamToByte(jarInputStream),
                                                    annotationClazzContainer
                                            );
                                        }
                                        if (isRequiredResourceFile(jarEntryName) && Objects.isNull(AbstractConfiguration.resource.get(jarEntryName))) {
                                            AbstractConfiguration.resource.put(
                                                    jarEntryName,
                                                    jarInputStream
                                            );
                                        }
                                    }
                                }
                            }
                        }
                );
    }

    private void dealDirectory(File file) {
        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    File subFile = path.toFile();
                    String fileName = subFile.getName();
                    if (subFile.exists() && subFile.isFile()) {
                        if (isRequiredClazzFile(subFile)) {
                            dealClassFile(subFile);
                            return super.visitFile(path, attrs);
                        }
                        if (isRequiredResourceFile(fileName)) {
                            AbstractConfiguration.resource.put(
                                    fileName,
                                    Files.newInputStream(
                                            Paths.get(subFile.getAbsolutePath())
                                    )
                            );
                            return super.visitFile(path, attrs);
                        }
                        if (isRequiredJar(fileName)) {
                            dealJarFile(subFile, "");
                            return super.visitFile(path, attrs);
                        }
                    }
                    return super.visitFile(path, attrs);
                }
            });
        } catch (IOException e) {
            logger.error("failed to obtain file stream {}", file.getAbsoluteFile());
            throw IOCExceptionUtil.generateResourceScannerException(e);
        }
    }


    private void dealJarFile(File pathFile, String requirePackageName) {
        try (JarFile jarFile = new JarFile(pathFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                InputStream jarInputStream;
                try {
                    jarInputStream = jarFile.getInputStream(jarEntry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String jarEntryName = jarEntry.getName();
                if (!requirePackageName.isEmpty()) {
                    if (jarEntryName.contains(requirePackageName)) {
                        collectManagementObjects(
                                pathFile.toURI().toURL(),
                                IOUtil.inStreamToByte(jarInputStream),
                                annotationClazzContainer
                        );
                    }
                } else {
                    collectManagementObjects(
                            pathFile.toURI().toURL(),
                            IOUtil.inStreamToByte(jarInputStream),
                            annotationClazzContainer
                    );
                }
                if (isRequiredResourceFile(jarEntryName)) {
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

    private void dealClassFile(File pathFile) {
        try {
            collectManagementObjects(
                    pathFile.toURI().toURL(),
                    IOUtil.file2Bytes(pathFile.getAbsolutePath()),
                    annotationClazzContainer
            );
        } catch (Exception e) {
            logger.error("failed to obtain current resource {} stream", pathFile, e);
            throw IOCExceptionUtil.generateResourceScannerException(e);
        }
    }

    private boolean isRequiredJar(String fileName) {
        return fileName.endsWith(Common.JAR_FILE_SUFFIX.getName());
    }

    private boolean isRequiredResourceFile(String fileName) {
        return fileName.endsWith(".properties") && !fileName.contains("pom.properties");
    }

    private Boolean isRequiredClazzFile(File file) {
        String path;
        return (path = file.getAbsolutePath()).endsWith(Common.CLASS_FILE_SUFFIX.getName())
                &&
                requirePackageNames.stream()
                        .map(oldPackageName -> oldPackageName.replace(".", File.separator))
                        .anyMatch(packageName -> {
                            String filterSeparator;
                            if (!path.contains((filterSeparator = Common.FILTER_CLASS_FILE_SEPARATOR.getName())))
                                filterSeparator = Common.FILTER_TEST_CLASS_FILE_SEPARATOR.getName();
                            return path
                                    .substring(path.indexOf(filterSeparator) + filterSeparator.length() + 1)
                                    .contains(packageName);
                        });
    }

    private void collectManagementObjects(
            URL url,
            byte[] bytes,
            Set<Class<? extends Annotation>> annotations
    ) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        String className = Type.getObjectType(cn.name).getClassName();
        if (IOCClassLoader.getInstance().isAddedClazz(className))
            return;
        int access = cn.access;
        if ((access & ACC_INTERFACE) != 0 && (access & ACC_ABSTRACT) != 0) {
            IOCClassLoader.getInstance().putAbstractAndInterface(className);
            return;
        }
        if (Objects.nonNull(annotations) && !annotations.isEmpty()) {
            if (
                    annotations.stream()
                            .map(Class::getName)
                            .allMatch(
                                    name ->
                                            getAnnotationNodes(cn)
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
        if (!className.isEmpty()) {
            IOCClassLoader.getInstance().put(url, className);
        }
    }

    private List<AnnotationNode> getAnnotationNodes(ClassNode cn) {
        List<AnnotationNode> nodes = new ArrayList<>();
        List<AnnotationNode> visibleAnnotations;
        List<AnnotationNode> invisibleAnnotations;
        if (Objects.nonNull((visibleAnnotations = cn.visibleAnnotations))) {
            nodes.addAll(visibleAnnotations);
        }
        if (
                Objects.nonNull((invisibleAnnotations = cn.invisibleAnnotations))
        ) {
            nodes.addAll(invisibleAnnotations);
        }
        return nodes;
    }

    public void clearCacheAndCreateBeans() {
        this.requirePackageNames = null;
        this.rangePackageNames = null;
        this.annotationClazzContainer = null;
        this.urls = null;
        IOCClassLoader.getInstance().loadClass(AutoSetupExecutor.getInstance().getObjContainer());
    }

    private static final class Instance {
        private static final ResourceScanner instance = new ResourceScanner();
    }

}
