/**
 * OpenCode Base Email Module
 * OpenCode 基础邮件模块
 *
 * <p>Provides email sending and receiving utilities based on JDK 25 and Jakarta Mail,
 * supporting SMTP/SMTPS, IMAP, OAuth2 authentication, email templates,
 * attachments, and retry mechanisms.</p>
 * <p>提供基于 JDK 25 和 Jakarta Mail 的邮件收发工具，支持 SMTP/SMTPS、IMAP、
 * OAuth2 认证、邮件模板、附件和重试机制。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SMTP/SMTPS Email Sending - SMTP/SMTPS 邮件发送</li>
 *   <li>IMAP Email Receiving - IMAP 邮件接收</li>
 *   <li>OAuth2 Authentication - OAuth2 认证</li>
 *   <li>Email Template Engine - 邮件模板引擎</li>
 *   <li>Attachment Support - 附件支持</li>
 *   <li>Retry Mechanism - 重试机制</li>
 *   <li>Email Security (TLS, DKIM) - 邮件安全</li>
 *   <li>Event Listener - 事件监听器</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
module cloud.opencode.base.email {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional: OAuth2 support for modern email authentication
    requires static cloud.opencode.base.oauth2;

    // Jakarta Mail API (JPMS-aware since 2.1.x)
    requires jakarta.mail;

    // Export public API packages
    exports cloud.opencode.base.email;
    exports cloud.opencode.base.email.attachment;
    exports cloud.opencode.base.email.exception;
    exports cloud.opencode.base.email.listener;
    exports cloud.opencode.base.email.query;
    exports cloud.opencode.base.email.receiver;
    exports cloud.opencode.base.email.retry;
    exports cloud.opencode.base.email.security;
    exports cloud.opencode.base.email.sender;
    exports cloud.opencode.base.email.template;

    // Internal packages - not exported
    // cloud.opencode.base.email.internal
}
