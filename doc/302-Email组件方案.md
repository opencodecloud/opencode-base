# Email 组件方案

## 1. 组件概述

`opencode-base-email` 模块提供完整的邮件发送与接收能力，支持 SMTP 发送、IMAP/POP3 接收、模板引擎、附件处理、DKIM 签名、频率限制、异步操作和 IDLE 实时监听。

**核心特性：**
- SMTP/SMTPS 发送，IMAP/POP3 接收
- 纯文本/HTML/模板邮件
- 文件/字节数组/内嵌附件
- DKIM 签名、邮件头注入防护、附件安全校验
- 发送频率限制（分钟/小时/天级别）
- 异步发送与接收（虚拟线程）
- 重试机制（指数退避）
- IDLE 实时邮件监听
- OAuth2 认证支持

**模块依赖：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-email</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.email
├── OpenEmail.java                    # 门面入口类
├── Email.java                        # 邮件实体 (record)
├── EmailConfig.java                  # 发送配置 (record)
├── EmailReceiveConfig.java           # 接收配置 (record)
├── Attachment.java                   # 附件接口
├── ReceivedEmail.java                # 接收邮件实体 (record)
├── SendResult.java                   # 发送结果 (record)
├── EmailFlags.java                   # 邮件标记 (record)
│
├── sender/                           # 发送器
│   ├── SmtpEmailSender.java          # SMTP 发送器
│   └── AsyncEmailSender.java         # 异步发送器
│
├── receiver/                         # 接收器
│   ├── ImapEmailReceiver.java        # IMAP 接收器
│   ├── Pop3EmailReceiver.java        # POP3 接收器
│   └── AsyncEmailReceiver.java       # 异步接收器
│
├── listener/                         # 监听器
│   ├── EmailListener.java            # 邮件监听器接口
│   └── EmailIdleMonitor.java         # IDLE 实时监听器
│
├── query/                            # 查询
│   ├── EmailQuery.java               # 邮件查询 (record)
│   └── EmailFolder.java              # 邮件文件夹枚举
│
├── template/                         # 模板
│   └── SimpleEmailTemplate.java      # 简单模板引擎
│
├── attachment/                       # 附件实现
│   ├── FileAttachment.java           # 文件附件
│   ├── ByteArrayAttachment.java      # 字节数组附件
│   └── InlineAttachment.java         # 内嵌附件
│
├── security/                         # 安全组件
│   ├── EmailSecurity.java            # 安全工具类
│   ├── EmailRateLimiter.java         # 频率限制器
│   ├── DkimConfig.java               # DKIM 签名配置 (record)
│   └── DkimSigner.java               # DKIM 签名器
│
├── retry/                            # 重试机制
│   └── EmailRetryExecutor.java       # 重试执行器
│
├── exception/                        # 异常体系
│   ├── EmailException.java           # 邮件异常基类
│   ├── EmailErrorCode.java           # 错误码枚举
│   ├── EmailConfigException.java     # 配置异常
│   ├── EmailSendException.java       # 发送异常
│   ├── EmailReceiveException.java    # 接收异常
│   ├── EmailSecurityException.java   # 安全异常
│   └── EmailTemplateException.java   # 模板异常
│
└── internal/                         # 内部接口
    ├── EmailSender.java              # 发送器接口
    ├── EmailReceiver.java            # 接收器接口
    └── EmailTemplate.java            # 模板引擎接口
