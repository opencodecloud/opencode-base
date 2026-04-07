# OpenCode Base Email

Zero-dependency email library for JDK 25+ with built-in SMTP/IMAP/POP3 protocol implementations, template rendering, async sending/receiving, and real-time monitoring.

> **V1.0.3**: Removed Jakarta Mail dependency entirely. All SMTP, IMAP, POP3, and MIME handling is now implemented with pure JDK socket APIs.

## Features

- **Zero external dependencies** -- built-in SMTP/IMAP/POP3 protocol clients, no Jakarta Mail required
- Send plain text, HTML, and template-based emails
- **Multipart/alternative**: simultaneous text + HTML for optimal client rendering
- Attachment support: file, byte array, and inline attachments
- Async sending and receiving with CompletableFuture (virtual threads)
- **Batch sending** with connection reuse (single TCP/TLS for multiple emails)
- Retry with exponential backoff
- **Connection health check** (`testConnection()`)
- Receive emails via IMAP and POP3
- Email query and filtering (by date, subject, sender, flags, etc.)
- Email management: mark read/unread, flag, delete, move to folder
- Real-time email monitoring via IMAP IDLE
- Template engine with variable substitution (`${var}` and `{{var}}`)
- **OAuth2 / XOAUTH2** authentication for Gmail, Outlook, etc.
- **EmailAddress** validated value type (RFC 5321)
- DKIM signing support (RSA-SHA256)
- Rate limiting (per-minute, per-hour, per-day, per-recipient)
- **InMemoryEmailSender** for unit testing (no SMTP required)
- Unified exception hierarchy (extends `OpenException`)
- Fluent builder API for emails, configs, and queries
- Thread-safe

### Security

- **TLS hostname verification** on all SSL/STARTTLS connections
- **CRLF injection protection** on all protocol commands (SMTP, IMAP, POP3) and MIME headers
- **Attachment path traversal protection** on parsed filenames
- **DoS protection**: response size limits, recursion depth limits, literal size caps
- **Credential sanitization**: passwords/tokens never logged, `toString()` masks sensitive fields

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-email</artifactId>
    <version>1.0.3</version>
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
| `BatchSendResult` | Batch send result with per-email status |
| `ConnectionTestResult` | SMTP connection test result |
| `EmailAddress` | Validated email address value type |
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
| **Testing** | |
| `InMemoryEmailSender` | In-memory sender for unit testing |
| **Protocol** | |
| `SmtpClient` | Built-in SMTP protocol client (EHLO, STARTTLS, AUTH, DATA) |
| `ImapClient` | Built-in IMAP4rev1 client (LOGIN, SELECT, SEARCH, FETCH, IDLE) |
| `Pop3Client` | Built-in POP3 client (USER/PASS, RETR, DELE, TOP) |
| `MimeBuilder` | RFC 2822 MIME message builder |
| `MimeParser` | MIME message parser |
| `MimeEncoder` | Base64 / Quoted-Printable / RFC 2047 encoding |
| `MailConnection` | Socket/SSL connection wrapper with STARTTLS |
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

### Send Emails

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

// Multipart/alternative (text + HTML)
OpenEmail.send(OpenEmail.email()
    .to("user@example.com")
    .subject("Newsletter")
    .textAndHtml("Plain text version", "<h1>HTML version</h1>")
    .build());

// Batch sending (single connection)
BatchSendResult result = OpenEmail.sendBatch(List.of(email1, email2, email3));
System.out.println("Sent: " + result.successCount() + "/" + result.totalCount());

// Test connection
ConnectionTestResult test = OpenEmail.testConnection();
if (test.success()) {
    System.out.println("Connected in " + test.latency().toMillis() + "ms");
}

// Async send
OpenEmail.sendAsync(email)
    .thenRun(() -> System.out.println("Sent!"));
```

### OAuth2 Authentication (Gmail / Outlook)

```java
import cloud.opencode.base.email.EmailConfig;

EmailConfig config = EmailConfig.builder()
    .host("smtp.gmail.com")
    .port(587)
    .username("user@gmail.com")
    .oauth2Token(accessToken)  // XOAUTH2 mechanism
    .starttls(true)
    .build();
