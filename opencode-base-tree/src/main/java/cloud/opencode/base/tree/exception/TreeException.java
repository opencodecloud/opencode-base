package cloud.opencode.base.tree.exception;

/**
 * Tree Exception
 * 树异常
 *
 * <p>Base exception for tree operations.</p>
 * <p>树操作的基础异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for tree operations - 树操作的基础异常</li>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
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
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class TreeException extends RuntimeException {

    private final String code;

    public TreeException(String message) {
        super(message);
        this.code = TreeErrorCode.OPERATION_FAILED.getCode();
    }

    public TreeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public TreeException(TreeErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public TreeException(TreeErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public TreeException(String message, Throwable cause) {
        super(message, cause);
        this.code = TreeErrorCode.OPERATION_FAILED.getCode();
    }

    public TreeException(TreeErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    public String getCode() {
        return code;
    }

    // Factory methods

    public static TreeException buildFailed(String message) {
        return new TreeException(TreeErrorCode.BUILD_FAILED, message);
    }

    public static TreeException invalidNode(String message) {
        return new TreeException(TreeErrorCode.INVALID_NODE, message);
    }

    public static TreeException duplicateId(Object id) {
        return new TreeException(TreeErrorCode.DUPLICATE_ID, "Duplicate node ID: " + id);
    }

    public static TreeException parentNotFound(Object parentId) {
        return new TreeException(TreeErrorCode.PARENT_NOT_FOUND, "Parent not found: " + parentId);
    }

    public static TreeException nodeNotFound(Object id) {
        return new TreeException(TreeErrorCode.NODE_NOT_FOUND, "Node not found: " + id);
    }

    public static TreeException maxDepthExceeded(int maxDepth) {
        return new TreeException(TreeErrorCode.MAX_DEPTH_EXCEEDED, "Max depth exceeded: " + maxDepth);
    }
}
