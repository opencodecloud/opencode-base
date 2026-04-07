package cloud.opencode.base.email.protocol.imap;

import cloud.opencode.base.email.protocol.MailConnection;
import cloud.opencode.base.email.protocol.ProtocolException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IMAP4rev1 Protocol Client
 * IMAP4rev1 协议客户端
 *
 * <p>Implements the IMAP4rev1 protocol (RFC 3501) for mailbox access, message
 * retrieval, folder management, and IDLE push notifications. Supports LOGIN
 * and XOAUTH2 authentication, STARTTLS upgrade, and literal data handling.</p>
 *
 * <p>实现 IMAP4rev1 协议 (RFC 3501)，支持邮箱访问、邮件检索、文件夹管理和
 * IDLE 推送通知。支持 LOGIN 和 XOAUTH2 认证、STARTTLS 升级及字面量数据处理。</p>
 *
 * <pre>{@code
 * try (var client = new ImapClient("imap.example.com", 993, true, false,
 *         Duration.ofSeconds(10), Duration.ofSeconds(30))) {
 *     client.connect();
 *     client.login("user@example.com", "password");
 *     int[] counts = client.select("INBOX");
 *     System.out.println("Messages: " + counts[0] + ", Recent: " + counts[1]);
 *     List<Integer> unseen = client.search("UNSEEN");
 *     for (int seq : unseen) {
 *         String data = client.fetch(seq, "(ENVELOPE BODY[HEADER])");
 *         System.out.println(data);
 *     }
 *     client.logout();
 * }
 * }</pre>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc3501">RFC 3501 - IMAP4rev1</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class ImapClient implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(ImapClient.class.getName());

    /** Pattern to extract literal byte count from IMAP response, e.g., {1234} */
    private static final Pattern LITERAL_PATTERN = Pattern.compile("\\{(\\d+)\\}$");

    /** Pattern to match EXISTS untagged response */
    private static final Pattern EXISTS_PATTERN = Pattern.compile("^\\* (\\d+) EXISTS$");

    /** Pattern to match RECENT untagged response */
    private static final Pattern RECENT_PATTERN = Pattern.compile("^\\* (\\d+) RECENT$");

    /** Pattern to match SEARCH untagged response */
    private static final Pattern SEARCH_PATTERN = Pattern.compile("^\\* SEARCH(.*)$");

    /** Pattern to match LIST untagged response */
    private static final Pattern LIST_PATTERN =
            Pattern.compile("^\\* LIST \\(([^)]*)\\) \"([^\"]*|NIL)\" (.+)$");

    /** Pattern to extract CAPABILITY from greeting or response */
    private static final Pattern CAPABILITY_PATTERN =
            Pattern.compile("\\[CAPABILITY ([^]]+)]");

    /** Pattern to extract MESSAGES count from STATUS response */
    private static final Pattern MESSAGES_PATTERN =
            Pattern.compile("\\bMESSAGES\\s+(\\d+)");

    /** Pattern to match FETCH untagged response */
    private static final Pattern FETCH_RESPONSE_PATTERN =
            Pattern.compile("^\\* (\\d+) FETCH .*$");

    /** Maximum literal size to prevent memory exhaustion: 64 MB */
    private static final int MAX_LITERAL_SIZE = 64 * 1024 * 1024;

    /** Maximum number of untagged responses to collect to prevent unbounded growth */
    private static final int MAX_UNTAGGED_RESPONSES = 100_000;

    private int tagCounter;
    private MailConnection connection;
    private String selectedFolder;
    private final Set<String> capabilities = new HashSet<>();

    private final String host;
    private final int port;
    private final boolean ssl;
    private final boolean starttls;
    private final Duration connectionTimeout;
    private final Duration readTimeout;

    /**
     * Create a new IMAP client
     * 创建新的 IMAP 客户端
     *
     * @param host              the IMAP server hostname | IMAP 服务器主机名
     * @param port              the IMAP server port | IMAP 服务器端口
     * @param ssl               true for implicit SSL/TLS (e.g., port 993) | true 使用隐式 SSL/TLS（如 993 端口）
     * @param starttls          true to upgrade via STARTTLS after connect | true 连接后通过 STARTTLS 升级
     * @param connectionTimeout connection timeout | 连接超时
     * @param readTimeout       read timeout | 读取超时
     * @throws IllegalArgumentException if host is null/blank, port is invalid, or timeouts are null/negative
     *                                  | 主机为空、端口无效或超时为空/负值时抛出
     */
    public ImapClient(String host, int port, boolean ssl, boolean starttls,
                      Duration connectionTimeout, Duration readTimeout) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must not be null or blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535: " + port);
        }
        if (connectionTimeout == null || connectionTimeout.isNegative()) {
            throw new IllegalArgumentException("Connection timeout must not be null or negative");
        }
        if (readTimeout == null || readTimeout.isNegative()) {
            throw new IllegalArgumentException("Read timeout must not be null or negative");
        }
        if (ssl && starttls) {
            throw new IllegalArgumentException("Cannot use both SSL and STARTTLS simultaneously");
        }
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.starttls = starttls;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Connect to the IMAP server and read the greeting
     * 连接到 IMAP 服务器并读取问候语
     *
     * <p>Connects the underlying socket, reads the server greeting (which must
     * start with {@code * OK}), and parses any capabilities advertised in the
     * greeting. If STARTTLS is enabled, upgrades to TLS before proceeding.</p>
     *
     * <p>连接底层套接字，读取服务器问候语（必须以 {@code * OK} 开头），
     * 并解析问候语中公布的能力。如果启用了 STARTTLS，则在继续之前升级到 TLS。</p>
     *
     * @throws ProtocolException if connection or greeting fails | 连接或问候语失败时抛出
     */
    public void connect() throws ProtocolException {
        connection = new MailConnection(host, port, ssl, connectionTimeout, readTimeout);
        connection.connect();

        // Read server greeting
        String greeting = connection.readLine();
        LOG.log(System.Logger.Level.DEBUG, "IMAP greeting: {0}", greeting);

        if (!greeting.startsWith("* OK")) {
            connection.close();
            throw new ProtocolException("IMAP server rejected connection: " + greeting);
        }

        // Parse capabilities from greeting if present
        parseCapabilities(greeting);

        // STARTTLS upgrade
        if (starttls) {
            if (!capabilities.isEmpty() && !hasCapability("STARTTLS")) {
                throw new ProtocolException("Server does not advertise STARTTLS capability");
            }
            CommandResult result = sendCommand("STARTTLS");
            assertOk(result, "STARTTLS");
            connection.upgradeToTls();
            LOG.log(System.Logger.Level.DEBUG, "STARTTLS upgrade complete");
            // Re-read capabilities after TLS upgrade
            capabilities.clear();
            refreshCapabilities();
        }

        // If no capabilities from greeting, request them
        if (capabilities.isEmpty()) {
            refreshCapabilities();
        }
    }

    /**
     * Authenticate using LOGIN command
     * 使用 LOGIN 命令进行认证
     *
     * <p>Sends the IMAP LOGIN command with the given username and password.
     * Credentials are quoted according to IMAP string quoting rules.</p>
     *
     * <p>使用给定的用户名和密码发送 IMAP LOGIN 命令。
     * 凭证按照 IMAP 字符串引用规则进行转义。</p>
     *
     * @param username the login username | 登录用户名
     * @param password the login password | 登录密码
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     */
    public void login(String username, String password) throws ProtocolException {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(password, "Password must not be null");

        CommandResult result = sendCommand(
                "LOGIN " + quoteString(username) + " " + quoteString(password));
        assertOk(result, "LOGIN");

        // Parse CAPABILITY from OK response if present
        parseCapabilities(result.taggedResponse);
        LOG.log(System.Logger.Level.DEBUG, "LOGIN successful for {0}", username);
    }

    /**
     * Authenticate using XOAUTH2 mechanism
     * 使用 XOAUTH2 机制进行认证
     *
     * <p>Sends the IMAP AUTHENTICATE XOAUTH2 command with a Base64-encoded
     * SASL client response containing the username and OAuth2 bearer token.</p>
     *
     * <p>使用 Base64 编码的 SASL 客户端响应发送 IMAP AUTHENTICATE XOAUTH2 命令，
     * 响应中包含用户名和 OAuth2 Bearer 令牌。</p>
     *
     * @param username the email address | 电子邮件地址
     * @param token    the OAuth2 access token | OAuth2 访问令牌
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     * @see <a href="https://developers.google.com/gmail/imap/xoauth2-protocol">XOAUTH2</a>
     */
    public void authenticateXOAuth2(String username, String token) throws ProtocolException {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(token, "Token must not be null");

        // Build XOAUTH2 SASL initial response
        String authString = "user=" + username + "\u0001auth=Bearer " + token + "\u0001\u0001";
        String encoded = Base64.getEncoder().encodeToString(
                authString.getBytes(StandardCharsets.UTF_8));

        CommandResult result = sendCommand("AUTHENTICATE XOAUTH2 " + encoded);
        assertOk(result, "AUTHENTICATE XOAUTH2");

        parseCapabilities(result.taggedResponse);
        LOG.log(System.Logger.Level.DEBUG, "XOAUTH2 authentication successful for {0}", username);
    }

    /**
     * Select a folder for read-write access
     * 选择文件夹以读写方式打开
     *
     * <p>Issues the IMAP SELECT command to open the specified folder for
     * read-write access. Parses EXISTS and RECENT counts from the response.</p>
     *
     * <p>发出 IMAP SELECT 命令以读写方式打开指定文件夹。
     * 从响应中解析 EXISTS 和 RECENT 计数。</p>
     *
     * @param folder the folder name (e.g., "INBOX") | 文件夹名称（如 "INBOX"）
     * @return an array of {@code [exists, recent]} counts
     *         | 包含 {@code [exists, recent]} 计数的数组
     * @throws ProtocolException if the SELECT fails | SELECT 失败时抛出
     */
    public int[] select(String folder) throws ProtocolException {
        Objects.requireNonNull(folder, "Folder must not be null");
        return openFolder("SELECT", folder);
    }

    /**
     * Examine a folder for read-only access
     * 以只读方式检查文件夹
     *
     * <p>Issues the IMAP EXAMINE command to open the specified folder for
     * read-only access. Parses EXISTS and RECENT counts from the response.</p>
     *
     * <p>发出 IMAP EXAMINE 命令以只读方式打开指定文件夹。
     * 从响应中解析 EXISTS 和 RECENT 计数。</p>
     *
     * @param folder the folder name (e.g., "INBOX") | 文件夹名称（如 "INBOX"）
     * @return an array of {@code [exists, recent]} counts
     *         | 包含 {@code [exists, recent]} 计数的数组
     * @throws ProtocolException if the EXAMINE fails | EXAMINE 失败时抛出
     */
    public int[] examine(String folder) throws ProtocolException {
        Objects.requireNonNull(folder, "Folder must not be null");
        return openFolder("EXAMINE", folder);
    }

    /**
     * Search for messages matching the given criteria
     * 搜索匹配给定条件的邮件
     *
     * <p>Issues the IMAP SEARCH command with the specified criteria string.
     * Returns message sequence numbers matching the criteria.</p>
     *
     * <p>使用指定的条件字符串发出 IMAP SEARCH 命令。
     * 返回匹配条件的邮件序列号。</p>
     *
     * @param criteria the IMAP search criteria (e.g., "UNSEEN", "SINCE 1-Jan-2024")
     *                 | IMAP 搜索条件（如 "UNSEEN"、"SINCE 1-Jan-2024"）
     * @return a list of message sequence numbers | 邮件序列号列表
     * @throws ProtocolException if the SEARCH fails | SEARCH 失败时抛出
     */
    public List<Integer> search(String criteria) throws ProtocolException {
        Objects.requireNonNull(criteria, "Search criteria must not be null");
        requireSelectedFolder();

        CommandResult result = sendCommand("SEARCH " + criteria);
        assertOk(result, "SEARCH");

        List<Integer> sequences = new ArrayList<>();
        for (String line : result.untaggedResponses) {
            Matcher m = SEARCH_PATTERN.matcher(line);
            if (m.matches()) {
                String numbers = m.group(1).trim();
                if (!numbers.isEmpty()) {
                    for (String num : numbers.split("\\s+")) {
                        sequences.add(Integer.parseInt(num));
                    }
                }
            }
        }
        return sequences;
    }

    /**
     * Fetch data for a single message
     * 获取单条邮件的数据
     *
     * <p>Issues the IMAP FETCH command for the specified message sequence number.
     * Returns the raw response data including any literal content.</p>
     *
     * <p>对指定的邮件序列号发出 IMAP FETCH 命令。
     * 返回原始响应数据，包含字面量内容。</p>
     *
     * @param msgSeq     the message sequence number (1-based) | 邮件序列号（从 1 开始）
     * @param fetchItems the fetch data items, e.g., "(ENVELOPE BODY[] FLAGS)"
     *                   | 获取的数据项，如 "(ENVELOPE BODY[] FLAGS)"
     * @return the raw FETCH response data | 原始 FETCH 响应数据
     * @throws ProtocolException if the FETCH fails | FETCH 失败时抛出
     */
    public String fetch(int msgSeq, String fetchItems) throws ProtocolException {
        if (msgSeq < 1) {
            throw new IllegalArgumentException("Message sequence number must be >= 1: " + msgSeq);
        }
        Objects.requireNonNull(fetchItems, "Fetch items must not be null");
        requireSelectedFolder();

        CommandResult result = sendCommand("FETCH " + msgSeq + " " + fetchItems);
        assertOk(result, "FETCH");

        return String.join("\r\n", result.untaggedResponses);
    }

    /**
     * Fetch data for a range of messages
     * 获取一系列邮件的数据
     *
     * <p>Issues a single IMAP FETCH command for a range of message sequence numbers.
     * Returns a map from sequence number to the raw response data for that message.</p>
     *
     * <p>对一系列邮件序列号发出单个 IMAP FETCH 命令。
     * 返回序列号到该邮件原始响应数据的映射。</p>
     *
     * @param from       the starting sequence number (inclusive) | 起始序列号（包含）
     * @param to         the ending sequence number (inclusive) | 结束序列号（包含）
     * @param fetchItems the fetch data items | 获取的数据项
     * @return a map of sequence number to raw response data | 序列号到原始响应数据的映射
     * @throws ProtocolException if the FETCH fails | FETCH 失败时抛出
     */
    public Map<Integer, String> fetchRange(int from, int to, String fetchItems)
            throws ProtocolException {
        if (from < 1) {
            throw new IllegalArgumentException("From sequence must be >= 1: " + from);
        }
        if (to < from) {
            throw new IllegalArgumentException("To sequence must be >= from: " + to + " < " + from);
        }
        Objects.requireNonNull(fetchItems, "Fetch items must not be null");
        requireSelectedFolder();

        CommandResult result = sendCommand("FETCH " + from + ":" + to + " " + fetchItems);
        assertOk(result, "FETCH range");

        // Parse untagged FETCH responses into per-message groups
        Map<Integer, String> messages = new LinkedHashMap<>();

        StringBuilder current = null;
        int currentSeq = -1;

        for (String line : result.untaggedResponses) {
            Matcher m = FETCH_RESPONSE_PATTERN.matcher(line);
            if (m.matches()) {
                // Flush previous message
                if (current != null && currentSeq > 0) {
                    messages.put(currentSeq, current.toString());
                }
                currentSeq = Integer.parseInt(m.group(1));
                current = new StringBuilder(line);
            } else if (current != null) {
                current.append("\r\n").append(line);
            }
        }
        // Flush last message
        if (current != null && currentSeq > 0) {
            messages.put(currentSeq, current.toString());
        }

        return messages;
    }

    /**
     * Store flags on a message
     * 设置邮件的标志
     *
     * <p>Issues the IMAP STORE command to modify flags on the specified message.</p>
     *
     * <p>发出 IMAP STORE 命令以修改指定邮件的标志。</p>
     *
     * @param msgSeq the message sequence number (1-based) | 邮件序列号（从 1 开始）
     * @param action the flag action: "+FLAGS", "-FLAGS", or "FLAGS"
     *               | 标志操作："+FLAGS"、"-FLAGS" 或 "FLAGS"
     * @param flags  the flags, e.g., "(\\Seen)" or "(\\Deleted)"
     *               | 标志，如 "(\\Seen)" 或 "(\\Deleted)"
     * @throws ProtocolException if the STORE fails | STORE 失败时抛出
     */
    public void store(int msgSeq, String action, String flags) throws ProtocolException {
        if (msgSeq < 1) {
            throw new IllegalArgumentException("Message sequence number must be >= 1: " + msgSeq);
        }
        Objects.requireNonNull(action, "Action must not be null");
        Objects.requireNonNull(flags, "Flags must not be null");
        requireSelectedFolder();

        CommandResult result = sendCommand("STORE " + msgSeq + " " + action + " " + flags);
        assertOk(result, "STORE");
    }

    /**
     * Copy a message to another folder
     * 复制邮件到另一个文件夹
     *
     * <p>Issues the IMAP COPY command to copy the specified message to the
     * target folder.</p>
     *
     * <p>发出 IMAP COPY 命令将指定邮件复制到目标文件夹。</p>
     *
     * @param msgSeq       the message sequence number (1-based) | 邮件序列号（从 1 开始）
     * @param targetFolder the destination folder name | 目标文件夹名称
     * @throws ProtocolException if the COPY fails | COPY 失败时抛出
     */
    public void copy(int msgSeq, String targetFolder) throws ProtocolException {
        if (msgSeq < 1) {
            throw new IllegalArgumentException("Message sequence number must be >= 1: " + msgSeq);
        }
        Objects.requireNonNull(targetFolder, "Target folder must not be null");
        requireSelectedFolder();

        CommandResult result = sendCommand(
                "COPY " + msgSeq + " " + quoteString(targetFolder));
        assertOk(result, "COPY");
    }

    /**
     * Permanently remove messages marked with \Deleted flag
     * 永久删除标记为 \Deleted 的邮件
     *
     * <p>Issues the IMAP EXPUNGE command to permanently remove all messages
     * in the selected folder that have the \Deleted flag set.</p>
     *
     * <p>发出 IMAP EXPUNGE 命令以永久删除所选文件夹中
     * 设置了 \Deleted 标志的所有邮件。</p>
     *
     * @throws ProtocolException if the EXPUNGE fails | EXPUNGE 失败时抛出
     */
    public void expunge() throws ProtocolException {
        requireSelectedFolder();
        CommandResult result = sendCommand("EXPUNGE");
        assertOk(result, "EXPUNGE");
    }

    /**
     * List folders matching a pattern
     * 列出匹配模式的文件夹
     *
     * <p>Issues the IMAP LIST command to list folders matching the given
     * reference and pattern. Returns each folder as a {@code [flags, delimiter, name]}
     * array.</p>
     *
     * <p>发出 IMAP LIST 命令列出匹配给定引用和模式的文件夹。
     * 每个文件夹以 {@code [flags, delimiter, name]} 数组形式返回。</p>
     *
     * @param reference the reference name (e.g., "" for root) | 引用名（如 "" 表示根目录）
     * @param pattern   the folder pattern (e.g., "*" for all) | 文件夹模式（如 "*" 表示全部）
     * @return a list of {@code [flags, delimiter, name]} arrays | {@code [flags, delimiter, name]} 数组列表
     * @throws ProtocolException if the LIST fails | LIST 失败时抛出
     */
    public List<String[]> list(String reference, String pattern) throws ProtocolException {
        Objects.requireNonNull(reference, "Reference must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");

        CommandResult result = sendCommand(
                "LIST " + quoteString(reference) + " " + quoteString(pattern));
        assertOk(result, "LIST");

        List<String[]> folders = new ArrayList<>();
        for (String line : result.untaggedResponses) {
            Matcher m = LIST_PATTERN.matcher(line);
            if (m.matches()) {
                String flags = m.group(1);
                String delimiter = m.group(2);
                String name = unquoteString(m.group(3));
                folders.add(new String[]{flags, delimiter, name});
            }
        }
        return folders;
    }

    /**
     * Enter IDLE mode and wait for server notifications
     * 进入 IDLE 模式等待服务器通知
     *
     * <p>Issues the IMAP IDLE command to enter push notification mode.
     * Blocks until the server sends untagged responses (e.g., new mail
     * notifications) or the specified timeout is reached. Automatically
     * sends DONE to exit IDLE mode before returning.</p>
     *
     * <p>发出 IMAP IDLE 命令进入推送通知模式。
     * 阻塞直到服务器发送未标记响应（如新邮件通知）或达到指定超时时间。
     * 返回前自动发送 DONE 以退出 IDLE 模式。</p>
     *
     * @param timeout the maximum time to remain in IDLE mode | IDLE 模式的最大等待时间
     * @return the untagged responses received during IDLE | IDLE 期间接收到的未标记响应
     * @throws ProtocolException if IDLE is not supported or fails | 不支持 IDLE 或失败时抛出
     */
    public List<String> idle(Duration timeout) throws ProtocolException {
        Objects.requireNonNull(timeout, "Timeout must not be null");
        requireSelectedFolder();

        if (!hasCapability("IDLE")) {
            throw new ProtocolException("Server does not support IDLE capability");
        }

        String tag = nextTag();
        connection.writeLine(tag + " IDLE");
        LOG.log(System.Logger.Level.DEBUG, "C: {0} IDLE", tag);

        // Expect continuation response
        String continuation = connection.readLine();
        LOG.log(System.Logger.Level.DEBUG, "S: {0}", continuation);
        if (!continuation.startsWith("+")) {
            throw new ProtocolException("IDLE: expected continuation, got: " + continuation);
        }

        // Collect untagged responses until timeout
        List<String> responses = new ArrayList<>();
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        try {
            while (true) {
                long remainingNanos = deadlineNanos - System.nanoTime();
                if (remainingNanos <= 0) {
                    break;
                }
                try {
                    String line = connection.readLine();
                    LOG.log(System.Logger.Level.DEBUG, "S (IDLE): {0}", line);

                    if (line.startsWith(tag + " ")) {
                        // Server sent tagged response before DONE - unexpected but handle it
                        return responses;
                    }
                    responses.add(line);

                    // If we got an interesting notification, break to return it promptly
                    if (line.matches("\\* \\d+ (EXISTS|RECENT|EXPUNGE)")) {
                        break;
                    }
                } catch (ProtocolException e) {
                    if (e.isTimeout()) {
                        break;
                    }
                    throw e;
                }
            }
        } finally {
            // Send DONE to exit IDLE mode
            try {
                connection.writeLine("DONE");
                LOG.log(System.Logger.Level.DEBUG, "C: DONE");

                // Read tagged response
                String taggedResponse = readTaggedResponse(tag);
                LOG.log(System.Logger.Level.DEBUG, "S: {0}", taggedResponse);
            } catch (ProtocolException e) {
                LOG.log(System.Logger.Level.WARNING,
                        "Failed to cleanly exit IDLE mode: {0}", e.getMessage());
            }
        }

        return responses;
    }

    /**
     * Send NOOP command to keep the connection alive
     * 发送 NOOP 命令以保持连接活跃
     *
     * <p>Issues the IMAP NOOP command which has no effect on the server state
     * but resets idle timers and may trigger pending untagged notifications.</p>
     *
     * <p>发出 IMAP NOOP 命令，该命令不影响服务器状态，
     * 但会重置空闲计时器并可能触发待处理的未标记通知。</p>
     *
     * @throws ProtocolException if the NOOP fails | NOOP 失败时抛出
     */
    public void noop() throws ProtocolException {
        CommandResult result = sendCommand("NOOP");
        assertOk(result, "NOOP");
    }

    /**
     * Get the total number of messages in the selected folder
     * 获取所选文件夹中的邮件总数
     *
     * <p>Issues a STATUS command on the currently selected folder to retrieve
     * the MESSAGES count.</p>
     *
     * <p>对当前选中的文件夹发出 STATUS 命令以获取 MESSAGES 计数。</p>
     *
     * @return the message count | 邮件数量
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public int getMessageCount() throws ProtocolException {
        requireSelectedFolder();

        CommandResult result = sendCommand(
                "STATUS " + quoteString(selectedFolder) + " (MESSAGES)");
        assertOk(result, "STATUS MESSAGES");

        for (String line : result.untaggedResponses) {
            Matcher m = MESSAGES_PATTERN.matcher(line);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }
        throw new ProtocolException("STATUS response did not contain MESSAGES count");
    }

    /**
     * Get the count of unread (unseen) messages in the selected folder
     * 获取所选文件夹中未读（未查看）邮件的数量
     *
     * <p>Issues a SEARCH UNSEEN command and returns the count of matching
     * message sequence numbers.</p>
     *
     * <p>发出 SEARCH UNSEEN 命令并返回匹配的邮件序列号数量。</p>
     *
     * @return the unread message count | 未读邮件数量
     * @throws ProtocolException if the SEARCH fails | SEARCH 失败时抛出
     */
    public int getUnreadCount() throws ProtocolException {
        return search("UNSEEN").size();
    }

    /**
     * Logout from the server and close the connection
     * 从服务器注销并关闭连接
     *
     * <p>Issues the IMAP LOGOUT command, reads the BYE response, and closes
     * the underlying socket connection.</p>
     *
     * <p>发出 IMAP LOGOUT 命令，读取 BYE 响应，并关闭底层套接字连接。</p>
     *
     * @throws ProtocolException if the LOGOUT fails | LOGOUT 失败时抛出
     */
    public void logout() throws ProtocolException {
        try {
            CommandResult result = sendCommand("LOGOUT");
            // LOGOUT may return BYE untagged and then OK tagged
            // We do not assert OK strictly since some servers send BYE + OK,
            // but the response format varies
            LOG.log(System.Logger.Level.DEBUG, "LOGOUT completed");
        } finally {
            if (connection != null) {
                connection.close();
            }
            selectedFolder = null;
        }
    }

    /**
     * Close the IMAP client, logging out if connected
     * 关闭 IMAP 客户端，如已连接则先注销
     *
     * <p>Attempts a graceful LOGOUT before closing the connection. Any errors
     * during logout are silently ignored.</p>
     *
     * <p>在关闭连接前尝试正常注销。注销过程中的任何错误将被静默忽略。</p>
     */
    @Override
    public void close() {
        if (connection != null && connection.isConnected()) {
            try {
                logout();
            } catch (ProtocolException e) {
                LOG.log(System.Logger.Level.DEBUG,
                        "Error during close/logout: {0}", e.getMessage());
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
        selectedFolder = null;
    }

    /**
     * Check if the server advertises the given capability
     * 检查服务器是否公布了给定的能力
     *
     * @param cap the capability name (case-insensitive) | 能力名称（不区分大小写）
     * @return true if the capability is present | 如果存在该能力返回 true
     */
    public boolean hasCapability(String cap) {
        if (cap == null) {
            return false;
        }
        return capabilities.contains(cap.toUpperCase(Locale.ROOT));
    }

    /**
     * Get all server capabilities
     * 获取所有服务器能力
     *
     * @return an unmodifiable set of capability names (uppercase)
     *         | 能力名称的不可修改集合（大写）
     */
    public Set<String> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }

    /**
     * Check if the client is connected to the server
     * 检查客户端是否已连接到服务器
     *
     * @return true if connected | 已连接返回 true
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * Get the currently selected folder name
     * 获取当前选中的文件夹名称
     *
     * @return the selected folder name, or null if none is selected
     *         | 选中的文件夹名称，未选中则返回 null
     */
    public String getSelectedFolder() {
        return selectedFolder;
    }

    // ---- Internal Protocol Mechanics ----

    /**
     * Result of a tagged IMAP command, containing the tagged response line
     * and all untagged response lines collected before it.
     */
    private record CommandResult(String taggedResponse, List<String> untaggedResponses) {}

    /**
     * Generate the next IMAP command tag (a1, a2, a3, ...).
     */
    private String nextTag() {
        tagCounter++;
        return "a" + tagCounter;
    }

    /**
     * Send a tagged command and collect the response.
     *
     * <p>Generates the next tag, sends {@code tag command}, reads all untagged
     * responses (lines starting with {@code *} or {@code +}), and returns when
     * the tagged response is received. Handles IMAP literal continuations
     * ({N} syntax) in response lines.</p>
     */
    private CommandResult sendCommand(String command) throws ProtocolException {
        String tag = nextTag();
        String fullCommand = tag + " " + command;

        // Log command, but redact passwords
        if (command.startsWith("LOGIN ")) {
            LOG.log(System.Logger.Level.DEBUG, "C: {0} LOGIN *** ***", tag);
        } else if (command.startsWith("AUTHENTICATE ")) {
            LOG.log(System.Logger.Level.DEBUG, "C: {0} AUTHENTICATE ***", tag);
        } else {
            LOG.log(System.Logger.Level.DEBUG, "C: {0}", fullCommand);
        }

        connection.writeLine(fullCommand);

        List<String> untagged = new ArrayList<>();
        while (true) {
            if (untagged.size() >= MAX_UNTAGGED_RESPONSES) {
                throw new ProtocolException(
                        "Too many untagged responses (>" + MAX_UNTAGGED_RESPONSES + ")");
            }

            String line = connection.readLine();
            LOG.log(System.Logger.Level.TRACE, "S: {0}", line);

            // Check for literal continuation in fetch responses
            line = readLiteralContinuation(line);

            if (line.startsWith(tag + " ")) {
                // Tagged response - command complete
                return new CommandResult(line, untagged);
            } else if (line.startsWith("+ ") || line.equals("+")) {
                // Continuation request (e.g., during AUTHENTICATE)
                // For XOAUTH2, we already sent all data, just continue reading
                untagged.add(line);
            } else {
                // Untagged response (* ...)
                untagged.add(line);
            }
        }
    }

    /**
     * If the given line ends with an IMAP literal marker {@code {N}},
     * read exactly N bytes of literal data and any subsequent lines that
     * are part of the same response, appending them to the result.
     */
    private String readLiteralContinuation(String line) throws ProtocolException {
        Matcher m = LITERAL_PATTERN.matcher(line);
        if (!m.find()) {
            return line;
        }

        StringBuilder sb = new StringBuilder(line);
        while (true) {
            Matcher lm = LITERAL_PATTERN.matcher(sb);
            // Find the last occurrence of {N} in the accumulated content
            int literalSize = -1;
            int literalEnd = -1;
            while (lm.find()) {
                literalSize = Integer.parseInt(lm.group(1));
                literalEnd = lm.end();
            }
            if (literalSize < 0 || literalEnd != sb.length()) {
                break;
            }

            if (literalSize > MAX_LITERAL_SIZE) {
                throw new ProtocolException(
                        "IMAP literal too large: " + literalSize + " bytes (max "
                                + MAX_LITERAL_SIZE + ")");
            }

            // Read literal data from BufferedReader (not raw InputStream) to avoid
            // skipping data already buffered by the reader
            String literalData = connection.readExact(literalSize);
            sb.append("\r\n");
            sb.append(literalData);

            // Read the continuation line after the literal
            String nextLine = connection.readLine();
            sb.append("\r\n").append(nextLine);
        }

        return sb.toString();
    }

    /**
     * Read lines until we find the tagged response for the given tag.
     * Discards untagged lines.
     */
    private String readTaggedResponse(String tag) throws ProtocolException {
        int limit = MAX_UNTAGGED_RESPONSES;
        while (limit-- > 0) {
            String line = connection.readLine();
            if (line.startsWith(tag + " ")) {
                return line;
            }
        }
        throw new ProtocolException("Failed to read tagged response for " + tag);
    }

    /**
     * Assert that a tagged response indicates success (OK).
     * Throws ProtocolException if the response is NO or BAD.
     */
    private void assertOk(CommandResult result, String commandName) throws ProtocolException {
        String tagged = result.taggedResponse;
        // Tagged response format: aN OK/NO/BAD [text]
        // Find the status after the tag
        int spaceIdx = tagged.indexOf(' ');
        if (spaceIdx < 0) {
            throw new ProtocolException(commandName + ": malformed response: " + tagged);
        }
        String afterTag = tagged.substring(spaceIdx + 1);

        if (afterTag.startsWith("OK")) {
            return;
        }
        if (afterTag.startsWith("NO")) {
            throw new ProtocolException(commandName + " failed: " + afterTag);
        }
        if (afterTag.startsWith("BAD")) {
            throw new ProtocolException(commandName + " protocol error: " + afterTag);
        }
        throw new ProtocolException(commandName + ": unexpected response: " + tagged);
    }

    /**
     * Open a folder with SELECT or EXAMINE, parsing EXISTS and RECENT counts.
     */
    private int[] openFolder(String command, String folder) throws ProtocolException {
        CommandResult result = sendCommand(command + " " + quoteString(folder));
        assertOk(result, command);

        int exists = 0;
        int recent = 0;
        for (String line : result.untaggedResponses) {
            Matcher em = EXISTS_PATTERN.matcher(line);
            if (em.matches()) {
                exists = Integer.parseInt(em.group(1));
                continue;
            }
            Matcher rm = RECENT_PATTERN.matcher(line);
            if (rm.matches()) {
                recent = Integer.parseInt(rm.group(1));
            }
        }

        this.selectedFolder = folder;
        LOG.log(System.Logger.Level.DEBUG, "{0} {1}: exists={2}, recent={3}",
                command, folder, exists, recent);
        return new int[]{exists, recent};
    }

    /**
     * Refresh capabilities by sending a CAPABILITY command.
     */
    private void refreshCapabilities() throws ProtocolException {
        CommandResult result = sendCommand("CAPABILITY");
        assertOk(result, "CAPABILITY");

        for (String line : result.untaggedResponses) {
            if (line.startsWith("* CAPABILITY ")) {
                capabilities.clear();
                String caps = line.substring("* CAPABILITY ".length());
                for (String cap : caps.split("\\s+")) {
                    capabilities.add(cap.toUpperCase(Locale.ROOT));
                }
            }
        }
    }

    /**
     * Parse CAPABILITY from a response line that may contain [CAPABILITY ...].
     */
    private void parseCapabilities(String line) {
        if (line == null) {
            return;
        }
        Matcher m = CAPABILITY_PATTERN.matcher(line);
        if (m.find()) {
            capabilities.clear();
            for (String cap : m.group(1).split("\\s+")) {
                capabilities.add(cap.toUpperCase(Locale.ROOT));
            }
        }
    }

    /**
     * Require that a folder is currently selected.
     */
    private void requireSelectedFolder() throws ProtocolException {
        if (selectedFolder == null) {
            throw new ProtocolException("No folder selected. Call select() or examine() first.");
        }
    }

    /**
     * Quote an IMAP string value, escaping backslashes and double quotes.
     * Returns the value wrapped in double quotes.
     */
    private static String quoteString(String value) {
        // Reject CRLF and NUL to prevent command injection
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || c == '\0') {
                throw new IllegalArgumentException(
                        "Invalid character in IMAP string: 0x" + Integer.toHexString(c));
            }
        }
        // Escape backslashes and quotes, then wrap in double quotes
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Remove surrounding double quotes from an IMAP string value, if present.
     */
    private static String unquoteString(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            String inner = value.substring(1, value.length() - 1);
            return inner.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }
}