OpenEmail.configure(config);
```

### Receive Emails

```java
import cloud.opencode.base.email.OpenEmail;
import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.query.EmailQuery;

// Configure IMAP receiver
OpenEmail.configureReceiver(EmailReceiveConfig.builder()
    .host("imap.example.com")
    .username("user@example.com")
    .password("password")
    .imap()
    .ssl(true)
    .build());

// Receive unread emails
List<ReceivedEmail> unread = OpenEmail.receiveUnread();

// Query with filters
List<ReceivedEmail> results = OpenEmail.receive(EmailQuery.builder()
    .folder("INBOX")
    .unreadOnly()
    .subjectContains("Invoice")
    .fromDate(LocalDateTime.now().minusDays(7))
    .newestFirst()
    .limit(20)
    .build());

// Email management
OpenEmail.markAsRead(email.messageId());
OpenEmail.moveToFolder(email.messageId(), "Archive");
OpenEmail.delete(email.messageId());
```

### Real-Time Monitoring (IMAP IDLE)

```java
import cloud.opencode.base.email.listener.EmailIdleMonitor;
import cloud.opencode.base.email.listener.EmailListener;

EmailIdleMonitor monitor = EmailIdleMonitor.builder()
    .config(receiveConfig)
    .folder("INBOX")
    .onNewEmail(email -> System.out.println("New: " + email.subject()))
    .maxReconnectAttempts(5)
    .build();

monitor.start();
// ... application runs ...
monitor.stop();
```

### DKIM Signing

```java
import cloud.opencode.base.email.security.DkimConfig;

EmailConfig config = EmailConfig.builder()
    .host("smtp.example.com")
    .port(587)
    .username("user@example.com")
    .password("password")
    .starttls(true)
    .dkim(DkimConfig.load("example.com", "mail", Path.of("dkim-private.pem")))
    .build();
```

### Rate Limiting

```java
// Enable with default limits (10/min, 100/hour, 1000/day)
OpenEmail.enableRateLimiting();

// Or custom limits
OpenEmail.enableRateLimiting(20, 200, 2000);

// Check quota
var quota = OpenEmail.getRateLimitQuota("user@example.com");
```

### Unit Testing

```java
import cloud.opencode.base.email.testing.InMemoryEmailSender;

InMemoryEmailSender testSender = new InMemoryEmailSender();
OpenEmail.configure(config, testSender);

OpenEmail.sendText("user@test.com", "Test", "Hello");

assertThat(testSender.getSentCount()).isEqualTo(1);
assertThat(testSender.getLastEmail().subject()).isEqualTo("Test");
assertThat(testSender.hasSentTo("user@test.com")).isTrue();

// Simulate failures
testSender.simulateFailure(email -> email.to().contains("bad@test.com"));
```

### Validated Email Address

```java
import cloud.opencode.base.email.EmailAddress;

EmailAddress addr = EmailAddress.of("user@example.com");
System.out.println(addr.localPart());  // "user"
System.out.println(addr.domain());     // "example.com"

boolean valid = EmailAddress.isValid("not-an-email");  // false
```

## Architecture

```
opencode-base-email
  +-- protocol/           Built-in protocol layer (no external deps)
  |     +-- smtp/         SMTP client (RFC 5321)
  |     +-- imap/         IMAP4rev1 client (RFC 3501)
  |     +-- pop3/         POP3 client (RFC 1939)
  |     +-- mime/         MIME builder & parser (RFC 2045/2822)
  +-- sender/             SmtpEmailSender, AsyncEmailSender
  +-- receiver/           ImapEmailReceiver, Pop3EmailReceiver, AsyncEmailReceiver
  +-- listener/           EmailIdleMonitor, EmailListener
  +-- security/           DkimSigner, EmailRateLimiter, EmailSecurity
  +-- template/           SimpleEmailTemplate
  +-- query/              EmailQuery, EmailFolder
  +-- retry/              EmailRetryExecutor
  +-- testing/            InMemoryEmailSender
  +-- exception/          EmailException hierarchy
  +-- attachment/         FileAttachment, ByteArrayAttachment, InlineAttachment
```

## Requirements

- Java 25+
- No external dependencies (only `opencode-base-core` internal dependency)

## License

Apache License 2.0
