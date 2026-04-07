package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.BatchSendResult;
import cloud.opencode.base.email.ConnectionTestResult;
import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.exception.*;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.protocol.ProtocolException;
import cloud.opencode.base.email.protocol.mime.MimeBuilder;
import cloud.opencode.base.email.protocol.smtp.SmtpClient;
import cloud.opencode.base.email.security.DkimSigner;
import cloud.opencode.base.email.security.EmailSecurity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SMTP Email Sender
 * SMTP邮件发送器
 *
 * <p>Email sender implementation using SMTP/SMTPS protocol.</p>
 * <p>使用SMTP/SMTPS协议的邮件发送器实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SMTP/SMTPS/STARTTLS support - SMTP/SMTPS/STARTTLS支持</li>
 *   <li>Authentication support - 认证支持</li>
 *   <li>Attachments and inline images - 附件和内嵌图片</li>
 *   <li>HTML and plain text content - HTML和纯文本内容</li>
 *   <li>Custom headers - 自定义邮件头</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic authentication
 * EmailConfig config = EmailConfig.builder()
 *     .host("smtp.example.com")
 *     .port(587)
 *     .username("user@example.com")
 *     .password("password")
 *     .starttls(true)
 *     .build();
 *
 * // OAuth2 authentication (Gmail/Outlook)
 * EmailConfig oauthConfig = EmailConfig.builder()
 *     .host("smtp.gmail.com")
 *     .port(587)
 *     .username("user@gmail.com")
 *     .oauth2Token(accessToken)
 *     .starttls(true)
 *     .build();
 *
 * SmtpEmailSender sender = new SmtpEmailSender(config);
 * sender.send(email);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class SmtpEmailSender implements EmailSender {

    private final EmailConfig config;

    /**
     * Create SMTP sender with configuration
     * 使用配置创建SMTP发送器
     *
     * @param config the email configuration | 邮件配置
     */
    public SmtpEmailSender(EmailConfig config) {
        this.config = config;
    }

    @Override
    public void send(Email email) {
        sendWithResult(email);
    }

    @Override
    public SendResult sendWithResult(Email email) {
        try {
            // Build the raw MIME message
            String rawMessage = buildRawMessage(email);

            // Apply DKIM signature if configured
            if (config.hasDkim()) {
                rawMessage = DkimSigner.sign(rawMessage, config.dkim());
            }

            // Collect all recipients for RCPT TO
            List<String> allRecipients = collectRecipients(email);

            // Resolve sender address
            String sender = resolveSender(email);

            // Create SmtpClient, connect, authenticate, send, quit
            try (SmtpClient smtp = createSmtpClient()) {
                smtp.connect();
                authenticate(smtp);
                smtp.sendMessage(sender, allRecipients, rawMessage);
                smtp.quit();
            }

            // Extract message ID from raw message
            String messageId = MimeBuilder.getMessageId(rawMessage);
            return SendResult.success(messageId);
        } catch (ProtocolException e) {
            throw convertProtocolException(e, email);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e, email,
                    EmailErrorCode.UNKNOWN);
        }
    }

    /**
     * Send multiple emails using a single SMTP connection
     * 使用单个SMTP连接发送多封邮件
     *
     * <p>Reuses the same Transport connection for all emails in the batch,
     * reducing TCP handshake and TLS negotiation overhead.</p>
     * <p>对批量中所有邮件复用同一个Transport连接，减少TCP握手和TLS协商开销。</p>
     *
     * @param emails the emails to send | 要发送的邮件
     * @return the batch result | 批量结果
     */
    public BatchSendResult sendBatch(List<Email> emails) {
        if (emails == null || emails.isEmpty()) {
            return new BatchSendResult(List.of(), Instant.now(), Duration.ZERO);
        }

        Instant startedAt = Instant.now();
        List<BatchSendResult.ItemResult> results = new ArrayList<>(emails.size());

        try (SmtpClient smtp = createSmtpClient()) {
            smtp.connect();
            authenticate(smtp);

            for (Email email : emails) {
                try {
                    // Build the raw MIME message
                    String rawMessage = buildRawMessage(email);

                    // Apply DKIM signature if configured
                    if (config.hasDkim()) {
                        rawMessage = DkimSigner.sign(rawMessage, config.dkim());
                    }

                    // Collect all recipients for RCPT TO
                    List<String> allRecipients = collectRecipients(email);

                    // Resolve sender address
                    String sender = resolveSender(email);

                    smtp.sendMessage(sender, allRecipients, rawMessage);
                    String messageId = MimeBuilder.getMessageId(rawMessage);
                    results.add(BatchSendResult.ItemResult.success(email, messageId));
                } catch (Exception e) {
                    results.add(BatchSendResult.ItemResult.failure(email, e));
                }
            }

            smtp.quit();
        } catch (ProtocolException e) {
            // Connection-level failure: mark all remaining unsent as failed
            for (int i = results.size(); i < emails.size(); i++) {
                results.add(BatchSendResult.ItemResult.failure(emails.get(i), e));
            }
        } catch (Exception e) {
            // Connection-level failure: mark all remaining unsent as failed
            for (int i = results.size(); i < emails.size(); i++) {
                results.add(BatchSendResult.ItemResult.failure(emails.get(i), e));
            }
        }

        Duration duration = Duration.between(startedAt, Instant.now());
        return new BatchSendResult(List.copyOf(results), startedAt, duration);
    }

    /**
     * Test SMTP server connection and authentication
     * 测试SMTP服务器连接和认证
     *
     * <p>Attempts to connect to the SMTP server and authenticate,
     * then immediately disconnects. Useful for validating configuration.</p>
     * <p>尝试连接SMTP服务器并认证，然后立即断开。用于验证配置。</p>
     *
     * @return the test result | 测试结果
     */
    public ConnectionTestResult testConnection() {
        long startNanos = System.nanoTime();

        try (SmtpClient smtp = createSmtpClient()) {
            smtp.connect();
            if (config.requiresAuth()) {
                authenticate(smtp);
            }
            smtp.quit();

            long elapsedNanos = System.nanoTime() - startNanos;
            Duration latency = Duration.ofNanos(elapsedNanos);
            return ConnectionTestResult.success("Connected to " + config.host() + ":" + config.port(), latency);
        } catch (Exception e) {
            long elapsedNanos = System.nanoTime() - startNanos;
            Duration latency = Duration.ofNanos(elapsedNanos);
            return ConnectionTestResult.failure(e.getMessage(), e, latency);
        }
    }

    /**
     * Get email configuration
     * 获取邮件配置
     *
     * @return the configuration | 配置
     */
    public EmailConfig getConfig() {
        return config;
    }

    // ========== Internal helpers ==========

    /**
     * Create a new SmtpClient from the current configuration
     */
    private SmtpClient createSmtpClient() {
        return new SmtpClient(
                config.host(),
                config.port(),
                config.ssl(),
                config.starttls(),
                config.connectionTimeout(),
                config.timeout()
        );
    }

    /**
     * Authenticate with the SMTP server based on configuration
     */
    private void authenticate(SmtpClient smtp) throws ProtocolException {
        if (!config.requiresAuth()) {
            return;
        }
        if (config.hasOAuth2()) {
            smtp.authXOAuth2(config.username(), config.oauth2Token());
        } else {
            smtp.authPlain(config.username(), config.password());
        }
    }

    /**
     * Build a raw MIME message string from an Email object
     */
    private String buildRawMessage(Email email) {
        // Resolve sender address and name
        String from = email.from() != null ? email.from() : config.defaultFrom();
        String fromName = email.fromName() != null ? email.fromName() : config.defaultFromName();

        if (from == null) {
            throw new EmailConfigException("Sender email address is required");
        }

        // Sanitize subject and custom headers
        String subject = EmailSecurity.sanitizeHeader(email.subject());
        Map<String, String> sanitizedHeaders = null;
        if (email.headers() != null && !email.headers().isEmpty()) {
            sanitizedHeaders = new java.util.LinkedHashMap<>();
            for (var entry : email.headers().entrySet()) {
                String headerName = EmailSecurity.sanitizeHeader(entry.getKey());
                String headerValue = EmailSecurity.sanitizeHeader(entry.getValue());
                sanitizedHeaders.put(headerName, headerValue);
            }
        }

        // Resolve content fields for MimeBuilder
        String textContent = null;
        String htmlContent = null;
        boolean htmlFlag = email.html();
        String content = email.content();

        if (email.hasAlternativeContent()) {
            // Both text and HTML content provided
            textContent = email.textContent();
            htmlContent = email.content();
        }

        // Build attachment data list
        List<MimeBuilder.AttachmentData> attachments = null;
        if (email.hasAttachments()) {
            attachments = new ArrayList<>(email.attachments().size());
            for (Attachment attachment : email.attachments()) {
                byte[] data = readAttachmentData(attachment);
                attachments.add(new MimeBuilder.AttachmentData(
                        attachment.getFileName(),
                        attachment.getContentType(),
                        data,
                        attachment.isInline(),
                        attachment.getContentId()
                ));
            }
        }

        // Determine priority value
        int priority = 3; // NORMAL
        if (email.priority() != null && email.priority() != Email.Priority.NORMAL) {
            priority = email.priority().getValue();
        }

        // Determine domain for Message-ID generation
        String domain = "localhost";
        if (from.contains("@")) {
            domain = from.substring(from.indexOf('@') + 1);
        }

        // Build the raw MIME message
        return MimeBuilder.buildMessage(
                from,
                fromName,
                email.to(),
                email.cc(),
                email.bcc(),
                email.replyTo(),
                subject,
                textContent,
                htmlContent,
                htmlFlag,
                content,
                attachments,
                sanitizedHeaders,
                priority,
                domain
        );
    }

    /**
     * Read attachment data into a byte array
     */
    private static byte[] readAttachmentData(Attachment attachment) {
        try (InputStream is = attachment.getInputStream()) {
            int initialSize = attachment.getSize() > 0
                    ? (int) Math.min(attachment.getSize(), Integer.MAX_VALUE)
                    : 65536;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(initialSize);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new EmailSendException("Failed to read attachment: " + attachment.getFileName(),
                    e);
        }
    }

    /**
     * Resolve the sender address from Email or config defaults
     */
    private String resolveSender(Email email) {
        String from = email.from() != null ? email.from() : config.defaultFrom();
        if (from == null) {
            throw new EmailConfigException("Sender email address is required");
        }
        return from;
    }

    /**
     * Collect all recipients (to + cc + bcc) into a single list for RCPT TO
     */
    private static List<String> collectRecipients(Email email) {
        int capacity = (email.to() != null ? email.to().size() : 0)
                + (email.cc() != null ? email.cc().size() : 0)
                + (email.bcc() != null ? email.bcc().size() : 0);
        List<String> recipients = new ArrayList<>(capacity);
        if (email.to() != null) {
            for (String addr : email.to()) {
                if (addr != null && !addr.isBlank()) {
                    recipients.add(addr);
                }
            }
        }
        if (email.cc() != null) {
            for (String addr : email.cc()) {
                if (addr != null && !addr.isBlank()) {
                    recipients.add(addr);
                }
            }
        }
        if (email.bcc() != null) {
            for (String addr : email.bcc()) {
                if (addr != null && !addr.isBlank()) {
                    recipients.add(addr);
                }
            }
        }
        return recipients;
    }

    /**
     * Convert a ProtocolException to the appropriate EmailSendException
     */
    private static EmailSendException convertProtocolException(ProtocolException e, Email email) {
        if (e.isAuthenticationFailure()) {
            return new EmailSendException("Authentication failed", e, email, EmailErrorCode.AUTH_FAILED);
        }
        if (e.isTimeout()) {
            return new EmailSendException("Connection timeout", e, email, EmailErrorCode.CONNECTION_TIMEOUT);
        }
        if (e.isConnectionFailure()) {
            return new EmailSendException("Connection failed", e, email, EmailErrorCode.CONNECTION_FAILED);
        }
        return new EmailSendException("Failed to send email: " + e.getMessage(), e, email,
                EmailErrorCode.MESSAGE_REJECTED);
    }
}
