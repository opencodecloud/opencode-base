# OpenCode Base Email

面向 JDK 25+ 的零依赖邮件工具库，内置 SMTP/IMAP/POP3 协议实现，支持模板渲染、异步发送/接收和实时监控。

> **V1.0.3**: 完全移除 Jakarta Mail 依赖。所有 SMTP、IMAP、POP3 和 MIME 处理均使用纯 JDK 套接字 API 实现。

## 功能特性

- **零外部依赖** —— 内置 SMTP/IMAP/POP3 协议客户端，无需 Jakarta Mail
- 发送纯文本、HTML 和模板邮件
- **Multipart/Alternative**：同时设置文本和 HTML，邮件客户端自动选择最佳展示
- 附件支持：文件、字节数组、内联附件
- CompletableFuture 异步发送和接收（虚拟线程）
- **批量发送**：连接复用（单次 TCP/TLS 发送多封邮件）
- 重试机制，支持指数退避
- **连接健康检查**（`testConnection()`）
- 通过 IMAP 和 POP3 接收邮件
- 邮件查询和过滤（按日期、主题、发件人、标记等）
- 邮件管理：标记已读/未读、标记、删除、移动到文件夹
- 通过 IMAP IDLE 实时邮件监控
- 模板引擎，支持变量替换（`${var}` 和 `{{var}}`）
- **OAuth2 / XOAUTH2** 认证，支持 Gmail、Outlook 等
- **EmailAddress** 验证值类型（RFC 5321）
- DKIM 签名支持（RSA-SHA256）
- 频率限制（每分钟、每小时、每天、每收件人）
- **InMemoryEmailSender** 用于单元测试（无需 SMTP 服务器）
- 统一异常体系（继承自 `OpenException`）
- 流式构建器 API（邮件、配置、查询）
- 线程安全

### 安全特性

- **TLS 主机名验证** —— 所有 SSL/STARTTLS 连接均启用
- **CRLF 注入防护** —— 所有协议命令（SMTP、IMAP、POP3）和 MIME 头均受保护
- **附件路径穿越防护** —— 解析的文件名自动清洗
- **DoS 防护** —— 响应大小限制、递归深度限制、字面量大小上限
- **凭证保护** —— 密码/令牌不写入日志，`toString()` 掩码敏感字段

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-email</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenEmail` | 门面类——邮件发送和接收的统一入口 |
| `Email` | 邮件消息模型，带构建器 |
| `EmailConfig` | SMTP 发送配置，带构建器 |
| `EmailReceiveConfig` | IMAP/POP3 接收配置，带构建器 |
| `ReceivedEmail` | 接收的邮件消息模型 |
| `SendResult` | 包含消息 ID 的发送结果 |
| `BatchSendResult` | 批量发送结果，含每封邮件状态 |
| `ConnectionTestResult` | SMTP 连接测试结果 |
| `EmailAddress` | 经验证的邮箱地址值类型 |
| `EmailFlags` | 邮件标记定义 |
| **附件** | |
| `Attachment` | 附件接口 |
| `FileAttachment` | 基于文件的附件 |
| `ByteArrayAttachment` | 字节数组附件 |
| `InlineAttachment` | 内联/嵌入附件（用于 HTML 邮件） |
| **发送器** | |
| `SmtpEmailSender` | SMTP 邮件发送器实现 |
| `AsyncEmailSender` | 异步邮件发送器，支持重试 |
| **接收器** | |
| `ImapEmailReceiver` | IMAP 邮件接收器 |
| `Pop3EmailReceiver` | POP3 邮件接收器 |
| `AsyncEmailReceiver` | 异步邮件接收器 |
| **查询** | |
| `EmailQuery` | 邮件查询，带构建器用于过滤 |
| `EmailFolder` | 邮件文件夹定义 |
| **监听器** | |
| `EmailIdleMonitor` | 通过 IMAP IDLE 实时邮件监控 |
| `EmailListener` | 邮件事件监听器接口 |
| **模板** | |
| `SimpleEmailTemplate` | 简单变量替换模板引擎 |
| **安全** | |
| `DkimConfig` | DKIM 配置 |
| `DkimSigner` | DKIM 邮件签名 |
| `EmailRateLimiter` | 频率限制器，支持全局和每收件人限制 |
| `EmailSecurity` | 邮件安全工具 |
| **重试** | |
| `EmailRetryExecutor` | 重试执行器，支持指数退避 |
| **测试** | |
| `InMemoryEmailSender` | 内存邮件发送器，用于单元测试 |
| **协议层** | |
| `SmtpClient` | 内置 SMTP 协议客户端（EHLO、STARTTLS、AUTH、DATA） |
| `ImapClient` | 内置 IMAP4rev1 客户端（LOGIN、SELECT、SEARCH、FETCH、IDLE） |
| `Pop3Client` | 内置 POP3 客户端（USER/PASS、RETR、DELE、TOP） |
| `MimeBuilder` | RFC 2822 MIME 消息构建器 |
| `MimeParser` | MIME 消息解析器 |
| `MimeEncoder` | Base64 / Quoted-Printable / RFC 2047 编码 |
| `MailConnection` | 套接字/SSL 连接包装器，支持 STARTTLS |
| **内部** | |
| `EmailSender` | 邮件发送器接口 |
| `EmailReceiver` | 邮件接收器接口 |
| `EmailTemplate` | 模板引擎接口 |
| **异常** | |
| `EmailException` | 通用邮件异常 |
| `EmailSendException` | 发送相关异常 |
| `EmailReceiveException` | 接收相关异常 |
| `EmailConfigException` | 配置异常 |
| `EmailSecurityException` | 安全相关异常 |
| `EmailTemplateException` | 模板渲染异常 |
| `EmailErrorCode` | 错误码枚举 |

## 快速开始

### 发送邮件

```java
import cloud.opencode.base.email.OpenEmail;
import cloud.opencode.base.email.EmailConfig;

