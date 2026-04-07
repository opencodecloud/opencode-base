package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.*;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.protocol.ProtocolException;
import cloud.opencode.base.email.protocol.mime.MimeParser;
import cloud.opencode.base.email.protocol.mime.ParsedMessage;
import cloud.opencode.base.email.protocol.pop3.Pop3Client;
import cloud.opencode.base.email.query.EmailQuery;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * POP3 Email Receiver Implementation
 * POP3邮件接收器实现
 *
 * <p>Receives emails using POP3 protocol.</p>
 * <p>使用POP3协议接收邮件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>POP3/POP3S support - POP3/POP3S支持</li>
 *   <li>Basic message retrieval - 基本消息检索</li>
 *   <li>Delete support - 删除支持</li>
 *   <li>Attachment handling - 附件处理</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>No folder support (INBOX only) - 无文件夹支持（仅INBOX）</li>
 *   <li>No server-side search - 无服务器端搜索</li>
 *   <li>No flag persistence - 无标记持久化</li>
 *   <li>No move operations - 无移动操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailReceiveConfig config = EmailReceiveConfig.builder()
 *     .host("pop.example.com")
 *     .username("user@example.com")
 *     .password("password")
 *     .pop3()
 *     .ssl(true)
 *     .build();
 *
 * try (Pop3EmailReceiver receiver = new Pop3EmailReceiver(config)) {
 *     receiver.connect();
 *     List<ReceivedEmail> emails = receiver.receiveAll();
 *     for (ReceivedEmail email : emails) {
 *         processEmail(email);
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use one instance per thread) - 线程安全: 否（每线程使用一个实例）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class Pop3EmailReceiver implements EmailReceiver {

    private static final System.Logger LOGGER = System.getLogger(Pop3EmailReceiver.class.getName());
    private static final String INBOX = "INBOX";

    private final EmailReceiveConfig config;
    private Pop3Client client;

    /**
     * Create POP3 receiver with configuration
     * 使用配置创建POP3接收器
     *
     * @param config the receive configuration | 接收配置
     */
    public Pop3EmailReceiver(EmailReceiveConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        if (!config.isPop3()) {
            throw new IllegalArgumentException("Configuration must use POP3 protocol");
        }
        this.config = config;
    }

    @Override
    public void connect() {
        if (isConnected()) {
            return;
        }

        try {
            client = new Pop3Client(
                    config.host(),
                    config.port(),
                    config.ssl(),
                    config.starttls(),
                    config.connectionTimeout(),
                    config.timeout()
            );

            client.connect();

            if (config.hasOAuth2()) {
                client.authXOAuth2(config.username(), config.oauth2Token());
            } else if (config.requiresAuth()) {
                client.login(config.username(), config.password());
            }
        } catch (ProtocolException e) {
            client = null;
            if (e.isAuthenticationFailure()) {
                throw new EmailReceiveException("Authentication failed", e, EmailErrorCode.AUTH_FAILED);
            }
            throw new EmailReceiveException("Failed to connect to mail server", e, EmailErrorCode.CONNECTION_FAILED);
        }
    }

    @Override
    public void disconnect() {
        if (client != null) {
            try {
                client.quit();
            } catch (ProtocolException e) {
                // Ignore close errors
            } finally {
                try {
                    client.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
            client = null;
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @Override
    public List<ReceivedEmail> receiveUnread() {
        // POP3 doesn't support flag persistence, so we receive all
        return receive(EmailQuery.builder()
                .limit(config.maxMessages())
                .build());
    }

    @Override
    public List<ReceivedEmail> receive(EmailQuery query) {
        ensureConnected();

        try {
            int[] stat = client.stat();
            int messageCount = stat[0];

            // Calculate how many total results we need before we can stop
            int needed = query.offset() + query.limit();
            boolean canEarlyTerminate = query.sortOrder() == null;
            boolean headerFilters = hasHeaderFilters(query);

            // Retrieve and filter messages, using TOP for header pre-filtering when possible
            List<MessageWithNumber> filtered = new ArrayList<>();
            for (int i = 1; i <= messageCount; i++) {
                try {
                    String raw;
                    if (headerFilters) {
                        // Use TOP to download only headers for pre-filtering
                        String headerRaw = fetchHeaders(i);
                        ParsedMessage headerPm = MimeParser.parse(headerRaw);
                        if (!matchesQuery(headerPm, query)) {
                            continue;
                        }
                        // Header filters matched, download full message for body/attachment checks
                        raw = client.retr(i);
                    } else {
                        raw = client.retr(i);
                    }
                    ParsedMessage pm = MimeParser.parse(raw);
                    if (matchesQuery(pm, query)) {
                        filtered.add(new MessageWithNumber(pm, i));
                        if (canEarlyTerminate && filtered.size() >= needed) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to retrieve/parse message {0}", i, e);
                }
            }

            // Apply sorting
            filtered = sortMessages(filtered, query.sortOrder());

            // Apply pagination
            int start = Math.min(query.offset(), filtered.size());
            int end = Math.min(start + query.limit(), filtered.size());

            List<ReceivedEmail> result = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                MessageWithNumber mwn = filtered.get(i);
                try {
                    ReceivedEmail email = buildReceivedEmail(mwn.message(), mwn.msgNum());
                    result.add(email);

                    // Apply post-receive actions
                    if (config.deleteAfterReceive()) {
                        client.dele(mwn.msgNum());
                    }
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to process email message at index {0}", i, e);
                }
            }

            return result;
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to receive emails", e);
        }
    }

    @Override
    public ReceivedEmail receiveById(String messageId) {
        ensureConnected();

        if (messageId == null || messageId.isBlank()) {
            return null;
        }

        try {
            int[] stat = client.stat();
            int messageCount = stat[0];

            for (int i = 1; i <= messageCount; i++) {
                try {
                    // Use TOP to download only headers for Message-ID matching
                    String headerRaw = fetchHeaders(i);
                    ParsedMessage headerPm = MimeParser.parse(headerRaw);
                    if (messageId.equals(headerPm.messageId())) {
                        // Found it - download full message
                        String fullRaw = client.retr(i);
                        ParsedMessage full = MimeParser.parse(fullRaw);
                        return buildReceivedEmail(full, i);
                    }
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to retrieve/parse message {0}", i, e);
                }
            }
            return null;
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to receive email by ID: " + messageId, e);
        }
    }

    @Override
    public int getMessageCount(String folder) {
        ensureConnected();
        try {
            return client.stat()[0];
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to get message count", e);
        }
    }

    @Override
    public int getUnreadCount(String folder) {
        // POP3 doesn't track read/unread status
        return getMessageCount(folder);
    }

    @Override
    public void markAsRead(String messageId) {
        // POP3 doesn't support flags - no-op
    }

    @Override
    public void markAsUnread(String messageId) {
        // POP3 doesn't support flags - no-op
    }

    @Override
    public void setFlagged(String messageId, boolean flagged) {
        // POP3 doesn't support flags - no-op
    }

    @Override
    public void delete(String messageId) {
        ensureConnected();

        try {
            int[] stat = client.stat();
            int messageCount = stat[0];

            for (int i = 1; i <= messageCount; i++) {
                try {
                    // Use TOP to download only headers for Message-ID matching
                    String headerRaw = fetchHeaders(i);
                    ParsedMessage headerPm = MimeParser.parse(headerRaw);
                    if (messageId.equals(headerPm.messageId())) {
                        client.dele(i);
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to retrieve/parse message {0} while searching for deletion", i, e);
                }
            }
            throw EmailReceiveException.messageNotFound(messageId);
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to delete message: " + messageId, e);
        }
    }

    @Override
    public void moveToFolder(String messageId, String targetFolder) {
        throw new EmailReceiveException(
                "Move operation is not supported by POP3 protocol",
                EmailErrorCode.PROTOCOL_NOT_SUPPORTED
        );
    }

    @Override
    public List<String> listFolders() {
        // POP3 only supports INBOX
        return List.of(INBOX);
    }

    // ==================== Private Methods ====================

    private void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    /**
     * Fetch only message headers using POP3 TOP command.
     * Falls back to full RETR if the server does not support TOP.
     * 使用POP3 TOP命令仅获取消息头部。如果服务器不支持TOP，则回退到完整RETR。
     *
     * @param msgNum the POP3 message number | POP3消息编号
     * @return the raw message headers (or full message on fallback) | 原始消息头部（或回退时的完整消息）
     * @throws ProtocolException if both TOP and RETR fail | 如果TOP和RETR都失败
     */
    private String fetchHeaders(int msgNum) throws ProtocolException {
        try {
            return client.top(msgNum, 0);
        } catch (ProtocolException e) {
            // TOP is an optional POP3 extension; fall back to full RETR
            LOGGER.log(System.Logger.Level.DEBUG,
                    "TOP command not supported, falling back to RETR for message {0}", msgNum);
            return client.retr(msgNum);
        }
    }

    /**
     * Check whether the query has filters that can be evaluated from headers alone.
     * 检查查询是否包含可以仅从头部评估的过滤条件。
     *
     * @param query the email query | 邮件查询
     * @return true if header-based pre-filtering is possible | 如果可以基于头部预过滤则返回true
     */
    private boolean hasHeaderFilters(EmailQuery query) {
        return query.fromDate() != null || query.toDate() != null
                || (query.from() != null && !query.from().isEmpty())
                || (query.to() != null && !query.to().isEmpty())
                || query.subjectContains() != null;
    }

    private boolean matchesQuery(ParsedMessage pm, EmailQuery query) {
        // Date filters
        if (query.fromDate() != null) {
            Instant received = pm.receivedDate() != null ? pm.receivedDate() : pm.sentDate();
            if (received != null) {
                Instant from = query.fromDate().atZone(ZoneId.systemDefault()).toInstant();
                if (received.isBefore(from)) {
                    return false;
                }
            }
        }

        if (query.toDate() != null) {
            Instant received = pm.receivedDate() != null ? pm.receivedDate() : pm.sentDate();
            if (received != null) {
                Instant to = query.toDate().atZone(ZoneId.systemDefault()).toInstant();
                if (received.isAfter(to)) {
                    return false;
                }
            }
        }

        // From filter
        if (query.from() != null && !query.from().isEmpty()) {
            String fromAddr = pm.from();
            if (fromAddr == null || fromAddr.isBlank()) {
                return false;
            }
            boolean found = false;
            for (String queryFrom : query.from()) {
                if (fromAddr.toLowerCase().contains(queryFrom.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // To filter
        if (query.to() != null && !query.to().isEmpty()) {
            List<String> toAddrs = pm.to();
            if (toAddrs == null || toAddrs.isEmpty()) {
                return false;
            }
            boolean found = false;
            for (String addr : toAddrs) {
                for (String queryTo : query.to()) {
                    if (addr.toLowerCase().contains(queryTo.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }

        // Subject filter
        if (query.subjectContains() != null) {
            String subject = pm.subject();
            if (subject == null || !subject.toLowerCase().contains(query.subjectContains().toLowerCase())) {
                return false;
            }
        }

        // Attachment filter
        if (query.hasAttachments()) {
            List<ParsedMessage.ParsedAttachment> attachments = pm.attachments();
            if (attachments == null || attachments.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private List<MessageWithNumber> sortMessages(List<MessageWithNumber> messages, EmailQuery.SortOrder sortOrder) {
        if (sortOrder == null || messages.size() <= 1) {
            return messages;
        }

        Comparator<MessageWithNumber> comparator = switch (sortOrder) {
            case NEWEST_FIRST -> (m1, m2) -> {
                Instant d1 = effectiveDate(m1.message());
                Instant d2 = effectiveDate(m2.message());
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d2.compareTo(d1);
            };
            case OLDEST_FIRST -> (m1, m2) -> {
                Instant d1 = effectiveDate(m1.message());
                Instant d2 = effectiveDate(m2.message());
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            };
            case SUBJECT_ASC -> (m1, m2) -> {
                String s1 = m1.message().subject();
                String s2 = m2.message().subject();
                if (s1 == null) return 1;
                if (s2 == null) return -1;
                return s1.compareToIgnoreCase(s2);
            };
            case SUBJECT_DESC -> (m1, m2) -> {
                String s1 = m1.message().subject();
                String s2 = m2.message().subject();
                if (s1 == null) return 1;
                if (s2 == null) return -1;
                return s2.compareToIgnoreCase(s1);
            };
            case SENDER_ASC, SENDER_DESC -> (m1, m2) -> {
                String s1 = m1.message().from() != null ? m1.message().from() : "";
                String s2 = m2.message().from() != null ? m2.message().from() : "";
                int result = s1.compareToIgnoreCase(s2);
                return sortOrder == EmailQuery.SortOrder.SENDER_DESC ? -result : result;
            };
        };

        List<MessageWithNumber> sorted = new ArrayList<>(messages);
        sorted.sort(comparator);
        return sorted;
    }

    private static Instant effectiveDate(ParsedMessage pm) {
        return pm.receivedDate() != null ? pm.receivedDate() : pm.sentDate();
    }

    private ReceivedEmail buildReceivedEmail(ParsedMessage pm, int messageNumber) {
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(INBOX)
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

    /**
     * Internal record pairing a parsed message with its POP3 message number.
     */
    private record MessageWithNumber(ParsedMessage message, int msgNum) {}
}
