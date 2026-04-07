package cloud.opencode.base.email.protocol;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * Mail Protocol Socket Connection
 * 邮件协议套接字连接
 *
 * <p>Wraps a plain or SSL/TLS socket for mail protocol communication.
 * Provides line-oriented I/O for SMTP, IMAP, and POP3 protocols.</p>
 * <p>封装普通或SSL/TLS套接字用于邮件协议通信。
 * 为SMTP、IMAP和POP3协议提供面向行的I/O。</p>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public class MailConnection implements AutoCloseable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_LINE_LENGTH = 65536;

    private Socket socket;
    private BufferedReader reader;
    private BufferedOutputStream writer;
    private final String host;
    private final int port;
    private final boolean ssl;
    private final Duration connectionTimeout;
    private final Duration readTimeout;

    /**
     * Create a new mail connection
     * 创建新的邮件连接
     *
     * @param host              the server hostname | 服务器主机名
     * @param port              the server port | 服务器端口
     * @param ssl               true for SSL connection | true使用SSL连接
     * @param connectionTimeout connection timeout | 连接超时
     * @param readTimeout       read timeout | 读取超时
     */
    public MailConnection(String host, int port, boolean ssl,
                          Duration connectionTimeout, Duration readTimeout) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must not be null or blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535: " + port);
        }
        Objects.requireNonNull(connectionTimeout, "Connection timeout must not be null");
        Objects.requireNonNull(readTimeout, "Read timeout must not be null");
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Connect to the server
     * 连接到服务器
     *
     * @throws ProtocolException if connection fails | 连接失败时抛出
     */
    public void connect() throws ProtocolException {
        try {
            if (ssl) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) factory.createSocket();
                sslSocket.connect(new InetSocketAddress(host, port),
                        (int) connectionTimeout.toMillis());
                sslSocket.setSoTimeout((int) readTimeout.toMillis());
                SSLParameters sslParams = sslSocket.getSSLParameters();
                sslParams.setEndpointIdentificationAlgorithm("HTTPS");
                sslSocket.setSSLParameters(sslParams);
                sslSocket.startHandshake();
                this.socket = sslSocket;
            } else {
                Socket plainSocket = new Socket();
                plainSocket.connect(new InetSocketAddress(host, port),
                        (int) connectionTimeout.toMillis());
                plainSocket.setSoTimeout((int) readTimeout.toMillis());
                this.socket = plainSocket;
            }
            initStreams();
        } catch (IOException e) {
            close(); // ensure socket is closed on failure
            throw new ProtocolException("Failed to connect to " + host + ":" + port, e);
        }
    }

    /**
     * Upgrade the connection to TLS (STARTTLS)
     * 升级连接到TLS（STARTTLS）
     *
     * @throws ProtocolException if TLS upgrade fails | TLS升级失败时抛出
     */
    public void upgradeToTls() throws ProtocolException {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory factory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                    socket, host, port, true);
            sslSocket.setSoTimeout((int) readTimeout.toMillis());
            SSLParameters sslParams = sslSocket.getSSLParameters();
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            sslSocket.setSSLParameters(sslParams);
            sslSocket.startHandshake();
            this.socket = sslSocket;
            initStreams();
        } catch (Exception e) {
            throw new ProtocolException("Failed to upgrade to TLS", e);
        }
    }

    /**
     * Read a single line from the server (CRLF-terminated)
     * 从服务器读取一行（CRLF终止）
     *
     * @return the line without CRLF | 不包含CRLF的行
     * @throws ProtocolException if read fails | 读取失败时抛出
     */
    public String readLine() throws ProtocolException {
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new ProtocolException("Connection closed by server");
            }
            if (line.length() > MAX_LINE_LENGTH) {
                throw new ProtocolException("Server response line too long: " + line.length());
            }
            return line;
        } catch (ProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw new ProtocolException("Failed to read from server", e);
        }
    }

    /**
     * Write a line to the server (appends CRLF)
     * 向服务器写入一行（附加CRLF）
     *
     * @param line the line to write | 要写入的行
     * @throws ProtocolException if write fails | 写入失败时抛出
     */
    public void writeLine(String line) throws ProtocolException {
        try {
            byte[] data = (line + "\r\n").getBytes(StandardCharsets.UTF_8);
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            throw new ProtocolException("Failed to write to server", e);
        }
    }

    /**
     * Write raw bytes to the server (no CRLF appended)
     * 向服务器写入原始字节（不附加CRLF）
     *
     * @param data the data to write | 要写入的数据
     * @throws ProtocolException if write fails | 写入失败时抛出
     */
    public void writeBytes(byte[] data) throws ProtocolException {
        try {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            throw new ProtocolException("Failed to write to server", e);
        }
    }

    /**
     * Write raw string to the server (no CRLF appended)
     * 向服务器写入原始字符串（不附加CRLF）
     *
     * @param data the data to write | 要写入的数据
     * @throws ProtocolException if write fails | 写入失败时抛出
     */
    public void writeRaw(String data) throws ProtocolException {
        writeBytes(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Check if connected
     * 检查是否已连接
     *
     * @return true if connected | 已连接返回true
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Check if connection uses TLS
     * 检查连接是否使用TLS
     *
     * @return true if using TLS | 使用TLS返回true
     */
    public boolean isTls() {
        return socket instanceof SSLSocket;
    }

    /**
     * Get the underlying socket's InputStream for raw reads
     * 获取底层套接字的InputStream用于原始读取
     *
     * @return the input stream | 输入流
     * @throws ProtocolException if not connected | 未连接时抛出
     */
    public InputStream getInputStream() throws ProtocolException {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            throw new ProtocolException("Failed to get input stream", e);
        }
    }

    /**
     * Read exactly the specified number of characters from the server.
     * Used for IMAP literal data.
     * 从服务器精确读取指定数量的字符。用于IMAP字面量数据。
     *
     * <p>Reads from the internal {@link BufferedReader} to ensure consistency
     * with {@link #readLine()}. Reading directly from the raw socket
     * {@link InputStream} would skip data already buffered by the reader.</p>
     *
     * <p>从内部 {@link BufferedReader} 读取以确保与 {@link #readLine()} 一致。
     * 直接从原始套接字 {@link InputStream} 读取会跳过读取器已缓冲的数据。</p>
     *
     * @param count the number of characters to read | 要读取的字符数
     * @return the data as a String | 数据字符串
     * @throws ProtocolException if read fails or EOF reached | 读取失败或达到EOF时抛出
     */
    public String readExact(int count) throws ProtocolException {
        if (count <= 0) {
            return "";
        }
        try {
            char[] buffer = new char[count];
            int totalRead = 0;
            while (totalRead < count) {
                int read = reader.read(buffer, totalRead, count - totalRead);
                if (read == -1) {
                    throw new ProtocolException(
                            "Connection closed while reading literal data");
                }
                totalRead += read;
            }
            return new String(buffer);
        } catch (ProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw new ProtocolException("Failed to read literal data from server", e);
        }
    }

    /**
     * Get the host
     * 获取主机名
     *
     * @return the host | 主机名
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the port
     * 获取端口
     *
     * @return the port | 端口
     */
    public int getPort() {
        return port;
    }

    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // intentionally ignored
            }
        }
        socket = null;
        reader = null;
        writer = null;
    }

    private void initStreams() throws IOException {
        this.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8),
                DEFAULT_BUFFER_SIZE);
        this.writer = new BufferedOutputStream(socket.getOutputStream(), DEFAULT_BUFFER_SIZE);
    }
}
