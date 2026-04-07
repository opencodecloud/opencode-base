package cloud.opencode.base.feature.exception;

/**
 * Feature Error Code
 * 功能错误码
 *
 * <p>Error codes for feature-related exceptions.</p>
 * <p>功能相关异常的错误码。</p>
 *
 * <p><strong>Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>0 - Unknown errors | 未知错误</li>
 *   <li>1xxx - Feature errors | 功能错误</li>
 *   <li>2xxx - Configuration errors | 配置错误</li>
 *   <li>3xxx - Store errors | 存储错误</li>
 *   <li>4xxx - Security errors | 安全错误</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes: general, storage, strategy, security - 分类错误代码：通用、存储、策略、安全</li>
 *   <li>Bilingual error messages (English and Chinese) - 双语错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureErrorCode value = FeatureErrorCode.values()[0];
 * // Use in switch or comparisons
 * // 在switch或比较中使用
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public enum FeatureErrorCode {

    // General errors
    UNKNOWN(0, "Unknown error", "未知错误"),

    // Feature errors (1xxx)
    NOT_FOUND(1001, "Feature not found", "功能不存在"),
    ALREADY_EXISTS(1002, "Feature already exists", "功能已存在"),
    INVALID_KEY(1003, "Invalid feature key", "无效的功能键"),
    EXPIRED(1004, "Feature expired", "功能已过期"),
    GROUP_NOT_FOUND(1005, "Feature group not found", "功能组不存在"),

    // Configuration errors (2xxx)
    INVALID_STRATEGY(2001, "Invalid strategy", "无效策略"),
    INVALID_CONTEXT(2002, "Invalid context", "无效上下文"),
    INVALID_CONFIG(2003, "Invalid configuration", "无效配置"),

    // Store errors (3xxx)
    STORE_ERROR(3001, "Store error", "存储错误"),
    PERSIST_FAILED(3002, "Persist failed", "持久化失败"),
    LOAD_FAILED(3003, "Load failed", "加载失败"),

    // Security errors (4xxx)
    UNAUTHORIZED(4001, "Unauthorized operation", "未授权操作"),
    AUDIT_FAILED(4002, "Audit logging failed", "审计日志失败"),
    SECURITY_VIOLATION(4003, "Security violation", "安全违规");

    private final int code;
    private final String message;
    private final String messageZh;

    FeatureErrorCode(int code, String message, String messageZh) {
        this.code = code;
        this.message = message;
        this.messageZh = messageZh;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get error message
     * 获取错误消息
     *
     * @return error message | 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get Chinese error message
     * 获取中文错误消息
     *
     * @return Chinese message | 中文消息
     */
    public String getMessageZh() {
        return messageZh;
    }

    /**
     * Get description (code: message)
     * 获取描述 (code: message)
     *
     * @return description | 描述
     */
    public String getDescription() {
        return code + ": " + message;
    }
}
