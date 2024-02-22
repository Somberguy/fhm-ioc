package org.fhm.ioc.service;

import org.fhm.ioc.annotation.Component;
import org.fhm.ioc.annotation.Configuration;
import org.fhm.ioc.config.AbstractConfiguration;
import org.fhm.ioc.constant.Common;
import org.fhm.ioc.constant.VMParameters;
import org.fhm.ioc.standard.ILoggerHandler;
import org.fhm.ioc.util.IOCExceptionUtil;
import org.fhm.ioc.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

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

    private Set<String> scanPackage = new HashSet<>();

    private Set<Class<? extends Annotation>> annotationClazzContainer = new HashSet<>(2);

    private Set<String> jarNames = new HashSet<>(1);

    {
        this.annotationClazzContainer.add(Component.class);
        this.annotationClazzContainer.add(Configuration.class);
    }

    public static ResourceScanner getInstance() {
        return Instance.instance;
    }

    private static List<AnnotationNode> getAnnotationNodes(ClassNode cn) {
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

    public void filterCPPath() {
        for (String url : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (
                    url.contains(Common.FILTER_CLASS_FILE_SEPARATOR.getName())
                            || url.contains(Common.FILTER_TEST_CLASS_FILE_SEPARATOR.getName())
                            || url.endsWith(Common.JAR_FILE_SUFFIX.getName())
            ) {
                urls.add(url);
            }
        }
    }

    public void scanRequiredSystem(Map<String, Object> objContainer) {
        VMParameters.REGISTRY_BEAN_DIR_PATH.use((name, dirPath) -> {
            File file = new File(dirPath);
            if (file.exists() && file.isDirectory()) {
                logger.info("start obtain the class file {} of VM system", dirPath);
                File[] files = file.listFiles();
                if (Objects.nonNull(files))
                    for (File jarFile : files) {
                        if (jarFile.getAbsolutePath().endsWith(Common.JAR_FILE_SUFFIX.getName()))
                            dealJarFile(jarFile, objContainer, VMParameters.REGISTRY_PACKAGE_NAME.getValue());
                    }
            }
        });
    }

    public void scanRequiredFileAndSetupObj(Map<String, Object> objContainer) {
        if (scanPackage.isEmpty())
            scanPackage.add(Common.PROJECT_PACKAGE_NAME.getName());
        logger.info("start to obtain the class files of CP");
        scanRequiredClassResource(objContainer);
        logger.info("start to obtain the class files in nested packages");
        scanJarResource(objContainer);
    }

    private void scanRequiredClassResource(Map<String, Object> objContainer) {
        urls.forEach(url -> {
            File file = new File(url);
            if (file.exists()) {
                if (file.isDirectory())
                    dealDirectory(objContainer, file);
                if (file.isFile()) {
                    if (isRequiredClazzFile(file))
                        dealClassFile(file, objContainer);
                    if (isRequiredJar(file.getName()))
                        dealJarFile(file, objContainer, "");
                }
            }

        });
    }

    private void dealDirectory(Map<String, Object> objContainer, File file) {
        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    File pathFile = path.toFile();
                    String fileName = pathFile.getName();
                    if (pathFile.exists() && pathFile.isFile()) {
                        if (isRequiredClazzFile(pathFile))
                            dealClassFile(pathFile, objContainer);
                        if (isRequiredResourceFile(fileName)) {
                            AbstractConfiguration.resource.put(
                                    fileName,
                                    Files.newInputStream(
                                            Paths.get(pathFile.getAbsolutePath())
                                    )
                            );
                        }
                        if (isRequiredJar(fileName))
                            dealJarFile(pathFile, objContainer, "");
                    }
                    return super.visitFile(path, attrs);
                }
            });
        } catch (IOException e) {
            logger.error("failed to obtain file stream {}", file.getAbsoluteFile());
            throw IOCExceptionUtil.generateResourceScannerException(e);
        }
    }

    private void scanJarResource(Map<String, Object> objContainer) {
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
                dealInnerJarClassFile(objContainer, jarEntry, jarFile);
                if (isRequiredResourceFile(jarEntryName) && AbstractConfiguration.resource.get(jarEntryName) == null) {
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

    private void dealJarFile(File pathFile, Map<String, Object> objContainer, String requirePackageName) {
        try (JarFile jarFile = new JarFile(pathFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (!requirePackageName.isEmpty()) {
                    if (jarEntryName.contains(requirePackageName)) {
                        dealInnerJarClassFile(objContainer, jarEntry, jarFile);
                    }
                } else {
                    dealInnerJarClassFile(objContainer, jarEntry, jarFile);
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

    private void dealInnerJarClassFile(Map<String, Object> objContainer, JarEntry jarEntry, JarFile jarFile) {
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

    private void dealClassFile(File pathFile, Map<String, Object> objContainer) {
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

    private boolean isRequiredJar(String fileName) {
        return fileName.endsWith(Common.JAR_FILE_SUFFIX.getName())
                &&
                jarNames.contains(fileName);
    }

    private boolean isRequiredResourceFile(String fileName) {
        return fileName.endsWith(".properties") && !fileName.contains("pom.properties");
    }

    private Boolean isRequiredClazzFile(File clazzFile) {
        String path;
        return (path = clazzFile.getAbsolutePath()).endsWith(Common.CLASS_FILE_SUFFIX.getName())
                &&
                scanPackage.stream().anyMatch(interceptPackageName(path)::contains);
    }

    private String interceptPackageName(String path) {
        String filterSeparator;
        if (!path.contains((filterSeparator = Common.FILTER_CLASS_FILE_SEPARATOR.getName()))) {
            filterSeparator = Common.FILTER_TEST_CLASS_FILE_SEPARATOR.getName();
        }
        return path.substring(path.indexOf(filterSeparator) + filterSeparator.length() + 1);
    }

    public void collectManagementObjects(
            byte[] bytes,
            Set<Class<? extends Annotation>> annotations,
            Map<String, Object> objContainer) {

        String clazzName = obtainRequireClazzName(annotations, objContainer, bytes);
        if (clazzName.isEmpty()) {
            return;
        }
        try {
            Class<?> srcClass = Class.forName(clazzName);
            Class<?> aClass = IOCClassLoader
                    .getInstance()
                    .loadByteArr(clazzName, bytes, srcClass.getProtectionDomain());
            if (Objects.nonNull(aClass)) {
                objContainer
                        .put(
                                clazzName,
                                aClass.getConstructor().newInstance()
                        );
            } else {
                logger.warn(clazzName + " has been loaded");
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | ClassNotFoundException ignore) {
        }
    }

    private String obtainRequireClazzName(Set<Class<? extends Annotation>> annotations, Map<String, Object> objContainer, byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        String className = Type.getObjectType(cn.name).getClassName();
        if (objContainer.containsKey(className))
            return className;
        int access = cn.access;
        if ((access & ACC_INTERFACE) != 0 && (access & ACC_ABSTRACT) != 0) {
            return className;
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
                return className;
            }
        }
        if (cn.methods.stream().noneMatch(m -> m.name.equals("<init>")
                && m.desc.equals("()V") && m.access == ACC_PUBLIC)) {
            logger.warn("class {} has no unmanaged parameterless constructor", cn.name);
            return className;
        }
        return className;
    }

    public void addScanAnnotationClazz(Collection<Class<? extends Annotation>> anno) {
        this.annotationClazzContainer.addAll(anno);
    }

    public void addScanJar(String jarName) {
        this.jarNames.add(jarName);
    }

    public void addScanPackage(Collection<String> packageName) {
        this.scanPackage.addAll(packageName);
    }

    public void clearCache() {
        this.scanPackage = null;
        this.annotationClazzContainer = null;
        this.jarNames = null;
        this.urls = null;
    }

    private static final class Instance {
        private static final ResourceScanner instance = new ResourceScanner();
    }

}
