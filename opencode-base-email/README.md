# OpenCode Base Email

Email utility library for JDK 25+ with SMTP/IMAP/POP3 support, template rendering, async sending/receiving, and real-time monitoring.

## Features

- Send plain text, HTML, and template-based emails
- Attachment support: file, byte array, and inline attachments
- Async sending and receiving with CompletableFuture
- Batch sending with retry and exponential backoff
- Receive emails via IMAP and POP3
- Email query and filtering (by date, subject, sender, flags, etc.)
- Email management: mark read/unread, flag, delete, move to folder
- Real-time email monitoring via IMAP IDLE
- Template engine with variable substitution
- DKIM signing support
- Rate limiting (per-minute, per-hour, per-day, per-recipient)
- Global and per-instance configuration
- Fluent builder API for emails, configs, and queries
- Thread-safe

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-email</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenEmail` | Facade class -- unified entry point for email sending and receiving |
| `Email` | Email message model with builder |
| `EmailConfig` | SMTP sender configuration with builder |
| `EmailReceiveConfig` | IMAP/POP3 receiver configuration with builder |
| `ReceivedEmail` | Received email message model |
| `SendResult` | Send result containing message ID |
| `EmailFlags` | Email flag definitions |
| **Attachments** | |
| `Attachment` | Attachment interface |
| `FileAttachment` | File-based attachment |
| `ByteArrayAttachment` | Byte array attachment |
| `InlineAttachment` | Inline/embedded attachment (for HTML emails) |
| **Senders** | |
| `SmtpEmailSender` | SMTP email sender implementation |
| `AsyncEmailSender` | Async email sender with retry support |
| **Receivers** | |
| `ImapEmailReceiver` | IMAP email receiver |
| `Pop3EmailReceiver` | POP3 email receiver |
| `AsyncEmailReceiver` | Async email receiver |
| **Query** | |
| `EmailQuery` | Email query with builder for filtering |
| `EmailFolder` | Email folder definitions |
| **Listener** | |
| `EmailIdleMonitor` | Real-time email monitoring via IMAP IDLE |
| `EmailListener` | Email event listener interface |
| **Template** | |
| `SimpleEmailTemplate` | Simple variable substitution template engine |
| **Security** | |
| `DkimConfig` | DKIM configuration |
| `DkimSigner` | DKIM email signing |
| `EmailRateLimiter` | Rate limiter with global and per-recipient limits |
| `EmailSecurity` | Email security utilities |
| **Retry** | |
| `EmailRetryExecutor` | Retry executor with exponential backoff |
| **Internal** | |
| `EmailSender` | Email sender interface |
| `EmailReceiver` | Email receiver interface |
| `EmailTemplate` | Template engine interface |
| **Exceptions** | |
| `EmailException` | General email exception |
| `EmailSendException` | Send-specific exception |
| `EmailReceiveException` | Receive-specific exception |
| `EmailConfigException` | Configuration exception |
| `EmailSecurityException` | Security-related exception |
| `EmailTemplateException` | Template rendering exception |
| `EmailErrorCode` | Error code enum |

## Quick Start

```java
import cloud.opencode.base.email.OpenEmail;
import cloud.opencode.base.email.EmailConfig;

// Configure SMTP
OpenEmail.configure(EmailConfig.builder()
    .host("smtp.example.com")
    .port(587)
    .username("user@example.com")
    .password("password")
    .starttls(true)
    .defaultFrom("noreply@example.com", "System")
    .build());

// Send simple text email
OpenEmail.sendText("user@example.com", "Hello", "Hello World!");

// Send HTML email
OpenEmail.sendHtml("user@example.com", "Welcome",
    "<h1>Welcome</h1><p>Thanks for signing up!</p>");

// Send template email
OpenEmail.sendTemplate("user@example.com", "Order Confirmation",
    "order-confirm.html",
    Map.of("orderNo", "12345", "amount", "99.00"));

// Send with builder (with attachment)
OpenEmail.send(OpenEmail.email()
    .to("user@example.com")
    .subject("Report")
    .html("<p>See attached report</p>")
    .attach(Path.of("report.pdf"))
    .build());

// Async send
OpenEmail.sendAsync(email)
    .thenRun(() -> System.out.println("Sent!"));

// Configure receiver and receive unread emails
OpenEmail.configureReceiver("imap.example.com", "user", "pass", true);
List<ReceivedEmail> unread = OpenEmail.receiveUnread();

// Real-time monitoring
var monitor = OpenEmail.createMonitor(event ->
    System.out.println("New email: " + event.subject()));
monitor.start();
```

## Requirements

- Java 25+

## License

Apache License 2.0
