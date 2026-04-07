package cloud.opencode.base.tree.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Tree Exception - Base exception for tree operations
 * 树异常 - 树操作的基础异常
 *
 * <p>Extends {@link OpenException} to integrate with the unified OpenCode exception hierarchy.</p>
 * <p>继承 {@link OpenException} 以集成统一的 OpenCode 异常体系。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for tree operations - 树操作的基础异常</li>
 *   <li>Error code support via {@link TreeErrorCode} - 通过 TreeErrorCode 支持错误码</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 *   <li>Integrates with OpenException (component="Tree") - 集成 OpenException（组件="Tree"）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw TreeException.duplicateId(nodeId);
 * throw TreeException.nodeNotFound(nodeId);
 * throw TreeException.maxDepthExceeded(1000);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 是（构造后不可变）</li>
 *   <li>Null-safe: No (message should not be null) - 否（消息不应为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class TreeException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Tree";

    /**
     * Creates a tree exception with message
     * 创建树异常（带消息）
     *
     * @param message the error message | 错误消息
     */
    public TreeException(String message) {
        super(COMPONENT, TreeErrorCode.OPERATION_FAILED.getCode(), message);
    }

    /**
     * Creates a tree exception with code and message
     * 创建树异常（带错误码和消息）
     *
     * @param code the error code | 错误码
     * @param message the error message | 错误消息
     */
    public TreeException(String code, String message) {
        super(COMPONENT, code, message);
    }

    /**
     * Creates a tree exception with error code enum
     * 创建树异常（带错误码枚举）
     *
     * @param errorCode the error code enum | 错误码枚举
     */
    public TreeException(TreeErrorCode errorCode) {
        super(COMPONENT, errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * Creates a tree exception with error code enum and custom message
     * 创建树异常（带错误码枚举和自定义消息）
     *
     * @param errorCode the error code enum | 错误码枚举
     * @param message the custom message | 自定义消息
     */
    public TreeException(TreeErrorCode errorCode, String message) {
        super(COMPONENT, errorCode.getCode(), message);
    }

    /**
     * Creates a tree exception with message and cause
     * 创建树异常（带消息和原因）
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public TreeException(String message, Throwable cause) {
        super(COMPONENT, TreeErrorCode.OPERATION_FAILED.getCode(), message, cause);
    }

    /**
     * Creates a tree exception with error code enum and cause
     * 创建树异常（带错误码枚举和原因）
     *
     * @param errorCode the error code enum | 错误码枚举
     * @param cause the cause | 原因
     */
    public TreeException(TreeErrorCode errorCode, Throwable cause) {
        super(COMPONENT, errorCode.getCode(), errorCode.getMessage(), cause);
    }

    /**
     * Gets the tree-specific error code
     * 获取树特定的错误码
     *
     * @return the error code string | 错误码字符串
     */
    public String getCode() {
        return getErrorCode();
    }

    // ==================== Factory methods | 工厂方法 ====================

    /**
     * Creates a build-failed exception
     * 创建构建失败异常
     *
     * @param message the error message | 错误消息
     * @return the exception | 异常
     */
    public static TreeException buildFailed(String message) {
        return new TreeException(TreeErrorCode.BUILD_FAILED, message);
    }

    /**
     * Creates an invalid-node exception
     * 创建无效节点异常
     *
     * @param message the error message | 错误消息
     * @return the exception | 异常
     */
    public static TreeException invalidNode(String message) {
        return new TreeException(TreeErrorCode.INVALID_NODE, message);
    }

    /**
     * Creates a duplicate-id exception
     * 创建重复ID异常
     *
     * @param id the duplicate ID | 重复的ID
     * @return the exception | 异常
     */
    public static TreeException duplicateId(Object id) {
        return new TreeException(TreeErrorCode.DUPLICATE_ID, "Duplicate node ID: " + id);
    }

    /**
     * Creates a parent-not-found exception
     * 创建父节点未找到异常
     *
     * @param parentId the parent ID | 父节点ID
     * @return the exception | 异常
     */
    public static TreeException parentNotFound(Object parentId) {
        return new TreeException(TreeErrorCode.PARENT_NOT_FOUND, "Parent not found: " + parentId);
    }

    /**
     * Creates a node-not-found exception
     * 创建节点未找到异常
     *
     * @param id the node ID | 节点ID
     * @return the exception | 异常
     */
    public static TreeException nodeNotFound(Object id) {
        return new TreeException(TreeErrorCode.NODE_NOT_FOUND, "Node not found: " + id);
    }

    /**
     * Creates a max-depth-exceeded exception
     * 创建超过最大深度异常
     *
     * @param maxDepth the max depth | 最大深度
     * @return the exception | 异常
     */
    public static TreeException maxDepthExceeded(int maxDepth) {
        return new TreeException(TreeErrorCode.MAX_DEPTH_EXCEEDED, "Max depth exceeded: " + maxDepth);
    }
}