```

---

## 3. 核心 API

### 3.1 OpenEmail

> 邮件门面入口类，提供发送配置、接收配置、同步/异步发送与接收、模板发送、频率限制、IDLE 监听等所有统一操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `configure(EmailConfig config)` | 配置默认邮件发送器 |
| `configure(String host, int port, String username, String password)` | 快速配置发送器 |
| `configure(EmailConfig config, EmailSender sender)` | 使用自定义发送器配置 |
| `setTemplateEngine(EmailTemplate engine)` | 设置模板引擎 |
| `enableRateLimiting()` | 启用默认频率限制 |
| `enableRateLimiting(int maxPerMinute, int maxPerHour, int maxPerDay)` | 启用自定义频率限制 |
| `disableRateLimiting()` | 禁用频率限制 |
| `isRateLimitingEnabled()` | 检查频率限制是否启用 |
| `getRateLimitQuota()` | 获取全局剩余配额 |
| `getRateLimitQuota(String recipient)` | 获取指定收件人剩余配额 |
| `isConfigured()` | 检查是否已配置 |
| `getConfig()` | 获取当前配置 |
| `send(Email email)` | 同步发送邮件 |
| `sendWithResult(Email email)` | 发送邮件并返回结果 |
| `sendText(String to, String subject, String content)` | 发送纯文本邮件 |
| `sendHtml(String to, String subject, String htmlContent)` | 发送 HTML 邮件 |
| `sendTemplate(String to, String subject, String template, Map<String, Object> variables)` | 发送模板邮件 |
| `sendToMultiple(List<String> recipients, String subject, String content)` | 发送给多个收件人 |
| `sendAsync(Email email)` | 异步发送邮件 |
| `sendAsync(Email email, ExecutorService executor)` | 使用指定线程池异步发送 |
| `sendTextAsync(String to, String subject, String content)` | 异步发送纯文本 |
| `sendHtmlAsync(String to, String subject, String htmlContent)` | 异步发送 HTML |
| `sendAllAsync(List<Email> emails)` | 批量异步发送 |
| `sendAllAndWait(List<Email> emails)` | 批量发送并等待全部完成 |
| `configureReceiver(EmailReceiveConfig config)` | 配置邮件接收器 |
| `configureReceiver(String host, String username, String password, boolean useImap)` | 快速配置接收器 |
| `configureReceiver(EmailReceiveConfig config, EmailReceiver receiver)` | 使用自定义接收器 |
| `isReceiverConfigured()` | 检查接收器是否已配置 |
| `getReceiveConfig()` | 获取接收配置 |
| `receiveUnread()` | 接收未读邮件 |
| `receive(EmailQuery query)` | 按查询条件接收邮件 |
| `receiveAll()` | 接收所有邮件 |
| `receiveById(String messageId)` | 按消息 ID 接收邮件 |
| `getMessageCount(String folder)` | 获取文件夹邮件数 |
| `getUnreadCount(String folder)` | 获取未读邮件数 |
| `markAsRead(String messageId)` | 标记为已读 |
| `markAsUnread(String messageId)` | 标记为未读 |
| `setFlagged(String messageId, boolean flagged)` | 设置星标 |
| `delete(String messageId)` | 删除邮件 |
| `moveToFolder(String messageId, String targetFolder)` | 移动到文件夹 |
| `listFolders()` | 列出所有文件夹 |
| `receiveUnreadAsync()` | 异步接收未读邮件 |
| `receiveAsync(EmailQuery query)` | 异步按条件接收 |
| `markAsReadAsync(String messageId)` | 异步标记已读 |
| `deleteAsync(String messageId)` | 异步删除邮件 |
| `createMonitor(EmailListener listener)` | 创建 IDLE 监听器 |
| `createMonitor(String folder, EmailListener listener)` | 创建指定文件夹的 IDLE 监听器 |
| `query()` | 创建查询构建器 |
| `receiveConfig()` | 创建接收配置构建器 |
| `email()` | 创建邮件构建器 |
| `config()` | 创建发送配置构建器 |
| `shutdown()` | 关闭所有资源 |
| `shutdownSender()` | 仅关闭发送器 |
| `shutdownReceiver()` | 仅关闭接收器 |

**示例：**
```java
// 配置
OpenEmail.configure(EmailConfig.builder()
    .host("smtp.example.com")
    .port(587)
    .username("user@example.com")
    .password("password")
    .starttls(true)
    .defaultFrom("noreply@example.com", "系统通知")
    .build());

// 发送文本邮件
OpenEmail.sendText("user@example.com", "测试", "这是一封测试邮件");

// 发送 HTML 邮件
OpenEmail.sendHtml("user@example.com", "欢迎", "<h1>欢迎加入</h1>");

// 发送模板邮件
OpenEmail.sendTemplate("user@example.com", "订单确认",
    "order-confirm.html",
    Map.of("orderNo", "202401010001", "amount", "99.00"));

// 异步发送
OpenEmail.sendAsync(email)
    .thenRun(() -> System.out.println("发送成功"));

