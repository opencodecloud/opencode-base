package cloud.opencode.base.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Environment - System environment and runtime detection utility
 * Environment - 系统环境与运行时检测工具
 *
 * <p>Provides cached access to JDK, OS, and runtime information.
 * Static values (JDK version, OS name, container detection) are cached via {@link Lazy}
 * for thread-safe single evaluation. Resource metrics (memory, processors) are
 * returned live on each call.</p>
 * <p>提供对 JDK、操作系统和运行时信息的缓存访问。
 * 静态值（JDK 版本、操作系统名称、容器检测）通过 {@link Lazy} 缓存，
 * 保证线程安全的单次求值。资源指标（内存、处理器）每次调用返回实时值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDK version and vendor detection - JDK 版本和厂商检测</li>
 *   <li>OS type detection (Windows, Linux, macOS) - 操作系统类型检测</li>
 *   <li>GraalVM native-image detection - GraalVM 原生镜像检测</li>
 *   <li>Container environment detection (Docker, Kubernetes, cgroup) - 容器环境检测</li>
 *   <li>Virtual thread detection - 虚拟线程检测</li>
 *   <li>Live runtime resource metrics - 实时运行时资源指标</li>
 *   <li>Process info (pid, uptime, javaHome, userDir, tempDir) - 进程信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check JDK version
 * if (Environment.isJavaVersionAtLeast(21)) {
 *     // Use virtual threads
 * }
 *
 * // OS-specific logic
 * if (Environment.isWindows()) {
 *     // Windows-specific path handling
 * }
 *
 * // Container-aware resource allocation
 * if (Environment.isContainer()) {
 *     int cpus = Environment.availableProcessors();
 *     // Adjust thread pool size for container limits
 * }
 *
 * // Virtual thread detection
 * if (Environment.isVirtualThread()) {
 *     // Avoid thread-local caching
 * }
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>All methods are thread-safe - 所有方法都是线程安全的</li>
 *   <li>Cached values use {@link Lazy} (VarHandle CAS) - 缓存值使用 {@link Lazy}（VarHandle CAS）</li>
 *   <li>Resource methods are stateless - 资源方法是无状态的</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lazy
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class Environment {

    // ==================== Cached JDK Info | 缓存的 JDK 信息 ====================

    private static final Lazy<Integer> JAVA_VERSION = Lazy.of((Supplier<Integer>) () -> Runtime.version().feature());

    private static final Lazy<String> JAVA_VENDOR = Lazy.of((Supplier<String>) () -> System.getProperty("java.vendor"));

    // ==================== Cached OS Info | 缓存的操作系统信息 ====================

    private static final Lazy<String> OS_NAME = Lazy.of((Supplier<String>) () -> System.getProperty("os.name", ""));

    private static final Lazy<Boolean> IS_WINDOWS = Lazy.of(
            (Supplier<Boolean>) () -> osName().toLowerCase(java.util.Locale.ROOT).contains("win"));

    private static final Lazy<Boolean> IS_LINUX = Lazy.of(
            (Supplier<Boolean>) () -> osName().toLowerCase(java.util.Locale.ROOT).contains("nux"));

    private static final Lazy<Boolean> IS_MAC_OS = Lazy.of((Supplier<Boolean>) () -> {
        String name = osName().toLowerCase(java.util.Locale.ROOT);
        return name.contains("mac") || name.contains("darwin");
    });

    // ==================== Cached Runtime Detection | 缓存的运行时检测 ====================

    private static final Lazy<Boolean> IS_GRAALVM_NATIVE = Lazy.of(
            (Supplier<Boolean>) () -> System.getProperty("org.graalvm.nativeimage.imagecode") != null
    );

    private static final Lazy<Boolean> IS_CONTAINER = Lazy.of((Supplier<Boolean>) Environment::detectContainer);

    private Environment() {
        throw new AssertionError("No Environment instances for you!");
    }

    // ==================== JDK Info | JDK 信息 ====================

    /**
     * Returns the Java feature version number (e.g. 25 for JDK 25).
     * 返回 Java 特性版本号（例如 JDK 25 返回 25）。
     *
     * @return the Java feature version
     */
    public static int javaVersion() {
        return JAVA_VERSION.get();
    }

    /**
     * Returns the Java vendor string.
     * 返回 Java 厂商字符串。
     *
     * @return the Java vendor, or {@code null} if the property is not set
     */
    public static String javaVendor() {
        return JAVA_VENDOR.get();
    }

    /**
     * Checks whether the current Java version is at least the given version.
     * 检查当前 Java 版本是否至少为给定版本。
     *
     * @param version the minimum version to check against - 要检查的最低版本
     * @return {@code true} if the current Java version is &gt;= the given version
     */
    public static boolean isJavaVersionAtLeast(int version) {
        return javaVersion() >= version;
    }

    // ==================== OS Info | 操作系统信息 ====================

    /**
     * Returns the operating system name.
     * 返回操作系统名称。
     *
     * @return the OS name from {@code os.name} system property, or empty string if not set
     */
    public static String osName() {
        return OS_NAME.get();
    }

    /**
     * Checks whether the current OS is Windows.
     * 检查当前操作系统是否为 Windows。
     *
     * @return {@code true} if running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS.get();
    }

    /**
     * Checks whether the current OS is Linux.
     * 检查当前操作系统是否为 Linux。
     *
     * @return {@code true} if running on Linux
     */
    public static boolean isLinux() {
        return IS_LINUX.get();
    }

    /**
     * Checks whether the current OS is macOS (or Darwin).
     * 检查当前操作系统是否为 macOS（或 Darwin）。
     *
     * @return {@code true} if running on macOS
     */
    public static boolean isMacOS() {
        return IS_MAC_OS.get();
    }

    // ==================== Runtime Detection | 运行时检测 ====================

    /**
     * Checks whether the code is running as a GraalVM native image.
     * 检查代码是否以 GraalVM 原生镜像方式运行。
     *
     * <p>Detection is based on the {@code org.graalvm.nativeimage.imagecode} system property.</p>
     * <p>基于 {@code org.graalvm.nativeimage.imagecode} 系统属性进行检测。</p>
     *
     * @return {@code true} if running in a GraalVM native image
     */
    public static boolean isGraalVmNativeImage() {
        return IS_GRAALVM_NATIVE.get();
    }

    /**
     * Best-effort detection of whether the JVM is running inside a container (Docker, Kubernetes, etc.).
     * 尽力检测 JVM 是否在容器（Docker、Kubernetes 等）中运行。
     *
     * <p><strong>Warning:</strong> This is best-effort detection and MUST NOT be used for security decisions.
     * The detection checks the following indicators:</p>
     * <p><strong>警告：</strong>这是尽力检测，不得用于安全决策。检测检查以下指标：</p>
     * <ul>
     *   <li>{@code /.dockerenv} file existence - 文件是否存在</li>
     *   <li>{@code KUBERNETES_SERVICE_HOST} environment variable - 环境变量</li>
     *   <li>{@code /proc/1/cgroup} for container indicators - 容器指标</li>
     *   <li>{@code /proc/1/mountinfo} for container mount points - 容器挂载点</li>
     * </ul>
     *
     * @return {@code true} if container indicators are detected (best-effort, NOT for security decisions)
     */
    public static boolean isContainer() {
        return IS_CONTAINER.get();
    }

    /**
     * Checks whether the current thread is a virtual thread.
     * 检查当前线程是否为虚拟线程。
     *
     * <p>This method is NOT cached because it depends on the calling thread.</p>
     * <p>此方法不缓存，因为结果取决于调用线程。</p>
     *
     * @return {@code true} if the current thread is a virtual thread
     */
    public static boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    // ==================== Resource Info (Live) | 资源信息（实时） ====================

    /**
     * Returns the number of processors available to the JVM.
     * 返回 JVM 可用的处理器数量。
     *
     * <p>This value may change during the lifetime of the JVM, especially in containerized environments.
     * The value is NOT cached.</p>
     * <p>此值在 JVM 生命周期内可能会变化，尤其是在容器化环境中。该值不缓存。</p>
     *
     * @return the number of available processors
     */
    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the maximum amount of memory the JVM will attempt to use, in bytes.
     * 返回 JVM 将尝试使用的最大内存量（字节）。
     *
     * @return the maximum memory in bytes
     */
    public static long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns the total amount of memory currently available to the JVM, in bytes.
     * 返回当前 JVM 可用的总内存量（字节）。
     *
     * @return the total memory in bytes
     */
    public static long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the amount of free memory in the JVM, in bytes.
     * 返回 JVM 中的空闲内存量（字节）。
     *
     * @return the free memory in bytes
     */
    public static long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    // ==================== Process Info | 进程信息 ====================

    /**
     * Returns the process ID (PID) of the current JVM.
     * 返回当前 JVM 的进程 ID（PID）。
     *
     * @return the process ID | 进程 ID
     */
    public static long pid() {
        return ProcessHandle.current().pid();
    }

    /**
     * Returns the uptime of the JVM since it started.
     * 返回 JVM 自启动以来的运行时间。
     *
     * @return the JVM uptime duration | JVM 运行时间
     */
    public static java.time.Duration uptime() {
        return java.time.Duration.ofMillis(
                java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());
    }

    /**
     * Returns the Java home directory path.
     * 返回 Java 安装目录路径。
     *
     * @return the Java home path | Java 安装路径
     */
    public static String javaHome() {
        return System.getProperty("java.home", "");
    }

    /**
     * Returns the user's current working directory.
     * 返回用户当前工作目录。
     *
     * @return the current working directory | 当前工作目录
     */
    public static String userDir() {
        return System.getProperty("user.dir", "");
    }

    /**
     * Returns the system temporary directory path.
     * 返回系统临时目录路径。
     *
     * @return the temp directory path | 临时目录路径
     */
    public static String tempDir() {
        return System.getProperty("java.io.tmpdir", "");
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Detects container environment using multiple heuristics.
     * 使用多种启发式方法检测容器环境。
     */
    private static boolean detectContainer() {
        // Check /.dockerenv
        if (Files.exists(Path.of("/.dockerenv"))) {
            return true;
        }

        // Check KUBERNETES_SERVICE_HOST environment variable
        String k8sHost = System.getenv("KUBERNETES_SERVICE_HOST");
        if (k8sHost != null && !k8sHost.isEmpty()) {
            return true;
        }

        // Check /proc/1/cgroup for container indicators
        if (checkFileContainsContainerIndicator(Path.of("/proc/1/cgroup"))) {
            return true;
        }

        // Check /proc/1/mountinfo for container mount points
        return checkFileContainsContainerIndicator(Path.of("/proc/1/mountinfo"));
    }

    /**
     * Checks whether a file contains container-related indicators using stream-based reading.
     * 使用基于流的读取检查文件是否包含容器相关指标。
     */
    private static boolean checkFileContainsContainerIndicator(Path path) {
        if (!Files.isReadable(path)) {
            return false;
        }
        try (Stream<String> lines = Files.lines(path)) {
            return lines.anyMatch(line -> {
                String lower = line.toLowerCase(java.util.Locale.ROOT);
                return lower.contains("docker")
                        || lower.contains("kubepods")
                        || lower.contains("containerd")
                        || lower.contains("/lxc/")
                        || lower.contains("cri-o");
            });
        } catch (IOException | SecurityException _) {
            return false;
        }
    }
}
