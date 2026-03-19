package cloud.opencode.base.email.internal;

import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.query.EmailQuery;

import java.util.List;

/**
 * Email Receiver Interface
 * 邮件接收器接口
 *
 * <p>Interface for receiving emails via IMAP/POP3 protocols.</p>
 * <p>通过IMAP/POP3协议接收邮件的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Receive emails with query support - 支持查询的邮件接收</li>
 *   <li>Email management (mark as read, delete, move) - 邮件管理（标记已读、删除、移动）</li>
 *   <li>Folder operations - 文件夹操作</li>
 *   <li>Connection lifecycle - 连接生命周期</li>
 * </ul>
 *
 * <p><strong>Implementation Notes | 实现说明:</strong></p>
 * <ul>
 *   <li>ImapEmailReceiver for IMAP protocol</li>
 *   <li>Pop3EmailReceiver for POP3 protocol</li>
 *   <li>AsyncEmailReceiver for async operations</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (EmailReceiver receiver = new ImapEmailReceiver(config)) {
 *     receiver.connect();
 *     List<ReceivedEmail> emails = receiver.receiveUnread();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public interface EmailReceiver extends AutoCloseable {

    /**
     * Receive all unread emails from default folder
     * 从默认文件夹接收所有未读邮件
     *
     * @return list of received emails | 接收到的邮件列表
     */
    List<ReceivedEmail> receiveUnread();

    /**
     * Receive emails matching the query
     * 接收符合查询条件的邮件
     *
     * @param query the email query | 邮件查询
     * @return list of received emails | 接收到的邮件列表
     */
    List<ReceivedEmail> receive(EmailQuery query);

    /**
     * Receive all emails from default folder
     * 从默认文件夹接收所有邮件
     *
     * @return list of received emails | 接收到的邮件列表
     */
    default List<ReceivedEmail> receiveAll() {
        return receive(EmailQuery.builder().build());
    }

    /**
     * Receive a specific email by message ID
     * 通过消息ID接收特定邮件
     *
     * @param messageId the message ID | 消息ID
     * @return the received email or null if not found | 接收到的邮件，未找到返回null
     */
    ReceivedEmail receiveById(String messageId);

    /**
     * Get email count in folder
     * 获取文件夹中的邮件数量
     *
     * @param folder the folder name | 文件夹名称
     * @return the email count | 邮件数量
     */
    int getMessageCount(String folder);

    /**
     * Get unread email count in folder
     * 获取文件夹中的未读邮件数量
     *
     * @param folder the folder name | 文件夹名称
     * @return the unread email count | 未读邮件数量
     */
    int getUnreadCount(String folder);

    /**
     * Mark email as read
     * 标记邮件为已读
     *
     * @param messageId the message ID | 消息ID
     */
    void markAsRead(String messageId);

    /**
     * Mark email as unread
     * 标记邮件为未读
     *
     * @param messageId the message ID | 消息ID
     */
    void markAsUnread(String messageId);

    /**
     * Mark email as flagged/starred
     * 标记邮件为星标
     *
     * @param messageId the message ID | 消息ID
     * @param flagged   true to flag, false to unflag | true为标记，false为取消标记
     */
    void setFlagged(String messageId, boolean flagged);

    /**
     * Delete email
     * 删除邮件
     *
     * @param messageId the message ID | 消息ID
     */
    void delete(String messageId);

    /**
     * Move email to another folder (IMAP only)
     * 移动邮件到另一个文件夹（仅IMAP）
     *
     * @param messageId    the message ID | 消息ID
     * @param targetFolder the target folder | 目标文件夹
     */
    void moveToFolder(String messageId, String targetFolder);

    /**
     * List all available folders
     * 列出所有可用文件夹
     *
     * @return list of folder names | 文件夹名称列表
     */
    List<String> listFolders();

    /**
     * Check if connected to mail server
     * 检查是否已连接到邮件服务器
     *
     * @return true if connected | 已连接返回true
     */
    boolean isConnected();

    /**
     * Connect to mail server
     * 连接到邮件服务器
     */
    void connect();

    /**
     * Disconnect from mail server
     * 断开与邮件服务器的连接
     */
    void disconnect();

    /**
     * Close the receiver (alias for disconnect)
     * 关闭接收器（disconnect的别名）
     */
    @Override
    default void close() {
        disconnect();
    }
}