// 配置接收器并接收邮件
OpenEmail.configureReceiver("imap.example.com", "user", "pass", true);
List<ReceivedEmail> unread = OpenEmail.receiveUnread();
```

### 3.2 Email

> 邮件实体 Record，使用 Builder 模式构建，包含发件人、收件人、主题、内容、附件、头信息、优先级等所有邮件属性。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建邮件构建器 |
| `hasAttachments()` | 是否包含附件 |
| `hasInlineAttachments()` | 是否包含内嵌附件 |
| `getAllRecipients()` | 获取所有收件人（to + cc + bcc） |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `from(String from)` | 设置发件人 |
| `from(String from, String name)` | 设置发件人和显示名 |
| `to(String... addresses)` | 添加收件人 |
| `to(Collection<String> addresses)` | 添加收件人集合 |
| `cc(String... addresses)` | 添加抄送 |
| `cc(Collection<String> addresses)` | 添加抄送集合 |
| `bcc(String... addresses)` | 添加密送 |
| `bcc(Collection<String> addresses)` | 添加密送集合 |
| `subject(String subject)` | 设置主题 |
| `text(String content)` | 设置纯文本内容 |
| `html(String content)` | 设置 HTML 内容 |
| `attach(Attachment attachment)` | 添加附件 |
| `attach(Path file)` | 添加文件附件 |
| `attachAll(Collection<Attachment> attachments)` | 批量添加附件 |
| `header(String name, String value)` | 添加自定义头 |
| `headers(Map<String, String> headers)` | 批量添加头 |
| `replyTo(String replyTo)` | 设置回复地址 |
| `priority(Priority priority)` | 设置优先级 |
| `build()` | 构建邮件 |

**Priority 枚举：** `HIGH`、`NORMAL`、`LOW`

**示例：**
```java
Email email = Email.builder()
    .from("sender@example.com", "发件人")
    .to("recipient@example.com")
    .cc("cc@example.com")
    .subject("月度报告")
    .html("<p>请查收附件中的报告</p>")
    .attach(Path.of("report.pdf"))
    .priority(Email.Priority.HIGH)
    .build();
```

### 3.3 EmailConfig

> 邮件发送配置 Record，包含 SMTP 服务器信息、认证方式、连接池、超时、DKIM 签名等配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建配置构建器 |
| `requiresAuth()` | 是否需要认证 |
| `hasOAuth2()` | 是否使用 OAuth2 |
| `hasDkim()` | 是否配置了 DKIM |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `host(String host)` | SMTP 服务器地址 |
| `port(int port)` | SMTP 端口 |
| `username(String username)` | 用户名 |
| `password(String password)` | 密码 |
| `oauth2Token(String oauth2Token)` | OAuth2 令牌 |
| `ssl(boolean ssl)` | 启用 SSL |
| `starttls(boolean starttls)` | 启用 STARTTLS |
| `defaultFrom(String from)` | 默认发件人 |
| `defaultFrom(String from, String name)` | 默认发件人和显示名 |
| `timeout(Duration timeout)` | 读写超时 |
| `connectionTimeout(Duration timeout)` | 连接超时 |
| `maxRetries(int retries)` | 最大重试次数 |
| `poolSize(int size)` | 连接池大小 |
| `poolIdleTimeout(Duration timeout)` | 连接池空闲超时 |
| `debug(boolean debug)` | 启用调试模式 |
| `dkim(DkimConfig dkim)` | DKIM 签名配置 |
| `build()` | 构建配置 |

**示例：**
```java
EmailConfig config = EmailConfig.builder()
    .host("smtp.example.com")
    .port(465)
    .username("user@example.com")
    .password("password")
    .ssl(true)
    .defaultFrom("noreply@example.com", "系统通知")
    .poolSize(10)
    .connectionTimeout(Duration.ofSeconds(10))
    .maxRetries(3)
    .build();
```

### 3.4 EmailReceiveConfig

> 邮件接收配置 Record，包含 IMAP/POP3 协议选择、服务器信息、认证方式、文件夹和接收行为设置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建配置构建器 |
| `requiresAuth()` | 是否需要认证 |
| `hasOAuth2()` | 是否使用 OAuth2 |
| `getStoreProtocol()` | 获取存储协议名称 |
| `isImap()` | 是否为 IMAP 协议 |
| `isPop3()` | 是否为 POP3 协议 |

**Protocol 枚举：** `IMAP`、`POP3`

| 方法 | 描述 |
|------|------|
| `getName()` | 获取协议名称 |
| `getDefaultPort()` | 获取默认端口 |
| `getDefaultSslPort()` | 获取默认 SSL 端口 |
| `getStoreProtocol(boolean ssl)` | 获取存储协议标识 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `host(String host)` | 服务器地址 |
| `port(int port)` | 端口 |
| `username(String username)` | 用户名 |
| `password(String password)` | 密码 |
| `oauth2Token(String oauth2Token)` | OAuth2 令牌 |
| `imap()` | 使用 IMAP 协议 |
| `pop3()` | 使用 POP3 协议 |
| `protocol(Protocol protocol)` | 设置协议 |
| `ssl(boolean ssl)` | 启用 SSL |
| `starttls(boolean starttls)` | 启用 STARTTLS |
| `timeout(Duration timeout)` | 读写超时 |
| `connectionTimeout(Duration timeout)` | 连接超时 |
| `defaultFolder(String folder)` | 默认文件夹 |
| `maxMessages(int maxMessages)` | 最大接收数 |
| `deleteAfterReceive(boolean delete)` | 接收后删除 |
| `markAsReadAfterReceive(boolean markAsRead)` | 接收后标记已读 |
| `debug(boolean debug)` | 启用调试 |
| `build()` | 构建配置 |

**示例：**
```java
EmailReceiveConfig config = EmailReceiveConfig.builder()
    .host("imap.example.com")
    .imap()
    .ssl(true)
    .username("user@example.com")
    .password("password")
    .defaultFolder("INBOX")
    .maxMessages(50)
    .markAsReadAfterReceive(true)
    .build();
