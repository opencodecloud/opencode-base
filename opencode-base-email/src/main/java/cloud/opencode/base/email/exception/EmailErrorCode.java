package cloud.opencode.base.email.exception;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Email Error Code Enumeration
 * 邮件错误码枚举
 *
 * <p>Defines all error codes for email operations.</p>
 * <p>定义所有邮件操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes - 分类的错误码</li>
 *   <li>Retryable flag for automatic retry - 可重试标志用于自动重试</li>
 *   <li>Exception mapping support - 异常映射支持</li>
 * </ul>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>1xxx - Configuration errors - 配置错误</li>
 *   <li>2xxx - Connection errors - 连接错误</li>
 *   <li>3xxx - Sending errors - 发送错误</li>
 *   <li>4xxx - Security errors - 安全错误</li>
 *   <li>5xxx - Template errors - 模板错误</li>
 *   <li>6xxx - Receiving errors - 接收错误</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailErrorCode code = EmailErrorCode.fromException(cause);
 * if (code.isRetryable()) {
 *     // Schedule retry
 * }
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public enum EmailErrorCode {

    /**
     * Unknown error - 未知错误
     */
    UNKNOWN(0, "Unknown error", "未知错误", false),

    /**
     * Invalid configuration - 配置无效
     */
    CONFIG_INVALID(1001, "Invalid configuration", "配置无效", false),

    /**
     * Authentication failed - 认证失败
     */
    AUTH_FAILED(1002, "Authentication failed", "认证失败", false),

    /**
     * Connection failed - 连接失败
     */
    CONNECTION_FAILED(2001, "Connection failed", "连接失败", true),

    /**
     * Connection timeout - 连接超时
     */
    CONNECTION_TIMEOUT(2002, "Connection timeout", "连接超时", true),

    /**
     * Send timeout - 发送超时
     */
    SEND_TIMEOUT(2003, "Send timeout", "发送超时", true),

    /**
     * Recipient rejected - 收件人被拒绝
     */
    RECIPIENT_REJECTED(3001, "Recipient rejected", "收件人被拒绝", false),

    /**
     * Message rejected - 邮件被拒绝
     */
    MESSAGE_REJECTED(3002, "Message rejected", "邮件被拒绝", false),

    /**
     * Mailbox full - 邮箱已满
     */
    MAILBOX_FULL(3003, "Mailbox full", "邮箱已满", true),

    /**
     * Rate limited - 发送频率超限
     */
    RATE_LIMITED(3004, "Rate limited", "发送频率超限", true),

    /**
     * Header injection detected - 邮件头注入
     */
    HEADER_INJECTION(4001, "Header injection detected", "邮件头注入", false),

    /**
     * Invalid attachment - 无效附件
     */
    INVALID_ATTACHMENT(4002, "Invalid attachment", "无效附件", false),

    /**
     * Template error - 模板错误
     */
    TEMPLATE_ERROR(5001, "Template error", "模板错误", false),

    // ==================== Receiving errors (6xxx) ====================

    /**
     * Folder not found - 文件夹未找到
     */
    FOLDER_NOT_FOUND(6001, "Folder not found", "文件夹未找到", false),

    /**
     * Message not found - 消息未找到
     */
    MESSAGE_NOT_FOUND(6002, "Message not found", "消息未找到", false),

    /**
     * Receive timeout - 接收超时
     */
    RECEIVE_TIMEOUT(6003, "Receive timeout", "接收超时", true),

    /**
     * Folder access denied - 文件夹访问被拒绝
     */
    FOLDER_ACCESS_DENIED(6004, "Folder access denied", "文件夹访问被拒绝", false),

    /**
     * IDLE not supported - IDLE不支持
     */
    IDLE_NOT_SUPPORTED(6005, "IDLE not supported", "IDLE不支持", false),

    /**
     * Protocol not supported - 协议不支持
     */
    PROTOCOL_NOT_SUPPORTED(6006, "Protocol not supported", "协议不支持", false),

    /**
     * Attachment download failed - 附件下载失败
     */
    ATTACHMENT_DOWNLOAD_FAILED(6007, "Attachment download failed", "附件下载失败", true),

    /**
     * Message parse failed - 消息解析失败
     */
    MESSAGE_PARSE_FAILED(6008, "Message parse failed", "消息解析失败", false),

    /**
     * Connection lost - 连接丢失
     */
    CONNECTION_LOST(6009, "Connection lost", "连接丢失", true);

    private final int code;
    private final String description;
    private final String descriptionCn;
    private final boolean retryable;

    EmailErrorCode(int code, String description, String descriptionCn, boolean retryable) {
        this.code = code;
        this.description = description;
        this.descriptionCn = descriptionCn;
        this.retryable = retryable;
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
     * Check if error is retryable
     * 检查错误是否可重试
     *
     * @return true if retryable | 可重试返回true
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Get error code from exception
     * 从异常获取错误码
     *
     * @param e the exception | 异常
     * @return the error code | 错误码
     */
    public static EmailErrorCode fromException(Throwable e) {
        if (e == null) {
            return UNKNOWN;
        }
        // Check for ProtocolException authentication failures
        if (e instanceof cloud.opencode.base.email.protocol.ProtocolException pe) {
            if (pe.isAuthenticationFailure()) {
                return AUTH_FAILED;
            }
            if (pe.isTimeout()) {
                return CONNECTION_TIMEOUT;
            }
            if (pe.isConnectionFailure()) {
                return CONNECTION_FAILED;
            }
        }
        if (e instanceof SocketTimeoutException) {
            return CONNECTION_TIMEOUT;
        }
        if (e instanceof ConnectException) {
            return CONNECTION_FAILED;
        }
        return UNKNOWN;
    }
}
