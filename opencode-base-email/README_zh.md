# OpenCode Base Email

面向 JDK 25+ 的邮件工具库，支持 SMTP/IMAP/POP3、模板渲染、异步发送/接收和实时监控。

## 功能特性

- 发送纯文本、HTML 和模板邮件
- 附件支持：文件、字节数组、内联附件
- CompletableFuture 异步发送和接收
- 批量发送，支持重试和指数退避
- 通过 IMAP 和 POP3 接收邮件
- 邮件查询和过滤（按日期、主题、发件人、标记等）
- 邮件管理：标记已读/未读、标记、删除、移动到文件夹
- 通过 IMAP IDLE 实时邮件监控
- 模板引擎，支持变量替换
- DKIM 签名支持
- 频率限制（每分钟、每小时、每天、每收件人）
- 全局和实例级配置
- 流式构建器 API（邮件、配置、查询）
- 线程安全

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-email</artifactId>
    <version>1.0.0</version>
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

// 异步发送
OpenEmail.sendAsync(email)
    .thenRun(() -> System.out.println("发送成功！"));

// 配置接收器并接收未读邮件
OpenEmail.configureReceiver("imap.example.com", "user", "pass", true);
List<ReceivedEmail> unread = OpenEmail.receiveUnread();

// 实时监控
var monitor = OpenEmail.createMonitor(event ->
    System.out.println("新邮件: " + event.subject()));
monitor.start();
```

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0