// 配置 SMTP
OpenEmail.configure(EmailConfig.builder()
    .host("smtp.example.com")
    .port(587)
    .username("user@example.com")
    .password("password")
    .starttls(true)
    .defaultFrom("noreply@example.com", "System")
    .build());

// 发送简单文本邮件
OpenEmail.sendText("user@example.com", "你好", "你好世界！");

// 发送 HTML 邮件
OpenEmail.sendHtml("user@example.com", "欢迎",
    "<h1>欢迎</h1><p>感谢您的注册！</p>");

// 发送模板邮件
OpenEmail.sendTemplate("user@example.com", "订单确认",
    "order-confirm.html",
    Map.of("orderNo", "12345", "amount", "99.00"));

// 使用构建器发送（带附件）
OpenEmail.send(OpenEmail.email()
    .to("user@example.com")
    .subject("报告")
    .html("<p>请查看附件报告</p>")
    .attach(Path.of("report.pdf"))
    .build());

// Multipart/Alternative（文本 + HTML 双格式）
OpenEmail.send(OpenEmail.email()
    .to("user@example.com")
    .subject("周报")
    .textAndHtml("纯文本版本", "<h1>HTML 版本</h1>")
    .build());

// 批量发送（单次连接复用）
BatchSendResult result = OpenEmail.sendBatch(List.of(email1, email2, email3));
System.out.println("发送: " + result.successCount() + "/" + result.totalCount());

// 连接健康检查
ConnectionTestResult test = OpenEmail.testConnection();
if (test.success()) {
    System.out.println("连接成功，耗时 " + test.latency().toMillis() + "ms");
}

// 异步发送
OpenEmail.sendAsync(email)
    .thenRun(() -> System.out.println("发送成功！"));
```

### OAuth2 认证（Gmail / Outlook）

```java
import cloud.opencode.base.email.EmailConfig;

EmailConfig config = EmailConfig.builder()
    .host("smtp.gmail.com")
    .port(587)
    .username("user@gmail.com")
    .oauth2Token(accessToken)  // XOAUTH2 机制
    .starttls(true)
    .build();
