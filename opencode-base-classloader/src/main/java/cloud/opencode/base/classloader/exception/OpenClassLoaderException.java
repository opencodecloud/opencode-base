package cloud.opencode.base.classloader.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;
import java.util.Optional;

/**
 * ClassLoader Component Unified Exception
 * ClassLoader 组件统一异常
 *
 * <p>Exception class for class loading, resource access and metadata reading operations.</p>
 * <p>用于类加载、资源访问和元数据读取操作的异常类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class not found exception - 类未找到异常</li>
 *   <li>Class load failed exception - 类加载失败异常</li>
 *   <li>Resource not found exception - 资源未找到异常</li>
 *   <li>Resource read failed exception - 资源读取失败异常</li>
 *   <li>Metadata parse failed exception - 元数据解析失败异常</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenClassLoaderException.classNotFound("com.example.MyClass");
 * throw OpenClassLoaderException.resourceNotFound("config.yml");
 * throw OpenClassLoaderException.classLoadFailed("MyClass", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class OpenClassLoaderException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "classloader";

    private final String className;
    private final String resourceName;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message exception message | 异常消息
     */
    public OpenClassLoaderException(String message) {
        super(COMPONENT, null, message);
        this.className = null;
        this.resourceName = null;
    }

    /**
     * Create exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message exception message | 异常消息
     * @param cause   root cause | 原始异常
     */
    public OpenClassLoaderException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.className = null;
        this.resourceName = null;
    }

    /**
     * Create exception with class name, resource name and message
     * 创建带类名、资源名和消息的异常
     *
     * @param className    class name | 类名
     * @param resourceName resource name | 资源名
     * @param message      exception message | 异常消息
     */
    private OpenClassLoaderException(String className, String resourceName, String message) {
        super(COMPONENT, null, message);
        this.className = className;
        this.resourceName = resourceName;
    }

    /**
     * Create exception with class name, resource name, message and cause
     * 创建带类名、资源名、消息和原因的异常
     *
     * @param className    class name | 类名
     * @param resourceName resource name | 资源名
     * @param message      exception message | 异常消息
     * @param cause        root cause | 原始异常
     */
    private OpenClassLoaderException(String className, String resourceName, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.className = className;
        this.resourceName = resourceName;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create class not found exception
     * 创建类未找到异常
     *
     * @param className class name | 类名
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException classNotFound(String className) {
        return new OpenClassLoaderException(className, null, "Class not found: " + className);
    }

    /**
     * Create class not found exception with cause
     * 创建带原因的类未找到异常
     *
     * @param className class name | 类名
     * @param cause     root cause | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException classNotFound(String className, Throwable cause) {
        return new OpenClassLoaderException(className, null, "Class not found: " + className, cause);
    }

    /**
     * Create class load failed exception
     * 创建类加载失败异常
     *
     * @param className class name | 类名
     * @param cause     root cause | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException classLoadFailed(String className, Throwable cause) {
        return new OpenClassLoaderException(className, null,
                "Failed to load class: " + className, cause);
    }

    /**
     * Create resource not found exception
     * 创建资源未找到异常
     *
     * @param resourceName resource name | 资源名
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException resourceNotFound(String resourceName) {
        return new OpenClassLoaderException(null, resourceName,
                "Resource not found: " + resourceName);
    }

    /**
     * Create resource read failed exception
     * 创建资源读取失败异常
     *
     * @param resourceName resource name | 资源名
     * @param cause        root cause | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException resourceReadFailed(String resourceName, Throwable cause) {
        return new OpenClassLoaderException(null, resourceName,
                "Failed to read resource: " + resourceName, cause);
    }

    /**
     * Create metadata parse failed exception
     * 创建元数据解析失败异常
     *
     * @param className class name | 类名
     * @param cause     root cause | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException metadataParseFailed(String className, Throwable cause) {
        return new OpenClassLoaderException(className, null,
                "Failed to parse metadata for class: " + className, cause);
    }

    /**
     * Create scan failed exception
     * 创建扫描失败异常
     *
     * @param packageName package name | 包名
     * @param cause       root cause | 原始异常
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException scanFailed(String packageName, Throwable cause) {
        return new OpenClassLoaderException(null, packageName,
                "Failed to scan package: " + packageName, cause);
    }

    /**
     * Create class loader closed exception
     * 创建类加载器已关闭异常
     *
     * @return exception instance | 异常实例
     */
    public static OpenClassLoaderException classLoaderClosed() {
        return new OpenClassLoaderException("ClassLoader has been closed");
    }

    // ==================== Getters ====================

    /**
     * Get the class name associated with this exception
     * 获取与此异常关联的类名
     *
     * @return optional class name | 可选的类名
     */
    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    /**
     * Get the resource name associated with this exception
     * 获取与此异常关联的资源名
     *
     * @return optional resource name | 可选的资源名
     */
    public Optional<String> getResourceName() {
        return Optional.ofNullable(resourceName);
    }
}
