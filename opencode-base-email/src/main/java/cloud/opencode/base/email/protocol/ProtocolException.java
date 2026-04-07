package cloud.opencode.base.email.protocol;

/**
 * Protocol-level Exception
 * 协议层异常
 *
 * <p>Thrown when a mail protocol operation fails at the transport level.</p>
 * <p>当邮件协议操作在传输层失败时抛出。</p>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public class ProtocolException extends Exception {

    private final int replyCode;

    public ProtocolException(String message) {
        super(message);
        this.replyCode = -1;
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
        this.replyCode = -1;
    }

    public ProtocolException(String message, int replyCode) {
        super(message);
        this.replyCode = replyCode;
    }

    public ProtocolException(String message, int replyCode, Throwable cause) {
        super(message, cause);
        this.replyCode = replyCode;
    }

    /**
     * Get the protocol reply code (-1 if not applicable)
     * 获取协议回复码（不适用时为-1）
     *
     * @return the reply code | 回复码
     */
    public int getReplyCode() {
        return replyCode;
    }

    /**
     * Check if this is an authentication failure
     * 检查是否为认证失败
     *
     * @return true if authentication failed | 认证失败返回true
     */
    public boolean isAuthenticationFailure() {
        return replyCode == 535 || replyCode == 534
                || getMessage() != null && getMessage().toLowerCase().contains("auth");
    }

    /**
     * Check if this is a connection timeout
     * 检查是否为连接超时
     *
     * @return true if timeout | 超时返回true
     */
    public boolean isTimeout() {
        return getCause() instanceof java.net.SocketTimeoutException;
    }

    /**
     * Check if this is a connection failure
     * 检查是否为连接失败
     *
     * @return true if connection failed | 连接失败返回true
     */
    public boolean isConnectionFailure() {
        return getCause() instanceof java.net.ConnectException
                || getCause() instanceof java.net.UnknownHostException;
    }
}
