package cloud.opencode.base.classloader.graalvm;

/**
 * GraalVM Native Image detection utility
 * GraalVM Native Image 检测工具
 *
 * <p>Provides static methods to detect whether the application is running
 * inside a GraalVM native image, and distinguishes build-time from run-time.</p>
 * <p>提供静态方法检测应用程序是否在 GraalVM Native Image 中运行，
 * 并区分构建时和运行时。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class NativeImageSupport {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    private NativeImageSupport() {
        // Utility class
    }

    /**
     * Check if running in a GraalVM native image environment
     * 检测是否在 GraalVM Native Image 环境中运行
     *
     * <p>Reads the system property on each call to support dynamic testing.</p>
     * <p>每次调用时读取系统属性，以支持动态测试。</p>
     *
     * @return true if running in native image | 如果在 Native Image 中运行返回 true
     */
    public static boolean isNativeImage() {
        return System.getProperty(NATIVE_IMAGE_PROPERTY) != null;
    }

    /**
     * Check if running at GraalVM native image build time
     * 检测是否在 GraalVM Native Image 构建时运行
     *
     * @return true if at build time | 如果在构建时返回 true
     */
    public static boolean isBuildTime() {
        return "buildtime".equals(System.getProperty(NATIVE_IMAGE_PROPERTY));
    }

    /**
     * Check if running at GraalVM native image run time
     * 检测是否在 GraalVM Native Image 运行时运行
     *
     * @return true if at run time | 如果在运行时返回 true
     */
    public static boolean isRunTime() {
        return "runtime".equals(System.getProperty(NATIVE_IMAGE_PROPERTY));
    }
}