```

### 3.5 ReceivedEmail

> 接收到的邮件实体 Record，包含消息 ID、发件人、收件人、主题、正文内容、附件、标记、文件夹等信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建构建器 |
| `hasTextContent()` | 是否有纯文本内容 |
| `hasHtmlContent()` | 是否有 HTML 内容 |
| `getContent()` | 获取内容（优先 HTML） |
| `getTextOrHtmlContent()` | 获取内容（优先纯文本） |
| `hasAttachments()` | 是否有附件 |
| `getAttachmentCount()` | 获取附件数量 |
| `isUnread()` | 是否未读 |
| `isFlagged()` | 是否星标 |
| `isAnswered()` | 是否已回复 |
| `getHeader(String name)` | 获取指定头信息 |
| `getAllRecipients()` | 获取所有收件人 |

**示例：**
```java
List<ReceivedEmail> emails = OpenEmail.receiveUnread();
for (ReceivedEmail email : emails) {
    System.out.println("来自: " + email.from());
    System.out.println("主题: " + email.subject());
    System.out.println("内容: " + email.getTextOrHtmlContent());
    if (email.hasAttachments()) {
        System.out.println("附件数: " + email.getAttachmentCount());
    }
}
```

### 3.6 SendResult

> 邮件发送结果 Record，包含是否成功、消息 ID 和发送时间。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `success(String messageId)` | 创建成功结果 |
| `failure()` | 创建失败结果 |
| `hasMessageId()` | 是否有消息 ID |

**示例：**
```java
SendResult result = OpenEmail.sendWithResult(email);
if (result.success()) {
    System.out.println("消息 ID: " + result.messageId());
    System.out.println("发送时间: " + result.sentAt());
}
```

### 3.7 EmailFlags

> 邮件标记 Record，封装已读、星标、删除、回复、草稿、最近等状态。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `UNREAD` | 未读标记常量 |
| `READ` | 已读标记常量 |
| `from(jakarta.mail.Flags flags)` | 从 Jakarta Mail 标记创建 |
| `toMailFlags()` | 转换为 Jakarta Mail 标记 |
| `isUnread()` | 是否未读 |
| `withSeen(boolean seen)` | 设置已读状态（返回新实例） |
| `withFlagged(boolean flagged)` | 设置星标状态 |
| `withDeleted(boolean deleted)` | 设置删除状态 |
| `withAnswered(boolean answered)` | 设置已回复状态 |

### 3.8 Attachment

> 邮件附件接口，定义附件的基本属性和行为。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `getFileName()` | 获取文件名 |
| `getContentType()` | 获取 MIME 类型 |
| `getInputStream()` | 获取数据输入流 |
| `getSize()` | 获取大小（字节） |
| `isInline()` | 是否为内嵌附件（默认 false） |
| `getContentId()` | 获取 Content-ID（默认 null） |

### 3.9 FileAttachment

> 基于文件路径的附件实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `FileAttachment(Path path)` | 从路径创建 |
| `FileAttachment(Path path, String fileName)` | 指定文件名 |
| `FileAttachment(Path path, String fileName, String contentType)` | 指定文件名和类型 |
| `of(Path path)` | 工厂方法 |
| `of(String path)` | 从字符串路径创建 |
| `getPath()` | 获取文件路径 |

**示例：**
```java
Attachment attachment = new FileAttachment(Path.of("report.pdf"));
Attachment named = FileAttachment.of(Path.of("data.csv"));
```

### 3.10 ByteArrayAttachment

> 基于字节数组的附件实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ByteArrayAttachment(String fileName, byte[] data)` | 从字节数组创建 |
| `ByteArrayAttachment(String fileName, byte[] data, String contentType)` | 指定 MIME 类型 |
| `of(String fileName, byte[] data, String contentType)` | 工厂方法 |
| `of(String fileName, byte[] data)` | 工厂方法（自动推断类型） |
| `getData()` | 获取原始字节数据 |

