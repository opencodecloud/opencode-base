package cloud.opencode.base.event.exception;

/**
 * Event Error Code Enumeration
 * 事件错误码枚举
 *
 * <p>Defines all error codes for event operations.</p>
 * <p>定义所有事件操作的错误码。</p>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>0 - Unknown error - 未知错误</li>
 *   <li>1xxx - Publish errors - 发布错误</li>
 *   <li>2xxx - Listener errors - 监听器错误</li>
 *   <li>3xxx - Store errors - 存储错误</li>
 *   <li>4xxx - Security errors - 安全错误</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes - 分类错误码</li>
 *   <li>Bilingual descriptions - 双语描述</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventErrorCode code = EventErrorCode.LISTENER_ERROR;
 * System.out.println(code.getCode() + ": " + code.getDescription());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public enum EventErrorCode {

    /**
     * Unknown error - 未知错误
     */
    UNKNOWN(0, "Unknown error", "未知错误"),

    /**
     * Publish failed - 发布失败
     */
    PUBLISH_FAILED(1001, "Publish failed", "发布失败"),

    /**
     * Event cancelled - 事件被取消
     */
    EVENT_CANCELLED(1002, "Event cancelled", "事件被取消"),

    /**
     * Timeout - 超时
     */
    TIMEOUT(1003, "Timeout", "超时"),

    /**
     * Listener error - 监听器错误
     */
    LISTENER_ERROR(2001, "Listener error", "监听器错误"),

    /**
     * Registration failed - 注册失败
     */
    REGISTRATION_FAILED(2002, "Registration failed", "注册失败"),

    /**
     * Duplicate listener - 重复注册
     */
    DUPLICATE_LISTENER(2003, "Duplicate listener", "重复注册"),

    /**
     * Invalid listener method - 无效的监听器方法
     */
    INVALID_LISTENER_METHOD(2004, "Invalid listener method", "无效的监听器方法"),

    /**
     * Store error - 存储错误
     */
    STORE_ERROR(3001, "Store error", "存储错误"),

    /**
     * Persist failed - 持久化失败
     */
    PERSIST_FAILED(3002, "Persist failed", "持久化失败"),

    /**
     * Replay failed - 重放失败
     */
    REPLAY_FAILED(3003, "Replay failed", "重放失败"),

    /**
     * Verification failed - 验证失败
     */
    VERIFICATION_FAILED(4001, "Verification failed", "验证失败"),

    /**
     * Rate limited - 频率超限
     */
    RATE_LIMITED(4002, "Rate limited", "频率超限"),

    /**
     * Security violation - 安全违规
     */
    SECURITY_VIOLATION(4003, "Security violation", "安全违规");

    private final int code;
    private final String description;
    private final String descriptionCn;

    EventErrorCode(int code, String description, String descriptionCn) {
        this.code = code;
        this.description = description;
        this.descriptionCn = descriptionCn;
    }

    /**
     * Get error code number
     * 获取错误码数字
     *
     * @return the error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get error description in English
     * 获取英文错误描述
     *
     * @return the description | 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get error description in Chinese
     * 获取中文错误描述
     *
     * @return the description in Chinese | 中文描述
     */
    public String getDescriptionCn() {
        return descriptionCn;
    }

    /**
     * Get error code from exception
     * 从异常获取错误码
     *
     * @param e the exception | 异常
     * @return the error code | 错误码
     */
    public static EventErrorCode fromException(Throwable e) {
        if (e == null) {
            return UNKNOWN;
        }
        if (e instanceof InterruptedException) {
            return TIMEOUT;
        }
        return UNKNOWN;
    }
}
