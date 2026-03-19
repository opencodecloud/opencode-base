package cloud.opencode.base.io.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.nio.file.Path;

/**
 * IO Operation Exception (Unchecked)
 * IO操作异常（非受检）
 *
 * <p>Unchecked exception for IO operations that wraps checked IOException.
 * Simplifies caller code by eliminating mandatory try-catch blocks.</p>
 * <p>用于IO操作的非受检异常，封装受检的IOException。
 * 通过消除强制性的try-catch块来简化调用方代码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unchecked exception wrapper - 非受检异常包装</li>
 *   <li>Operation and path tracking - 操作和路径追踪</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with factory method
 * throw OpenIOOperationException.fileNotFound(path);
 *
 * // Create with operation details
 * throw new OpenIOOperationException("read", path.toString(), "File not readable");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes, operation and path may be null - 空值安全: 是，operation和path可以为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public class OpenIOOperationException extends OpenException {

    private static final String COMPONENT = "io";

    /**
     * Operation type
     * 操作类型
     */
    private final String operation;

    /**
     * Path involved
     * 涉及的路径
     */
    private final String path;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates an exception with message
     * 创建带消息的异常
     *
     * @param message the error message | 错误消息
     */
    public OpenIOOperationException(String message) {
        super(COMPONENT, null, message);
        this.operation = null;
        this.path = null;
    }

    /**
     * Creates an exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenIOOperationException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.operation = null;
        this.path = null;
    }

    /**
     * Creates an exception with operation, path and message
     * 创建带操作、路径和消息的异常
     *
     * @param operation the operation type | 操作类型
     * @param path      the path involved | 涉及的路径
     * @param message   the error message | 错误消息
     */
    public OpenIOOperationException(String operation, String path, String message) {
        super(COMPONENT, null, message);
        this.operation = operation;
        this.path = path;
    }

    /**
     * Creates an exception with operation, path, message and cause
     * 创建带操作、路径、消息和原因的异常
     *
     * @param operation the operation type | 操作类型
     * @param path      the path involved | 涉及的路径
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     */
    public OpenIOOperationException(String operation, String path, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.operation = operation;
        this.path = path;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the operation type
     * 获取操作类型
     *
     * @return operation type | 操作类型
     */
    public String operation() {
        return operation;
    }

    /**
     * Gets the path involved
     * 获取涉及的路径
     *
     * @return path | 路径
     */
    public String path() {
        return path;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a file not found exception
     * 创建文件未找到异常
     *
     * @param path the file path | 文件路径
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException fileNotFound(Path path) {
        return new OpenIOOperationException("read", path.toString(),
                String.format("File not found: %s", path));
    }

    /**
     * Creates a resource not found exception
     * 创建资源未找到异常
     *
     * @param resource the resource name | 资源名称
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException resourceNotFound(String resource) {
        return new OpenIOOperationException("load", resource,
                String.format("Resource not found: %s", resource));
    }

    /**
     * Creates a read failed exception
     * 创建读取失败异常
     *
     * @param path  the file path | 文件路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException readFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("read", path.toString(),
                String.format("Failed to read file: %s", path), cause);
    }

    /**
     * Creates a read failed exception for stream
     * 创建流读取失败异常
     *
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException readFailed(Throwable cause) {
        return new OpenIOOperationException("read", null,
                "Failed to read stream", cause);
    }

    /**
     * Creates a write failed exception
     * 创建写入失败异常
     *
     * @param path  the file path | 文件路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException writeFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("write", path.toString(),
                String.format("Failed to write file: %s", path), cause);
    }

    /**
     * Creates a write failed exception for stream
     * 创建流写入失败异常
     *
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException writeFailed(Throwable cause) {
        return new OpenIOOperationException("write", null,
                "Failed to write stream", cause);
    }

    /**
     * Creates a copy failed exception
     * 创建复制失败异常
     *
     * @param source the source path | 源路径
     * @param target the target path | 目标路径
     * @param cause  the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException copyFailed(Path source, Path target, Throwable cause) {
        return new OpenIOOperationException("copy", source.toString(),
                String.format("Failed to copy from %s to %s", source, target), cause);
    }

    /**
     * Creates a move failed exception
     * 创建移动失败异常
     *
     * @param source the source path | 源路径
     * @param target the target path | 目标路径
     * @param cause  the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException moveFailed(Path source, Path target, Throwable cause) {
        return new OpenIOOperationException("move", source.toString(),
                String.format("Failed to move from %s to %s", source, target), cause);
    }

    /**
     * Creates a delete failed exception
     * 创建删除失败异常
     *
     * @param path  the path | 路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException deleteFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("delete", path.toString(),
                String.format("Failed to delete: %s", path), cause);
    }

    /**
     * Creates a create file failed exception
     * 创建文件创建失败异常
     *
     * @param path  the path | 路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException createFileFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("createFile", path.toString(),
                String.format("Failed to create file: %s", path), cause);
    }

    /**
     * Creates a create directory failed exception
     * 创建目录创建失败异常
     *
     * @param path  the path | 路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException createDirectoryFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("createDirectory", path.toString(),
                String.format("Failed to create directory: %s", path), cause);
    }

    /**
     * Creates a stream operation failed exception
     * 创建流操作失败异常
     *
     * @param operation the operation name | 操作名称
     * @param cause     the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException streamOperationFailed(String operation, Throwable cause) {
        return new OpenIOOperationException(operation, null,
                String.format("Stream operation failed: %s", operation), cause);
    }

    /**
     * Creates a size limit exceeded exception
     * 创建大小超限异常
     *
     * @param maxSize    the max size | 最大大小
     * @param actualSize the actual size | 实际大小
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException sizeLimitExceeded(long maxSize, long actualSize) {
        return new OpenIOOperationException("read", null,
                String.format("Size limit exceeded: max %d bytes, actual %d bytes", maxSize, actualSize));
    }

    /**
     * Creates an invalid path exception
     * 创建无效路径异常
     *
     * @param path  the path | 路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException invalidPath(String path, Throwable cause) {
        return new OpenIOOperationException("path", path,
                String.format("Invalid path: %s", path), cause);
    }

    /**
     * Creates a watch failed exception
     * 创建监听失败异常
     *
     * @param path  the path | 路径
     * @param cause the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException watchFailed(Path path, Throwable cause) {
        return new OpenIOOperationException("watch", path.toString(),
                String.format("Failed to watch: %s", path), cause);
    }

    /**
     * Creates a checksum failed exception
     * 创建校验和计算失败异常
     *
     * @param algorithm the algorithm | 算法
     * @param cause     the cause | 原因
     * @return exception instance | 异常实例
     */
    public static OpenIOOperationException checksumFailed(String algorithm, Throwable cause) {
        return new OpenIOOperationException("checksum", null,
                String.format("Failed to calculate checksum with algorithm: %s", algorithm), cause);
    }
}
