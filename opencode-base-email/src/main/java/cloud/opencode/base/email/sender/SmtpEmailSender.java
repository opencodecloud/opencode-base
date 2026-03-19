package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.exception.*;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.security.DkimSigner;
import cloud.opencode.base.email.security.EmailSecurity;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

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
    private final Session session;

    /**
     * Create SMTP sender with configuration
     * 使用配置创建SMTP发送器
     *
     * @param config the email configuration | 邮件配置
     */
    public SmtpEmailSender(EmailConfig config) {
        this.config = config;
        this.session = createSession();
    }

    @Override
    public void send(Email email) {
        sendWithResult(email);
    }

    @Override
    public SendResult sendWithResult(Email email) {
        try {
            MimeMessage message = createMessage(email);

            // Apply DKIM signature if configured
            if (config.hasDkim()) {
                DkimSigner.sign(message, config.dkim());
            }

            // Use try-with-resources for Transport to ensure proper cleanup
            String protocol = config.ssl() ? "smtps" : "smtp";
            try (Transport transport = session.getTransport(protocol)) {
                if (config.requiresAuth()) {
                    String credential = config.hasOAuth2() ? config.oauth2Token() : config.password();
                    transport.connect(config.host(), config.port(), config.username(), credential);
                } else {
                    transport.connect();
                }
                transport.sendMessage(message, message.getAllRecipients());
            }

            // Get message ID from the sent message
            String messageId = message.getMessageID();
            return SendResult.success(messageId);
        } catch (AuthenticationFailedException e) {
            throw new EmailSendException("Authentication failed", e, email, EmailErrorCode.AUTH_FAILED);
        } catch (SendFailedException e) {
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e, email,
                    EmailErrorCode.MESSAGE_REJECTED);
        } catch (MessagingException e) {
            EmailErrorCode errorCode = EmailErrorCode.fromException(e);
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e, email, errorCode);
        }
    }

    /**
     * Create mail session
     */
    private Session createSession() {
        Properties props = new Properties();

        // Basic settings
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));

        // Timeout settings
        String timeoutMs = String.valueOf(config.timeout().toMillis());
        String connectionTimeoutMs = String.valueOf(config.connectionTimeout().toMillis());
        props.put("mail.smtp.connectiontimeout", connectionTimeoutMs);
        props.put("mail.smtp.timeout", timeoutMs);
        props.put("mail.smtp.writetimeout", timeoutMs);

        // Authentication
        if (config.requiresAuth()) {
            props.put("mail.smtp.auth", "true");

            // OAuth2 XOAUTH2 mechanism for Gmail/Outlook
            if (config.hasOAuth2()) {
                props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            }
        }

        // SSL/TLS settings
        if (config.ssl()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(config.port()));
        } else if (config.starttls()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        // Debug mode
        if (config.debug()) {
            props.put("mail.debug", "true");
        }

        // Create session with authenticator if needed
        if (config.requiresAuth()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // Use OAuth2 token if configured, otherwise use password
                    String credential = config.hasOAuth2()
                            ? config.oauth2Token()
                            : config.password();
                    return new PasswordAuthentication(config.username(), credential);
                }
            });
        }

        return Session.getInstance(props);
    }

    /**
     * Create MIME message from email
     */
    private MimeMessage createMessage(Email email) throws MessagingException {
        MimeMessage message = new MimeMessage(session);

        // Set from address
        String from = email.from() != null ? email.from() : config.defaultFrom();
        String fromName = email.fromName() != null ? email.fromName() : config.defaultFromName();

        if (from == null) {
            throw new EmailConfigException("Sender email address is required");
        }

        try {
            if (fromName != null && !fromName.isBlank()) {
                message.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            } else {
                message.setFrom(new InternetAddress(from));
            }
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(from));
        }

        // Set recipients
        addRecipients(message, Message.RecipientType.TO, email.to());
        addRecipients(message, Message.RecipientType.CC, email.cc());
        addRecipients(message, Message.RecipientType.BCC, email.bcc());

        // Set reply-to
        if (email.replyTo() != null && !email.replyTo().isBlank()) {
            message.setReplyTo(new InternetAddress[]{new InternetAddress(email.replyTo())});
        }

        // Set subject (sanitized)
        String subject = EmailSecurity.sanitizeHeader(email.subject());
        message.setSubject(subject, "UTF-8");

        // Set priority
        if (email.priority() != null && email.priority() != Email.Priority.NORMAL) {
            message.setHeader("X-Priority", String.valueOf(email.priority().getValue()));
        }

        // Set custom headers
        if (email.headers() != null) {
            for (var entry : email.headers().entrySet()) {
                String headerName = EmailSecurity.sanitizeHeader(entry.getKey());
                String headerValue = EmailSecurity.sanitizeHeader(entry.getValue());
                message.setHeader(headerName, headerValue);
            }
        }

        // Set date and message ID
        message.setSentDate(new Date());
        message.setHeader("Message-ID", generateMessageId());

        // Set content
        setContent(message, email);

        return message;
    }

    /**
     * Add recipients to message
     */
    private void addRecipients(MimeMessage message, Message.RecipientType type,
                               java.util.List<String> addresses) throws MessagingException {
        if (addresses == null || addresses.isEmpty()) {
            return;
        }
        for (String address : addresses) {
            if (address != null && !address.isBlank()) {
                message.addRecipient(type, new InternetAddress(address));
            }
        }
    }

    /**
     * Set message content
     */
    private void setContent(MimeMessage message, Email email) throws MessagingException {
        boolean hasAttachments = email.hasAttachments();
        boolean hasInlineAttachments = email.hasInlineAttachments();

        if (!hasAttachments && !hasInlineAttachments) {
            // Simple message
            if (email.html()) {
                message.setContent(email.content(), "text/html; charset=UTF-8");
            } else {
                message.setText(email.content(), "UTF-8");
            }
        } else if (hasInlineAttachments && email.html()) {
            // HTML with inline images
            MimeMultipart multipart = new MimeMultipart("related");

            // Add HTML content
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(email.content(), "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // Add inline attachments
            for (Attachment attachment : email.attachments()) {
                if (attachment.isInline()) {
                    MimeBodyPart attachmentPart = createAttachmentPart(attachment);
                    attachmentPart.setDisposition(MimeBodyPart.INLINE);
                    attachmentPart.setHeader("Content-ID", "<" + attachment.getContentId() + ">");
                    multipart.addBodyPart(attachmentPart);
                }
            }

            // Wrap in mixed if there are regular attachments too
            if (email.attachments().stream().anyMatch(a -> !a.isInline())) {
                MimeMultipart mixedMultipart = new MimeMultipart("mixed");

                // Add related part
                MimeBodyPart relatedPart = new MimeBodyPart();
                relatedPart.setContent(multipart);
                mixedMultipart.addBodyPart(relatedPart);

                // Add regular attachments
                for (Attachment attachment : email.attachments()) {
                    if (!attachment.isInline()) {
                        mixedMultipart.addBodyPart(createAttachmentPart(attachment));
                    }
                }

                message.setContent(mixedMultipart);
            } else {
                message.setContent(multipart);
            }
        } else {
            // Mixed content with attachments
            MimeMultipart multipart = new MimeMultipart("mixed");

            // Add body content
            MimeBodyPart bodyPart = new MimeBodyPart();
            if (email.html()) {
                bodyPart.setContent(email.content(), "text/html; charset=UTF-8");
            } else {
                bodyPart.setText(email.content(), "UTF-8");
            }
            multipart.addBodyPart(bodyPart);

            // Add attachments
            for (Attachment attachment : email.attachments()) {
                multipart.addBodyPart(createAttachmentPart(attachment));
            }

            message.setContent(multipart);
        }
    }

    /**
     * Create attachment body part
     */
    private MimeBodyPart createAttachmentPart(Attachment attachment) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();

        // Use data handler
        part.setDataHandler(new jakarta.activation.DataHandler(
                new jakarta.activation.DataSource() {
                    @Override
                    public java.io.InputStream getInputStream() {
                        return attachment.getInputStream();
                    }

                    @Override
                    public java.io.OutputStream getOutputStream() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public String getContentType() {
                        return attachment.getContentType();
                    }

                    @Override
                    public String getName() {
                        return attachment.getFileName();
                    }
                }
        ));

        try {
            part.setFileName(MimeUtility.encodeText(attachment.getFileName(), "UTF-8", "B"));
        } catch (UnsupportedEncodingException e) {
            part.setFileName(attachment.getFileName());
        }

        return part;
    }

    /**
     * Generate message ID
     */
    private String generateMessageId() {
        String domain = "localhost";
        if (config.defaultFrom() != null && config.defaultFrom().contains("@")) {
            domain = config.defaultFrom().substring(config.defaultFrom().indexOf('@') + 1);
        }
        return "<" + UUID.randomUUID() + "@" + domain + ">";
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

    /**
     * Get mail session
     * 获取邮件会话
     *
     * @return the session | 会话
     */
    public Session getSession() {
        return session;
    }
}
