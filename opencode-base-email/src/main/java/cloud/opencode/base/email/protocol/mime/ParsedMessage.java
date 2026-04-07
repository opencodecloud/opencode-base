package cloud.opencode.base.email.protocol.mime;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Parsed MIME Message Record
 * 已解析的 MIME 消息记录
 *
 * <p>Immutable result of parsing a raw RFC 2822 MIME message. Contains all
 * extracted headers, body content (plain text and/or HTML), sent/received dates,
 * and attachments.</p>
 * <p>解析原始 RFC 2822 MIME 消息的不可变结果。包含所有提取的邮件头、消息体内容
 * （纯文本和/或HTML）、发送/接收日期和附件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extracted addresses (from, to, cc, bcc) - 提取的地址</li>
 *   <li>Decoded subject (RFC 2047) - 解码的主题</li>
 *   <li>Plain text and HTML body - 纯文本和HTML正文</li>
 *   <li>Attachment data with metadata - 附件数据及元数据</li>
 *   <li>All raw headers - 所有原始邮件头</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Fields may be null for absent values - 缺失值的字段可能为null</li>
 * </ul>
 *
 * @param messageId    the Message-ID header value | Message-ID 邮件头值
 * @param from         the sender email address | 发件人邮箱地址
 * @param fromName     the sender display name (nullable) | 发件人显示名称
 * @param to           the recipient addresses | 收件人地址列表
 * @param cc           the CC addresses | 抄送地址列表
 * @param bcc          the BCC addresses | 密送地址列表
 * @param replyTo      the Reply-To address (nullable) | 回复地址
 * @param subject      the decoded subject | 解码后的主题
 * @param textContent  the plain text body (nullable) | 纯文本正文
 * @param htmlContent  the HTML body (nullable) | HTML正文
 * @param sentDate     the sent date (from Date header, nullable) | 发送日期
 * @param receivedDate the received date (from Received header, nullable) | 接收日期
 * @param size         the total message size in bytes | 消息总字节大小
 * @param headers      all parsed headers as name-value pairs | 所有解析的邮件头
 * @param attachments  the parsed attachments | 解析的附件列表
 *
 * @author Leon Soo
 * @see MimeParser
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public record ParsedMessage(
        String messageId,
        String from,
        String fromName,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String replyTo,
        String subject,
        String textContent,
        String htmlContent,
        Instant sentDate,
        Instant receivedDate,
        int size,
        Map<String, String> headers,
        List<ParsedAttachment> attachments
) {

    /**
     * Parsed Attachment Record
     * 已解析的附件记录
     *
     * <p>Contains the decoded binary data and metadata for a single
     * MIME attachment extracted from a parsed message.</p>
     * <p>包含从已解析消息中提取的单个 MIME 附件的解码二进制数据和元数据。</p>
     *
     * @param fileName    the attachment file name (nullable if not specified) | 附件文件名
     * @param contentType the MIME content type | MIME 内容类型
     * @param data        the decoded attachment data | 解码后的附件数据
     * @param inline      true if the attachment is inline (Content-Disposition: inline) | 是否内嵌附件
     * @param contentId   the Content-ID for inline attachments (nullable) | 内嵌附件的 Content-ID
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-email V1.0.3
     */
    public record ParsedAttachment(
            String fileName,
            String contentType,
            byte[] data,
            boolean inline,
            String contentId
    ) {}
}
