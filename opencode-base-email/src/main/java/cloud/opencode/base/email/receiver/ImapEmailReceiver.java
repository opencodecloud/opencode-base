package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.*;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.query.EmailQuery;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * IMAP Email Receiver Implementation
 * IMAP邮件接收器实现
 *
 * <p>Receives emails using IMAP protocol with full feature support.</p>
 * <p>使用IMAP协议接收邮件，支持完整功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full IMAP support (SSL/STARTTLS) - 完整IMAP支持（SSL/STARTTLS）</li>
 *   <li>Folder management - 文件夹管理</li>
 *   <li>Server-side search - 服务器端搜索</li>
 *   <li>Flag manipulation - 标记操作</li>
 *   <li>Move/copy operations - 移动/复制操作</li>
 *   <li>Attachment handling - 附件处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Password authentication
 * EmailReceiveConfig config = EmailReceiveConfig.builder()
 *     .host("imap.gmail.com")
 *     .username("user@gmail.com")
 *     .password("app-password")
 *     .imap()
 *     .ssl(true)
 *     .build();
 *
 * // OAuth2 authentication
 * EmailReceiveConfig oauthConfig = EmailReceiveConfig.builder()
 *     .host("imap.gmail.com")
 *     .username("user@gmail.com")
 *     .oauth2Token(accessToken)
 *     .imap()
 *     .ssl(true)
 *     .build();
 *
 * try (ImapEmailReceiver receiver = new ImapEmailReceiver(config)) {
 *     receiver.connect();
 *     List<ReceivedEmail> emails = receiver.receiveUnread();
 *     for (ReceivedEmail email : emails) {
 *         processEmail(email);
 *         receiver.markAsRead(email.messageId());
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
public class ImapEmailReceiver implements EmailReceiver {

    private static final System.Logger logger = System.getLogger(ImapEmailReceiver.class.getName());

    private final EmailReceiveConfig config;
    private Session session;
    private Store store;
    private final Map<String, Folder> openFolders = new HashMap<>();

    /**
     * Create IMAP receiver with configuration
     * 使用配置创建IMAP接收器
     *
     * @param config the receive configuration | 接收配置
     */
    public ImapEmailReceiver(EmailReceiveConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        if (!config.isImap()) {
            throw new IllegalArgumentException("Configuration must use IMAP protocol");
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
        } catch (AuthenticationFailedException e) {
            throw new EmailReceiveException("Authentication failed", e, EmailErrorCode.AUTH_FAILED);
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to connect to mail server", e, EmailErrorCode.CONNECTION_FAILED);
        }
    }

    @Override
    public void disconnect() {
        // Close all open folders
        for (Folder folder : openFolders.values()) {
            try {
                if (folder.isOpen()) {
                    folder.close(true);
                }
            } catch (MessagingException e) {
                // Ignore close errors
            }
        }
        openFolders.clear();

        // Close store
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
        return receive(EmailQuery.builder()
                .folder(config.defaultFolder())
                .unreadOnly()
                .limit(config.maxMessages())
                .build());
    }

    @Override
    public List<ReceivedEmail> receive(EmailQuery query) {
        ensureConnected();

        String folderName = query.folder() != null ? query.folder() : config.defaultFolder();
        Folder folder = getFolder(folderName, Folder.READ_WRITE);

        try {
            SearchTerm searchTerm = buildSearchTerm(query);
            Message[] messages;

            if (searchTerm != null) {
                messages = folder.search(searchTerm);
            } else {
                messages = folder.getMessages();
            }

            // Apply sorting
            messages = sortMessages(messages, query.sortOrder());

            // Apply pagination
            int start = Math.min(query.offset(), messages.length);
            int end = Math.min(start + query.limit(), messages.length);

            List<ReceivedEmail> result = new ArrayList<>();
            for (int i = start; i < end; i++) {
                try {
                    ReceivedEmail email = parseMessage(messages[i], folderName);
                    result.add(email);

                    // Apply post-receive actions
                    if (config.markAsReadAfterReceive()) {
                        messages[i].setFlag(Flags.Flag.SEEN, true);
                    }
                    if (config.deleteAfterReceive()) {
                        messages[i].setFlag(Flags.Flag.DELETED, true);
                    }
                } catch (Exception e) {
                    // Log and skip problematic messages
                    // 记录日志并跳过有问题的消息
                    logger.log(System.Logger.Level.WARNING,
                            "Failed to parse message at index " + i, e);
                }
            }

            // Expunge if any messages were marked for deletion
            if (config.deleteAfterReceive()) {
                folder.expunge();
            }

            return result;
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to receive emails", e, EmailErrorCode.UNKNOWN);
        }
    }

    @Override
    public ReceivedEmail receiveById(String messageId) {
        ensureConnected();

        if (messageId == null || messageId.isBlank()) {
            return null;
        }

        String folderName = config.defaultFolder();
        Folder folder = getFolder(folderName, Folder.READ_ONLY);

        try {
            SearchTerm searchTerm = new MessageIDTerm(messageId);
            Message[] messages = folder.search(searchTerm);

            if (messages.length == 0) {
                // Try other folders
                for (String altFolder : listFolders()) {
                    if (altFolder.equals(folderName)) continue;
                    Folder alt = getFolder(altFolder, Folder.READ_ONLY);
                    messages = alt.search(searchTerm);
                    if (messages.length > 0) {
                        return parseMessage(messages[0], altFolder);
                    }
                }
                return null;
            }

            return parseMessage(messages[0], folderName);
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to receive email by ID: " + messageId, e);
        }
    }

    @Override
    public int getMessageCount(String folder) {
        ensureConnected();
        try {
            Folder f = getFolder(folder, Folder.READ_ONLY);
            return f.getMessageCount();
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to get message count", e);
        }
    }

    @Override
    public int getUnreadCount(String folder) {
        ensureConnected();
        try {
            Folder f = getFolder(folder, Folder.READ_ONLY);
            return f.getUnreadMessageCount();
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to get unread count", e);
        }
    }

    @Override
    public void markAsRead(String messageId) {
        setMessageFlag(messageId, Flags.Flag.SEEN, true);
    }

    @Override
    public void markAsUnread(String messageId) {
        setMessageFlag(messageId, Flags.Flag.SEEN, false);
    }

    @Override
    public void setFlagged(String messageId, boolean flagged) {
        setMessageFlag(messageId, Flags.Flag.FLAGGED, flagged);
    }

    @Override
    public void delete(String messageId) {
        ensureConnected();
        Message message = findMessage(messageId);
        if (message == null) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            message.setFlag(Flags.Flag.DELETED, true);
            message.getFolder().expunge();
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to delete message: " + messageId, e);
        }
    }

    @Override
    public void moveToFolder(String messageId, String targetFolder) {
        ensureConnected();

        Message message = findMessage(messageId);
        if (message == null) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            Folder source = message.getFolder();
            Folder target = getFolder(targetFolder, Folder.READ_WRITE);

            // Copy to target
            source.copyMessages(new Message[]{message}, target);

            // Delete from source
            message.setFlag(Flags.Flag.DELETED, true);
            source.expunge();
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to move message to folder: " + targetFolder, e);
        }
    }

    @Override
    public List<String> listFolders() {
        ensureConnected();
        try {
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] folders = defaultFolder.list("*");

            List<String> result = new ArrayList<>();
            for (Folder folder : folders) {
                if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                    result.add(folder.getFullName());
                }
            }
            return result;
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to list folders", e);
        }
    }

    // ==================== Private Methods ====================

    private void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    private Folder getFolder(String folderName, int mode) {
        try {
            Folder folder = openFolders.get(folderName);
            if (folder != null && folder.isOpen()) {
                // Check if we need to upgrade from READ_ONLY to READ_WRITE
                if (mode == Folder.READ_WRITE && folder.getMode() == Folder.READ_ONLY) {
                    folder.close(false);
                    try {
                        folder.open(mode);
                    } catch (MessagingException e) {
                        openFolders.remove(folderName);
                        throw e;
                    }
                }
                return folder;
            }

            folder = store.getFolder(folderName);
            if (!folder.exists()) {
                throw EmailReceiveException.folderNotFound(folderName);
            }

            folder.open(mode);
            openFolders.put(folderName, folder);
            return folder;
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to open folder: " + folderName, e,
                    EmailErrorCode.FOLDER_ACCESS_DENIED);
        }
    }

    private SearchTerm buildSearchTerm(EmailQuery query) {
        List<SearchTerm> terms = new ArrayList<>();

        if (query.unreadOnly()) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        }

        if (query.flaggedOnly()) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));
        }

        if (!query.includeDeleted()) {
            terms.add(new FlagTerm(new Flags(Flags.Flag.DELETED), false));
        }

        if (query.fromDate() != null) {
            Date date = Date.from(query.fromDate().atZone(ZoneId.systemDefault()).toInstant());
            terms.add(new ReceivedDateTerm(ComparisonTerm.GE, date));
        }

        if (query.toDate() != null) {
            Date date = Date.from(query.toDate().atZone(ZoneId.systemDefault()).toInstant());
            terms.add(new ReceivedDateTerm(ComparisonTerm.LE, date));
        }

        if (query.from() != null && !query.from().isEmpty()) {
            List<SearchTerm> fromTerms = new ArrayList<>();
            for (String from : query.from()) {
                fromTerms.add(new FromStringTerm(from));
            }
            terms.add(new OrTerm(fromTerms.toArray(new SearchTerm[0])));
        }

        if (query.to() != null && !query.to().isEmpty()) {
            List<SearchTerm> toTerms = new ArrayList<>();
            for (String to : query.to()) {
                toTerms.add(new RecipientStringTerm(Message.RecipientType.TO, to));
            }
            terms.add(new OrTerm(toTerms.toArray(new SearchTerm[0])));
        }

        if (query.subjectContains() != null) {
            terms.add(new SubjectTerm(query.subjectContains()));
        }

        if (query.bodyContains() != null) {
            terms.add(new BodyTerm(query.bodyContains()));
        }

        if (terms.isEmpty()) {
            return null;
        }

        if (terms.size() == 1) {
            return terms.getFirst();
        }

        return new AndTerm(terms.toArray(new SearchTerm[0]));
    }

    private Message[] sortMessages(Message[] messages, EmailQuery.SortOrder sortOrder) {
        if (sortOrder == null || messages.length <= 1) {
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

        List<Message> sorted = new ArrayList<>(Arrays.asList(messages));
        sorted.sort(comparator);
        return sorted.toArray(new Message[0]);
    }

    private ReceivedEmail parseMessage(Message message, String folderName) throws MessagingException {
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(folderName)
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

        // Flags
        builder.flags(EmailFlags.from(message.getFlags()));

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
            parseContentRecursive(part, builder, attachments, 0);
            builder.attachments(attachments);
        } catch (IOException e) {
            throw new MessagingException("Failed to parse message content", e);
        }
    }

    private static final int MAX_MIME_DEPTH = 50;

    private void parseContentRecursive(Part part, ReceivedEmail.Builder builder,
                                        List<Attachment> attachments, int depth) throws MessagingException, IOException {
        if (depth > MAX_MIME_DEPTH) {
            return;
        }
        String contentType = part.getContentType().toLowerCase();
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
                parseContentRecursive(multipart.getBodyPart(i), builder, attachments, depth + 1);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                || Part.INLINE.equalsIgnoreCase(part.getDisposition())
                || part.getFileName() != null) {
            // Handle attachment
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

    private void setMessageFlag(String messageId, Flags.Flag flag, boolean value) {
        ensureConnected();
        Message message = findMessage(messageId);
        if (message == null) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            // Ensure folder is open in READ_WRITE mode
            Folder folder = message.getFolder();
            if (!folder.isOpen() || folder.getMode() != Folder.READ_WRITE) {
                if (folder.isOpen()) {
                    folder.close(false);
                }
                folder.open(Folder.READ_WRITE);
            }
            message.setFlag(flag, value);
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to set message flag: " + messageId, e);
        }
    }

    private Message findMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return null;
        }

        try {
            // Search in default folder first
            Folder folder = getFolder(config.defaultFolder(), Folder.READ_WRITE);
            SearchTerm searchTerm = new MessageIDTerm(messageId);
            Message[] messages = folder.search(searchTerm);

            if (messages.length > 0) {
                return messages[0];
            }

            // Search in other folders
            for (String folderName : listFolders()) {
                if (folderName.equals(config.defaultFolder())) continue;
                folder = getFolder(folderName, Folder.READ_WRITE);
                messages = folder.search(searchTerm);
                if (messages.length > 0) {
                    return messages[0];
                }
            }

            return null;
        } catch (MessagingException e) {
            throw new EmailReceiveException("Failed to find message: " + messageId, e);
        }
    }
}
