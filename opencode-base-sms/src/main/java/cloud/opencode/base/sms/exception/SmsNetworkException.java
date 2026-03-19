package cloud.opencode.base.sms.exception;

import java.net.URI;

/**
 * SMS Network Exception
 * 短信网络异常
 *
 * <p>Exception thrown when network errors occur during SMS operations.</p>
 * <p>短信操作期间发生网络错误时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Host and port tracking for diagnostics - 主机和端口跟踪用于诊断</li>
 *   <li>HTTP status code capture - HTTP状态码捕获</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "api.example.com", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsNetworkException extends SmsException {

    private final String host;
    private final int port;
    private final int statusCode;

    public SmsNetworkException(SmsErrorCode errorCode, String host) {
        super(errorCode);
        this.host = host;
        this.port = -1;
        this.statusCode = -1;
    }

    public SmsNetworkException(SmsErrorCode errorCode, String host, int port) {
        super(errorCode);
        this.host = host;
        this.port = port;
        this.statusCode = -1;
    }

    public SmsNetworkException(SmsErrorCode errorCode, String host, int statusCode, String message) {
        super(errorCode, message);
        this.host = host;
        this.port = -1;
        this.statusCode = statusCode;
    }

    public SmsNetworkException(SmsErrorCode errorCode, String host, Throwable cause) {
        super(errorCode, cause);
        this.host = host;
        this.port = -1;
        this.statusCode = -1;
    }

    /**
     * Gets the target host
     * 获取目标主机
     *
     * @return host | 主机
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the target port
     * 获取目标端口
     *
     * @return port or -1 if not applicable | 端口，如果不适用则为-1
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the HTTP status code
     * 获取HTTP状态码
     *
     * @return status code or -1 if not applicable | 状态码，如果不适用则为-1
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Creates a network error exception
     * 创建网络错误异常
     *
     * @param host target host | 目标主机
     * @param cause original exception | 原始异常
     * @return network exception | 网络异常
     */
    public static SmsNetworkException networkError(String host, Throwable cause) {
        return new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, host, cause);
    }

    /**
     * Creates a network error exception for a URI
     * 为URI创建网络错误异常
     *
     * @param uri target URI | 目标URI
     * @param cause original exception | 原始异常
     * @return network exception | 网络异常
     */
    public static SmsNetworkException networkError(URI uri, Throwable cause) {
        return new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, uri.getHost(), cause);
    }

    /**
     * Creates an HTTP error exception
     * 创建HTTP错误异常
     *
     * @param host target host | 目标主机
     * @param statusCode HTTP status code | HTTP状态码
     * @param body response body | 响应体
     * @return network exception | 网络异常
     */
    public static SmsNetworkException httpError(String host, int statusCode, String body) {
        return new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, host, statusCode,
                "HTTP " + statusCode + ": " + body);
    }

    /**
     * Creates a connection refused exception
     * 创建连接被拒绝异常
     *
     * @param host target host | 目标主机
     * @param port target port | 目标端口
     * @return network exception | 网络异常
     */
    public static SmsNetworkException connectionRefused(String host, int port) {
        return new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, host, port);
    }
}
