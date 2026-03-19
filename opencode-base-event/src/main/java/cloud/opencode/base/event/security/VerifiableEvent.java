package cloud.opencode.base.event.security;

/**
 * Verifiable Event Interface
 * 可验证事件接口
 *
 * <p>Interface for events that can be cryptographically verified.</p>
 * <p>可以进行加密验证的事件接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event signature - 事件签名</li>
 *   <li>Signature verification - 签名验证</li>
 *   <li>Tamper detection - 篡改检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * if (event instanceof VerifiableEvent ve) {
 *     if (!ve.verify(secretKey)) {
 *         throw new EventSecurityException("Event verification failed");
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public interface VerifiableEvent {

    /**
     * Get the event signature
     * 获取事件签名
     *
     * @return the signature string | 签名字符串
     */
    String getSignature();

    /**
     * Verify the event signature
     * 验证事件签名
     *
     * @param secret the secret key used for verification | 用于验证的密钥
     * @return true if signature is valid | 如果签名有效返回true
     */
    boolean verify(String secret);
}
