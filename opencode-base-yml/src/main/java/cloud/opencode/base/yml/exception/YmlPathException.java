package cloud.opencode.base.yml.exception;

/**
 * YAML Path Exception - Thrown when path access fails
 * YAML 路径异常 - 当路径访问失败时抛出
 *
 * <p>This exception is thrown when a path does not exist or is invalid.</p>
 * <p>当路径不存在或无效时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tracks the failing path for diagnostics - 跟踪失败路径以供诊断</li>
 *   <li>Factory methods for index-out-of-bounds and type-mismatch errors - 索引越界和类型不匹配错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     String value = PathResolver.get(data, "missing.path");
 * } catch (YmlPathException e) {
 *     System.err.println("Path not found: " + e.getPath());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (path may be null) - 空值安全: 否（路径可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public class YmlPathException extends OpenYmlException {

    private final String path;

    /**
     * Constructs a path exception for missing path.
     * 为缺失路径构造路径异常。
     *
     * @param path the missing path | 缺失的路径
     */
    public YmlPathException(String path) {
        super("Path not found: " + path);
        this.path = path;
    }

    /**
     * Constructs a path exception with message.
     * 构造带消息的路径异常。
     *
     * @param path    the path | 路径
     * @param message the detail message | 详细消息
     */
    public YmlPathException(String path, String message) {
        super(message);
        this.path = path;
    }

    /**
     * Constructs a path exception with message and cause.
     * 构造带消息和原因的路径异常。
     *
     * @param path    the path | 路径
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public YmlPathException(String path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    /**
     * Gets the path that caused the exception.
     * 获取导致异常的路径。
     *
     * @return the path | 路径
     */
    public String getPath() {
        return path;
    }

    /**
     * Creates an exception for index out of bounds.
     * 为索引越界创建异常。
     *
     * @param path  the path | 路径
     * @param index the index | 索引
     * @param size  the sequence size | 序列大小
     * @return the exception | 异常
     */
    public static YmlPathException indexOutOfBounds(String path, int index, int size) {
        return new YmlPathException(path,
            String.format("Index %d out of bounds for sequence at '%s' (size: %d)", index, path, size));
    }

    /**
     * Creates an exception for type mismatch.
     * 为类型不匹配创建异常。
     *
     * @param path     the path | 路径
     * @param expected the expected type | 期望类型
     * @param actual   the actual type | 实际类型
     * @return the exception | 异常
     */
    public static YmlPathException typeMismatch(String path, String expected, String actual) {
        return new YmlPathException(path,
            String.format("Type mismatch at '%s': expected %s but found %s", path, expected, actual));
    }
}
