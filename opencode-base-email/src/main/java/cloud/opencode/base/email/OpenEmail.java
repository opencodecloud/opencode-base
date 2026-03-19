package cloud.opencode.base.email;

import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.exception.EmailSendException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.internal.EmailTemplate;
import cloud.opencode.base.email.listener.EmailIdleMonitor;
import cloud.opencode.base.email.listener.EmailListener;
import cloud.opencode.base.email.query.EmailQuery;
import cloud.opencode.base.email.receiver.AsyncEmailReceiver;
import cloud.opencode.base.email.receiver.ImapEmailReceiver;
import cloud.opencode.base.email.receiver.Pop3EmailReceiver;
import cloud.opencode.base.email.retry.EmailRetryExecutor;
import cloud.opencode.base.email.security.EmailRateLimiter;
import cloud.opencode.base.email.sender.AsyncEmailSender;
import cloud.opencode.base.email.sender.SmtpEmailSender;
import cloud.opencode.base.email.template.SimpleEmailTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * Email Utility Facade Class
 * 邮件工具门面类
 *
 * <p>Unified entry point for email sending and receiving operations.</p>
 * <p>邮件发送和接收操作的统一入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Send plain text emails - 发送纯文本邮件</li>
 *   <li>Send HTML emails - 发送HTML邮件</li>
 *   <li>Send template-based emails - 发送模板邮件</li>
 *   <li>Async sending with CompletableFuture - 异步发送</li>
 *   <li>Batch sending support - 批量发送支持</li>
 *   <li>Receive emails via IMAP/POP3 - 通过IMAP/POP3接收邮件</li>
 *   <li>Email query and filtering - 邮件查询和过滤</li>
 *   <li>Email management (mark read, delete, move) - 邮件管理（标记已读、删除、移动）</li>
 *   <li>Real-time monitoring (IMAP IDLE) - 实时监控（IMAP IDLE）</li>
 *   <li>Global configuration - 全局配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Configure
 * OpenEmail.configure(EmailConfig.builder()
 *     .host("smtp.example.com")
 *     .port(587)
 *     .username("user@example.com")
 *     .password("password")
 *     .starttls(true)
 *     .defaultFrom("noreply@example.com", "System")
 *     .build());
 *
 * // Send simple text email
 * OpenEmail.sendText("user@example.com", "Hello", "Hello World!");
 *
 * // Send HTML email
 * OpenEmail.sendHtml("user@example.com", "Welcome",
 *     "<h1>Welcome</h1><p>Thanks for signing up!</p>");
 *
 * // Send template email
 * OpenEmail.sendTemplate("user@example.com", "Order Confirmation",
 *     "order-confirm.html",
 *     Map.of("orderNo", "12345", "amount", "99.00"));
 *
 * // Send with builder
 * OpenEmail.send(OpenEmail.email()
 *     .to("user@example.com")
 *     .subject("Report")
 *     .html("<p>See attached report</p>")
 *     .attach(Path.of("report.pdf"))
 *     .build());
 *
 * // Async send
 * OpenEmail.sendAsync(email)
 *     .thenRun(() -> log.info("Sent successfully"))
 *     .exceptionally(e -> { log.error("Failed", e); return null; });
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
public final class OpenEmail {

    private static final ReentrantLock LOCK = new ReentrantLock();

    // Sending
    private static volatile EmailSender defaultSender;
    private static volatile AsyncEmailSender asyncSender;
    private static volatile EmailConfig defaultConfig;
    private static volatile EmailTemplate templateEngine = SimpleEmailTemplate.getInstance();
    private static volatile EmailRateLimiter rateLimiter;

    // Receiving
    private static volatile EmailReceiver defaultReceiver;
    private static volatile AsyncEmailReceiver asyncReceiver;
    private static volatile EmailReceiveConfig defaultReceiveConfig;

    private OpenEmail() {
        // Utility class
    }

    // ==================== Configuration ====================

