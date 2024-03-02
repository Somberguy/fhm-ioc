package org.fhm.substrate.service;

import org.fhm.substrate.annotation.Component;
import org.fhm.substrate.annotation.Configuration;
import org.fhm.substrate.annotation.ScanPackageConfig;
import org.fhm.substrate.config.AbstractConfiguration;
import org.fhm.substrate.constant.Common;
import org.fhm.substrate.constant.VMParameters;
import org.fhm.substrate.standard.ILogger;
import org.fhm.substrate.standard.IStarter;
import org.fhm.substrate.util.IOCExceptionUtil;
import org.fhm.substrate.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.*;

/**
 * @Classname ClassFileScanner
 * @Description TODO
 * @Date 2023/10/22 14:49
 * @Author by 月光叶
 */
public class ResourceScanner {

    private final ILogger logger = LoggerHandler.getLogger(ResourceScanner.class);

    private Set<String> urls = new HashSet<>();

    private Set<Pattern> requirePackageNames = new HashSet<>();

    private Set<String> rangePackageNames = new HashSet<>();

    private Set<Class<? extends Annotation>> annotationClazzContainer = new HashSet<>(2);

    private Class<? extends IStarter> starterClazz;

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
        this.starterClazz = starterClazz;
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
                this.rangePackageNames.add((
                        defaultPackageName = Common.PROJECT_PACKAGE_NAME.getName())
                        .replaceFirst("\\.\\*.*$", "")
                        .replace(".", "/")
                );
                this.requirePackageNames.add(obtainPattern(defaultPackageName));
            } else {
                Arrays.stream(packageNames)
                        .forEach(name -> {
                            this.requirePackageNames.add(obtainPattern(name));
                            this.rangePackageNames.add(
                                    name.replaceFirst("\\.\\*.*$", "")
                                            .replace(".", "/")
                            );
                        });
            }
        }
        return newManageAnnotations;
    }

    private Pattern obtainPattern(String regex){
        String ALL_MATCH = "**";
        String ONE_MATCH = "*";
        if (regex.contains(ALL_MATCH) || regex.contains(ONE_MATCH)){
            String temp = "[a-zA-Z0-9]+";
            String end = "." + temp + Common.CLASS_FILE_SUFFIX.getName() + "$";
            if (regex.endsWith("**"))
                end = ".*";
            regex = "^.*" + regex.replace(ALL_MATCH, temp).replace(ONE_MATCH, temp) + end;
        } else
            regex = "^.*" + regex + ".*$";
        return Pattern.compile(regex);
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
                            if (isRequiredClazzFile(file.getAbsolutePath()))
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
            Constructor<? extends IStarter> constructor = starterClazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            IStarter iStarter = constructor.newInstance();
            AutoSetupExecutor.getInstance().getObjContainer().put(starterClazz.getName(), iStarter);
            return iStarter.newManageMembers();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignore) {
            return null;
        } catch (NoSuchMethodException e) {
            throw IOCExceptionUtil.generateResourceScannerException("a starter class " + starterClazz.getName()
                    + " must have a parameterless constructor.", e);
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
                                URLConnection urlConnection;
                                try {
                                    if (Objects.nonNull(url) && (urlConnection = url.openConnection()) instanceof JarURLConnection) {
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
                                        if (isRequiredClazzFile(jarEntryName)) {
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
                    String fileName = subFile.getAbsolutePath();
                    if (subFile.exists() && subFile.isFile()) {
                        if (isRequiredClazzFile(fileName)) {
                            dealClassFile(subFile);
                            return super.visitFile(path, attrs);
                        }
                        if (isRequiredResourceFile(fileName)) {
                            AbstractConfiguration.resource.put(
                                    subFile.getName(),
                                    Files.newInputStream(
                                            Paths.get(fileName)
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
                String jarEntryName;
                if (isRequiredResourceFile((jarEntryName = jarEntry.getName()))) {
                    AbstractConfiguration.resource.put(
                            pathFile.getName(),
                            jarFile.getInputStream(jarEntry)
                    );
                    return;
                }
                if (jarEntryName.endsWith(Common.CLASS_FILE_SUFFIX.getName())){
                    String newEntryName = jarEntryName.replace("/", ".");
                    if (!requirePackageName.isEmpty()) {
                        if (newEntryName.contains(requirePackageName))
                            collectManagementObjects(
                                    pathFile.toURI().toURL(),
                                    IOUtil.inStreamToByte(jarInputStream),
                                    annotationClazzContainer
                            );
                    } else {
                        if (isRequiredClass(newEntryName))
                            collectManagementObjects(
                                    pathFile.toURI().toURL(),
                                    IOUtil.inStreamToByte(jarInputStream),
                                    annotationClazzContainer
                            );
                    }
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

    private Boolean isRequiredClazzFile(String filePath) {
        return filePath.endsWith(Common.CLASS_FILE_SUFFIX.getName())
                &&
                isRequiredClass(filePath);
    }

    private Boolean isRequiredClass(String classDesc){
        return requirePackageNames.stream().anyMatch(pattern -> pattern.matcher(classDesc.replace(File.separator, ".")).matches());
    }

    private void collectManagementObjects(
            URL url,
            byte[] bytes,
            Set<Class<? extends Annotation>> annotations
    ) {
        try {
            ClassNode cn = new ClassNode();
            new ClassReader(bytes).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            String className = Type.getObjectType(cn.name).getClassName();
            if (className.isEmpty() || className.equals(starterClazz.getName()))
                return;
            if (IOCClassLoader.getInstance().isAddedClazz(className))
                return;
            int access = cn.access;
            if ((access & ACC_INTERFACE) != 0 || (access & ACC_ABSTRACT) != 0) {
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
                    && m.desc.equals("()V"))) {
                logger.error("the class {} does not have a parameterless constructor, which results in failure to inject into the IOC", className);
                return;
            }
            IOCClassLoader.getInstance().put(url, className);
        } catch (Exception e){
            logger.error("failed to collect the management object，the error description is {}", e.getMessage(), e);
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
