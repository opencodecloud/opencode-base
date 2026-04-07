package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.Attachment;
import cloud.opencode.base.email.EmailFlags;
import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.protocol.ProtocolException;
import cloud.opencode.base.email.protocol.imap.ImapClient;
import cloud.opencode.base.email.protocol.mime.MimeParser;
import cloud.opencode.base.email.protocol.mime.ParsedMessage;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email IDLE Monitor
 * 邮件IDLE监控器
 *
 * <p>Monitors mailbox for new emails using IMAP IDLE command.</p>
 * <p>使用IMAP IDLE命令监控邮箱中的新邮件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Real-time email notification - 实时邮件通知</li>
 *   <li>IMAP IDLE support - IMAP IDLE支持</li>
 *   <li>Automatic reconnection - 自动重连</li>
 *   <li>Multiple listener support - 多监听器支持</li>
 *   <li>Folder monitoring - 文件夹监控</li>
 * </ul>
 *
 * <p><strong>Note | 注意:</strong></p>
 * <p>IDLE is only supported by IMAP protocol. POP3 does not support real-time notifications.</p>
 * <p>IDLE仅IMAP协议支持。POP3不支持实时通知。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailIdleMonitor monitor = EmailIdleMonitor.builder()
 *     .config(config)
 *     .folder("INBOX")
 *     .listener(email -> {
 *         System.out.println("New email: " + email.subject());
 *     })
 *     .build();
 *
 * monitor.start();
 * // ... application runs ...
 * monitor.stop();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailIdleMonitor implements AutoCloseable {

    private static final System.Logger logger = System.getLogger(EmailIdleMonitor.class.getName());

    /** Pattern to match IMAP untagged EXISTS response, e.g. "* 5 EXISTS" */
    private static final Pattern EXISTS_PATTERN = Pattern.compile("\\* (\\d+) EXISTS");

    /** Maximum reconnect delay cap to prevent unbounded sleep (5 minutes). */
    private static final long MAX_RECONNECT_DELAY_MS = 5 * 60 * 1000L;

    private final EmailReceiveConfig config;
    private final String folderName;
    private final List<EmailListener> listeners;
    private final Duration idleTimeout;
    private final int maxReconnectAttempts;
    private final Duration reconnectDelay;

    private ImapClient client;
    private int previousCount;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private EmailIdleMonitor(Builder builder) {
        this.config = builder.config;
        this.folderName = builder.folder;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
        this.idleTimeout = builder.idleTimeout;
        this.maxReconnectAttempts = builder.maxReconnectAttempts;
        this.reconnectDelay = builder.reconnectDelay;

        if (!config.isImap()) {
            throw new EmailReceiveException(
                    "IDLE monitoring is only supported by IMAP protocol",
                    EmailErrorCode.IDLE_NOT_SUPPORTED
            );
        }
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Start monitoring for new emails
     * 开始监控新邮件
     */
    public synchronized void start() {
        if (running.get()) {
            return;
        }

        stopping.set(false);
        running.set(true);
        reconnectAttempts.set(0);

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "EmailIdleMonitor-" + folderName);
            t.setDaemon(true);
            return t;
        });

        executor.submit(this::monitorLoop);
    }

    /**
     * Stop monitoring
     * 停止监控
     */
    public synchronized void stop() {
        if (!running.get()) {
            return;
        }

        stopping.set(true);
        running.set(false);

        // Close client to interrupt IDLE
        disconnect();

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        notifyMonitoringStopped();
    }

    /**
     * Check if monitoring is running
     * 检查是否正在监控
     *
     * @return true if running | 正在运行返回true
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Add a listener
     * 添加监听器
     *
     * @param listener the listener to add | 要添加的监听器
     */
    public void addListener(EmailListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     * 移除监听器
     *
     * @param listener the listener to remove | 要移除的监听器
     */
    public void removeListener(EmailListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void close() {
        stop();
    }

    // ==================== Private Methods ====================

    private void monitorLoop() {
        try {
            connect();
            notifyMonitoringStarted();

            while (running.get() && !stopping.get()) {
                try {
                    // Use IDLE if supported, otherwise poll
                    if (supportsIdle()) {
                        performIdle();
                    } else {
                        performPoll();
                    }
                } catch (ProtocolException e) {
                    if (!stopping.get()) {
                        handleError(e);
                        if (!attemptReconnect()) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (!stopping.get()) {
                notifyError(e);
            }
        } finally {
            running.set(false);
        }
    }

    private void connect() throws ProtocolException {
        client = new ImapClient(
                config.host(),
                config.port(),
                config.ssl(),
                config.starttls(),
                config.connectionTimeout(),
                idleTimeout
        );

        client.connect();

        if (config.hasOAuth2()) {
            client.authenticateXOAuth2(config.username(), config.oauth2Token());
        } else {
            client.login(config.username(), config.password());
        }

        int[] counts = client.select(folderName);
        previousCount = counts[0];

        reconnectAttempts.set(0);
    }

    private void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                // Ignore
            }
            client = null;
        }
    }

    private boolean supportsIdle() {
        return client.hasCapability("IDLE");
    }

    private void performIdle() throws ProtocolException {
        List<String> responses = client.idle(idleTimeout);

        for (String resp : responses) {
            Matcher matcher = EXISTS_PATTERN.matcher(resp);
            if (matcher.find()) {
                int newTotal = Integer.parseInt(matcher.group(1));
                if (newTotal > previousCount) {
                    fetchAndNotifyNew(previousCount, newTotal);
                    previousCount = newTotal;
                }
            }
        }
    }

    private void performPoll() throws ProtocolException {
        // Fallback polling for servers that don't support IDLE
        try {
            Thread.sleep(idleTimeout.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // NOOP to keep connection alive and get updated counts
        client.noop();

        // Re-select to get current message count
        int[] counts = client.select(folderName);
        int newCount = counts[0];

        if (newCount > previousCount) {
            fetchAndNotifyNew(previousCount, newCount);
            previousCount = newCount;
        }
    }

    private void fetchAndNotifyNew(int fromCount, int toCount) {
        for (int i = fromCount + 1; i <= toCount; i++) {
            try {
                String fetchResp = client.fetch(i, "(BODY[] RFC822.SIZE)");
                String rawMessage = extractBodyFromFetch(fetchResp);
                if (rawMessage != null) {
                    ParsedMessage pm = MimeParser.parse(rawMessage);
                    ReceivedEmail email = buildReceivedEmail(pm, i);
                    notifyNewEmail(email);
                }
            } catch (Exception e) {
                notifyError(e);
            }
        }
    }

    /**
     * Extracts the raw message body from an IMAP FETCH response.
     * The FETCH response wraps the literal message content between
     * a literal size indicator and the closing parenthesis.
     */
    private String extractBodyFromFetch(String fetchResponse) {
        if (fetchResponse == null || fetchResponse.isEmpty()) {
            return null;
        }
        // The FETCH response contains the raw message as a literal.
        // Find the first blank line separator after the FETCH metadata line,
        // which begins the actual RFC 2822 message content.
        // Typical format:
        //   * N FETCH (BODY[] {SIZE}\r\n<raw message>\r\n)\r\n
        // We look for the literal content between {size}\r\n and the trailing )
        int braceStart = fetchResponse.indexOf('{');
        if (braceStart < 0) {
            // No literal marker; the whole response may be the content
            return fetchResponse;
        }
        int braceEnd = fetchResponse.indexOf('}', braceStart);
        if (braceEnd < 0) {
            return fetchResponse;
        }
        // Skip past the closing brace and the CRLF that follows it
        int contentStart = braceEnd + 1;
        if (contentStart < fetchResponse.length() && fetchResponse.charAt(contentStart) == '\r') {
            contentStart++;
        }
        if (contentStart < fetchResponse.length() && fetchResponse.charAt(contentStart) == '\n') {
            contentStart++;
        }
        // The content ends at the last closing parenthesis
        int contentEnd = fetchResponse.lastIndexOf(')');
        if (contentEnd <= contentStart) {
            contentEnd = fetchResponse.length();
        }
        return fetchResponse.substring(contentStart, contentEnd).strip();
    }

    private ReceivedEmail buildReceivedEmail(ParsedMessage pm, int messageNumber) {
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(folderName)
                .messageNumber(messageNumber)
                .messageId(pm.messageId())
                .from(pm.from())
                .fromName(pm.fromName())
                .to(pm.to() != null ? pm.to() : List.of())
                .cc(pm.cc() != null ? pm.cc() : List.of())
                .bcc(pm.bcc() != null ? pm.bcc() : List.of())
                .replyTo(pm.replyTo())
                .subject(pm.subject())
                .textContent(pm.textContent())
                .htmlContent(pm.htmlContent())
                .sentDate(pm.sentDate())
                .receivedDate(pm.receivedDate())
                .size(pm.size())
                .headers(pm.headers() != null ? pm.headers() : Map.of())
                .flags(EmailFlags.UNREAD);

        // Convert ParsedAttachments to Attachments
        if (pm.attachments() != null && !pm.attachments().isEmpty()) {
            List<Attachment> attachments = new ArrayList<>(pm.attachments().size());
            for (ParsedMessage.ParsedAttachment pa : pm.attachments()) {
                if (pa.fileName() != null) {
                    attachments.add(ByteArrayAttachment.of(
                            pa.fileName(), pa.data(), pa.contentType()));
                }
            }
            builder.attachments(attachments);
        }

        return builder.build();
    }

    private boolean attemptReconnect() {
        int attempt = reconnectAttempts.incrementAndGet();

        if (attempt > maxReconnectAttempts) {
            notifyError(new EmailReceiveException(
                    "Max reconnection attempts reached",
                    EmailErrorCode.CONNECTION_LOST
            ));
            return false;
        }

        notifyReconnecting(attempt);

        try {
            long delayMs = Math.min(reconnectDelay.toMillis() * attempt, MAX_RECONNECT_DELAY_MS);
            Thread.sleep(delayMs);
            disconnect();
            connect();
            notifyReconnected();
            return true;
        } catch (Exception e) {
            notifyError(e); // don't swallow reconnect failures
            return false;
        }
    }

    private void handleError(Exception e) {
        if (!stopping.get()) {
            notifyError(e);
        }
    }

    // ==================== Notification Methods ====================

    private void notifyNewEmail(ReceivedEmail email) {
        for (EmailListener listener : listeners) {
            try {
                listener.onNewEmail(email);
            } catch (Exception e) {
                // Log and continue | 记录日志并继续
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onNewEmail", e);
            }
        }
    }

    private void notifyEmailDeleted(String messageId) {
        for (EmailListener listener : listeners) {
            try {
                listener.onEmailDeleted(messageId);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onEmailDeleted", e);
            }
        }
    }

    private void notifyError(Throwable error) {
        for (EmailListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onError", e);
            }
        }
    }

    private void notifyMonitoringStarted() {
        for (EmailListener listener : listeners) {
            try {
                listener.onMonitoringStarted(folderName);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onMonitoringStarted", e);
            }
        }
    }

    private void notifyMonitoringStopped() {
        for (EmailListener listener : listeners) {
            try {
                listener.onMonitoringStopped(folderName);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onMonitoringStopped", e);
            }
        }
    }

    private void notifyReconnecting(int attempt) {
        for (EmailListener listener : listeners) {
            try {
                listener.onReconnecting(attempt);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onReconnecting", e);
            }
        }
    }

    private void notifyReconnected() {
        for (EmailListener listener : listeners) {
            try {
                listener.onReconnected();
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Listener threw exception in onReconnected", e);
            }
        }
    }

    // ==================== Builder ====================

    /**
     * Email IDLE Monitor Builder
     * 邮件IDLE监控器构建器
     */
    public static class Builder {
        private EmailReceiveConfig config;
        private String folder = "INBOX";
        private final List<EmailListener> listeners = new ArrayList<>();
        private Duration idleTimeout = Duration.ofMinutes(29); // RFC recommends < 30 min
        private int maxReconnectAttempts = 5;
        private Duration reconnectDelay = Duration.ofSeconds(5);

        /**
         * Set the receive configuration
         * 设置接收配置
         *
         * @param config the configuration | 配置
         * @return this builder | 构建器
         */
        public Builder config(EmailReceiveConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Set the folder to monitor
         * 设置要监控的文件夹
         *
         * @param folder the folder name | 文件夹名称
         * @return this builder | 构建器
         */
        public Builder folder(String folder) {
            this.folder = folder;
            return this;
        }

        /**
         * Add a listener
         * 添加监听器
         *
         * @param listener the listener | 监听器
         * @return this builder | 构建器
         */
        public Builder listener(EmailListener listener) {
            if (listener != null) {
                this.listeners.add(listener);
            }
            return this;
        }

        /**
         * Add a simple new email handler
         * 添加简单的新邮件处理器
         *
         * @param handler the handler | 处理器
         * @return this builder | 构建器
         */
        public Builder onNewEmail(java.util.function.Consumer<ReceivedEmail> handler) {
            return listener(EmailListener.onNewEmail(handler));
        }

        /**
         * Set IDLE timeout
         * 设置IDLE超时
         *
         * @param timeout the timeout | 超时时间
         * @return this builder | 构建器
         */
        public Builder idleTimeout(Duration timeout) {
            this.idleTimeout = timeout;
            return this;
        }

        /**
         * Set max reconnection attempts
         * 设置最大重连次数
         *
         * @param attempts the max attempts | 最大次数
         * @return this builder | 构建器
         */
        public Builder maxReconnectAttempts(int attempts) {
            this.maxReconnectAttempts = attempts;
            return this;
        }

        /**
         * Set reconnection delay
         * 设置重连延迟
         *
         * @param delay the delay | 延迟
         * @return this builder | 构建器
         */
        public Builder reconnectDelay(Duration delay) {
            this.reconnectDelay = delay;
            return this;
        }

        /**
         * Build the monitor
         * 构建监控器
         *
         * @return the monitor | 监控器
         */
        public EmailIdleMonitor build() {
            if (config == null) {
                throw new IllegalStateException("Configuration is required");
            }
            return new EmailIdleMonitor(this);
        }
    }
}
