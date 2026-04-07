package cloud.opencode.base.email.protocol.smtp;

import cloud.opencode.base.email.protocol.MailConnection;
import cloud.opencode.base.email.protocol.ProtocolException;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * SMTP Protocol Client (RFC 5321)
 * SMTP协议客户端（RFC 5321）
 *
 * <p>Implements the Simple Mail Transfer Protocol for sending emails.
 * Supports PLAIN, LOGIN, and XOAUTH2 authentication mechanisms,
 * STARTTLS upgrade, and transparent dot-stuffing for message data.</p>
 * <p>实现简单邮件传输协议用于发送电子邮件。
 * 支持PLAIN、LOGIN和XOAUTH2认证机制、STARTTLS升级以及消息数据的透明点填充。</p>
 *
 * <p>This class is <strong>not</strong> thread-safe. Each instance manages
 * a single SMTP connection and should be used by one thread at a time.</p>
 * <p>此类<strong>非</strong>线程安全。每个实例管理单个SMTP连接，应在同一时间仅由一个线程使用。</p>
 *
 * @author Leon Soo
 * @see MailConnection
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class SmtpClient implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(SmtpClient.class.getName());

    private final String host;
    private final int port;
    private final boolean ssl;
    private final boolean starttls;
    private final Duration connectionTimeout;
    private final Duration readTimeout;

    private MailConnection connection;
    private final Set<String> capabilities = new LinkedHashSet<>();

    /**
     * Create a new SMTP client
     * 创建新的SMTP客户端
     *
     * @param host              the SMTP server hostname | SMTP服务器主机名
     * @param port              the SMTP server port | SMTP服务器端口
     * @param ssl               true for implicit SSL/TLS connection | true使用隐式SSL/TLS连接
     * @param starttls          true to upgrade via STARTTLS after connecting | true连接后通过STARTTLS升级
     * @param connectionTimeout connection timeout | 连接超时
     * @param readTimeout       read timeout | 读取超时
     */
    public SmtpClient(String host, int port, boolean ssl, boolean starttls,
                      Duration connectionTimeout, Duration readTimeout) {
        this.host = Objects.requireNonNull(host, "host must not be null");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.port = port;
        this.ssl = ssl;
        this.starttls = starttls;
        this.connectionTimeout = Objects.requireNonNull(connectionTimeout, "connectionTimeout must not be null");
        this.readTimeout = Objects.requireNonNull(readTimeout, "readTimeout must not be null");
    }

    /**
     * Connect to the SMTP server and perform the EHLO handshake
     * 连接到SMTP服务器并执行EHLO握手
     *
     * <p>Establishes the connection, reads the server greeting, sends EHLO,
     * and optionally upgrades to TLS via STARTTLS.</p>
     * <p>建立连接，读取服务器问候语，发送EHLO，并可选地通过STARTTLS升级到TLS。</p>
     *
     * @return the server greeting string | 服务器问候字符串
     * @throws ProtocolException if connection or handshake fails | 连接或握手失败时抛出
     */
    public String connect() throws ProtocolException {
        connection = new MailConnection(host, port, ssl, connectionTimeout, readTimeout);
        connection.connect();

        // Read 220 greeting
        SmtpResponse greeting = readResponse();
        checkReply(greeting, 220, "Server greeting");
        LOG.log(System.Logger.Level.DEBUG, "SMTP greeting: {0}", greeting.message());

        // Send EHLO
        String localHost = getLocalHostName();
        sendEhlo(localHost);

        // STARTTLS if requested
        if (starttls && hasCapability("STARTTLS")) {
            LOG.log(System.Logger.Level.DEBUG, "Upgrading to TLS via STARTTLS");
            connection.writeLine("STARTTLS");
            SmtpResponse tlsReply = readResponse();
            checkReply(tlsReply, 220, "STARTTLS");
            connection.upgradeToTls();

            // Re-send EHLO after TLS upgrade
            sendEhlo(localHost);
        }

        return greeting.message();
    }

    /**
     * Authenticate using the PLAIN mechanism (RFC 4616)
     * 使用PLAIN机制认证（RFC 4616）
     *
     * @param username the username | 用户名
     * @param password the password | 密码
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     */
    public void authPlain(String username, String password) throws ProtocolException {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        validateCredential(username, "username");
        validateCredential(password, "password");

        // AUTH PLAIN base64(\0username\0password)
        byte[] authBytes = ("\0" + username + "\0" + password).getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().encodeToString(authBytes);

        LOG.log(System.Logger.Level.DEBUG, "AUTH PLAIN for user: {0}", username);
        connection.writeLine("AUTH PLAIN " + encoded);

        SmtpResponse reply = readResponse();
        checkReply(reply, 235, "AUTH PLAIN");
    }

    /**
     * Authenticate using the LOGIN mechanism
     * 使用LOGIN机制认证
     *
     * @param username the username | 用户名
     * @param password the password | 密码
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     */
    public void authLogin(String username, String password) throws ProtocolException {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        validateCredential(username, "username");
        validateCredential(password, "password");

        LOG.log(System.Logger.Level.DEBUG, "AUTH LOGIN for user: {0}", username);
        connection.writeLine("AUTH LOGIN");

        SmtpResponse reply = readResponse();
        checkReply(reply, 334, "AUTH LOGIN initial");

        // Send base64-encoded username
        connection.writeLine(Base64.getEncoder().encodeToString(
                username.getBytes(StandardCharsets.UTF_8)));
        reply = readResponse();
        checkReply(reply, 334, "AUTH LOGIN username");

        // Send base64-encoded password
        connection.writeLine(Base64.getEncoder().encodeToString(
                password.getBytes(StandardCharsets.UTF_8)));
        reply = readResponse();
        checkReply(reply, 235, "AUTH LOGIN password");
    }

    /**
     * Authenticate using the XOAUTH2 mechanism (for Gmail/Outlook)
     * 使用XOAUTH2机制认证（适用于Gmail/Outlook）
     *
     * @param username the email address | 邮箱地址
     * @param token    the OAuth2 access token | OAuth2访问令牌
     * @throws ProtocolException if authentication fails | 认证失败时抛出
     */
    public void authXOAuth2(String username, String token) throws ProtocolException {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(token, "token must not be null");
        validateCredential(username, "username");
        validateCredential(token, "token");

        // Build XOAUTH2 string: "user=<username>\x01auth=Bearer <token>\x01\x01"
        String xoauth2 = "user=" + username + "\u0001auth=Bearer " + token + "\u0001\u0001";
        String encoded = Base64.getEncoder().encodeToString(
                xoauth2.getBytes(StandardCharsets.UTF_8));

        LOG.log(System.Logger.Level.DEBUG, "AUTH XOAUTH2 for user: {0}", username);
        connection.writeLine("AUTH XOAUTH2 " + encoded);

        SmtpResponse reply = readResponse();
        if (reply.code() == 334) {
            // Server sent error details; send empty line to cancel
            connection.writeLine("");
            readResponse(); // read the final error response (ignored)
            throw new ProtocolException(
                    "XOAUTH2 authentication failed: " + reply.message(), reply.code());
        }
        checkReply(reply, 235, "AUTH XOAUTH2");
    }

    /**
     * Send a complete email message
     * 发送完整的电子邮件
     *
     * <p>Performs the MAIL FROM, RCPT TO, and DATA sequence.
     * The rawMessage must be a complete RFC 2822 formatted message.
     * Lines starting with "." are transparently dot-stuffed per RFC 5321 Section 4.5.2.</p>
     * <p>执行MAIL FROM、RCPT TO和DATA序列。
     * rawMessage必须是完整的RFC 2822格式消息。
     * 以"."开头的行会根据RFC 5321第4.5.2节进行透明点填充。</p>
     *
     * @param sender     the envelope sender address (MAIL FROM) | 信封发件人地址
     * @param recipients all envelope recipient addresses (RCPT TO) | 所有信封收件人地址
     * @param rawMessage the complete RFC 2822 message | 完整的RFC 2822消息
     * @throws ProtocolException if sending fails | 发送失败时抛出
     */
    public void sendMessage(String sender, List<String> recipients, String rawMessage)
            throws ProtocolException {
        Objects.requireNonNull(sender, "sender must not be null");
        Objects.requireNonNull(recipients, "recipients must not be null");
        Objects.requireNonNull(rawMessage, "rawMessage must not be null");
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("recipients must not be empty");
        }

        // Validate envelope addresses to prevent SMTP command injection
        validateEnvelopeAddress(sender);
        for (String recipient : recipients) {
            validateEnvelopeAddress(recipient);
        }

        // MAIL FROM
        LOG.log(System.Logger.Level.DEBUG, "MAIL FROM:<{0}>", sender);
        connection.writeLine("MAIL FROM:<" + sender + ">");
        SmtpResponse reply = readResponse();
        checkReply(reply, 250, "MAIL FROM");

        // RCPT TO
        for (String recipient : recipients) {
            LOG.log(System.Logger.Level.DEBUG, "RCPT TO:<{0}>", recipient);
            connection.writeLine("RCPT TO:<" + recipient + ">");
            reply = readResponse();
            // Accept both 250 (OK) and 251 (user not local, will forward) per RFC 5321
            if (reply.code() != 250 && reply.code() != 251) {
                throw new ProtocolException("RCPT TO rejected: " + reply.message(), reply.code());
            }
        }

        // DATA
        connection.writeLine("DATA");
        reply = readResponse();
        checkReply(reply, 354, "DATA");

        // Send message body with dot-stuffing
        sendMessageBody(rawMessage);

        // End data with <CRLF>.<CRLF>
        connection.writeLine(".");
        reply = readResponse();
        checkReply(reply, 250, "DATA end");

        LOG.log(System.Logger.Level.DEBUG, "Message sent successfully");
    }

    /**
     * Check if the underlying connection is active
     * 检查底层连接是否活跃
     *
     * @return true if connected | 已连接返回true
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * Send QUIT command and close the connection
     * 发送QUIT命令并关闭连接
     *
     * @throws ProtocolException if the QUIT exchange fails | QUIT交换失败时抛出
     */
    public void quit() throws ProtocolException {
        if (connection == null || !connection.isConnected()) {
            return;
        }
        try {
            connection.writeLine("QUIT");
            SmtpResponse reply = readResponse();
            checkReply(reply, 221, "QUIT");
        } finally {
            connection.close();
        }
    }

    /**
     * Close the connection, ignoring any errors
     * 关闭连接，忽略任何错误
     */
    @Override
    public void close() {
        if (connection != null) {
            try {
                if (connection.isConnected()) {
                    connection.writeLine("QUIT");
                    readResponse();
                }
            } catch (Exception ignored) {
                // intentionally ignored
            } finally {
                connection.close();
            }
        }
    }

    /**
     * Get the server capabilities parsed from the EHLO response
     * 获取从EHLO响应中解析的服务器能力集
     *
     * @return unmodifiable set of capability strings | 不可修改的能力字符串集合
     */
    public Set<String> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }

    /**
     * Check if the server supports a specific capability
     * 检查服务器是否支持指定能力
     *
     * <p>Matching is case-insensitive and checks only the first token
     * of each capability line (e.g., "AUTH" matches "AUTH PLAIN LOGIN").</p>
     * <p>匹配不区分大小写，仅检查每个能力行的第一个令牌
     * （例如，"AUTH"匹配"AUTH PLAIN LOGIN"）。</p>
     *
     * @param capability the capability to check (e.g., "STARTTLS", "AUTH") | 要检查的能力
     * @return true if the server advertises the capability | 服务器公布该能力返回true
     */
    public boolean hasCapability(String capability) {
        Objects.requireNonNull(capability, "capability must not be null");
        String upper = capability.toUpperCase(Locale.ROOT);
        for (String cap : capabilities) {
            if (cap.equals(upper) || cap.startsWith(upper + " ")) {
                return true;
            }
        }
        return false;
    }

    // ========== Internal methods ==========

    /**
     * Validate that a credential (username, password, or token) does not contain
     * characters that could corrupt SASL tokens or inject SMTP commands.
     * 验证凭据（用户名、密码或令牌）不包含可能破坏SASL令牌或注入SMTP命令的字符。
     *
     * @param value the credential value to validate | 要验证的凭据值
     * @param name  the credential name for error messages | 用于错误消息的凭据名称
     * @throws ProtocolException if the value is empty or contains invalid characters | 值为空或包含无效字符时抛出
     */
    private static void validateCredential(String value, String name) throws ProtocolException {
        if (value == null || value.isEmpty()) {
            throw new ProtocolException(name + " must not be empty");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || c == '\0') {
                throw new ProtocolException(
                        "Invalid character in " + name + ": 0x" + Integer.toHexString(c));
            }
        }
    }

    /**
     * Validate that an address does not contain characters that could inject SMTP commands.
     * 验证地址不包含可能注入SMTP命令的字符。
     */
    private static String validateEnvelopeAddress(String address) throws ProtocolException {
        if (address == null || address.isBlank()) {
            throw new ProtocolException("Envelope address must not be blank");
        }
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (c == '\r' || c == '\n' || c == '\0') {
                throw new ProtocolException("Invalid character in envelope address: 0x" +
                        Integer.toHexString(c), 553);
            }
        }
        return address;
    }

    /**
     * Send EHLO and parse capabilities.
     */
    private void sendEhlo(String localHost) throws ProtocolException {
        connection.writeLine("EHLO " + localHost);
        capabilities.clear();

        SmtpResponse reply = readResponse();
        checkReply(reply, 250, "EHLO");

        // Parse capability lines from multi-line response
        for (String line : reply.lines()) {
            // Skip the first line (server domain greeting)
            // Each subsequent line is a capability
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                capabilities.add(trimmed.toUpperCase(Locale.ROOT));
            }
        }
    }

    /**
     * Send the message body with dot-stuffing (RFC 5321 Section 4.5.2).
     * Lines starting with "." are sent as ".." to prevent premature end-of-data.
     *
     * <p>Uses indexOf-based streaming instead of split() to avoid
     * allocating a large String array for big messages.</p>
     */
    private void sendMessageBody(String rawMessage) throws ProtocolException {
        int start = 0;
        int len = rawMessage.length();
        while (start < len) {
            int lineEnd = rawMessage.indexOf('\n', start);
            if (lineEnd == -1) {
                lineEnd = len;
            }

            // Handle \r\n or bare \n
            int contentEnd = (lineEnd > start && rawMessage.charAt(lineEnd - 1) == '\r')
                    ? lineEnd - 1 : lineEnd;

            String line = rawMessage.substring(start, contentEnd);
            if (!line.isEmpty() && line.charAt(0) == '.') {
                connection.writeLine("." + line);
            } else {
                connection.writeLine(line);
            }

            start = (lineEnd < len) ? lineEnd + 1 : len;
        }
    }

    /**
     * Read a full SMTP response (possibly multi-line).
     * Multi-line responses have '-' at position 3; the last line has ' ' at position 3.
     */
    private SmtpResponse readResponse() throws ProtocolException {
        List<String> lines = new ArrayList<>();
        int code = -1;

        while (true) {
            String line = connection.readLine();
            LOG.log(System.Logger.Level.TRACE, "S: {0}", line);

            if (line.length() < 3) {
                throw new ProtocolException("Invalid SMTP response: " + line);
            }

            // Parse reply code from first 3 characters
            int lineCode;
            try {
                lineCode = Integer.parseInt(line.substring(0, 3));
            } catch (NumberFormatException e) {
                throw new ProtocolException("Invalid SMTP reply code: " + line);
            }

            if (code == -1) {
                code = lineCode;
            } else if (lineCode != code) {
                throw new ProtocolException(
                        "Inconsistent reply codes in multi-line response: " + code + " vs " + lineCode);
            }

            // Extract text after "NNN " or "NNN-"
            String text = line.length() > 4 ? line.substring(4) : "";
            lines.add(text);

            // Last line has space (or nothing) at position 3
            if (line.length() == 3 || line.charAt(3) == ' ') {
                break;
            }
            // Continue reading if line.charAt(3) == '-'
        }

        return new SmtpResponse(code, lines);
    }

    /**
     * Verify the response code matches the expected code.
     * Throws ProtocolException with the reply code if it does not match.
     */
    private void checkReply(SmtpResponse response, int expected, String context)
            throws ProtocolException {
        if (response.code() != expected) {
            throw new ProtocolException(
                    context + " failed: expected " + expected + ", got " + response.code()
                            + " " + response.message(),
                    response.code());
        }
    }

    /**
     * Get the local hostname for the EHLO command.
     */
    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Parsed SMTP response with reply code and text lines.
     *
     * @param code  the 3-digit reply code
     * @param lines the text content of each response line
     */
    private record SmtpResponse(int code, List<String> lines) {

        /**
         * Get the full response message (all lines joined).
         */
        String message() {
            return String.join(" ", lines);
        }
    }
}
