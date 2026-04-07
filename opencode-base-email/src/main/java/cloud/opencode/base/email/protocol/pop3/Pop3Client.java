package cloud.opencode.base.email.protocol.pop3;

import cloud.opencode.base.email.protocol.MailConnection;
import cloud.opencode.base.email.protocol.ProtocolException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * POP3 Protocol Client (RFC 1939)
 * POP3协议客户端（RFC 1939）
 *
 * <p>Implements the Post Office Protocol version 3 for retrieving email messages
 * from a remote server. Supports plain USER/PASS authentication, XOAUTH2,
 * and STARTTLS (STLS) connection upgrade.</p>
 * <p>实现邮局协议第3版，用于从远程服务器检索邮件消息。
 * 支持USER/PASS明文认证、XOAUTH2认证以及STARTTLS（STLS）连接升级。</p>
 *
 * <p>This class is <strong>NOT</strong> thread-safe. Each thread should use its own
 * {@code Pop3Client} instance.</p>
 * <p>此类<strong>非</strong>线程安全。每个线程应使用独立的{@code Pop3Client}实例。</p>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc1939">RFC 1939 - POP3</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class Pop3Client implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(Pop3Client.class.getName());

    private static final String OK_PREFIX = "+OK";
    private static final String ERR_PREFIX = "-ERR";
    private static final String TERMINATOR = ".";
    private static final int MAX_MULTILINE_BYTES = 64 * 1024 * 1024; // 64 MB max

    private final MailConnection connection;
    private final boolean starttls;

    /**
     * Create a new POP3 client
     * 创建新的POP3客户端
     *
     * @param host              the server hostname | 服务器主机名
     * @param port              the server port | 服务器端口
     * @param ssl               true for implicit SSL connection | true使用隐式SSL连接
     * @param starttls          true to upgrade via STLS after connect | true在连接后通过STLS升级
     * @param connectionTimeout connection timeout | 连接超时
     * @param readTimeout       read timeout | 读取超时
     * @throws NullPointerException if host, connectionTimeout or readTimeout is null
     *                              | host、connectionTimeout或readTimeout为null时抛出
     */
    public Pop3Client(String host, int port, boolean ssl, boolean starttls,
                      Duration connectionTimeout, Duration readTimeout) {
        Objects.requireNonNull(host, "host must not be null");
        Objects.requireNonNull(connectionTimeout, "connectionTimeout must not be null");
        Objects.requireNonNull(readTimeout, "readTimeout must not be null");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535: " + port);
        }
        this.connection = new MailConnection(host, port, ssl, connectionTimeout, readTimeout);
        this.starttls = starttls;
    }

    /**
     * Connect to the POP3 server and read the greeting
     * 连接到POP3服务器并读取问候语
     *
     * <p>If STARTTLS is configured, sends the STLS command and upgrades the
     * connection to TLS before returning.</p>
     * <p>如果配置了STARTTLS，会发送STLS命令并将连接升级到TLS后返回。</p>
     *
     * @return the server greeting text | 服务器问候语文本
     * @throws ProtocolException if connection or greeting fails | 连接或问候失败时抛出
     */
    public String connect() throws ProtocolException {
        connection.connect();
        String greeting = readResponse();
        LOG.log(System.Logger.Level.DEBUG, "POP3 greeting: {0}", greeting);

        if (starttls) {
            LOG.log(System.Logger.Level.DEBUG, "Upgrading to TLS via STLS");
            sendCommand("STLS");
            readResponse();
            connection.upgradeToTls();
        }

        return greeting;
    }

    /**
     * Authenticate using USER/PASS commands
     * 使用USER/PASS命令进行认证
     *
     * @param username the username | 用户名
     * @param password the password | 密码
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     * @throws NullPointerException if username or password is null
     *                              | username或password为null时抛出
     */
    public void login(String username, String password) throws ProtocolException {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        validateCredential(username, "username");
        validateCredential(password, "password");
        LOG.log(System.Logger.Level.DEBUG, "POP3 login: USER {0}", username);
        try {
            sendCommand("USER " + username);
            readResponse();
            sendCommand("PASS " + password);
            readResponse();
        } catch (ProtocolException e) {
            throw new ProtocolException(
                    "POP3 authentication failed for user '" + username
                            + "': " + e.getMessage()
                            + " (check username/password or enable less-secure apps)", e);
        }
    }

    /**
     * Authenticate using AUTH XOAUTH2
     * 使用AUTH XOAUTH2进行认证
     *
     * <p>Builds the XOAUTH2 authentication string per the Google XOAUTH2 spec,
     * Base64-encodes it, and sends it with the AUTH command.</p>
     * <p>按照Google XOAUTH2规范构建认证字符串，进行Base64编码后通过AUTH命令发送。</p>
     *
     * @param username the email address | 邮箱地址
     * @param token    the OAuth2 access token | OAuth2访问令牌
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     * @throws NullPointerException if username or token is null
     *                              | username或token为null时抛出
     */
    public void authXOAuth2(String username, String token) throws ProtocolException {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(token, "token must not be null");
        validateCredential(username, "username");
        validateCredential(token, "token");
        LOG.log(System.Logger.Level.DEBUG, "POP3 AUTH XOAUTH2 for {0}", username);

        String xoauth2String = "user=" + username + "\u0001auth=Bearer " + token + "\u0001\u0001";
        String encoded = Base64.getEncoder().encodeToString(xoauth2String.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        sendCommand("AUTH XOAUTH2 " + encoded);
        String response = connection.readLine();

        if (response.startsWith(OK_PREFIX)) {
            return;
        }

        if (response.startsWith("+") && !response.startsWith(OK_PREFIX)) {
            // Server sent a continuation challenge; send empty line to cancel
            connection.writeLine("");
            String errResponse = connection.readLine();
            throw new ProtocolException(
                    "POP3 XOAUTH2 authentication failed for user '" + username
                            + "': " + errResponse
                            + " (check OAuth2 token validity and scopes)");
        }

        // -ERR response
        throw new ProtocolException(
                "POP3 XOAUTH2 authentication failed for user '" + username
                        + "': " + response
                        + " (check OAuth2 token validity and scopes)");
    }

    /**
     * STAT - get message count and total mailbox size in octets
     * STAT - 获取邮件数量和邮箱总大小（字节）
     *
     * @return an array of {@code [count, totalSize]} | 数组 {@code [数量, 总大小]}
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public int[] stat() throws ProtocolException {
        sendCommand("STAT");
        String response = readResponse();
        // Response format: "count size" (after +OK prefix stripped)
        String[] parts = splitResponse(response.trim(), 2);
        if (parts[0] == null || parts[1] == null) {
            throw new ProtocolException("Invalid STAT response: +OK " + response);
        }
        try {
            int count = Integer.parseInt(parts[0]);
            int totalSize = Integer.parseInt(parts[1]);
            return new int[]{count, totalSize};
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid STAT response: +OK " + response, e);
        }
    }

    /**
     * LIST - get the size of all messages
     * LIST - 获取所有邮件的大小
     *
     * @return list of {@code [messageNumber, sizeInOctets]} arrays
     *         | {@code [邮件编号, 大小（字节）]} 数组列表
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public List<int[]> list() throws ProtocolException {
        sendCommand("LIST");
        readResponse();
        List<String> lines = readMultiLineData();
        List<int[]> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            String[] parts = splitResponse(line.trim(), 2);
            if (parts[0] == null || parts[1] == null) {
                continue;
            }
            try {
                int msgNum = Integer.parseInt(parts[0]);
                int size = Integer.parseInt(parts[1]);
                result.add(new int[]{msgNum, size});
            } catch (NumberFormatException e) {
                LOG.log(System.Logger.Level.WARNING, "Skipping invalid LIST line: {0}", line);
            }
        }
        return result;
    }

    /**
     * LIST n - get the size of a specific message
     * LIST n - 获取指定邮件的大小
     *
     * @param msgNum the message number (1-based) | 邮件编号（从1开始）
     * @return the message size in octets | 邮件大小（字节）
     * @throws ProtocolException      if the command fails | 命令失败时抛出
     * @throws IllegalArgumentException if msgNum is less than 1 | msgNum小于1时抛出
     */
    public int list(int msgNum) throws ProtocolException {
        requirePositive(msgNum, "msgNum");
        sendCommand("LIST " + msgNum);
        String response = readResponse();
        // Response format: "msgNum size"
        String[] parts = splitResponse(response.trim(), 2);
        if (parts[0] == null || parts[1] == null) {
            throw new ProtocolException("Invalid LIST response: +OK " + response);
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid LIST response: +OK " + response, e);
        }
    }

    /**
     * UIDL - get unique IDs for all messages
     * UIDL - 获取所有邮件的唯一标识
     *
     * @return list of {@code [messageNumber, uniqueId]} arrays
     *         | {@code [邮件编号, 唯一标识]} 数组列表
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public List<String[]> uidl() throws ProtocolException {
        sendCommand("UIDL");
        readResponse();
        List<String> lines = readMultiLineData();
        List<String[]> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            String[] parts = splitResponse(line.trim(), 2);
            if (parts[0] == null || parts[1] == null) {
                continue;
            }
            result.add(new String[]{parts[0], parts[1]});
        }
        return result;
    }

    /**
     * UIDL n - get the unique ID of a specific message
     * UIDL n - 获取指定邮件的唯一标识
     *
     * @param msgNum the message number (1-based) | 邮件编号（从1开始）
     * @return the unique ID | 唯一标识
     * @throws ProtocolException      if the command fails | 命令失败时抛出
     * @throws IllegalArgumentException if msgNum is less than 1 | msgNum小于1时抛出
     */
    public String uidl(int msgNum) throws ProtocolException {
        requirePositive(msgNum, "msgNum");
        sendCommand("UIDL " + msgNum);
        String response = readResponse();
        // Response format: "msgNum uniqueId"
        String[] parts = splitResponse(response.trim(), 2);
        if (parts[0] == null || parts[1] == null) {
            throw new ProtocolException("Invalid UIDL response: +OK " + response);
        }
        return parts[1];
    }

    /**
     * RETR - retrieve a complete message
     * RETR - 检索完整邮件
     *
     * <p>Returns the raw RFC 2822 message content with byte-stuffing removed.</p>
     * <p>返回去除字节填充后的原始RFC 2822邮件内容。</p>
     *
     * @param msgNum the message number (1-based) | 邮件编号（从1开始）
     * @return the raw message content | 原始邮件内容
     * @throws ProtocolException      if the command fails | 命令失败时抛出
     * @throws IllegalArgumentException if msgNum is less than 1 | msgNum小于1时抛出
     */
    public String retr(int msgNum) throws ProtocolException {
        requirePositive(msgNum, "msgNum");
        sendCommand("RETR " + msgNum);
        readResponse();
        return readMultiLineContent();
    }

    /**
     * TOP - retrieve message headers and the first n lines of the body
     * TOP - 检索邮件头和正文的前n行
     *
     * <p>This is an optional POP3 extension command. Not all servers support it.</p>
     * <p>这是一个可选的POP3扩展命令，并非所有服务器都支持。</p>
     *
     * @param msgNum the message number (1-based) | 邮件编号（从1开始）
     * @param lines  the number of body lines to retrieve | 要检索的正文行数
     * @return the message headers and partial body | 邮件头和部分正文
     * @throws ProtocolException      if the command fails | 命令失败时抛出
     * @throws IllegalArgumentException if msgNum is less than 1 or lines is negative
     *                                  | msgNum小于1或lines为负数时抛出
     */
    public String top(int msgNum, int lines) throws ProtocolException {
        requirePositive(msgNum, "msgNum");
        if (lines < 0) {
            throw new IllegalArgumentException("lines must not be negative: " + lines);
        }
        sendCommand("TOP " + msgNum + " " + lines);
        readResponse();
        return readMultiLineContent();
    }

    /**
     * DELE - mark a message for deletion
     * DELE - 标记邮件待删除
     *
     * <p>The message is not actually removed until a successful QUIT.</p>
     * <p>邮件在成功执行QUIT之前不会被实际删除。</p>
     *
     * @param msgNum the message number (1-based) | 邮件编号（从1开始）
     * @throws ProtocolException      if the command fails | 命令失败时抛出
     * @throws IllegalArgumentException if msgNum is less than 1 | msgNum小于1时抛出
     */
    public void dele(int msgNum) throws ProtocolException {
        requirePositive(msgNum, "msgNum");
        sendCommand("DELE " + msgNum);
        readResponse();
    }

    /**
     * RSET - unmark all messages marked for deletion
     * RSET - 取消所有标记为删除的邮件
     *
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public void rset() throws ProtocolException {
        sendCommand("RSET");
        readResponse();
    }

    /**
     * NOOP - keep the connection alive
     * NOOP - 保持连接活跃
     *
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public void noop() throws ProtocolException {
        sendCommand("NOOP");
        readResponse();
    }

    /**
     * QUIT - commit deletions and close the connection
     * QUIT - 提交删除操作并关闭连接
     *
     * <p>Messages marked for deletion are permanently removed by the server
     * when this command completes successfully.</p>
     * <p>成功执行此命令后，服务器会永久删除标记为删除的邮件。</p>
     *
     * @throws ProtocolException if the command fails | 命令失败时抛出
     */
    public void quit() throws ProtocolException {
        try {
            sendCommand("QUIT");
            readResponse();
        } finally {
            connection.close();
        }
    }

    /**
     * Close the connection without sending QUIT
     * 不发送QUIT直接关闭连接
     *
     * <p>Messages marked for deletion will NOT be removed by the server.</p>
     * <p>标记为删除的邮件将不会被服务器删除。</p>
     */
    @Override
    public void close() {
        connection.close();
    }

    /**
     * Check if the connection is alive
     * 检查连接是否存活
     *
     * @return true if connected | 已连接返回true
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Send a POP3 command to the server.
     */
    private void sendCommand(String command) throws ProtocolException {
        LOG.log(System.Logger.Level.DEBUG, "C: {0}",
                command.startsWith("PASS ") ? "PASS ****" : command);
        connection.writeLine(command);
    }

    /**
     * Read a single-line response and verify it starts with +OK.
     * Returns the text after "+OK " (or empty string if just "+OK").
     */
    private String readResponse() throws ProtocolException {
        String line = connection.readLine();
        LOG.log(System.Logger.Level.DEBUG, "S: {0}", line);

        if (line.startsWith(ERR_PREFIX)) {
            String errMsg = line.length() > ERR_PREFIX.length()
                    ? line.substring(ERR_PREFIX.length()).trim()
                    : "Unknown error";
            throw new ProtocolException("POP3 server error: " + errMsg);
        }

        if (!line.startsWith(OK_PREFIX)) {
            throw new ProtocolException("Unexpected POP3 response: " + line);
        }

        // Return text after "+OK" (skip the space if present)
        if (line.length() <= OK_PREFIX.length()) {
            return "";
        }
        return line.substring(OK_PREFIX.length() + 1);
    }

    /**
     * Read multi-line data response until the termination line (".").
     * Returns raw lines (with byte-stuffing NOT removed) for structured parsing.
     */
    private List<String> readMultiLineData() throws ProtocolException {
        List<String> lines = new ArrayList<>();
        int totalBytes = 0;
        String line;
        while (true) {
            line = connection.readLine();
            if (TERMINATOR.equals(line)) {
                break;
            }
            totalBytes += line.length() + 2; // +2 for CRLF
            if (totalBytes > MAX_MULTILINE_BYTES) {
                throw new ProtocolException("Server response too large: exceeded " + MAX_MULTILINE_BYTES + " bytes");
            }
            // Remove byte-stuffing: leading "." on non-terminator lines
            if (line.startsWith(".")) {
                line = line.substring(1);
            }
            lines.add(line);
        }
        return lines;
    }

    /**
     * Read multi-line content response (e.g., RETR, TOP) and join into a single string
     * with CRLF line endings. Handles byte-stuffing removal.
     */
    private String readMultiLineContent() throws ProtocolException {
        List<String> lines = readMultiLineData();
        StringBuilder sb = new StringBuilder(lines.size() * 80);
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append("\r\n");
            }
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    /**
     * Validate that a credential value contains no CRLF or null characters.
     * 验证凭据值不包含CRLF或空字符。
     */
    private static void validateCredential(String value, String name) throws ProtocolException {
        if (value == null || value.isEmpty()) {
            throw new ProtocolException(name + " must not be empty");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || c == '\0') {
                throw new ProtocolException("Invalid character in " + name + ": 0x" + Integer.toHexString(c));
            }
        }
    }

    /**
     * Validate that a message number is positive.
     */
    private static void requirePositive(int value, String name) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be >= 1, got: " + value);
        }
    }

    /**
     * Split a protocol response string on whitespace without regex overhead.
     * 使用indexOf方式分割协议响应字符串，避免正则表达式开销。
     *
     * @param response the response string to split | 要分割的响应字符串
     * @param maxParts the maximum number of parts (last part gets the remainder)
     *                 | 最大分割数（最后一部分获取剩余内容）
     * @return array of parts; entries may be null if fewer parts were found
     *         | 分割后的数组；如果找到的部分较少，条目可能为null
     */
    private static String[] splitResponse(String response, int maxParts) {
        String[] result = new String[maxParts];
        int idx = 0;
        int start = 0;
        int len = response.length();
        while (idx < maxParts - 1 && start < len) {
            // Skip whitespace
            while (start < len && response.charAt(start) <= ' ') start++;
            if (start >= len) break;
            int end = start;
            while (end < len && response.charAt(end) > ' ') end++;
            result[idx++] = response.substring(start, end);
            start = end;
        }
        // Last part gets the rest
        if (start < len && idx < maxParts) {
            while (start < len && response.charAt(start) <= ' ') start++;
            if (start < len) result[idx] = response.substring(start);
        }
        return result;
    }
}