**示例：**
```java
byte[] data = generatePdfReport();
Attachment attachment = new ByteArrayAttachment("report.pdf", data, "application/pdf");
```

### 3.11 InlineAttachment

> HTML 内嵌附件实现，用于在 HTML 邮件中通过 `cid:` 引用图片等资源。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `InlineAttachment(String contentId, String fileName, byte[] data, String contentType)` | 构造方法 |
| `of(String contentId, Path path, String contentType)` | 从文件创建 |
| `of(String contentId, String fileName, byte[] data, String contentType)` | 从字节数组创建 |
| `isInline()` | 返回 true |
| `getContentId()` | 获取 Content-ID |
| `getData()` | 获取原始字节数据 |

**示例：**
```java
InlineAttachment logo = InlineAttachment.of("logo", Path.of("logo.png"), "image/png");
String html = "<img src=\"cid:logo\" alt=\"Logo\"/>";
Email email = Email.builder()
    .to("user@example.com")
    .subject("欢迎")
    .html(html)
    .attach(logo)
    .build();
```

### 3.12 EmailQuery

> 邮件查询条件 Record，支持按文件夹、日期范围、发件人、主题、未读状态、星标、附件等条件过滤。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建查询构建器 |
| `unread()` | 快捷创建未读查询 |
| `forFolder(EmailFolder folder)` | 按文件夹查询 |
| `forFolder(String folder)` | 按文件夹名查询 |
| `hasFilters()` | 是否设置了过滤条件 |

**SortOrder 枚举：** 排序方式

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `folder(EmailFolder folder)` | 设置文件夹 |
| `folder(String folder)` | 设置文件夹名 |
| `fromDate(LocalDateTime fromDate)` | 起始日期 |
| `toDate(LocalDateTime toDate)` | 结束日期 |
| `dateRange(LocalDateTime fromDate, LocalDateTime toDate)` | 日期范围 |
| `from(String from)` | 按发件人 |
| `to(String to)` | 按收件人 |
| `subjectContains(String subject)` | 主题包含 |
| `bodyContains(String body)` | 正文包含 |
| `unreadOnly()` | 仅未读 |
| `flaggedOnly()` | 仅星标 |
| `hasAttachments()` | 有附件 |
| `includeDeleted()` | 包含已删除 |
| `limit(int limit)` | 限制数量 |
| `offset(int offset)` | 偏移量 |
| `page(int limit, int offset)` | 分页 |
| `newestFirst()` | 最新优先 |
| `oldestFirst()` | 最旧优先 |
| `build()` | 构建查询 |

**示例：**
```java
EmailQuery query = EmailQuery.builder()
    .folder(EmailFolder.INBOX)
    .unreadOnly()
    .fromDate(LocalDateTime.now().minusDays(7))
    .subjectContains("报告")
    .newestFirst()
    .limit(20)
    .build();
List<ReceivedEmail> emails = OpenEmail.receive(query);
```

### 3.13 EmailFolder

> 邮件文件夹枚举，定义常用文件夹及其别名。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `getName()` | 获取文件夹名 |
| `getAlternativeNames()` | 获取别名 |
| `getAllNames()` | 获取所有名称 |
| `fromName(String name)` | 根据名称查找 |
| `matches(String name)` | 检查名称是否匹配 |

### 3.14 SmtpEmailSender