    /**
     * Configure default email sender
     * 配置默认邮件发送器
     *
     * @param config the email configuration | 邮件配置
     */
    public static void configure(EmailConfig config) {
        LOCK.lock();
        try {
            defaultConfig = config;
            defaultSender = new SmtpEmailSender(config);

            // Create async sender with retry support
            EmailRetryExecutor retryExecutor = new EmailRetryExecutor(
                    config.maxRetries(),
                    java.time.Duration.ofSeconds(1),
                    2.0
            );
            asyncSender = AsyncEmailSender.builder()
                    .sender(defaultSender)
                    .retryExecutor(retryExecutor)
                    .build();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Quick configuration
     * 快速配置
     *
     * @param host     SMTP host | SMTP主机
     * @param port     SMTP port | SMTP端口
     * @param username username | 用户名
     * @param password password | 密码
     */
    public static void configure(String host, int port, String username, String password) {
        configure(EmailConfig.builder()
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .build());
    }

    /**
     * Configure with custom sender
     * 使用自定义发送器配置
     *
     * @param config the configuration | 配置
     * @param sender the custom sender | 自定义发送器
     */
    public static void configure(EmailConfig config, EmailSender sender) {
        LOCK.lock();
        try {
            defaultConfig = config;
            defaultSender = sender;

            EmailRetryExecutor retryExecutor = new EmailRetryExecutor(
                    config.maxRetries(),
                    java.time.Duration.ofSeconds(1),
                    2.0
            );
            asyncSender = AsyncEmailSender.builder()
                    .sender(sender)
                    .retryExecutor(retryExecutor)
                    .build();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Set custom template engine
     * 设置自定义模板引擎
     *
     * @param engine the template engine | 模板引擎
     */
    public static void setTemplateEngine(EmailTemplate engine) {
        LOCK.lock();
        try {
            templateEngine = engine;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Configure rate limiter with default limits
     * 使用默认限制配置频率限制器
     *
     * <p>Default limits: 10/minute, 100/hour, 1000/day</p>
     */
    public static void enableRateLimiting() {
        LOCK.lock();
        try {
            rateLimiter = new EmailRateLimiter();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Configure rate limiter with custom limits
     * 使用自定义限制配置频率限制器
     *
     * @param maxPerMinute max emails per minute | 每分钟最大邮件数
     * @param maxPerHour   max emails per hour | 每小时最大邮件数
     * @param maxPerDay    max emails per day | 每天最大邮件数
     */
    public static void enableRateLimiting(int maxPerMinute, int maxPerHour, int maxPerDay) {
        LOCK.lock();
        try {
            rateLimiter = new EmailRateLimiter(maxPerMinute, maxPerHour, maxPerDay);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Disable rate limiting
     * 禁用频率限制
     */
    public static void disableRateLimiting() {
        LOCK.lock();
        try {
            rateLimiter = null;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Check if rate limiting is enabled
     * 检查是否启用了频率限制
     *
     * @return true if enabled | 启用返回true
     */
    public static boolean isRateLimitingEnabled() {
        return rateLimiter != null;
    }

    /**
     * Get remaining rate limit quota (global)
     * 获取剩余频率限制配额（全局）
     *
     * @return the quota or null if rate limiting disabled | 配额，禁用时返回null
     */
    public static EmailRateLimiter.RateLimitQuota getRateLimitQuota() {
        return rateLimiter != null ? rateLimiter.getQuota("__global__") : null;
    }

    /**
     * Get remaining rate limit quota for recipient
     * 获取收件人的剩余频率限制配额
     *
     * @param recipient the recipient email | 收件人邮箱
     * @return the quota or null if rate limiting disabled | 配额，禁用时返回null
     */
    public static EmailRateLimiter.RateLimitQuota getRateLimitQuota(String recipient) {
        return rateLimiter != null ? rateLimiter.getQuota(recipient) : null;
    }

    /**
     * Check if configured
     * 检查是否已配置
     *
     * @return true if configured | 已配置返回true
     */
    public static boolean isConfigured() {
        return defaultSender != null && defaultConfig != null;
    }

    /**
     * Get current configuration
     * 获取当前配置
     *
     * @return the configuration | 配置
     */
    public static EmailConfig getConfig() {
        return defaultConfig;
    }

    // ==================== Send Methods ====================

    /**
     * Send email
     * 发送邮件
     *
     * @param email the email to send | 要发送的邮件
     * @throws EmailException if sending fails | 发送失败时抛出
     */
    public static void send(Email email) {
        checkRateLimit(email);
        getSender().send(email);
    }

    /**
     * Send email and return result with message ID
     * 发送邮件并返回包含消息ID的结果
     *
     * @param email the email to send | 要发送的邮件
     * @return the send result containing message ID | 包含消息ID的发送结果
     * @throws EmailException if sending fails | 发送失败时抛出
     */
    public static SendResult sendWithResult(Email email) {
        checkRateLimit(email);
        return getSender().sendWithResult(email);
    }

    /**
     * Check rate limit before sending
     */
    private static void checkRateLimit(Email email) {
        if (rateLimiter == null) {
            return;
        }

        // Check global rate limit
        if (!rateLimiter.allowSend()) {
            throw new EmailSendException(
                    "Global rate limit exceeded",
                    email,
                    EmailErrorCode.RATE_LIMITED
            );
        }

        // Check per-recipient rate limit (for first recipient)
        if (email.to() != null && !email.to().isEmpty()) {
            String firstRecipient = email.to().getFirst();
            if (!rateLimiter.allowSend(firstRecipient)) {
                throw new EmailSendException(
                        "Rate limit exceeded for recipient: " + firstRecipient,
                        email,
                        EmailErrorCode.RATE_LIMITED
                );
            }
        }
    }

    /**
     * Send simple text email
     * 发送简单文本邮件
     *
     * @param to      recipient email | 收件人邮箱
     * @param subject email subject | 邮件主题
     * @param content text content | 文本内容
     */
    public static void sendText(String to, String subject, String content) {
        send(email()
                .to(to)
                .subject(subject)
                .text(content)
                .build());
    }

    /**
     * Send HTML email
     * 发送HTML邮件
     *
     * @param to          recipient email | 收件人邮箱
     * @param subject     email subject | 邮件主题
     * @param htmlContent HTML content | HTML内容
     */
    public static void sendHtml(String to, String subject, String htmlContent) {
        send(email()
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build());
    }

    /**
     * Send template email
     * 发送模板邮件
     *
     * @param to        recipient email | 收件人邮箱
     * @param subject   email subject | 邮件主题
     * @param template  template content or path | 模板内容或路径
     * @param variables template variables | 模板变量
     */
    public static void sendTemplate(String to, String subject,
                                    String template, Map<String, Object> variables) {
        String content = templateEngine.render(template, variables);
        sendHtml(to, subject, content);
    }

    /**
     * Send to multiple recipients
     * 发送给多个收件人
     *
     * @param recipients recipient emails | 收件人邮箱列表
     * @param subject    email subject | 邮件主题
     * @param content    text content | 文本内容
     */
    public static void sendToMultiple(List<String> recipients, String subject, String content) {
        send(email()
                .to(recipients)
                .subject(subject)
                .text(content)
                .build());
    }

    // ==================== Async Send Methods ====================

    /**
     * Send email asynchronously
     * 异步发送邮件
     *
     * @param email the email to send | 要发送的邮件
     * @return future that completes when sent | 发送完成时的future
     */
    public static CompletableFuture<Void> sendAsync(Email email) {
        return getAsyncSender().sendAsync(email);
    }

    /**
     * Send email asynchronously with callback
     * 使用回调异步发送邮件
     *
     * @param email    the email to send | 要发送的邮件
     * @param callback callback on completion | 完成时的回调
     * @return future that completes when sent | 发送完成时的future
     */
    public static CompletableFuture<Void> sendAsync(Email email,
                                                    BiConsumer<Email, Throwable> callback) {
        return getAsyncSender().sendAsync(email, callback);
    }

    /**
     * Send simple text email asynchronously
     * 异步发送简单文本邮件
     *
     * @param to      recipient email | 收件人邮箱
     * @param subject email subject | 邮件主题
     * @param content text content | 文本内容
     * @return future that completes when sent | 发送完成时的future
     */
    public static CompletableFuture<Void> sendTextAsync(String to, String subject, String content) {
        return sendAsync(email()
                .to(to)
                .subject(subject)
                .text(content)
                .build());
    }

    /**
     * Send HTML email asynchronously
     * 异步发送HTML邮件
     *
     * @param to          recipient email | 收件人邮箱
     * @param subject     email subject | 邮件主题
     * @param htmlContent HTML content | HTML内容
     * @return future that completes when sent | 发送完成时的future
     */
    public static CompletableFuture<Void> sendHtmlAsync(String to, String subject, String htmlContent) {
        return sendAsync(email()
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build());
    }

    /**
     * Send multiple emails asynchronously
     * 异步发送多封邮件
     *
     * @param emails the emails to send | 要发送的邮件列表
     * @return list of futures | future列表
     */
    public static List<CompletableFuture<Void>> sendAllAsync(List<Email> emails) {
        return getAsyncSender().sendAllAsync(emails);
    }

    /**
     * Send multiple emails and wait for completion
     * 发送多封邮件并等待完成
     *
     * @param emails the emails to send | 要发送的邮件列表
     * @return future that completes when all sent | 全部发送完成时的future
     */
    public static CompletableFuture<Void> sendAllAndWait(List<Email> emails) {
        return getAsyncSender().sendAllAndWait(emails);
    }

    // ==================== Receiver Configuration ====================

    /**
     * Configure email receiver
     * 配置邮件接收器
     *
     * @param config the receive configuration | 接收配置
     */
    public static void configureReceiver(EmailReceiveConfig config) {
        LOCK.lock();
        try {
            defaultReceiveConfig = config;
            defaultReceiver = config.isImap()
                    ? new ImapEmailReceiver(config)
                    : new Pop3EmailReceiver(config);
            asyncReceiver = AsyncEmailReceiver.builder()
                    .receiver(defaultReceiver)
                    .build();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Quick receiver configuration
     * 快速接收器配置
     *
     * @param host     mail server host | 邮件服务器主机
     * @param username username | 用户名
     * @param password password | 密码
     * @param useImap  true for IMAP, false for POP3 | true使用IMAP，false使用POP3
     */
    public static void configureReceiver(String host, String username, String password, boolean useImap) {
        EmailReceiveConfig.Builder builder = EmailReceiveConfig.builder()
                .host(host)
                .username(username)
                .password(password)
                .ssl(true);
        if (useImap) {
            builder.imap();
        } else {
            builder.pop3();
        }
        configureReceiver(builder.build());
    }

    /**
     * Configure receiver with custom implementation
     * 使用自定义实现配置接收器
     *
     * @param config   the configuration | 配置
     * @param receiver the custom receiver | 自定义接收器
     */
    public static void configureReceiver(EmailReceiveConfig config, EmailReceiver receiver) {
        LOCK.lock();
        try {
            defaultReceiveConfig = config;
            defaultReceiver = receiver;
            asyncReceiver = AsyncEmailReceiver.builder()
                    .receiver(receiver)
                    .build();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Check if receiver is configured
     * 检查接收器是否已配置
     *
     * @return true if configured | 已配置返回true
     */
    public static boolean isReceiverConfigured() {
        return defaultReceiver != null && defaultReceiveConfig != null;
    }

    /**
     * Get current receive configuration
     * 获取当前接收配置
     *
     * @return the receive configuration | 接收配置
     */
    public static EmailReceiveConfig getReceiveConfig() {
        return defaultReceiveConfig;
    }

    // ==================== Receive Methods ====================

    /**
     * Receive unread emails from default folder
     * 从默认文件夹接收未读邮件
     *
     * @return list of received emails | 接收到的邮件列表
     */
    public static List<ReceivedEmail> receiveUnread() {
        return getReceiver().receiveUnread();
    }

    /**
     * Receive emails with query
     * 使用查询接收邮件
     *
     * @param query the email query | 邮件查询
     * @return list of received emails | 接收到的邮件列表
     */
    public static List<ReceivedEmail> receive(EmailQuery query) {
        return getReceiver().receive(query);
    }

    /**
     * Receive all emails from default folder
     * 从默认文件夹接收所有邮件
     *
     * @return list of received emails | 接收到的邮件列表
     */
    public static List<ReceivedEmail> receiveAll() {
        return getReceiver().receiveAll();
    }

    /**
     * Receive email by message ID
     * 通过消息ID接收邮件
     *
     * @param messageId the message ID | 消息ID
     * @return the received email or null | 接收到的邮件或null
     */
    public static ReceivedEmail receiveById(String messageId) {
        return getReceiver().receiveById(messageId);
    }

    /**
     * Get message count in folder
     * 获取文件夹中的消息数量
     *
     * @param folder the folder name | 文件夹名称
     * @return the message count | 消息数量
     */
    public static int getMessageCount(String folder) {
        return getReceiver().getMessageCount(folder);
    }

    /**
     * Get unread message count in folder
     * 获取文件夹中的未读消息数量
     *
     * @param folder the folder name | 文件夹名称
     * @return the unread count | 未读数量
     */
    public static int getUnreadCount(String folder) {
        return getReceiver().getUnreadCount(folder);
    }

    // ==================== Email Management ====================

    /**
     * Mark email as read
     * 标记邮件为已读
     *
     * @param messageId the message ID | 消息ID
     */
    public static void markAsRead(String messageId) {
        getReceiver().markAsRead(messageId);
    }

    /**
     * Mark email as unread
     * 标记邮件为未读
     *
     * @param messageId the message ID | 消息ID
     */
    public static void markAsUnread(String messageId) {
        getReceiver().markAsUnread(messageId);
    }

    /**
     * Set email flagged status
     * 设置邮件标记状态
     *
     * @param messageId the message ID | 消息ID
     * @param flagged   true to flag, false to unflag | true为标记，false为取消标记
     */
    public static void setFlagged(String messageId, boolean flagged) {
        getReceiver().setFlagged(messageId, flagged);
    }

    /**
     * Delete email
     * 删除邮件
     *
     * @param messageId the message ID | 消息ID
     */
    public static void delete(String messageId) {
        getReceiver().delete(messageId);
    }

    /**
     * Move email to folder (IMAP only)
     * 移动邮件到文件夹（仅IMAP）
     *
     * @param messageId    the message ID | 消息ID
     * @param targetFolder the target folder | 目标文件夹
     */
    public static void moveToFolder(String messageId, String targetFolder) {
        getReceiver().moveToFolder(messageId, targetFolder);
    }

    /**
     * List available folders
     * 列出可用文件夹
     *
     * @return list of folder names | 文件夹名称列表
     */
    public static List<String> listFolders() {
        return getReceiver().listFolders();
    }

    // ==================== Async Receive Methods ====================

    /**
     * Receive unread emails asynchronously
     * 异步接收未读邮件
     *
     * @return future containing received emails | 包含接收邮件的future
     */
    public static CompletableFuture<List<ReceivedEmail>> receiveUnreadAsync() {
        return getAsyncReceiver().receiveUnreadAsync();
    }

    /**
     * Receive emails asynchronously with query
     * 使用查询异步接收邮件
     *
     * @param query the email query | 邮件查询
     * @return future containing received emails | 包含接收邮件的future
     */
    public static CompletableFuture<List<ReceivedEmail>> receiveAsync(EmailQuery query) {
        return getAsyncReceiver().receiveAsync(query);
    }

    /**
     * Receive emails asynchronously with callback
     * 使用回调异步接收邮件
     *
     * @param query    the email query | 邮件查询
     * @param callback callback on completion | 完成时的回调
     * @return future containing received emails | 包含接收邮件的future
     */
    public static CompletableFuture<List<ReceivedEmail>> receiveAsync(
            EmailQuery query,
            BiConsumer<List<ReceivedEmail>, Throwable> callback) {
        return getAsyncReceiver().receiveAsync(query, callback);
    }

    /**
     * Mark email as read asynchronously
     * 异步标记邮件为已读
     *
     * @param messageId the message ID | 消息ID
     * @return future that completes when done | 完成时的future
     */
    public static CompletableFuture<Void> markAsReadAsync(String messageId) {
        return getAsyncReceiver().markAsReadAsync(messageId);
    }

    /**
     * Delete email asynchronously
     * 异步删除邮件
     *
     * @param messageId the message ID | 消息ID
     * @return future that completes when done | 完成时的future
     */
    public static CompletableFuture<Void> deleteAsync(String messageId) {
        return getAsyncReceiver().deleteAsync(messageId);
    }

    // ==================== Email Monitoring ====================

    /**
     * Create email monitor for real-time notifications
     * 创建邮件监控器用于实时通知
     *
     * @param listener the email listener | 邮件监听器
     * @return the monitor | 监控器
     */
    public static EmailIdleMonitor createMonitor(EmailListener listener) {
        checkReceiverConfigured();
        return EmailIdleMonitor.builder()
                .config(defaultReceiveConfig)
                .listener(listener)
                .build();
    }

    /**
     * Create email monitor for specific folder
     * 为特定文件夹创建邮件监控器
     *
     * @param folder   the folder to monitor | 要监控的文件夹
     * @param listener the email listener | 邮件监听器
     * @return the monitor | 监控器
     */
    public static EmailIdleMonitor createMonitor(String folder, EmailListener listener) {
        checkReceiverConfigured();
        return EmailIdleMonitor.builder()
                .config(defaultReceiveConfig)
                .folder(folder)
                .listener(listener)
                .build();
    }

    // ==================== Builder Methods ====================

    /**
     * Create email query builder
     * 创建邮件查询构建器
     *
     * @return the query builder | 查询构建器
     */
    public static EmailQuery.Builder query() {
        return EmailQuery.builder();
    }

    /**
     * Create receive configuration builder
     * 创建接收配置构建器
     *
     * @return the receive config builder | 接收配置构建器
     */
    public static EmailReceiveConfig.Builder receiveConfig() {
        return EmailReceiveConfig.builder();
    }

    /**
     * Create email builder with default from address
     * 创建带默认发件人的邮件构建器
     *
     * @return the email builder | 邮件构建器
     */
    public static Email.Builder email() {
        checkConfigured();
        Email.Builder builder = Email.builder();
        if (defaultConfig.defaultFrom() != null) {
            if (defaultConfig.defaultFromName() != null) {
                builder.from(defaultConfig.defaultFrom(), defaultConfig.defaultFromName());
            } else {
                builder.from(defaultConfig.defaultFrom());
            }
        }
        return builder;
    }

    /**
     * Create configuration builder
     * 创建配置构建器
     *
     * @return the config builder | 配置构建器
     */
    public static EmailConfig.Builder config() {
        return EmailConfig.builder();
    }

    // ==================== Internal Methods ====================

    private static EmailSender getSender() {
        checkConfigured();
        return defaultSender;
    }

    private static AsyncEmailSender getAsyncSender() {
        checkConfigured();
        return asyncSender;
    }

    private static EmailReceiver getReceiver() {
        LOCK.lock();
        try {
            checkReceiverConfigured();
            if (!defaultReceiver.isConnected()) {
                defaultReceiver.connect();
            }
            return defaultReceiver;
        } finally {
            LOCK.unlock();
        }
    }

    private static AsyncEmailReceiver getAsyncReceiver() {
        checkReceiverConfigured();
        return asyncReceiver;
    }

    private static void checkConfigured() {
        if (defaultSender == null || defaultConfig == null) {
            throw new EmailException("OpenEmail not configured. Call OpenEmail.configure() first.");
        }
    }

    private static void checkReceiverConfigured() {
        if (defaultReceiver == null || defaultReceiveConfig == null) {
            throw new EmailReceiveException("OpenEmail receiver not configured. Call OpenEmail.configureReceiver() first.");
        }
    }

    /**
     * Shutdown all resources (call on application shutdown)
     * 关闭所有资源（应用关闭时调用）
     */
    public static void shutdown() {
        LOCK.lock();
        try {
            // Shutdown sender resources
            if (asyncSender != null) {
                asyncSender.close();
                asyncSender = null;
            }
            if (defaultSender != null) {
                defaultSender.close();
                defaultSender = null;
            }
            defaultConfig = null;

            // Shutdown receiver resources
            if (asyncReceiver != null) {
                asyncReceiver.close();
                asyncReceiver = null;
            }
            if (defaultReceiver != null) {
                defaultReceiver.close();
                defaultReceiver = null;
            }
            defaultReceiveConfig = null;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Shutdown only sender resources
     * 仅关闭发送器资源
     */
    public static void shutdownSender() {
        LOCK.lock();
        try {
            if (asyncSender != null) {
                asyncSender.close();
                asyncSender = null;
            }
            if (defaultSender != null) {
                defaultSender.close();
                defaultSender = null;
            }
            defaultConfig = null;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Shutdown only receiver resources
     * 仅关闭接收器资源
     */
    public static void shutdownReceiver() {
        LOCK.lock();
        try {
            if (asyncReceiver != null) {
                asyncReceiver.close();
                asyncReceiver = null;
            }
            if (defaultReceiver != null) {
                defaultReceiver.close();
                defaultReceiver = null;
            }
            defaultReceiveConfig = null;
        } finally {
            LOCK.unlock();
        }
    }
}
