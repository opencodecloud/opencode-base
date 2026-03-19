package cloud.opencode.base.email;

import cloud.opencode.base.email.attachment.FileAttachment;
import cloud.opencode.base.email.exception.EmailException;

import java.nio.file.Path;
import java.util.*;

/**
 * Email Entity Record
 * 邮件实体记录
 *
 * <p>Immutable email entity containing all email properties.</p>
 * <p>包含所有邮件属性的不可变邮件实体。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder pattern - 流畅的构建器模式</li>
 *   <li>Multiple recipients support (to/cc/bcc) - 多收件人支持</li>
 *   <li>HTML and plain text content - HTML和纯文本内容</li>
 *   <li>Attachment support - 附件支持</li>
 *   <li>Custom headers - 自定义邮件头</li>
 *   <li>Priority levels - 优先级</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple text email
 * Email email = Email.builder()
 *     .from("sender@example.com")
 *     .to("recipient@example.com")
 *     .subject("Hello")
 *     .text("Hello World!")
 *     .build();
 *
 * // HTML email with attachment
 * Email email = Email.builder()
 *     .from("sender@example.com", "Sender Name")
 *     .to("user1@example.com", "user2@example.com")
 *     .cc("manager@example.com")
 *     .subject("Monthly Report")
 *     .html("<h1>Report</h1><p>See attachment</p>")
 *     .attach(Path.of("report.pdf"))
 *     .priority(Email.Priority.HIGH)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record Email(
        String from,
        String fromName,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String subject,
        String content,
        boolean html,
        List<Attachment> attachments,
        Map<String, String> headers,
        String replyTo,
        Priority priority
) {

    /**
     * Email priority levels
     * 邮件优先级
     */
    public enum Priority {
        /**
         * High priority - 高优先级
         */
        HIGH(1),

        /**
         * Normal priority - 普通优先级
         */
        NORMAL(3),

        /**
         * Low priority - 低优先级
         */
        LOW(5);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        /**
         * Get X-Priority header value
         * 获取X-Priority邮件头值
         *
         * @return the priority value | 优先级值
         */
        public int getValue() {
            return value;
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

    /**
     * Email Builder
     * 邮件构建器
     */
    public static class Builder {
        private String from;
        private String fromName;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject;
        private String content;
        private boolean html = false;
        private final List<Attachment> attachments = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();
        private String replyTo;
        private Priority priority = Priority.NORMAL;

        /**
         * Set sender email address
         * 设置发件人邮箱地址
         *
         * @param from the sender email | 发件人邮箱
         * @return this builder | 构建器
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        /**
         * Set sender email address with display name
         * 设置发件人邮箱地址和显示名称
         *
         * @param from the sender email | 发件人邮箱
         * @param name the display name | 显示名称
         * @return this builder | 构建器
         */
        public Builder from(String from, String name) {
            this.from = from;
            this.fromName = name;
            return this;
        }

        /**
         * Add recipient email addresses
         * 添加收件人邮箱地址
         *
         * @param addresses the recipient emails | 收件人邮箱
         * @return this builder | 构建器
         */
        public Builder to(String... addresses) {
            to.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Add recipient email addresses from collection
         * 从集合添加收件人邮箱地址
         *
         * @param addresses the recipient emails | 收件人邮箱
         * @return this builder | 构建器
         */
        public Builder to(Collection<String> addresses) {
            to.addAll(addresses);
            return this;
        }

        /**
         * Add CC recipient email addresses
         * 添加抄送收件人邮箱地址
         *
         * @param addresses the CC emails | 抄送邮箱
         * @return this builder | 构建器
         */
        public Builder cc(String... addresses) {
            cc.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Add CC recipient email addresses from collection
         * 从集合添加抄送收件人邮箱地址
         *
         * @param addresses the CC emails | 抄送邮箱
         * @return this builder | 构建器
         */
        public Builder cc(Collection<String> addresses) {
            cc.addAll(addresses);
            return this;
        }

        /**
         * Add BCC recipient email addresses
         * 添加密送收件人邮箱地址
         *
         * @param addresses the BCC emails | 密送邮箱
         * @return this builder | 构建器
         */
        public Builder bcc(String... addresses) {
            bcc.addAll(Arrays.asList(addresses));
            return this;
        }

        /**
         * Add BCC recipient email addresses from collection
         * 从集合添加密送收件人邮箱地址
         *
         * @param addresses the BCC emails | 密送邮箱
         * @return this builder | 构建器
         */
        public Builder bcc(Collection<String> addresses) {
            bcc.addAll(addresses);
            return this;
        }

        /**
         * Set email subject
         * 设置邮件主题
         *
         * @param subject the subject | 主题
         * @return this builder | 构建器
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Set plain text content
         * 设置纯文本内容
         *
         * @param content the text content | 文本内容
         * @return this builder | 构建器
         */
        public Builder text(String content) {
            this.content = content;
            this.html = false;
            return this;
        }

        /**
         * Set HTML content
         * 设置HTML内容
         *
         * @param content the HTML content | HTML内容
         * @return this builder | 构建器
         */
        public Builder html(String content) {
            this.content = content;
            this.html = true;
            return this;
        }

        /**
         * Add attachment
         * 添加附件
         *
         * @param attachment the attachment | 附件
         * @return this builder | 构建器
         */
        public Builder attach(Attachment attachment) {
            attachments.add(attachment);
            return this;
        }

        /**
         * Add file attachment
         * 添加文件附件
         *
         * @param file the file path | 文件路径
         * @return this builder | 构建器
         */
        public Builder attach(Path file) {
            attachments.add(new FileAttachment(file));
            return this;
        }

        /**
         * Add multiple attachments
         * 添加多个附件
         *
         * @param attachments the attachments | 附件列表
         * @return this builder | 构建器
         */
        public Builder attachAll(Collection<Attachment> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        /**
         * Add custom header
         * 添加自定义邮件头
         *
         * @param name  the header name | 邮件头名称
         * @param value the header value | 邮件头值
         * @return this builder | 构建器
         */
        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Add multiple custom headers
         * 添加多个自定义邮件头
         *
         * @param headers the headers | 邮件头
         * @return this builder | 构建器
         */
        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Set reply-to address
         * 设置回复地址
         *
         * @param replyTo the reply-to email | 回复邮箱
         * @return this builder | 构建器
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * Set email priority
         * 设置邮件优先级
         *
         * @param priority the priority | 优先级
         * @return this builder | 构建器
         */
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Build the email
         * 构建邮件
         *
         * @return the email | 邮件
         * @throws EmailException if validation fails | 验证失败时抛出
         */
        public Email build() {
            if (to.isEmpty()) {
                throw new EmailException("At least one recipient required");
            }
            return new Email(
                    from,
                    fromName,
                    List.copyOf(to),
                    List.copyOf(cc),
                    List.copyOf(bcc),
                    subject,
                    content,
                    html,
                    List.copyOf(attachments),
                    Map.copyOf(headers),
                    replyTo,
                    priority
            );
        }
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
     * Check if email has inline attachments
     * 检查邮件是否有内嵌附件
     *
     * @return true if has inline attachments | 有内嵌附件返回true
     */
    public boolean hasInlineAttachments() {
        return attachments != null && attachments.stream().anyMatch(Attachment::isInline);
    }

    /**
     * Get all recipients (to + cc + bcc)
     * 获取所有收件人
     *
     * @return all recipient addresses | 所有收件人地址
     */
    public List<String> getAllRecipients() {
        List<String> all = new ArrayList<>();
        if (to != null) all.addAll(to);
        if (cc != null) all.addAll(cc);
        if (bcc != null) all.addAll(bcc);
        return all;
    }
}