> SMTP 邮件发送器实现，支持 SSL/STARTTLS、OAuth2 认证、DKIM 签名。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SmtpEmailSender(EmailConfig config)` | 构造方法 |
| `send(Email email)` | 同步发送邮件 |
| `sendWithResult(Email email)` | 发送并返回结果 |
| `getConfig()` | 获取配置 |
| `getSession()` | 获取 JavaMail Session |

**示例：**
```java
SmtpEmailSender sender = new SmtpEmailSender(config);
sender.send(email);
SendResult result = sender.sendWithResult(email);
```

### 3.15 AsyncEmailSender

> 异步邮件发送器，支持虚拟线程、线程池配置和重试。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建构建器 |
| `send(Email email)` | 同步发送 |
| `sendAsync(Email email)` | 异步发送 |
| `sendAsync(Email email, EmailRetryExecutor.RetryCallback callback)` | 异步发送带回调 |
| `sendAllAsync(List<Email> emails)` | 批量异步发送 |
| `sendAllAndWait(List<Email> emails)` | 批量发送并等待 |
| `close()` | 关闭资源 |
| `getDelegate()` | 获取底层发送器 |
| `getExecutor()` | 获取线程池 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `sender(EmailSender sender)` | 设置底层发送器 |
| `config(EmailConfig config)` | 设置配置 |
| `executor(ExecutorService executor)` | 自定义线程池 |
| `retryExecutor(EmailRetryExecutor retryExecutor)` | 重试执行器 |
| `corePoolSize(int corePoolSize)` | 核心线程数 |
| `maxPoolSize(int maxPoolSize)` | 最大线程数 |
| `queueCapacity(int queueCapacity)` | 队列容量 |
| `threadNamePrefix(String prefix)` | 线程名前缀 |
| `useVirtualThreads(boolean useVirtualThreads)` | 使用虚拟线程 |
| `build()` | 构建 |

**示例：**
```java
AsyncEmailSender asyncSender = AsyncEmailSender.builder()
    .config(config)
    .useVirtualThreads(true)
    .build();
asyncSender.sendAsync(email).thenRun(() -> System.out.println("发送成功"));
```

### 3.16 ImapEmailReceiver

> IMAP 邮件接收器实现，支持邮件查询、标记管理、文件夹操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ImapEmailReceiver(EmailReceiveConfig config)` | 构造方法 |
| `connect()` | 连接服务器 |
| `disconnect()` | 断开连接 |
| `isConnected()` | 检查连接状态 |
| `receiveUnread()` | 接收未读邮件 |
| `receive(EmailQuery query)` | 按条件接收 |
| `receiveById(String messageId)` | 按 ID 接收 |
| `getMessageCount(String folder)` | 获取邮件数 |
| `getUnreadCount(String folder)` | 获取未读数 |
| `markAsRead(String messageId)` | 标记已读 |
| `markAsUnread(String messageId)` | 标记未读 |
| `setFlagged(String messageId, boolean flagged)` | 设置星标 |
| `delete(String messageId)` | 删除邮件 |
| `moveToFolder(String messageId, String targetFolder)` | 移动邮件 |
| `listFolders()` | 列出文件夹 |

### 3.17 Pop3EmailReceiver

> POP3 邮件接收器实现，功能与 IMAP 类似但受 POP3 协议限制。

**主要方法：** 与 `ImapEmailReceiver` 相同的接口方法。

### 3.18 AsyncEmailReceiver

> 异步邮件接收器包装，将同步接收操作转换为异步。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建构建器 |
| `receiveUnreadAsync()` | 异步接收未读邮件 |
| `receiveAsync(EmailQuery query)` | 异步按条件接收 |
| `receiveAllAsync()` | 异步接收全部 |
| `receiveByIdAsync(String messageId)` | 异步按 ID 接收 |
| `markAsReadAsync(String messageId)` | 异步标记已读 |
| `markAsUnreadAsync(String messageId)` | 异步标记未读 |
| `setFlaggedAsync(String messageId, boolean flagged)` | 异步设置星标 |
| `deleteAsync(String messageId)` | 异步删除 |
| `moveToFolderAsync(String messageId, String targetFolder)` | 异步移动 |
| `listFoldersAsync()` | 异步列出文件夹 |
| `getMessageCountAsync(String folder)` | 异步获取邮件数 |
| `getUnreadCountAsync(String folder)` | 异步获取未读数 |
| `connectAsync()` | 异步连接 |
| `connect()` | 同步连接 |
| `disconnect()` | 断开连接 |
| `isConnected()` | 检查连接状态 |
| `close()` | 关闭资源 |

### 3.19 EmailListener

> 邮件监听器函数式接口，提供新邮件、删除、标记变更、错误、重连等回调。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `onNewEmail(ReceivedEmail email)` | 收到新邮件回调（抽象方法） |
| `onEmailDeleted(String messageId)` | 邮件删除回调 |
| `onFlagsChanged(String messageId, String flagName, boolean value)` | 标记变更回调 |
| `onError(Throwable error)` | 错误回调 |
| `onMonitoringStarted(String folder)` | 开始监听回调 |
| `onMonitoringStopped(String folder)` | 停止监听回调 |
| `onReconnecting(int attempt)` | 重连中回调 |
| `onReconnected()` | 重连成功回调 |
| `onNewEmail(Consumer<ReceivedEmail> handler)` | 创建简单监听器（静态方法） |
| `of(Consumer<ReceivedEmail> emailHandler, Consumer<Throwable> errorHandler)` | 创建含错误处理的监听器（静态方法） |

