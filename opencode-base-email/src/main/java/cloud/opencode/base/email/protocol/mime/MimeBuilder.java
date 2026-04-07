package cloud.opencode.base.email.protocol.mime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MIME Message Builder
 * MIME 消息构建器
 *
 * <p>Builds a complete RFC 2822 MIME message as a raw string, including
 * all headers and a properly structured multipart body. This class replaces
 * Jakarta Mail's {@code MimeMessage}, {@code MimeMultipart}, and {@code MimeBodyPart}.</p>
 * <p>构建完整的 RFC 2822 MIME 消息原始字符串，包括所有邮件头和正确结构化的
 * multipart 消息体。此类替代 Jakarta Mail 的 {@code MimeMessage}、{@code MimeMultipart}
 * 和 {@code MimeBodyPart}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Plain text and HTML content - 纯文本和HTML内容</li>
 *   <li>Multipart/alternative (text + HTML) - 多部分/备选（文本+HTML）</li>
 *   <li>Regular and inline attachments - 常规和内嵌附件</li>
 *   <li>RFC 2047 encoded headers for non-ASCII - RFC 2047 编码非ASCII邮件头</li>
 *   <li>RFC 2822 date formatting - RFC 2822 日期格式化</li>
 *   <li>X-Priority support - X-Priority 支持</li>
 *   <li>Custom headers - 自定义邮件头</li>
 * </ul>
 *
 * <p><strong>Message Structure | 消息结构:</strong></p>
 * <pre>
 * Case 1: text only          → text/plain
 * Case 2: HTML only          → text/html
 * Case 3: text + HTML        → multipart/alternative { text, html }
 * Case 4: + attachments      → multipart/mixed { body, attachments... }
 * Case 5: + inline + HTML    → multipart/related { html, inline... }
 * Case 6: all combined       → multipart/mixed {
 *                                 multipart/related {
 *                                   multipart/alternative { text, html },
 *                                   inline...
 *                                 },
 *                                 attachments...
 *                               }
 * </pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless builder method) - 线程安全: 是（无状态构建方法）</li>
 *   <li>Null-safe: Partial (nullable parameters documented) - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * @see MimeEncoder
 * @see MimeParser
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class MimeBuilder {

    /** RFC 2822 date format: "EEE, dd MMM yyyy HH:mm:ss Z" */
    private static final DateTimeFormatter RFC_2822_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private static final String CRLF = "\r\n";

    private MimeBuilder() {
        // utility class
    }

    /**
     * Attachment data for building MIME messages
     * 构建 MIME 消息的附件数据
     *
     * @param fileName    the attachment file name | 附件文件名
     * @param contentType the MIME content type | MIME 内容类型
     * @param data        the attachment binary data | 附件二进制数据
     * @param inline      true for inline attachment (e.g., embedded images) | 是否内嵌附件
     * @param contentId   the Content-ID for inline attachments (nullable) | 内嵌附件的 Content-ID
     *
     * @author Leon Soo
     * @since JDK 25, opencode-base-email V1.0.3
     */
    public record AttachmentData(
            String fileName,
            String contentType,
            byte[] data,
            boolean inline,
            String contentId
    ) {}

    /**
     * Build a complete RFC 2822 MIME message
     * 构建完整的 RFC 2822 MIME 消息
     *
     * <p>Constructs headers and body according to the content types provided.
     * When both {@code textContent} and {@code htmlContent} are provided, a
     * multipart/alternative structure is created. Attachments result in
     * multipart/mixed wrapping.</p>
     * <p>根据提供的内容类型构建邮件头和消息体。当同时提供 {@code textContent} 和
     * {@code htmlContent} 时，创建 multipart/alternative 结构。附件导致
     * multipart/mixed 包装。</p>
     *
     * @param from        sender address ("Name &lt;email&gt;" or just "email") | 发件人地址
     * @param fromName    sender display name (nullable, used for RFC 2047 encoding) | 发件人显示名称
     * @param to          recipient addresses | 收件人地址列表
     * @param cc          CC addresses (nullable) | 抄送地址列表
     * @param bcc         BCC addresses (nullable) | 密送地址列表
     * @param replyTo     reply-to address (nullable) | 回复地址
     * @param subject     email subject | 邮件主题
     * @param textContent plain text body (nullable) | 纯文本正文
     * @param htmlContent HTML body (nullable) | HTML正文
     * @param htmlFlag    true if {@code content} is HTML when only one content type is used | 单一内容类型时是否为HTML
     * @param content     the main content string (used when textContent/htmlContent are not both set) | 主内容字符串
     * @param attachments attachment data list (nullable) | 附件数据列表
     * @param headers     custom headers (nullable) | 自定义邮件头
     * @param priority    X-Priority value (1=high, 3=normal, 5=low) | 优先级值
     * @param domain      domain for Message-ID generation | 消息ID生成用的域名
     * @return the complete MIME message string | 完整的 MIME 消息字符串
     */
    public static String buildMessage(
            String from,
            String fromName,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String replyTo,
            String subject,
            String textContent,
            String htmlContent,
            boolean htmlFlag,
            String content,
            List<AttachmentData> attachments,
            Map<String, String> headers,
            int priority,
            String domain) {

        String messageId = MimeEncoder.generateMessageId(domain);

        StringBuilder msg = new StringBuilder(4096);

        // ===== Headers =====
        buildHeaders(msg, from, fromName, to, cc, bcc, replyTo, subject,
                messageId, headers, priority);

        // ===== Resolve content =====
        String resolvedText = textContent;
        String resolvedHtml = htmlContent;

        // If textContent and htmlContent are both null, use 'content' + htmlFlag
        if (resolvedText == null && resolvedHtml == null && content != null) {
            if (htmlFlag) {
                resolvedHtml = content;
            } else {
                resolvedText = content;
            }
        }
        // If only htmlContent is set and textContent is null, but content is provided as text
        if (resolvedText == null && resolvedHtml != null && content != null && !htmlFlag
                && !content.equals(resolvedHtml)) {
            resolvedText = content;
        }

        boolean hasText = resolvedText != null && !resolvedText.isEmpty();
        boolean hasHtml = resolvedHtml != null && !resolvedHtml.isEmpty();
        boolean hasAlternative = hasText && hasHtml;

        List<AttachmentData> safeAttachments =
                attachments != null ? attachments : List.of();
        List<AttachmentData> regularAttachments = new ArrayList<>(safeAttachments.size());
        List<AttachmentData> inlineAttachments = new ArrayList<>(safeAttachments.size());
        for (AttachmentData a : safeAttachments) {
            if (a.inline()) {
                inlineAttachments.add(a);
            } else {
                regularAttachments.add(a);
            }
        }

        boolean hasRegularAttachments = !regularAttachments.isEmpty();
        boolean hasInlineAttachments = !inlineAttachments.isEmpty();

        // ===== Body =====
        if (!hasRegularAttachments && !hasInlineAttachments) {
            // Simple case: no attachments
            if (hasAlternative) {
                buildAlternativePart(msg, resolvedText, resolvedHtml);
            } else if (hasHtml) {
                buildHtmlPart(msg, resolvedHtml);
            } else {
                buildTextPart(msg, hasText ? resolvedText : "");
            }
        } else if (hasRegularAttachments && !hasInlineAttachments) {
            // multipart/mixed { body, regular attachments }
            String mixedBoundary = MimeEncoder.generateBoundary();
            appendContentType(msg, "multipart/mixed", mixedBoundary);
            msg.append(CRLF);

            msg.append("--").append(mixedBoundary).append(CRLF);
            if (hasAlternative) {
                buildAlternativePartInline(msg, resolvedText, resolvedHtml);
            } else if (hasHtml) {
                buildHtmlPartInline(msg, resolvedHtml);
            } else {
                buildTextPartInline(msg, hasText ? resolvedText : "");
            }

            for (AttachmentData att : regularAttachments) {
                msg.append("--").append(mixedBoundary).append(CRLF);
                buildAttachmentPart(msg, att);
            }
            msg.append("--").append(mixedBoundary).append("--").append(CRLF);

        } else if (!hasRegularAttachments) {
            // hasInlineAttachments only: multipart/related { html, inline attachments }
            if (hasAlternative) {
                // multipart/related { multipart/alternative { text, html }, inline }
                String relatedBoundary = MimeEncoder.generateBoundary();
                appendContentType(msg, "multipart/related", relatedBoundary);
                msg.append(CRLF);

                msg.append("--").append(relatedBoundary).append(CRLF);
                buildAlternativePartInline(msg, resolvedText, resolvedHtml);

                for (AttachmentData att : inlineAttachments) {
                    msg.append("--").append(relatedBoundary).append(CRLF);
                    buildAttachmentPart(msg, att);
                }
                msg.append("--").append(relatedBoundary).append("--").append(CRLF);
            } else {
                String relatedBoundary = MimeEncoder.generateBoundary();
                appendContentType(msg, "multipart/related", relatedBoundary);
                msg.append(CRLF);

                msg.append("--").append(relatedBoundary).append(CRLF);
                if (hasHtml) {
                    buildHtmlPartInline(msg, resolvedHtml);
                } else {
                    buildTextPartInline(msg, hasText ? resolvedText : "");
                }

                for (AttachmentData att : inlineAttachments) {
                    msg.append("--").append(relatedBoundary).append(CRLF);
                    buildAttachmentPart(msg, att);
                }
                msg.append("--").append(relatedBoundary).append("--").append(CRLF);
            }
        } else {
            // Both regular and inline: multipart/mixed { multipart/related { body, inline }, regular }
            String mixedBoundary = MimeEncoder.generateBoundary();
            appendContentType(msg, "multipart/mixed", mixedBoundary);
            msg.append(CRLF);

            // Related part
            msg.append("--").append(mixedBoundary).append(CRLF);
            String relatedBoundary = MimeEncoder.generateBoundary();
            msg.append("Content-Type: multipart/related;").append(CRLF);
            msg.append(" boundary=\"").append(relatedBoundary).append("\"").append(CRLF);
            msg.append(CRLF);

            msg.append("--").append(relatedBoundary).append(CRLF);
            if (hasAlternative) {
                buildAlternativePartInline(msg, resolvedText, resolvedHtml);
            } else if (hasHtml) {
                buildHtmlPartInline(msg, resolvedHtml);
            } else {
                buildTextPartInline(msg, hasText ? resolvedText : "");
            }

            for (AttachmentData att : inlineAttachments) {
                msg.append("--").append(relatedBoundary).append(CRLF);
                buildAttachmentPart(msg, att);
            }
            msg.append("--").append(relatedBoundary).append("--").append(CRLF);

            // Regular attachments
            for (AttachmentData att : regularAttachments) {
                msg.append("--").append(mixedBoundary).append(CRLF);
                buildAttachmentPart(msg, att);
            }
            msg.append("--").append(mixedBoundary).append("--").append(CRLF);
        }

        return msg.toString();
    }

    /**
     * Extract the Message-ID from a built message
     * 从已构建的消息中提取 Message-ID
     *
     * @param rawMessage the raw MIME message | 原始 MIME 消息
     * @return the Message-ID value, or null if not found | Message-ID 值，未找到返回 null
     */
    public static String getMessageId(String rawMessage) {
        if (rawMessage == null) {
            return null;
        }
        int idx = rawMessage.indexOf("Message-ID: ");
        if (idx < 0) {
            return null;
        }
        int start = idx + "Message-ID: ".length();
        int end = rawMessage.indexOf('\r', start);
        if (end < 0) {
            end = rawMessage.indexOf('\n', start);
        }
        if (end < 0) {
            end = rawMessage.length();
        }
        return rawMessage.substring(start, end).trim();
    }

    // ========== Header Building ==========

    private static void buildHeaders(
            StringBuilder msg,
            String from,
            String fromName,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String replyTo,
            String subject,
            String messageId,
            Map<String, String> headers,
            int priority) {

        // MIME-Version
        msg.append("MIME-Version: 1.0").append(CRLF);

        // Date
        msg.append("Date: ").append(ZonedDateTime.now().format(RFC_2822_DATE)).append(CRLF);

        // Message-ID
        msg.append("Message-ID: ").append(messageId).append(CRLF);

        // From (sanitize to prevent CRLF header injection)
        String safeFrom = sanitizeHeaderValue(from);
        String safeFromName = sanitizeHeaderValue(fromName);
        if (safeFromName != null && !safeFromName.isEmpty()) {
            String encodedName = MimeEncoder.encodeWord(safeFromName, "UTF-8");
            msg.append("From: ").append(encodedName).append(" <").append(safeFrom).append(">").append(CRLF);
        } else {
            msg.append("From: ").append(safeFrom).append(CRLF);
        }

        // To (sanitize each address)
        if (to != null && !to.isEmpty()) {
            appendAddressList(msg, "To", sanitizeAddresses(to));
        }

        // CC (sanitize each address)
        if (cc != null && !cc.isEmpty()) {
            appendAddressList(msg, "Cc", sanitizeAddresses(cc));
        }

        // BCC (included in message but stripped by MTA before delivery, sanitize each address)
        if (bcc != null && !bcc.isEmpty()) {
            appendAddressList(msg, "Bcc", sanitizeAddresses(bcc));
        }

        // Reply-To (sanitize to prevent CRLF header injection)
        String safeReplyTo = sanitizeHeaderValue(replyTo);
        if (safeReplyTo != null && !safeReplyTo.isEmpty()) {
            msg.append("Reply-To: ").append(safeReplyTo).append(CRLF);
        }

        // Subject (sanitize before RFC 2047 encoding)
        String safeSubject = sanitizeHeaderValue(subject);
        if (safeSubject != null) {
            msg.append("Subject: ").append(MimeEncoder.encodeWord(safeSubject, "UTF-8")).append(CRLF);
        }

        // X-Priority
        if (priority != 3) {
            msg.append("X-Priority: ").append(priority).append(CRLF);
        }

        // Custom headers (sanitize both keys and values)
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String safeKey = sanitizeHeaderValue(entry.getKey());
                String safeValue = sanitizeHeaderValue(entry.getValue());
                msg.append(safeKey).append(": ").append(safeValue).append(CRLF);
            }
        }
    }

    // ========== Body Parts ==========

    /**
     * Build a simple text/plain body (top-level Content-Type header + body).
     */
    private static void buildTextPart(StringBuilder msg, String text) {
        msg.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
        msg.append("Content-Transfer-Encoding: quoted-printable").append(CRLF);
        msg.append(CRLF);
        msg.append(MimeEncoder.encodeQuotedPrintable(text, "UTF-8")).append(CRLF);
    }

    /**
     * Build a simple text/html body (top-level Content-Type header + body).
     */
    private static void buildHtmlPart(StringBuilder msg, String html) {
        msg.append("Content-Type: text/html; charset=UTF-8").append(CRLF);
        msg.append("Content-Transfer-Encoding: quoted-printable").append(CRLF);
        msg.append(CRLF);
        msg.append(MimeEncoder.encodeQuotedPrintable(html, "UTF-8")).append(CRLF);
    }

    /**
     * Build multipart/alternative (top-level Content-Type header + parts).
     */
    private static void buildAlternativePart(StringBuilder msg, String text, String html) {
        String altBoundary = MimeEncoder.generateBoundary();
        appendContentType(msg, "multipart/alternative", altBoundary);
        msg.append(CRLF);

        msg.append("--").append(altBoundary).append(CRLF);
        buildTextPartInline(msg, text);

        msg.append("--").append(altBoundary).append(CRLF);
        buildHtmlPartInline(msg, html);

        msg.append("--").append(altBoundary).append("--").append(CRLF);
    }

    /**
     * Build text/plain part as an inline sub-part (with its own Content-Type).
     */
    private static void buildTextPartInline(StringBuilder msg, String text) {
        msg.append("Content-Type: text/plain; charset=UTF-8").append(CRLF);
        msg.append("Content-Transfer-Encoding: quoted-printable").append(CRLF);
        msg.append(CRLF);
        msg.append(MimeEncoder.encodeQuotedPrintable(text, "UTF-8")).append(CRLF);
    }

    /**
     * Build text/html part as an inline sub-part (with its own Content-Type).
     */
    private static void buildHtmlPartInline(StringBuilder msg, String html) {
        msg.append("Content-Type: text/html; charset=UTF-8").append(CRLF);
        msg.append("Content-Transfer-Encoding: quoted-printable").append(CRLF);
        msg.append(CRLF);
        msg.append(MimeEncoder.encodeQuotedPrintable(html, "UTF-8")).append(CRLF);
    }

    /**
     * Build multipart/alternative as an inline sub-part.
     */
    private static void buildAlternativePartInline(StringBuilder msg, String text, String html) {
        String altBoundary = MimeEncoder.generateBoundary();
        msg.append("Content-Type: multipart/alternative;").append(CRLF);
        msg.append(" boundary=\"").append(altBoundary).append("\"").append(CRLF);
        msg.append(CRLF);

        msg.append("--").append(altBoundary).append(CRLF);
        buildTextPartInline(msg, text);

        msg.append("--").append(altBoundary).append(CRLF);
        buildHtmlPartInline(msg, html);

        msg.append("--").append(altBoundary).append("--").append(CRLF);
    }

    /**
     * Build an attachment part (Content-Type, Content-Transfer-Encoding, Content-Disposition).
     */
    private static void buildAttachmentPart(StringBuilder msg, AttachmentData att) {
        String encodedFileName = MimeEncoder.encodeWord(att.fileName(), "UTF-8");

        msg.append("Content-Type: ").append(att.contentType());
        msg.append("; name=\"").append(encodedFileName).append("\"").append(CRLF);
        msg.append("Content-Transfer-Encoding: base64").append(CRLF);

        if (att.inline()) {
            msg.append("Content-Disposition: inline; filename=\"")
                    .append(encodedFileName).append("\"").append(CRLF);
            if (att.contentId() != null && !att.contentId().isEmpty()) {
                String cid = att.contentId();
                if (!cid.startsWith("<")) {
                    cid = "<" + cid + ">";
                }
                msg.append("Content-ID: ").append(cid).append(CRLF);
            }
        } else {
            msg.append("Content-Disposition: attachment; filename=\"")
                    .append(encodedFileName).append("\"").append(CRLF);
        }

        msg.append(CRLF);
        msg.append(MimeEncoder.encodeBase64(att.data())).append(CRLF);
    }

    /**
     * Sanitize a list of email addresses by removing CRLF and NUL characters.
     * 清理邮件地址列表，移除 CRLF 和 NUL 字符。
     */
    private static List<String> sanitizeAddresses(List<String> addresses) {
        List<String> sanitized = new ArrayList<>(addresses.size());
        for (String addr : addresses) {
            sanitized.add(sanitizeHeaderValue(addr));
        }
        return sanitized;
    }

    /**
     * Append an address list header (To, Cc, Bcc) without intermediate String allocation.
     */
    private static void appendAddressList(StringBuilder msg, String header, List<String> addresses) {
        msg.append(header).append(": ");
        for (int i = 0; i < addresses.size(); i++) {
            if (i > 0) msg.append(", ");
            msg.append(addresses.get(i));
        }
        msg.append(CRLF);
    }

    /**
     * Append a Content-Type header with boundary parameter.
     */
    private static void appendContentType(StringBuilder msg, String type, String boundary) {
        msg.append("Content-Type: ").append(type).append(";").append(CRLF);
        msg.append(" boundary=\"").append(boundary).append("\"").append(CRLF);
    }

    /**
     * Sanitize a header value by removing CR, LF, and NUL characters.
     * This prevents CRLF injection attacks.
     * 移除 CR、LF 和 NUL 字符以防止 CRLF 注入攻击。
     *
     * @param value the header value to sanitize | 要清理的邮件头值
     * @return the sanitized value, or null if input is null | 清理后的值，输入为 null 则返回 null
     */
    private static String sanitizeHeaderValue(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\r' && c != '\n' && c != '\0') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
