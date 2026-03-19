package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.EmailFlags;
import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.Attachment;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final int MAX_MIME_DEPTH = 50;

    private final EmailReceiveConfig config;
    private final String folderName;
    private final List<EmailListener> listeners;
    private final Duration idleTimeout;
    private final int maxReconnectAttempts;
    private final Duration reconnectDelay;

    private Session session;
    private Store store;
    private Folder folder;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private EmailIdleMonitor(Builder builder) {
        this.config = builder.config;
        this.folderName = builder.folder;
        this.listeners = new ArrayList<>(builder.listeners);
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

        // Interrupt idle
        if (folder != null && folder.isOpen()) {
            try {
                // Close folder to interrupt IDLE
                folder.close(false);
            } catch (MessagingException e) {
                // Ignore
            }
        }

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
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Remove a listener
     * 移除监听器
     *
     * @param listener the listener to remove | 要移除的监听器
     */
    public void removeListener(EmailListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
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
                } catch (MessagingException e) {
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

    private void connect() throws MessagingException {
        Properties props = new Properties();
        String protocol = config.getStoreProtocol();

        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".host", config.host());
        props.put("mail." + protocol + ".port", String.valueOf(config.port()));

        if (config.ssl()) {
            props.put("mail." + protocol + ".ssl.enable", "true");
        }

        if (config.starttls()) {
            props.put("mail." + protocol + ".starttls.enable", "true");
            props.put("mail." + protocol + ".starttls.required", "true");
        }

        props.put("mail." + protocol + ".connectiontimeout",
                String.valueOf(config.connectionTimeout().toMillis()));
        props.put("mail." + protocol + ".timeout",
                String.valueOf(idleTimeout.toMillis()));

        session = Session.getInstance(props);
        session.setDebug(config.debug());

        store = session.getStore(protocol);
        store.connect(config.host(), config.port(), config.username(), config.password());

        folder = store.getFolder(folderName);
        folder.open(Folder.READ_WRITE);

        // Add message count listener
        folder.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                for (Message message : e.getMessages()) {
                    try {
                        ReceivedEmail email = parseMessage(message);
                        notifyNewEmail(email);
                    } catch (Exception ex) {
                        notifyError(ex);
                    }
                }
            }

            @Override
            public void messagesRemoved(MessageCountEvent e) {
                for (Message message : e.getMessages()) {
                    try {
                        if (message instanceof MimeMessage mimeMessage) {
                            notifyEmailDeleted(mimeMessage.getMessageID());
                        }
                    } catch (Exception ex) {
                        notifyError(ex);
                    }
                }
            }
        });

        reconnectAttempts.set(0);
    }

    private void disconnect() {
        if (folder != null) {
            try {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            } catch (MessagingException e) {
                // Ignore
            }
            folder = null;
        }

        if (store != null) {
            try {
                if (store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException e) {
                // Ignore
            }
            store = null;
        }
    }

    private boolean supportsIdle() {
        // Use reflection to check for IDLE support without direct dependency on implementation
        try {
            var imapFolderClass = Class.forName("org.eclipse.angus.mail.imap.IMAPFolder");
            if (imapFolderClass.isInstance(folder)) {
                var storeMethod = imapFolderClass.getMethod("getStore");
                var store = storeMethod.invoke(folder);
                var hasCapabilityMethod = store.getClass().getMethod("hasCapability", String.class);
                return (Boolean) hasCapabilityMethod.invoke(store, "IDLE");
            }
        } catch (Exception e) {
            // Fallback: try legacy com.sun.mail package
            try {
                var imapFolderClass = Class.forName("com.sun.mail.imap.IMAPFolder");
                if (imapFolderClass.isInstance(folder)) {
                    var storeMethod = imapFolderClass.getMethod("getStore");
                    var store = storeMethod.invoke(folder);
                    var hasCapabilityMethod = store.getClass().getMethod("hasCapability", String.class);
                    return (Boolean) hasCapabilityMethod.invoke(store, "IDLE");
                }
            } catch (Exception ex) {
                // IDLE not supported
            }
        }
        return false;
    }

    private void performIdle() throws MessagingException {
        // Use reflection to call idle() without direct dependency on implementation
        try {
            var idleMethod = folder.getClass().getMethod("idle");
            idleMethod.invoke(folder);
        } catch (Exception e) {
            if (e.getCause() instanceof MessagingException me) {
                throw me;
            }
            throw new MessagingException("Failed to perform IDLE", e);
        }
    }

    private void performPoll() throws MessagingException {
        // Fallback polling for servers that don't support IDLE
        int count = folder.getMessageCount();
        try {
            Thread.sleep(idleTimeout.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for new messages
        int newCount = folder.getMessageCount();
        if (newCount > count) {
            for (int i = count + 1; i <= newCount; i++) {
                try {
                    Message message = folder.getMessage(i);
                    ReceivedEmail email = parseMessage(message);
                    notifyNewEmail(email);
                } catch (Exception e) {
                    notifyError(e);
                }
            }
        }
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
            Thread.sleep(reconnectDelay.toMillis() * attempt);
            disconnect();
            connect();
            notifyReconnected();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleError(Exception e) {
        if (!stopping.get()) {
            notifyError(e);
        }
    }

    private ReceivedEmail parseMessage(Message message) throws MessagingException {
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(folderName)
                .messageNumber(message.getMessageNumber());

        if (message instanceof MimeMessage mimeMessage) {
            builder.messageId(mimeMessage.getMessageID());
        }

        Address[] from = message.getFrom();
        if (from != null && from.length > 0) {
            if (from[0] instanceof InternetAddress ia) {
                builder.from(ia.getAddress());
                builder.fromName(ia.getPersonal());
            } else {
                builder.from(from[0].toString());
            }
        }

        builder.to(parseAddresses(message.getRecipients(Message.RecipientType.TO)));
        builder.cc(parseAddresses(message.getRecipients(Message.RecipientType.CC)));
        builder.subject(message.getSubject());

        if (message.getSentDate() != null) {
            builder.sentDate(message.getSentDate().toInstant());
        }
        if (message.getReceivedDate() != null) {
            builder.receivedDate(message.getReceivedDate().toInstant());
        }

        builder.flags(EmailFlags.from(message.getFlags()));
        builder.size(message.getSize());

        // Parse content
        parseContent(message, builder);

        return builder.build();
    }

    private List<String> parseAddresses(Address[] addresses) {
        if (addresses == null) return List.of();
        List<String> result = new ArrayList<>();
        for (Address addr : addresses) {
            if (addr instanceof InternetAddress ia) {
                result.add(ia.getAddress());
            } else {
                result.add(addr.toString());
            }
        }
        return result;
    }

    private void parseContent(Part part, ReceivedEmail.Builder builder) throws MessagingException {
        try {
            List<Attachment> attachments = new ArrayList<>();
            parseContentRecursive(part, builder, attachments, 0);
            builder.attachments(attachments);
        } catch (IOException e) {
            throw new MessagingException("Failed to parse content", e);
        }
    }

    private void parseContentRecursive(Part part, ReceivedEmail.Builder builder,
                                        List<Attachment> attachments, int depth) throws MessagingException, IOException {
        if (depth > MAX_MIME_DEPTH) {
            return;
        }

        Object content = part.getContent();

        if (part.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            if (builder.build().textContent() == null) {
                builder.textContent(content.toString());
            }
        } else if (part.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            if (builder.build().htmlContent() == null) {
                builder.htmlContent(content.toString());
            }
        } else if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                parseContentRecursive(multipart.getBodyPart(i), builder, attachments, depth + 1);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                || part.getFileName() != null) {
            String fileName = part.getFileName();
            if (fileName != null) {
                try (InputStream is = part.getInputStream()) {
                    byte[] data = readAllBytes(is);
                    attachments.add(ByteArrayAttachment.of(fileName, data, part.getContentType()));
                }
            }
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    // ==================== Notification Methods ====================

    private void notifyNewEmail(ReceivedEmail email) {
        synchronized (listeners) {
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
    }

    private void notifyEmailDeleted(String messageId) {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onEmailDeleted(messageId);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onEmailDeleted", e);
                }
            }
        }
    }

    private void notifyError(Throwable error) {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onError", e);
                }
            }
        }
    }

    private void notifyMonitoringStarted() {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onMonitoringStarted(folderName);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onMonitoringStarted", e);
                }
            }
        }
    }

    private void notifyMonitoringStopped() {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onMonitoringStopped(folderName);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onMonitoringStopped", e);
                }
            }
        }
    }

    private void notifyReconnecting(int attempt) {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onReconnecting(attempt);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onReconnecting", e);
                }
            }
        }
    }

    private void notifyReconnected() {
        synchronized (listeners) {
            for (EmailListener listener : listeners) {
                try {
                    listener.onReconnected();
                } catch (Exception e) {
                    logger.log(System.Logger.Level.WARNING,
                            "Listener threw exception in onReconnected", e);
                }
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