### 3.20 EmailIdleMonitor

> IDLE 实时邮件监听器，支持自动重连、多监听器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `builder()` | 创建构建器 |
| `start()` | 启动监听 |
| `stop()` | 停止监听 |
| `isRunning()` | 检查是否运行中 |
| `addListener(EmailListener listener)` | 添加监听器 |
| `removeListener(EmailListener listener)` | 移除监听器 |
| `close()` | 关闭（AutoCloseable） |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `config(EmailReceiveConfig config)` | 接收配置 |
| `folder(String folder)` | 监听文件夹 |
| `listener(EmailListener listener)` | 添加监听器 |
| `onNewEmail(Consumer<ReceivedEmail> handler)` | 新邮件处理器 |
| `idleTimeout(Duration timeout)` | IDLE 超时 |
| `maxReconnectAttempts(int attempts)` | 最大重连次数 |
| `reconnectDelay(Duration delay)` | 重连延迟 |
| `build()` | 构建 |

**示例：**
```java
EmailIdleMonitor monitor = EmailIdleMonitor.builder()
    .config(receiveConfig)
    .folder("INBOX")
    .onNewEmail(email -> System.out.println("新邮件: " + email.subject()))
    .maxReconnectAttempts(5)
    .reconnectDelay(Duration.ofSeconds(30))
    .build();
monitor.start();
```

### 3.21 SimpleEmailTemplate

> 简单模板引擎，支持变量替换、HTML 转义、文件加载和缓存。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `getInstance()` | 获取单例实例 |
| `render(String template, Map<String, Object> variables)` | 渲染模板（实例方法） |
| `renderTemplate(String template, Map<String, Object> variables)` | 渲染模板（静态方法） |
| `renderTemplate(String template, Map<String, Object> variables, boolean escapeHtml)` | 渲染并控制 HTML 转义 |
| `loadTemplate(Path path)` | 从文件加载模板 |
| `loadTemplateFromClasspath(String resourcePath)` | 从类路径加载 |
| `loadTemplateFromClasspath(String resourcePath, ClassLoader classLoader)` | 指定 ClassLoader 加载 |
| `loadTemplateCached(Path path)` | 从文件加载（带缓存） |
| `loadTemplateFromClasspathCached(String resourcePath)` | 从类路径加载（带缓存） |
| `clearCache()` | 清除模板缓存 |
| `removeFromCache(String key)` | 移除指定缓存 |
| `getCacheSize()` | 获取缓存大小 |
| `escapeHtml(String text)` | HTML 转义 |
| `createHtmlTemplate(String title, String body)` | 创建 HTML 模板 |

**示例：**
```java
String template = SimpleEmailTemplate.loadTemplateFromClasspathCached("templates/welcome.html");
String result = SimpleEmailTemplate.renderTemplate(template,
    Map.of("name", "张三", "link", "https://example.com"));
```

### 3.22 EmailSecurity

> 邮件安全工具类，提供头注入防护、地址验证、附件安全校验。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `sanitizeHeader(String value)` | 清理邮件头（防注入） |
| `isValidEmail(String email)` | 验证邮件地址格式 |
| `validateAttachment(Attachment attachment)` | 验证附件安全性（默认配置） |
| `validateAttachment(Attachment attachment, Set<String> allowedExtensions, long maxSize)` | 验证附件安全性（自定义配置） |
| `isAllowedExtension(String fileName)` | 检查扩展名是否允许 |
| `isDangerousExtension(String fileName)` | 检查是否为危险扩展名 |
| `getExtension(String fileName)` | 获取文件扩展名 |
| `getDefaultAllowedExtensions()` | 获取默认允许的扩展名集合 |
| `getDangerousExtensions()` | 获取危险扩展名集合 |
| `getDefaultMaxAttachmentSize()` | 获取默认最大附件大小 |

### 3.23 EmailRateLimiter

