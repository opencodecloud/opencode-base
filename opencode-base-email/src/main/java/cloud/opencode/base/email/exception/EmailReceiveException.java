package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.ReceivedEmail;

/**
 * Email Receive Exception
 * 邮件接收异常
 *
 * <p>Exception thrown when email receiving fails.</p>
 * <p>邮件接收失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Connection failed - 连接失败</li>
 *   <li>Authentication failed - 认证失败</li>
 *   <li>Folder not found - 文件夹未找到</li>
 *   <li>Receive timeout - 接收超时</li>
 *   <li>Message parse failed - 消息解析失败</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Folder and message context tracking - 文件夹和消息上下文跟踪</li>
 *   <li>Factory methods for common receive errors - 常见接收错误的工厂方法</li>
 *   <li>Error code support - 错误码支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailReceiveException extends EmailException {

    private final ReceivedEmail receivedEmail;
    private final String folder;
    private final String messageId;

    /**
     * Create receive exception with message
     * 使用消息创建接收异常
     *
     * @param message the error message | 错误消息
     */
    public EmailReceiveException(String message) {
        super(message, EmailErrorCode.UNKNOWN);
        this.receivedEmail = null;
        this.folder = null;
        this.messageId = null;
    }

    /**
     * Create receive exception with message and error code
     * 使用消息和错误码创建接收异常
     *
     * @param message   the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public EmailReceiveException(String message, EmailErrorCode errorCode) {
        super(message, errorCode);
        this.receivedEmail = null;
        this.folder = null;
        this.messageId = null;
    }

    /**
     * Create receive exception with message and cause
     * 使用消息和原因创建接收异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailReceiveException(String message, Throwable cause) {
        super(message, cause);
        this.receivedEmail = null;
        this.folder = null;
        this.messageId = null;
    }

    /**
     * Create receive exception with message, cause and error code
     * 使用消息、原因和错误码创建接收异常
     *
     * @param message   the error message | 错误消息
     * @param cause     the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public EmailReceiveException(String message, Throwable cause, EmailErrorCode errorCode) {
        super(message, cause, null, errorCode);
        this.receivedEmail = null;
        this.folder = null;
        this.messageId = null;
    }

    /**
     * Create receive exception with full context
     * 使用完整上下文创建接收异常
     *
     * @param message       the error message | 错误消息
     * @param cause         the cause | 原因
     * @param errorCode     the error code | 错误码
     * @param folder        the folder name | 文件夹名称
     * @param messageId     the message ID | 消息ID
     * @param receivedEmail the partial received email | 部分接收的邮件
     */
    public EmailReceiveException(String message, Throwable cause, EmailErrorCode errorCode,
                                  String folder, String messageId, ReceivedEmail receivedEmail) {
        super(message, cause, null, errorCode);
        this.folder = folder;
        this.messageId = messageId;
        this.receivedEmail = receivedEmail;
    }

    /**
     * Get the partially received email (if available)
     * 获取部分接收的邮件（如果可用）
     *
     * @return the received email or null | 接收的邮件或null
     */
    public ReceivedEmail getReceivedEmail() {
        return receivedEmail;
    }

    /**
     * Get the folder where error occurred
     * 获取发生错误的文件夹
     *
     * @return the folder name or null | 文件夹名称或null
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Get the message ID related to the error
     * 获取与错误相关的消息ID
     *
     * @return the message ID or null | 消息ID或null
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Create exception for folder not found
     * 创建文件夹未找到异常
     *
     * @param folder the folder name | 文件夹名称
     * @return the exception | 异常
     */
    public static EmailReceiveException folderNotFound(String folder) {
        return new EmailReceiveException(
                "Folder not found: " + folder,
                null,
                EmailErrorCode.FOLDER_NOT_FOUND,
                folder,
                null,
                null
        );
    }

    /**
     * Create exception for message not found
     * 创建消息未找到异常
     *
     * @param messageId the message ID | 消息ID
     * @return the exception | 异常
     */
    public static EmailReceiveException messageNotFound(String messageId) {
        return new EmailReceiveException(
                "Message not found: " + messageId,
                null,
                EmailErrorCode.MESSAGE_NOT_FOUND,
                null,
                messageId,
                null
        );
    }

    /**
     * Create exception for connection lost
     * 创建连接丢失异常
     *
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static EmailReceiveException connectionLost(Throwable cause) {
        return new EmailReceiveException(
                "Connection lost to mail server",
                cause,
                EmailErrorCode.CONNECTION_LOST
        );
    }

    /**
     * Create exception for receive timeout
     * 创建接收超时异常
     *
     * @return the exception | 异常
     */
    public static EmailReceiveException timeout() {
        return new EmailReceiveException(
                "Receive operation timed out",
                EmailErrorCode.RECEIVE_TIMEOUT
        );
    }

    /**
     * Create exception for message parse failure
     * 创建消息解析失败异常
     *
     * @param messageId the message ID | 消息ID
     * @param cause     the cause | 原因
     * @return the exception | 异常
     */
    public static EmailReceiveException parseFailed(String messageId, Throwable cause) {
        return new EmailReceiveException(
                "Failed to parse message: " + messageId,
                cause,
                EmailErrorCode.MESSAGE_PARSE_FAILED,
                null,
                messageId,
                null
        );
    }
}
