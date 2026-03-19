package cloud.opencode.base.email;

import java.io.InputStream;

/**
 * Email Attachment Interface
 * 邮件附件接口
 *
 * <p>Interface for email attachments with different sources.</p>
 * <p>支持不同来源的邮件附件接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File attachments - 文件附件</li>
 *   <li>Byte array attachments - 字节数组附件</li>
 *   <li>Inline attachments for HTML - HTML内嵌附件</li>
 * </ul>
 *
 * <p><strong>Implementations | 实现类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.email.attachment.FileAttachment}</li>
 *   <li>{@link cloud.opencode.base.email.attachment.ByteArrayAttachment}</li>
 *   <li>{@link cloud.opencode.base.email.attachment.InlineAttachment}</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Attachment file = new FileAttachment(Path.of("report.pdf"));
 * Attachment bytes = new ByteArrayAttachment("data.csv", csvBytes);
 * Attachment inline = InlineAttachment.of("logo", Path.of("logo.png"), "image/png");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public interface Attachment {

    /**
     * Get attachment file name
     * 获取附件文件名
     *
     * @return the file name | 文件名
     */
    String getFileName();

    /**
     * Get attachment MIME type
     * 获取附件MIME类型
     *
     * @return the MIME type | MIME类型
     */
    String getContentType();

    /**
     * Get attachment data as input stream
     * 获取附件数据输入流
     *
     * @return the input stream | 输入流
     */
    InputStream getInputStream();

    /**
     * Get attachment size in bytes
     * 获取附件大小（字节）
     *
     * @return the size in bytes | 字节大小
     */
    long getSize();

    /**
     * Check if attachment is inline (for HTML embedding)
     * 检查附件是否为内嵌（用于HTML嵌入）
     *
     * @return true if inline | 内嵌返回true
     */
    default boolean isInline() {
        return false;
    }

    /**
     * Get Content-ID for inline attachments
     * 获取内嵌附件的Content-ID
     *
     * @return the content ID or null | Content-ID或null
     */
    default String getContentId() {
        return null;
    }
}