> 邮件发送频率限制器，支持分钟/小时/天三级限流。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EmailRateLimiter()` | 使用默认限额构造 |
| `EmailRateLimiter(int maxPerMinute, int maxPerHour, int maxPerDay)` | 自定义限额构造 |
| `allowSend(String recipient)` | 检查是否允许向指定收件人发送 |
| `allowSend()` | 检查是否允许全局发送 |
| `getQuota(String recipient)` | 获取剩余配额 |
| `reset(String recipient)` | 重置指定收件人计数 |
| `resetAll()` | 重置所有计数 |
| `getMaxPerMinute()` | 获取每分钟限额 |
| `getMaxPerHour()` | 获取每小时限额 |
| `getMaxPerDay()` | 获取每天限额 |

**RateLimitQuota Record：** `minuteRemaining`、`hourRemaining`、`dayRemaining`

### 3.24 DkimConfig

> DKIM 签名配置 Record，包含域名、选择器、私钥和签名头列表。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `load(String domain, String selector, Path keyPath)` | 从文件加载私钥 |
| `load(String domain, String selector, Path keyPath, Set<String> headersToSign)` | 从文件加载（自定义签名头） |
| `of(String domain, String selector, PrivateKey privateKey)` | 直接指定私钥 |
| `of(String domain, String selector, PrivateKey privateKey, Set<String> headersToSign)` | 直接指定私钥和签名头 |
| `getDefaultHeadersToSign()` | 获取默认签名头集合 |

**示例：**
```java
DkimConfig dkim = DkimConfig.load("example.com", "mail", Path.of("dkim-private.pem"));
EmailConfig config = EmailConfig.builder()
    .host("smtp.example.com")
    .dkim(dkim)
    .build();
```

### 3.25 DkimSigner

> DKIM 消息签名器，对 MimeMessage 进行 DKIM 签名。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `sign(MimeMessage message, DkimConfig config)` | 对消息进行 DKIM 签名 |

### 3.26 EmailRetryExecutor

> 邮件发送重试执行器，支持指数退避和最大延迟限制。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EmailRetryExecutor()` | 使用默认参数构造 |
| `EmailRetryExecutor(int maxRetries, Duration initialDelay, double backoffMultiplier)` | 自定义参数构造 |
| `executeWithRetry(Email email, EmailSender sender)` | 带重试的发送 |
| `executeWithRetry(Email email, EmailSender sender, RetryCallback callback)` | 带重试和回调的发送 |
| `builder()` | 创建构建器 |
| `getMaxRetries()` | 获取最大重试次数 |
| `getInitialDelay()` | 获取初始延迟 |
| `getBackoffMultiplier()` | 获取退避因子 |
| `getMaxDelay()` | 获取最大延迟 |

**RetryCallback 接口：** 重试回调

**示例：**
```java
EmailRetryExecutor executor = EmailRetryExecutor.builder()
    .maxRetries(3)
    .initialDelay(Duration.ofSeconds(1))
    .backoffMultiplier(2.0)
    .maxDelay(Duration.ofSeconds(30))
    .build();
executor.executeWithRetry(email, sender);
```

### 3.27 EmailException

> 邮件异常基类，包含错误码和关联的邮件实体。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EmailException(String message)` | 构造 |
| `EmailException(String message, Throwable cause)` | 构造（带原因） |
| `EmailException(String message, EmailErrorCode errorCode)` | 构造（带错误码） |
| `EmailException(String message, Throwable cause, Email email, EmailErrorCode errorCode)` | 完整构造 |
| `getErrorCode()` | 获取错误码 |
| `getEmail()` | 获取关联邮件 |
| `isRetryable()` | 是否可重试 |

### 3.28 EmailErrorCode

> 邮件错误码枚举，定义配置、连接、发送、安全、模板等各类错误码。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `getCode()` | 获取数字错误码 |
| `getDescription()` | 获取英文描述 |
| `getDescriptionCn()` | 获取中文描述 |
| `isRetryable()` | 是否可重试 |
| `fromException(Throwable e)` | 根据异常推断错误码 |

### 3.29 EmailReceiveException

> 邮件接收异常，包含文件夹和消息 ID 信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `getReceivedEmail()` | 获取关联的接收邮件 |
| `getFolder()` | 获取文件夹 |
| `getMessageId()` | 获取消息 ID |
| `folderNotFound(String folder)` | 创建文件夹不存在异常 |
| `messageNotFound(String messageId)` | 创建消息不存在异常 |
| `connectionLost(Throwable cause)` | 创建连接丢失异常 |
| `timeout()` | 创建超时异常 |
| `parseFailed(String messageId, Throwable cause)` | 创建解析失败异常 |
