package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Resource Loader - Loads resources from various locations
 * 资源加载器 - 从各种位置加载资源
 *
 * <p>Provides unified resource loading with protocol support (classpath:, file:, url:, jar:).</p>
 * <p>提供带协议支持的统一资源加载（classpath:、file:、url:、jar:）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Protocol-based loading - 基于协议的加载</li>
 *   <li>Wildcard pattern support - 通配符模式支持</li>
 *   <li>Custom ClassLoader support - 自定义 ClassLoader 支持</li>
 * </ul>
 *
 * <p><strong>Supported Protocols | 支持的协议:</strong></p>
 * <ul>
 *   <li>classpath: - classpath resource | classpath 资源</li>
 *   <li>classpath*: - all matching classpath resources | 所有匹配的 classpath 资源</li>
 *   <li>file: - file system resource | 文件系统资源</li>
 *   <li>url: - URL resource | URL 资源</li>
 *   <li>jar: - JAR resource | JAR 资源</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceLoader loader = ResourceLoader.create();
 * Resource config = loader.load("classpath:config.yml");
 * List<Resource> xmlFiles = loader.loadAll("classpath*:META-INF/*.xml");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class ResourceLoader {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String CLASSPATH_ALL_PREFIX = "classpath*:";
    private static final String FILE_PREFIX = "file:";
    private static final String URL_PREFIX = "url:";
    private static final String JAR_PREFIX = "jar:";

    /**
     * Shared NestedJarHandler singleton for nested JAR resource parsing.
     * Lazily initialized on first use; cleaned up via a single persistent shutdown hook.
     * 用于嵌套 JAR 资源解析的共享 NestedJarHandler 单例。
     * 首次使用时延迟初始化；通过单一持久 shutdown hook 清理。
     */
    private static volatile NestedJarHandler sharedNestedJarHandler;
    private static final Object NESTED_HANDLER_LOCK = new Object();
    private static volatile boolean shutdownHookRegistered;

    private static NestedJarHandler getSharedNestedJarHandler() {
        NestedJarHandler handler = sharedNestedJarHandler;
        if (handler != null && !handler.isClosed()) {
            return handler;
        }
        synchronized (NESTED_HANDLER_LOCK) {
            handler = sharedNestedJarHandler;
            if (handler != null && !handler.isClosed()) {
                return handler;
            }
            handler = NestedJarHandler.builder().build();
            sharedNestedJarHandler = handler;
            // Register shutdown hook only once — it reads the volatile field at shutdown time
            if (!shutdownHookRegistered) {
                shutdownHookRegistered = true;
                Runtime.getRuntime().addShutdownHook(Thread.ofVirtual()
                        .name("nested-jar-handler-cleanup")
                        .unstarted(() -> {
                            NestedJarHandler h = sharedNestedJarHandler;
                            if (h != null) {
                                h.close();
                            }
                        }));
            }
            return handler;
        }
    }

    private ClassLoader classLoader;

    /**
     * Create resource loader with default classloader
     * 使用默认类加载器创建资源加载器
     */
    public ResourceLoader() {
        this.classLoader = getDefaultClassLoader();
    }

    /**
     * Create resource loader with specified classloader
     * 使用指定类加载器创建资源加载器
     *
     * @param classLoader class loader | 类加载器
     */
    public ResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getDefaultClassLoader();
    }

    /**
     * Create resource loader with default classloader
     * 使用默认类加载器创建资源加载器
     *
     * @return resource loader | 资源加载器
     */
    public static ResourceLoader create() {
        return new ResourceLoader();
    }

    /**
     * Create resource loader with specified classloader
     * 使用指定类加载器创建资源加载器
     *
     * @param classLoader class loader | 类加载器
     * @return resource loader | 资源加载器
     */
    public static ResourceLoader create(ClassLoader classLoader) {
        return new ResourceLoader(classLoader);
    }

    /**
     * Load single resource from location
     * 从位置加载单个资源
     *
     * @param location resource location | 资源位置
     * @return resource | 资源
     */
    public Resource load(String location) {
        Objects.requireNonNull(location, "Location must not be null");

        if (location.startsWith(CLASSPATH_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_PREFIX.length()), classLoader);
        }
        if (location.startsWith(CLASSPATH_ALL_PREFIX)) {
            // For single load, just return the first match
            List<Resource> resources = loadAll(location);
            if (resources.isEmpty()) {
                throw OpenClassLoaderException.resourceNotFound(location);
            }
            return resources.getFirst();
        }
        if (location.startsWith(FILE_PREFIX)) {
            return new FileResource(location.substring(FILE_PREFIX.length()));
        }
        if (location.startsWith(URL_PREFIX)) {
            try {
                return new UrlResource(location.substring(URL_PREFIX.length()));
            } catch (MalformedURLException e) {
                throw new OpenClassLoaderException("Invalid URL: " + location, e);
            }
        }
        if (location.startsWith(JAR_PREFIX)) {
            return parseJarResource(location);
        }

        // Default to classpath
        return new ClassPathResource(location, classLoader);
    }

    /**
     * Load all resources matching the location pattern
     * 加载所有匹配位置模式的资源
     *
     * @param locationPattern resource location pattern | 资源位置模式
     * @return list of resources | 资源列表
     */
    public List<Resource> loadAll(String locationPattern) {
        Objects.requireNonNull(locationPattern, "Location pattern must not be null");

        if (locationPattern.startsWith(CLASSPATH_ALL_PREFIX)) {
            return loadClassPathResources(locationPattern.substring(CLASSPATH_ALL_PREFIX.length()));
        }
        if (locationPattern.startsWith(FILE_PREFIX)) {
            return loadFileResources(locationPattern.substring(FILE_PREFIX.length()));
        }

        // Default behavior: try as classpath pattern
        return loadClassPathResources(locationPattern);
    }

    /**
     * Set class loader
     * 设置类加载器
     *
     * @param classLoader class loader | 类加载器
     * @return this loader | 此加载器
     */
    public ResourceLoader classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getDefaultClassLoader();
        return this;
    }

    private List<Resource> loadClassPathResources(String pattern) {
        List<Resource> result = new ArrayList<>();
        try {
            if (pattern.contains("*")) {
                // Wildcard pattern - scan for matching resources
                result.addAll(scanClassPathResources(pattern));
            } else {
                // Exact match
                Enumeration<URL> urls = classLoader.getResources(pattern);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    result.add(createResourceFromUrl(url));
                }
            }
        } catch (IOException e) {
            throw OpenClassLoaderException.resourceReadFailed(pattern, e);
        }
        return result;
    }

    private List<Resource> scanClassPathResources(String pattern) {
        List<Resource> result = new ArrayList<>();
        try {
            // Extract base path (before first wildcard)
            int wildcardIndex = pattern.indexOf('*');
            String basePath = wildcardIndex > 0 ? pattern.substring(0, pattern.lastIndexOf('/', wildcardIndex) + 1) : "";
            String subPattern = pattern.substring(basePath.length());

            Enumeration<URL> urls = classLoader.getResources(basePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                result.addAll(scanUrl(url, basePath, subPattern));
            }
        } catch (IOException e) {
            throw OpenClassLoaderException.resourceReadFailed(pattern, e);
        }
        return result;
    }

    private List<Resource> scanUrl(URL url, String basePath, String subPattern) {
        List<Resource> result = new ArrayList<>();
        try {
            if ("file".equals(url.getProtocol())) {
                Path rootPath = Path.of(url.toURI());
                if (Files.isDirectory(rootPath)) {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + subPattern);
                    try (Stream<Path> paths = Files.walk(rootPath)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> matcher.matches(rootPath.relativize(p)))
                                .forEach(p -> result.add(new FileResource(p)));
                    }
                }
            }
        } catch (Exception e) {
            // Log scanning errors so failures are observable
            // 记录扫描错误，使失败可观测
            System.getLogger(ResourceLoader.class.getName()).log(
                    System.Logger.Level.WARNING,
                    () -> "Error scanning classpath resource URL: " + url, e);
        }
        return result;
    }

    private List<Resource> loadFileResources(String pattern) {
        List<Resource> result = new ArrayList<>();
        try {
            if (pattern.contains("*")) {
                // Wildcard pattern
                int wildcardIndex = pattern.indexOf('*');
                String basePath = pattern.substring(0, pattern.lastIndexOf('/', wildcardIndex) + 1);
                String subPattern = pattern.substring(basePath.length());

                Path rootPath = Path.of(basePath);
                if (Files.isDirectory(rootPath)) {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + subPattern);
                    try (Stream<Path> paths = Files.walk(rootPath)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> matcher.matches(rootPath.relativize(p)))
                                .forEach(p -> result.add(new FileResource(p)));
                    }
                }
            } else {
                Path path = Path.of(pattern);
                if (Files.exists(path)) {
                    result.add(new FileResource(path));
                }
            }
        } catch (IOException e) {
            throw OpenClassLoaderException.resourceReadFailed(pattern, e);
        }
        return result;
    }

    private Resource parseJarResource(String location) {
        // jar:file:/path/to/jar.jar!/entry/path
        // Nested JAR: jar:file:/app.jar!/BOOT-INF/lib/dep.jar!/com/Config.class
        String jarLocation = location.substring(JAR_PREFIX.length());
        int firstSep = jarLocation.indexOf("!/");
        if (firstSep == -1) {
            throw new OpenClassLoaderException("Invalid JAR URL: " + location);
        }

        String afterFirstSep = jarLocation.substring(firstSep + 2);
        int secondSep = afterFirstSep.indexOf("!/");

        if (secondSep >= 0) {
            // Double-nested JAR: outer.jar!/nested.jar!/resource
            String outerJarPath = jarLocation.substring(0, firstSep);
            if (outerJarPath.startsWith("file:")) {
                outerJarPath = outerJarPath.substring(5);
            }
            String nestedJarEntry = afterFirstSep.substring(0, secondSep);
            String resourceEntry = afterFirstSep.substring(secondSep + 2);

            return new NestedJarResource(getSharedNestedJarHandler(), Path.of(outerJarPath), nestedJarEntry, resourceEntry);
        }

        // Simple JAR entry
        String jarPath = jarLocation.substring(0, firstSep);
        String entryPath = afterFirstSep;

        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }
        return new JarResource(Path.of(jarPath), entryPath);
    }

    private Resource createResourceFromUrl(URL url) {
        String protocol = url.getProtocol();
        return switch (protocol) {
            case "file" -> new FileResource(Path.of(url.getPath()));
            case "jar" -> new UrlResource(url);
            default -> new UrlResource(url);
        };
    }

    private ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ResourceLoader.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }
}
