package cloud.opencode.base.email;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Received Email Record
 * 接收的邮件记录
 *
 * <p>Immutable record representing an email received via IMAP/POP3.</p>
 * <p>表示通过IMAP/POP3接收的邮件的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full email content (text and HTML) - 完整邮件内容（文本和HTML）</li>
 *   <li>Attachment support - 附件支持</li>
 *   <li>Email flags (read, answered, etc.) - 邮件标记（已读、已回复等）</li>
 *   <li>Header access - 邮件头访问</li>
 *   <li>Folder information - 文件夹信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Process received emails
 * List<ReceivedEmail> emails = OpenEmail.receiveUnread();
 * for (ReceivedEmail email : emails) {
 *     System.out.println("From: " + email.from());
 *     System.out.println("Subject: " + email.subject());
 *
 *     if (email.hasAttachments()) {
 *         for (Attachment att : email.attachments()) {
 *             saveAttachment(att);
 *         }
 *     }
 * }
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
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record ReceivedEmail(
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
        List<Attachment> attachments,
        Map<String, String> headers,
        EmailFlags flags,
        String folder,
        int messageNumber,
        long size
) {

    /**
     * Check if email has text content
     * 检查邮件是否有文本内容
     *
     * @return true if has text content | 有文本内容返回true
     */
    public boolean hasTextContent() {
        return textContent != null && !textContent.isBlank();
    }

    /**
     * Check if email has HTML content
     * 检查邮件是否有HTML内容
     *
     * @return true if has HTML content | 有HTML内容返回true
     */
    public boolean hasHtmlContent() {
        return htmlContent != null && !htmlContent.isBlank();
    }

    /**
     * Get the best available content (prefer HTML)
     * 获取最佳可用内容（优先HTML）
     *
     * @return HTML content if available, otherwise text content | HTML内容（如果可用），否则返回文本内容
     */
    public String getContent() {
        return hasHtmlContent() ? htmlContent : textContent;
    }

    /**
     * Get the best available content (prefer text)
     * 获取最佳可用内容（优先文本）
     *
     * @return text content if available, otherwise HTML content | 文本内容（如果可用），否则返回HTML内容
     */
    public String getTextOrHtmlContent() {
        return hasTextContent() ? textContent : htmlContent;
    }

    /**
     * Check if email has attachments
     * 检查邮件是否有附件
     *
     * @return true if has attachments | 有附件返回true
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * Get attachment count
     * 获取附件数量
     *
     * @return the attachment count | 附件数量
     */
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }

    /**
     * Check if email is unread
     * 检查邮件是否未读
     *
     * @return true if unread | 未读返回true
     */
    public boolean isUnread() {
        return flags != null && flags.isUnread();
    }

    /**
     * Check if email is flagged/starred
     * 检查邮件是否已标记/星标
     *
     * @return true if flagged | 已标记返回true
     */
    public boolean isFlagged() {
        return flags != null && flags.flagged();
    }

    /**
     * Check if email has been answered
     * 检查邮件是否已回复
     *
     * @return true if answered | 已回复返回true
     */
    public boolean isAnswered() {
        return flags != null && flags.answered();
    }

    /**
     * Get header value by name
     * 根据名称获取邮件头值
     *
     * @param name the header name | 邮件头名称
     * @return the header value or null | 邮件头值或null
     */
    public String getHeader(String name) {
        return headers != null ? headers.get(name) : null;
    }

    /**
     * Get all recipients (to + cc + bcc)
     * 获取所有收件人
     *
     * @return all recipient addresses | 所有收件人地址
     */
    public List<String> getAllRecipients() {
        var all = new java.util.ArrayList<String>();
        if (to != null) all.addAll(to);
        if (cc != null) all.addAll(cc);
        if (bcc != null) all.addAll(bcc);
        return List.copyOf(all);
    }

    /**
     * Builder for ReceivedEmail
     * ReceivedEmail构建器
     */
    public static class Builder {
        private String messageId;
        private String from;
        private String fromName;
        private List<String> to = List.of();
        private List<String> cc = List.of();
        private List<String> bcc = List.of();
        private String replyTo;
        private String subject;
        private String textContent;
        private String htmlContent;
        private Instant sentDate;
        private Instant receivedDate;
        private List<Attachment> attachments = List.of();
        private Map<String, String> headers = Map.of();
        private EmailFlags flags = EmailFlags.UNREAD;
        private String folder = "INBOX";
        private int messageNumber;
        private long size;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        public Builder to(List<String> to) {
            this.to = to != null ? List.copyOf(to) : List.of();
            return this;
        }

        public Builder cc(List<String> cc) {
            this.cc = cc != null ? List.copyOf(cc) : List.of();
            return this;
        }

        public Builder bcc(List<String> bcc) {
            this.bcc = bcc != null ? List.copyOf(bcc) : List.of();
            return this;
        }

        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder textContent(String textContent) {
            this.textContent = textContent;
            return this;
        }

        public Builder htmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public Builder sentDate(Instant sentDate) {
            this.sentDate = sentDate;
            return this;
        }

        public Builder receivedDate(Instant receivedDate) {
            this.receivedDate = receivedDate;
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            this.attachments = attachments != null ? List.copyOf(attachments) : List.of();
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers != null ? Map.copyOf(headers) : Map.of();
            return this;
        }

        public Builder flags(EmailFlags flags) {
            this.flags = flags != null ? flags : EmailFlags.UNREAD;
            return this;
        }

        public Builder folder(String folder) {
            this.folder = folder;
            return this;
        }

        public Builder messageNumber(int messageNumber) {
            this.messageNumber = messageNumber;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public ReceivedEmail build() {
            return new ReceivedEmail(
                    messageId,
                    from,
                    fromName,
                    to,
                    cc,
                    bcc,
                    replyTo,
                    subject,
                    textContent,
                    htmlContent,
                    sentDate,
                    receivedDate,
                    attachments,
                    headers,
                    flags,
                    folder,
                    messageNumber,
                    size
            );
        }
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }
}