OpenEmail.configure(config);
```

### 接收邮件

```java
import cloud.opencode.base.email.OpenEmail;
import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.query.EmailQuery;

// 配置 IMAP 接收器
OpenEmail.configureReceiver(EmailReceiveConfig.builder()
    .host("imap.example.com")
    .username("user@example.com")
    .password("password")
    .imap()
    .ssl(true)
    .build());

// 接收未读邮件
List<ReceivedEmail> unread = OpenEmail.receiveUnread();

// 使用查询过滤
List<ReceivedEmail> results = OpenEmail.receive(EmailQuery.builder()
    .folder("INBOX")
    .unreadOnly()
    .subjectContains("发票")
    .fromDate(LocalDateTime.now().minusDays(7))
    .newestFirst()
    .limit(20)
    .build());

// 邮件管理
OpenEmail.markAsRead(email.messageId());
OpenEmail.moveToFolder(email.messageId(), "Archive");
OpenEmail.delete(email.messageId());
```

### 实时监控（IMAP IDLE）

```java
import cloud.opencode.base.email.listener.EmailIdleMonitor;
import cloud.opencode.base.email.listener.EmailListener;

EmailIdleMonitor monitor = EmailIdleMonitor.builder()
    .config(receiveConfig)
    .folder("INBOX")
    .onNewEmail(email -> System.out.println("新邮件: " + email.subject()))
    .maxReconnectAttempts(5)
    .build();

monitor.start();
// ... 应用运行中 ...
monitor.stop();
```

### DKIM 签名

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

### 频率限制

```java
// 启用默认限制（10/分钟, 100/小时, 1000/天）
OpenEmail.enableRateLimiting();

// 或自定义限制
OpenEmail.enableRateLimiting(20, 200, 2000);

// 查看配额
var quota = OpenEmail.getRateLimitQuota("user@example.com");
```

### 单元测试

```java
import cloud.opencode.base.email.testing.InMemoryEmailSender;

InMemoryEmailSender testSender = new InMemoryEmailSender();
OpenEmail.configure(config, testSender);

OpenEmail.sendText("user@test.com", "测试", "你好");

assertThat(testSender.getSentCount()).isEqualTo(1);
assertThat(testSender.getLastEmail().subject()).isEqualTo("测试");
assertThat(testSender.hasSentTo("user@test.com")).isTrue();

// 模拟发送失败
testSender.simulateFailure(email -> email.to().contains("bad@test.com"));
```

### 邮箱地址验证

```java
import cloud.opencode.base.email.EmailAddress;

EmailAddress addr = EmailAddress.of("user@example.com");
System.out.println(addr.localPart());  // "user"
System.out.println(addr.domain());     // "example.com"

boolean valid = EmailAddress.isValid("not-an-email");  // false
```

## 架构

```
opencode-base-email
  +-- protocol/           内置协议层（零外部依赖）
  |     +-- smtp/         SMTP 客户端（RFC 5321）
  |     +-- imap/         IMAP4rev1 客户端（RFC 3501）
  |     +-- pop3/         POP3 客户端（RFC 1939）
  |     +-- mime/         MIME 构建器 & 解析器（RFC 2045/2822）
  +-- sender/             SmtpEmailSender, AsyncEmailSender
  +-- receiver/           ImapEmailReceiver, Pop3EmailReceiver, AsyncEmailReceiver
  +-- listener/           EmailIdleMonitor, EmailListener
  +-- security/           DkimSigner, EmailRateLimiter, EmailSecurity
  +-- template/           SimpleEmailTemplate
  +-- query/              EmailQuery, EmailFolder
  +-- retry/              EmailRetryExecutor
  +-- testing/            InMemoryEmailSender
  +-- exception/          EmailException 异常体系
  +-- attachment/         FileAttachment, ByteArrayAttachment, InlineAttachment
```

## 环境要求

- Java 25+
- 无外部依赖（仅依赖内部模块 `opencode-base-core`）

## 开源许可

Apache License 2.0
