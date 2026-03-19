package cloud.opencode.base.hash.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Hash operation exception
 * 哈希操作异常
 *
 * <p>Exception thrown when hash operations fail, including algorithm not supported,
 * invalid input, illegal state, and other hash-related errors.</p>
 * <p>哈希操作失败时抛出的异常，包括算法不支持、无效输入、非法状态和其他哈希相关错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Algorithm information - 算法信息</li>
 *   <li>Operation context - 操作上下文</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch hash operation failures
 * // 捕获哈希操作失败
 * try {
 *     hashFunction.hash(data);
 * } catch (OpenHashException e) {
 *     log.error("Hash failed: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public class OpenHashException extends OpenException {

    private static final String COMPONENT = "hash";

    /**
     * Algorithm name
     */
    private final String algorithm;

    /**
     * Operation type
     */
    private final String operation;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates exception with message
     * 使用消息创建异常
     *
     * @param message error message | 错误消息
     */
    public OpenHashException(String message) {
        super(COMPONENT, null, message, null);
        this.algorithm = null;
        this.operation = null;
    }

    /**
     * Creates exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message error message | 错误消息
     * @param cause   underlying cause | 底层原因
     */
    public OpenHashException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.algorithm = null;
        this.operation = null;
    }

    /**
     * Creates exception with algorithm and operation context
     * 使用算法和操作上下文创建异常
     *
     * @param algorithm algorithm name | 算法名称
     * @param operation operation type | 操作类型
     * @param message   error message | 错误消息
     */
    public OpenHashException(String algorithm, String operation, String message) {
        super(COMPONENT, null, message, null);
        this.algorithm = algorithm;
        this.operation = operation;
    }

    /**
     * Creates exception with algorithm, operation, and cause
     * 使用算法、操作和原因创建异常
     *
     * @param algorithm algorithm name | 算法名称
     * @param operation operation type | 操作类型
     * @param message   error message | 错误消息
     * @param cause     underlying cause | 底层原因
     */
    public OpenHashException(String algorithm, String operation, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.algorithm = algorithm;
        this.operation = operation;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the algorithm name
     * 获取算法名称
     *
     * @return algorithm name or null | 算法名称或null
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Gets the operation type
     * 获取操作类型
     *
     * @return operation type or null | 操作类型或null
     */
    public String operation() {
        return operation;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an algorithm not supported exception
     * 创建算法不支持异常
     *
     * @param algorithm the unsupported algorithm | 不支持的算法
     * @return exception instance | 异常实例
     */
    public static OpenHashException algorithmNotSupported(String algorithm) {
        return new OpenHashException(algorithm, "init",
                String.format("Hash algorithm not supported: %s", algorithm));
    }

    /**
     * Creates an invalid input exception
     * 创建无效输入异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenHashException invalidInput(String message) {
        return new OpenHashException(null, "hash",
                String.format("Invalid input: %s", message));
    }

    /**
     * Creates an illegal state exception
     * 创建非法状态异常
     *
     * @param message error message | 错误消息
     * @return exception instance | 异常实例
     */
    public static OpenHashException illegalState(String message) {
        return new OpenHashException(null, null,
                String.format("Illegal state: %s", message));
    }

    /**
     * Creates a hash computation failed exception
     * 创建哈希计算失败异常
     *
     * @param algorithm algorithm name | 算法名称
     * @param cause     underlying cause | 底层原因
     * @return exception instance | 异常实例
     */
    public static OpenHashException hashFailed(String algorithm, Throwable cause) {
        return new OpenHashException(algorithm, "hash",
                String.format("Hash computation failed for algorithm: %s", algorithm), cause);
    }

    /**
     * Creates an invalid bloom filter configuration exception
     * 创建无效布隆过滤器配置异常
     *
     * @param reason the reason | 原因
     * @return exception instance | 异常实例
     */
    public static OpenHashException invalidBloomFilterConfig(String reason) {
        return new OpenHashException(null, "bloomFilter",
                String.format("Invalid bloom filter configuration: %s", reason));
    }

    /**
     * Creates a node not found exception
     * 创建节点不存在异常
     *
     * @param nodeId node id | 节点ID
     * @return exception instance | 异常实例
     */
    public static OpenHashException nodeNotFound(String nodeId) {
        return new OpenHashException(null, "consistentHash",
                String.format("Node not found: %s", nodeId));
    }

    /**
     * Creates a hasher already used exception
     * 创建Hasher已使用异常
     *
     * @return exception instance | 异常实例
     */
    public static OpenHashException hasherAlreadyUsed() {
        return new OpenHashException(null, "hash",
                "Hasher has already been used; create a new one");
    }
}
