package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.*;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.query.EmailQuery;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private Session session;
    private Store store;
    private Folder inbox;

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

        // OAuth2 XOAUTH2 mechanism for Gmail/Outlook
        if (config.hasOAuth2()) {
            props.put("mail." + protocol + ".auth.mechanisms", "XOAUTH2");
        }

        props.put("mail." + protocol + ".connectiontimeout",
                String.valueOf(config.connectionTimeout().toMillis()));
        props.put("mail." + protocol + ".timeout",
                String.valueOf(config.timeout().toMillis()));

        session = Session.getInstance(props);
        session.setDebug(config.debug());

        try {
            store = session.getStore(protocol);
            if (config.requiresAuth()) {
                // Use OAuth2 token if configured, otherwise use password
                String credential = config.hasOAuth2()
                        ? config.oauth2Token()
                        : config.password();
                store.connect(config.host(), config.port(), config.username(), credential);
            } else {
                store.connect();
            }

            // Open INBOX
            inbox = store.getFolder(INBOX);
            inbox.open(Folder.READ_WRITE);
        } catch (AuthenticationFailedException e) {
            throw new EmailReceiveException("Authentication failed", e, EmailErrorCode.AUTH_FAILED);
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to connect to mail server", e, EmailErrorCode.CONNECTION_FAILED);
        }
    }

    @Override
    public void disconnect() {
        if (inbox != null && inbox.isOpen()) {
            try {
                inbox.close(true);
            } catch (MessagingException e) {
                // Ignore close errors
            }
        }
        inbox = null;

        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                // Ignore close errors
            }
        }
        store = null;
    }

    @Override
    public boolean isConnected() {
        return store != null && store.isConnected();
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
            Message[] messages = inbox.getMessages();

            // Apply client-side filtering since POP3 doesn't support server-side search
            List<Message> filtered = filterMessages(messages, query);

            // Apply sorting
            filtered = sortMessages(filtered, query.sortOrder());

            // Apply pagination
            int start = Math.min(query.offset(), filtered.size());
            int end = Math.min(start + query.limit(), filtered.size());

            List<ReceivedEmail> result = new ArrayList<>();
            for (int i = start; i < end; i++) {
                try {
                    ReceivedEmail email = parseMessage(filtered.get(i));
                    result.add(email);

                    // Apply post-receive actions
                    if (config.deleteAfterReceive()) {
                        filtered.get(i).setFlag(Flags.Flag.DELETED, true);
                    }
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to parse email message at index {0}", i, e);
                }
            }

            return result;
        } catch (MessagingException e) {
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
            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message instanceof MimeMessage mimeMessage) {
                    if (messageId.equals(mimeMessage.getMessageID())) {
                        return parseMessage(message);
                    }
                }
            }
            return null;
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to receive email by ID: " + messageId, e);
        }
    }

    @Override
    public int getMessageCount(String folder) {
        ensureConnected();
        try {
            return inbox.getMessageCount();
        } catch (MessagingException e) {
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
            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message instanceof MimeMessage mimeMessage) {
                    if (messageId.equals(mimeMessage.getMessageID())) {
                        message.setFlag(Flags.Flag.DELETED, true);
                        inbox.expunge();
                        return;
                    }
                }
            }
            throw EmailReceiveException.messageNotFound(messageId);
        } catch (MessagingException e) {
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

    private List<Message> filterMessages(Message[] messages, EmailQuery query) throws MessagingException {
        List<Message> result = new ArrayList<>();

        for (Message message : messages) {
            if (matchesQuery(message, query)) {
                result.add(message);
            }
        }

        return result;
    }

    private boolean matchesQuery(Message message, EmailQuery query) throws MessagingException {
        // Date filters
        if (query.fromDate() != null) {
            Date received = message.getReceivedDate();
            if (received == null) received = message.getSentDate();
            if (received != null) {
                Date from = Date.from(query.fromDate().atZone(ZoneId.systemDefault()).toInstant());
                if (received.before(from)) {
                    return false;
                }
            }
        }

        if (query.toDate() != null) {
            Date received = message.getReceivedDate();
            if (received == null) received = message.getSentDate();
            if (received != null) {
                Date to = Date.from(query.toDate().atZone(ZoneId.systemDefault()).toInstant());
                if (received.after(to)) {
                    return false;
                }
            }
        }

        // From filter
        if (query.from() != null && !query.from().isEmpty()) {
            Address[] from = message.getFrom();
            if (from == null || from.length == 0) {
                return false;
            }
            boolean found = false;
            for (Address addr : from) {
                String addrStr = addr instanceof InternetAddress ia ? ia.getAddress() : addr.toString();
                for (String queryFrom : query.from()) {
                    if (addrStr.toLowerCase().contains(queryFrom.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }

        // To filter
        if (query.to() != null && !query.to().isEmpty()) {
            Address[] to = message.getRecipients(Message.RecipientType.TO);
            if (to == null || to.length == 0) {
                return false;
            }
            boolean found = false;
            for (Address addr : to) {
                String addrStr = addr instanceof InternetAddress ia ? ia.getAddress() : addr.toString();
                for (String queryTo : query.to()) {
                    if (addrStr.toLowerCase().contains(queryTo.toLowerCase())) {
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
            String subject = message.getSubject();
            if (subject == null || !subject.toLowerCase().contains(query.subjectContains().toLowerCase())) {
                return false;
            }
        }

        // Attachment filter (basic check)
        if (query.hasAttachments()) {
            try {
                Object content = message.getContent();
                if (!(content instanceof Multipart)) {
                    return false;
                }
                Multipart mp = (Multipart) content;
                boolean hasAttachment = false;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part part = mp.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                            || (part.getFileName() != null && !part.getFileName().isBlank())) {
                        hasAttachment = true;
                        break;
                    }
                }
                if (!hasAttachment) return false;
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    private List<Message> sortMessages(List<Message> messages, EmailQuery.SortOrder sortOrder) {
        if (sortOrder == null || messages.size() <= 1) {
            return messages;
        }

        Comparator<Message> comparator = switch (sortOrder) {
            case NEWEST_FIRST -> (m1, m2) -> {
                try {
                    Date d1 = m1.getReceivedDate();
                    Date d2 = m2.getReceivedDate();
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d2.compareTo(d1);
                } catch (MessagingException e) {
                    return 0;
                }
            };
            case OLDEST_FIRST -> (m1, m2) -> {
                try {
                    Date d1 = m1.getReceivedDate();
                    Date d2 = m2.getReceivedDate();
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);
                } catch (MessagingException e) {
                    return 0;
                }
            };
            case SUBJECT_ASC -> (m1, m2) -> {
                try {
                    String s1 = m1.getSubject();
                    String s2 = m2.getSubject();
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;
                    return s1.compareToIgnoreCase(s2);
                } catch (MessagingException e) {
                    return 0;
                }
            };
            case SUBJECT_DESC -> (m1, m2) -> {
                try {
                    String s1 = m1.getSubject();
                    String s2 = m2.getSubject();
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;
                    return s2.compareToIgnoreCase(s1);
                } catch (MessagingException e) {
                    return 0;
                }
            };
            case SENDER_ASC, SENDER_DESC -> (m1, m2) -> {
                try {
                    Address[] a1 = m1.getFrom();
                    Address[] a2 = m2.getFrom();
                    String s1 = a1 != null && a1.length > 0 ? a1[0].toString() : "";
                    String s2 = a2 != null && a2.length > 0 ? a2[0].toString() : "";
                    int result = s1.compareToIgnoreCase(s2);
                    return sortOrder == EmailQuery.SortOrder.SENDER_DESC ? -result : result;
                } catch (MessagingException e) {
                    return 0;
                }
            };
        };

        List<Message> sorted = new ArrayList<>(messages);
        sorted.sort(comparator);
        return sorted;
    }

    private ReceivedEmail parseMessage(Message message) throws MessagingException {
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(INBOX)
                .messageNumber(message.getMessageNumber());

        // Message ID
        if (message instanceof MimeMessage mimeMessage) {
            builder.messageId(mimeMessage.getMessageID());
        }

        // From
        Address[] from = message.getFrom();
        if (from != null && from.length > 0) {
            if (from[0] instanceof InternetAddress ia) {
                builder.from(ia.getAddress());
                builder.fromName(ia.getPersonal());
            } else {
                builder.from(from[0].toString());
            }
        }

        // To
        builder.to(parseAddresses(message.getRecipients(Message.RecipientType.TO)));

        // CC
        builder.cc(parseAddresses(message.getRecipients(Message.RecipientType.CC)));

        // BCC
        builder.bcc(parseAddresses(message.getRecipients(Message.RecipientType.BCC)));

        // Reply-To
        Address[] replyTo = message.getReplyTo();
        if (replyTo != null && replyTo.length > 0) {
            if (replyTo[0] instanceof InternetAddress ia) {
                builder.replyTo(ia.getAddress());
            } else {
                builder.replyTo(replyTo[0].toString());
            }
        }

        // Subject
        builder.subject(message.getSubject());

        // Dates
        if (message.getSentDate() != null) {
            builder.sentDate(message.getSentDate().toInstant());
        }
        if (message.getReceivedDate() != null) {
            builder.receivedDate(message.getReceivedDate().toInstant());
        }

        // Flags - POP3 doesn't persist flags, use default
        builder.flags(EmailFlags.UNREAD);

        // Size
        builder.size(message.getSize());

        // Headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<?> allHeaders = message.getAllHeaders();
        while (allHeaders.hasMoreElements()) {
            Header header = (Header) allHeaders.nextElement();
            headers.put(header.getName(), header.getValue());
        }
        builder.headers(headers);

        // Content and attachments
        parseContent(message, builder);

        return builder.build();
    }

    private List<String> parseAddresses(Address[] addresses) {
        if (addresses == null) {
            return List.of();
        }
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
            parseContentRecursive(part, builder, attachments);
            builder.attachments(attachments);
        } catch (IOException e) {
            throw new MessagingException("Failed to parse message content", e);
        }
    }

    private void parseContentRecursive(Part part, ReceivedEmail.Builder builder,
                                        List<Attachment> attachments) throws MessagingException, IOException {
        Object content = part.getContent();

        if (part.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            String text = content.toString();
            if (builder.build().textContent() == null) {
                builder.textContent(text);
            }
        } else if (part.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            String html = content.toString();
            if (builder.build().htmlContent() == null) {
                builder.htmlContent(html);
            }
        } else if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                parseContentRecursive(multipart.getBodyPart(i), builder, attachments);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                || Part.INLINE.equalsIgnoreCase(part.getDisposition())
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
}
