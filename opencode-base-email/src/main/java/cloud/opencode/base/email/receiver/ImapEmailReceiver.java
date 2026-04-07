package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.*;
import cloud.opencode.base.email.attachment.ByteArrayAttachment;
import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailReceiveException;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.protocol.ProtocolException;
import cloud.opencode.base.email.protocol.imap.ImapClient;
import cloud.opencode.base.email.protocol.mime.MimeParser;
import cloud.opencode.base.email.protocol.mime.ParsedMessage;
import cloud.opencode.base.email.query.EmailQuery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Date formatter for IMAP SEARCH date criteria (dd-MMM-yyyy).
     * IMAP SEARCH 日期条件的日期格式化器（dd-MMM-yyyy）。
     */
    private static final DateTimeFormatter IMAP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    /**
     * Pattern to extract FLAGS from an IMAP FETCH response line.
     * 从 IMAP FETCH 响应行中提取 FLAGS 的正则表达式。
     */
    private static final Pattern FLAGS_PATTERN = Pattern.compile("FLAGS \\(([^)]*)\\)");

    /**
     * Pattern to extract RFC822.SIZE from an IMAP FETCH response line.
     * 从 IMAP FETCH 响应行中提取 RFC822.SIZE 的正则表达式。
     */
    private static final Pattern SIZE_PATTERN = Pattern.compile("RFC822\\.SIZE (\\d+)");

    /**
     * Pattern to extract the raw BODY[] literal from an IMAP FETCH response.
     * 从 IMAP FETCH 响应中提取原始 BODY[] 字面量的正则表达式。
     */
    private static final Pattern BODY_LITERAL_PATTERN = Pattern.compile("\\{(\\d+)\\}\r?\n");

    private final EmailReceiveConfig config;
    private ImapClient client;
    private String currentFolder;

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

        try {
            client = new ImapClient(
                    config.host(),
                    config.port(),
                    config.ssl(),
                    config.starttls(),
                    config.connectionTimeout(),
                    config.timeout()
            );
            client.connect();

            if (config.requiresAuth()) {
                if (config.hasOAuth2()) {
                    client.authenticateXOAuth2(config.username(), config.oauth2Token());
                } else {
                    client.login(config.username(), config.password());
                }
            }
        } catch (ProtocolException e) {
            // Clean up on failure
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {
                    // Ignore close errors during cleanup
                }
                client = null;
            }
            if (e.isAuthenticationFailure()) {
                throw new EmailReceiveException("Authentication failed", e, EmailErrorCode.AUTH_FAILED);
            }
            throw new EmailReceiveException("Failed to connect to mail server", e,
                    EmailErrorCode.CONNECTION_FAILED);
        }
    }

    @Override
    public void disconnect() {
        if (client != null) {
            try {
                client.logout();
            } catch (ProtocolException e) {
                // Ignore logout errors, fall through to close
            }
            try {
                client.close();
            } catch (Exception e) {
                // Ignore close errors
            }
            client = null;
            currentFolder = null;
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
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

        try {
            selectFolder(folderName);

            // Build IMAP SEARCH criteria from query
            String criteria = buildSearchCriteria(query);
            List<Integer> seqNums = client.search(criteria);

            if (seqNums.isEmpty()) {
                return List.of();
            }

            // Fetch and parse messages — batch fetch when sequence numbers are contiguous
            // 获取并解析消息 — 序列号连续时使用批量获取
            List<ReceivedEmail> allEmails = new ArrayList<>(seqNums.size());
            int min = Collections.min(seqNums);
            int max = Collections.max(seqNums);
            Set<Integer> seqSet = (seqNums.size() > 1) ? new HashSet<>(seqNums) : null;
            boolean contiguous = (max - min + 1 == seqNums.size());

            if (contiguous) {
                // Contiguous range — single FETCH round trip
                // 连续范围 — 单次 FETCH 往返
                try {
                    Map<Integer, String> fetched = client.fetchRange(min, max,
                            "(BODY[] FLAGS RFC822.SIZE)");
                    for (int seq : seqNums) {
                        try {
                            String fetchResponse = fetched.get(seq);
                            if (fetchResponse == null) {
                                continue;
                            }
                            ReceivedEmail email = parseFetchResponse(fetchResponse, seq, folderName);
                            if (email != null) {
                                allEmails.add(email);
                            }
                        } catch (Exception e) {
                            logger.log(System.Logger.Level.WARNING,
                                    "Failed to parse message at sequence " + seq, e);
                        }
                    }
                } catch (ProtocolException e) {
                    // Fall back to individual fetch on batch failure
                    // 批量获取失败时回退到单条获取
                    logger.log(System.Logger.Level.WARNING,
                            "Batch fetch failed, falling back to individual fetch", e);
                    fetchIndividually(seqNums, folderName, allEmails);
                }
            } else {
                // Non-contiguous — individual fetch (existing behavior)
                // 非连续 — 逐条获取（现有行为）
                fetchIndividually(seqNums, folderName, allEmails);
            }

            // Apply sorting
            sortEmails(allEmails, query.sortOrder());

            // Apply pagination
            int start = Math.min(query.offset(), allEmails.size());
            int end = Math.min(start + query.limit(), allEmails.size());
            List<ReceivedEmail> result = new ArrayList<>(allEmails.subList(start, end));

            // Post-receive actions: mark as read and/or delete
            if (config.markAsReadAfterReceive() || config.deleteAfterReceive()) {
                for (ReceivedEmail email : result) {
                    int seq = email.messageNumber();
                    try {
                        if (config.markAsReadAfterReceive()) {
                            client.store(seq, "+FLAGS", "(\\Seen)");
                        }
                        if (config.deleteAfterReceive()) {
                            client.store(seq, "+FLAGS", "(\\Deleted)");
                        }
                    } catch (ProtocolException e) {
                        logger.log(System.Logger.Level.WARNING,
                                "Failed to update flags for message " + seq, e);
                    }
                }
                if (config.deleteAfterReceive()) {
                    client.expunge();
                }
            }

            return result;
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to receive emails", e, EmailErrorCode.UNKNOWN);
        }
    }

    @Override
    public ReceivedEmail receiveById(String messageId) {
        ensureConnected();

        if (messageId == null || messageId.isBlank()) {
            return null;
        }

        try {
            // Search in default folder first
            String folderName = config.defaultFolder();
            selectFolder(folderName);

            String searchCriteria = "HEADER Message-ID \"" + escapeSearchValue(messageId) + "\"";
            List<Integer> seqNums = client.search(searchCriteria);

            if (!seqNums.isEmpty()) {
                String fetchResponse = client.fetch(seqNums.getFirst(), "(BODY[] FLAGS RFC822.SIZE)");
                return parseFetchResponse(fetchResponse, seqNums.getFirst(), folderName);
            }

            // Try other folders
            for (String altFolder : listFolders()) {
                if (altFolder.equals(folderName)) continue;
                selectFolder(altFolder);
                seqNums = client.search(searchCriteria);
                if (!seqNums.isEmpty()) {
                    String fetchResponse = client.fetch(seqNums.getFirst(),
                            "(BODY[] FLAGS RFC822.SIZE)");
                    return parseFetchResponse(fetchResponse, seqNums.getFirst(), altFolder);
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
            examineFolder(folder);
            return client.getMessageCount();
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to get message count", e);
        }
    }

    @Override
    public int getUnreadCount(String folder) {
        ensureConnected();
        try {
            examineFolder(folder);
            return client.getUnreadCount();
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to get unread count", e);
        }
    }

    @Override
    public void markAsRead(String messageId) {
        setMessageFlag(messageId, "+FLAGS", "(\\Seen)");
    }

    @Override
    public void markAsUnread(String messageId) {
        setMessageFlag(messageId, "-FLAGS", "(\\Seen)");
    }

    @Override
    public void setFlagged(String messageId, boolean flagged) {
        setMessageFlag(messageId, flagged ? "+FLAGS" : "-FLAGS", "(\\Flagged)");
    }

    @Override
    public void delete(String messageId) {
        ensureConnected();
        int seq = findMessageSeq(messageId);
        if (seq < 0) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            client.store(seq, "+FLAGS", "(\\Deleted)");
            client.expunge();
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to delete message: " + messageId, e);
        }
    }

    @Override
    public void moveToFolder(String messageId, String targetFolder) {
        ensureConnected();

        int seq = findMessageSeq(messageId);
        if (seq < 0) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            // Copy to target folder
            client.copy(seq, targetFolder);

            // Delete from source
            client.store(seq, "+FLAGS", "(\\Deleted)");
            client.expunge();
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to move message to folder: " + targetFolder, e);
        }
    }

    @Override
    public List<String> listFolders() {
        ensureConnected();
        try {
            List<String[]> folders = client.list("", "*");

            List<String> result = new ArrayList<>(folders.size());
            for (String[] folderInfo : folders) {
                String flags = folderInfo[0];
                String name = folderInfo[2];
                // Filter out folders with \Noselect flag
                if (!flags.contains("\\Noselect")) {
                    // Remove quotes from folder name if present
                    if (name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                    result.add(name);
                }
            }
            return result;
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to list folders", e);
        }
    }

    // ==================== Private Methods ====================

    private void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    /**
     * Select a folder (READ_WRITE) if not already selected.
     * 选择文件夹（读写模式），如果尚未选择。
     */
    private void selectFolder(String folderName) throws ProtocolException {
        if (!folderName.equals(currentFolder)) {
            client.select(folderName);
            currentFolder = folderName;
        }
    }

    /**
     * Examine a folder (READ_ONLY) without changing the selected folder state.
     * 以只读模式检查文件夹，不改变已选择的文件夹状态。
     */
    private void examineFolder(String folderName) throws ProtocolException {
        client.examine(folderName);
        // examine does not change the selected state for write operations
        currentFolder = null;
    }

    /**
     * Build IMAP SEARCH criteria string from EmailQuery.
     * 从 EmailQuery 构建 IMAP SEARCH 条件字符串。
     */
    private String buildSearchCriteria(EmailQuery query) {
        List<String> parts = new ArrayList<>(8);

        if (query.unreadOnly()) {
            parts.add("UNSEEN");
        }

        if (query.flaggedOnly()) {
            parts.add("FLAGGED");
        }

        if (!query.includeDeleted()) {
            parts.add("NOT DELETED");
        }

        if (query.fromDate() != null) {
            parts.add("SINCE " + formatImapDate(query.fromDate()));
        }

        if (query.toDate() != null) {
            parts.add("BEFORE " + formatImapDate(query.toDate()));
        }

        if (query.from() != null && !query.from().isEmpty()) {
            if (query.from().size() == 1) {
                parts.add("FROM \"" + escapeSearchValue(query.from().iterator().next()) + "\"");
            } else {
                // Build OR chain for multiple from addresses
                List<String> fromList = new ArrayList<>(query.from());
                String orChain = buildOrChain("FROM", fromList);
                parts.add(orChain);
            }
        }

        if (query.to() != null && !query.to().isEmpty()) {
            if (query.to().size() == 1) {
                parts.add("TO \"" + escapeSearchValue(query.to().iterator().next()) + "\"");
            } else {
                List<String> toList = new ArrayList<>(query.to());
                String orChain = buildOrChain("TO", toList);
                parts.add(orChain);
            }
        }

        if (query.subjectContains() != null) {
            parts.add("SUBJECT \"" + escapeSearchValue(query.subjectContains()) + "\"");
        }

        if (query.bodyContains() != null) {
            parts.add("BODY \"" + escapeSearchValue(query.bodyContains()) + "\"");
        }

        if (parts.isEmpty()) {
            return "ALL";
        }

        return String.join(" ", parts);
    }

    /**
     * Build an OR chain for IMAP SEARCH: OR key "a" OR key "b" key "c"
     * 构建 IMAP SEARCH 的 OR 链：OR key "a" OR key "b" key "c"
     */
    private String buildOrChain(String key, List<String> values) {
        if (values.size() == 1) {
            return key + " \"" + escapeSearchValue(values.getFirst()) + "\"";
        }
        if (values.size() == 2) {
            return "OR " + key + " \"" + escapeSearchValue(values.get(0)) + "\" "
                    + key + " \"" + escapeSearchValue(values.get(1)) + "\"";
        }
        // Recursive OR: OR key "first" (rest)
        return "OR " + key + " \"" + escapeSearchValue(values.getFirst()) + "\" "
                + buildOrChain(key, values.subList(1, values.size()));
    }

    /**
     * Format a LocalDateTime as IMAP date string (dd-MMM-yyyy).
     * 将 LocalDateTime 格式化为 IMAP 日期字符串（dd-MMM-yyyy）。
     */
    private String formatImapDate(LocalDateTime date) {
        return date.format(IMAP_DATE_FORMAT);
    }

    /**
     * Escape special characters in IMAP SEARCH string values.
     * 转义 IMAP SEARCH 字符串值中的特殊字符。
     */
    private String escapeSearchValue(String value) {
        if (value == null) {
            return "";
        }
        // Reject characters that could inject IMAP commands
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || c == '\0') {
                throw new IllegalArgumentException(
                        "Invalid character in search value: 0x" + Integer.toHexString(c));
            }
        }
        // Escape backslashes and double quotes
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Parse a FETCH response into a ReceivedEmail.
     * 将 FETCH 响应解析为 ReceivedEmail。
     */
    private ReceivedEmail parseFetchResponse(String fetchResponse, int seq, String folderName) {
        if (fetchResponse == null || fetchResponse.isBlank()) {
            return null;
        }

        // Extract FLAGS from the response
        EmailFlags flags = parseFlagsFromResponse(fetchResponse);

        // Extract RFC822.SIZE from the response
        long size = parseSizeFromResponse(fetchResponse);

        // Extract the raw message body from the BODY[] literal
        String rawMessage = extractBodyLiteral(fetchResponse);
        if (rawMessage == null || rawMessage.isBlank()) {
            return null;
        }

        // Parse the raw MIME message
        ParsedMessage parsed = MimeParser.parse(rawMessage);

        // Build ReceivedEmail from ParsedMessage
        ReceivedEmail.Builder builder = ReceivedEmail.builder()
                .folder(folderName)
                .messageNumber(seq)
                .messageId(parsed.messageId())
                .from(parsed.from())
                .fromName(parsed.fromName())
                .to(parsed.to() != null ? parsed.to() : List.of())
                .cc(parsed.cc() != null ? parsed.cc() : List.of())
                .bcc(parsed.bcc() != null ? parsed.bcc() : List.of())
                .replyTo(parsed.replyTo())
                .subject(parsed.subject())
                .textContent(parsed.textContent())
                .htmlContent(parsed.htmlContent())
                .sentDate(parsed.sentDate())
                .receivedDate(parsed.receivedDate())
                .flags(flags)
                .size(size > 0 ? size : parsed.size())
                .headers(parsed.headers() != null ? parsed.headers() : Map.of());

        // Convert ParsedAttachments to Attachments
        if (parsed.attachments() != null && !parsed.attachments().isEmpty()) {
            List<Attachment> attachments = new ArrayList<>(parsed.attachments().size());
            for (ParsedMessage.ParsedAttachment pa : parsed.attachments()) {
                if (pa.fileName() != null && pa.data() != null) {
                    attachments.add(ByteArrayAttachment.of(
                            pa.fileName(),
                            pa.data(),
                            pa.contentType()
                    ));
                }
            }
            builder.attachments(attachments);
        }

        return builder.build();
    }

    /**
     * Parse IMAP FLAGS from a FETCH response string.
     * 从 FETCH 响应字符串中解析 IMAP FLAGS。
     */
    private EmailFlags parseFlagsFromResponse(String response) {
        Matcher matcher = FLAGS_PATTERN.matcher(response);
        if (!matcher.find()) {
            return EmailFlags.UNREAD;
        }

        String flagsStr = matcher.group(1).toUpperCase(Locale.ENGLISH);
        return new EmailFlags(
                flagsStr.contains("\\SEEN"),
                flagsStr.contains("\\ANSWERED"),
                flagsStr.contains("\\FLAGGED"),
                flagsStr.contains("\\DELETED"),
                flagsStr.contains("\\DRAFT"),
                flagsStr.contains("\\RECENT")
        );
    }

    /**
     * Parse RFC822.SIZE from a FETCH response string.
     * 从 FETCH 响应字符串中解析 RFC822.SIZE。
     */
    private long parseSizeFromResponse(String response) {
        Matcher matcher = SIZE_PATTERN.matcher(response);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Extract the raw BODY[] literal content from a FETCH response.
     * 从 FETCH 响应中提取原始 BODY[] 字面量内容。
     *
     * <p>The FETCH response contains a literal like {1234}\r\n followed by
     * exactly 1234 bytes of the raw message.</p>
     */
    private String extractBodyLiteral(String response) {
        Matcher matcher = BODY_LITERAL_PATTERN.matcher(response);
        if (!matcher.find()) {
            return null;
        }

        int literalSize;
        try {
            literalSize = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }

        int start = matcher.end();
        if (start + literalSize > response.length()) {
            // If the literal size exceeds available data, take what we have
            return response.substring(start);
        }

        return response.substring(start, start + literalSize);
    }

    /**
     * Sort ReceivedEmail list according to the specified sort order.
     * 根据指定的排序顺序对 ReceivedEmail 列表排序。
     */
    private void sortEmails(List<ReceivedEmail> emails, EmailQuery.SortOrder sortOrder) {
        if (sortOrder == null || emails.size() <= 1) {
            return;
        }

        Comparator<ReceivedEmail> comparator = switch (sortOrder) {
            case NEWEST_FIRST -> (e1, e2) -> {
                Instant d1 = e1.receivedDate() != null ? e1.receivedDate() : e1.sentDate();
                Instant d2 = e2.receivedDate() != null ? e2.receivedDate() : e2.sentDate();
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d2.compareTo(d1);
            };
            case OLDEST_FIRST -> (e1, e2) -> {
                Instant d1 = e1.receivedDate() != null ? e1.receivedDate() : e1.sentDate();
                Instant d2 = e2.receivedDate() != null ? e2.receivedDate() : e2.sentDate();
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            };
            case SUBJECT_ASC -> (e1, e2) -> {
                String s1 = e1.subject();
                String s2 = e2.subject();
                if (s1 == null) return 1;
                if (s2 == null) return -1;
                return s1.compareToIgnoreCase(s2);
            };
            case SUBJECT_DESC -> (e1, e2) -> {
                String s1 = e1.subject();
                String s2 = e2.subject();
                if (s1 == null) return 1;
                if (s2 == null) return -1;
                return s2.compareToIgnoreCase(s1);
            };
            case SENDER_ASC, SENDER_DESC -> (e1, e2) -> {
                String s1 = e1.from() != null ? e1.from() : "";
                String s2 = e2.from() != null ? e2.from() : "";
                int result = s1.compareToIgnoreCase(s2);
                return sortOrder == EmailQuery.SortOrder.SENDER_DESC ? -result : result;
            };
        };

        emails.sort(comparator);
    }

    /**
     * Fetch messages individually by sequence number.
     * 按序列号逐条获取消息。
     *
     * @param seqNums    the sequence numbers to fetch | 要获取的序列号列表
     * @param folderName the folder name for building ReceivedEmail | 用于构建 ReceivedEmail 的文件夹名
     * @param result     the list to accumulate parsed emails into | 用于累积解析邮件的列表
     */
    private void fetchIndividually(List<Integer> seqNums, String folderName,
                                   List<ReceivedEmail> result) {
        for (int seq : seqNums) {
            try {
                String fetchResponse = client.fetch(seq, "(BODY[] FLAGS RFC822.SIZE)");
                ReceivedEmail email = parseFetchResponse(fetchResponse, seq, folderName);
                if (email != null) {
                    result.add(email);
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Failed to parse message at sequence " + seq, e);
            }
        }
    }

    /**
     * Set a flag on a message identified by its Message-ID.
     * 为通过 Message-ID 标识的消息设置标记。
     */
    private void setMessageFlag(String messageId, String action, String flags) {
        ensureConnected();
        int seq = findMessageSeq(messageId);
        if (seq < 0) {
            throw EmailReceiveException.messageNotFound(messageId);
        }

        try {
            client.store(seq, action, flags);
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to set message flag: " + messageId, e);
        }
    }

    /**
     * Find the sequence number of a message by its Message-ID header.
     * 通过 Message-ID 邮件头查找消息的序列号。
     *
     * <p>Searches the default folder first, then other folders.</p>
     * <p>先搜索默认文件夹，然后搜索其他文件夹。</p>
     *
     * @param messageId the Message-ID to find | 要查找的 Message-ID
     * @return the sequence number, or -1 if not found | 序列号，未找到返回 -1
     */
    private int findMessageSeq(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return -1;
        }

        String searchCriteria = "HEADER Message-ID \"" + escapeSearchValue(messageId) + "\"";

        try {
            // Search in default folder first
            selectFolder(config.defaultFolder());
            List<Integer> seqNums = client.search(searchCriteria);
            if (!seqNums.isEmpty()) {
                return seqNums.getFirst();
            }

            // Search in other folders
            for (String folderName : listFolders()) {
                if (folderName.equals(config.defaultFolder())) continue;
                selectFolder(folderName);
                seqNums = client.search(searchCriteria);
                if (!seqNums.isEmpty()) {
                    return seqNums.getFirst();
                }
            }

            return -1;
        } catch (ProtocolException e) {
            throw new EmailReceiveException("Failed to find message: " + messageId, e);
        }
    }
}
