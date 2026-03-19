package cloud.opencode.base.tree.exception;

/**
 * Tree Error Code
 * 树错误码
 *
 * <p>Error codes for tree operations.</p>
 * <p>树操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes (build, traversal, operation, validation) - 分类错误码（构建、遍历、操作、验证）</li>
 *   <li>Bilingual error messages - 双语错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeErrorCode code = TreeErrorCode.CYCLE_DETECTED;
 * String errorCode = code.getCode();   // "TREE-4001"
 * String message = code.getMessage();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 是（枚举是不可变的）</li>
 *   <li>Null-safe: Yes - 是</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public enum TreeErrorCode {

    // Build errors (TREE-1xxx)
    BUILD_FAILED("TREE-1001", "Tree build failed | 树构建失败"),
    INVALID_NODE("TREE-1002", "Invalid node | 无效节点"),
    DUPLICATE_ID("TREE-1003", "Duplicate node ID | 节点ID重复"),
    PARENT_NOT_FOUND("TREE-1004", "Parent node not found | 父节点未找到"),

    // Traversal errors (TREE-2xxx)
    TRAVERSAL_ERROR("TREE-2001", "Traversal error | 遍历错误"),
    MAX_DEPTH_EXCEEDED("TREE-2002", "Maximum depth exceeded | 超过最大深度"),
    STACK_OVERFLOW("TREE-2003", "Stack overflow in traversal | 遍历时栈溢出"),

    // Operation errors (TREE-3xxx)
    OPERATION_FAILED("TREE-3001", "Tree operation failed | 树操作失败"),
    NODE_NOT_FOUND("TREE-3002", "Node not found | 节点未找到"),
    INVALID_OPERATION("TREE-3003", "Invalid operation | 无效操作"),

    // Validation errors (TREE-4xxx)
    CYCLE_DETECTED("TREE-4001", "Cycle detected in tree | 树中检测到循环"),
    VALIDATION_FAILED("TREE-4002", "Tree validation failed | 树验证失败"),
    NULL_ID("TREE-4003", "Node ID is null | 节点ID为空");

    private final String code;
    private final String message;

    TreeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
