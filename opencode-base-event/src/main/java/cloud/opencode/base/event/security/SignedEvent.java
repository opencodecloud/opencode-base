package cloud.opencode.base.event.security;

import cloud.opencode.base.event.Event;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Signed Event Base Class
 * 签名事件基类
 *
 * <p>Abstract base class for events that include cryptographic signatures.</p>
 * <p>包含加密签名的事件的抽象基类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC-SHA256 signature - HMAC-SHA256签名</li>
 *   <li>Automatic signing on creation - 创建时自动签名</li>
 *   <li>Signature verification - 签名验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class SecureOrderEvent extends SignedEvent {
 *     private final Order order;
 *
 *     public SecureOrderEvent(Order order, String secret) {
 *         super(secret);
 *         this.order = order;
 *     }
 *
 *     @Override
 *     protected String getPayload() {
 *         return order.getId().toString();
 *     }
 * }
 *
 * // Create and verify
 * SecureOrderEvent event = new SecureOrderEvent(order, secretKey);
 * if (event.verify(secretKey)) {
 *     // Event is authentic
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public abstract class SignedEvent extends Event implements VerifiableEvent {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String signature;

    /**
     * Create signed event with automatic signing
     * 使用自动签名创建签名事件
     *
     * @param secret the secret key for signing | 用于签名的密钥
     */
    protected SignedEvent(String secret) {
        super();
        this.signature = sign(secret);
    }

    /**
     * Create signed event with source and automatic signing
     * 使用来源和自动签名创建签名事件
     *
     * @param source the event source | 事件来源
     * @param secret the secret key for signing | 用于签名的密钥
     */
    protected SignedEvent(String source, String secret) {
        super(source);
        this.signature = sign(secret);
    }

    /**
     * Get the payload data for signing
     * 获取用于签名的负载数据
     *
     * <p>Subclasses must implement this method to provide
     * the data that should be included in the signature.</p>
     * <p>子类必须实现此方法以提供应包含在签名中的数据。</p>
     *
     * @return the payload string | 负载字符串
     */
    protected abstract String getPayload();

    /**
     * Sign the event data
     * 签署事件数据
     *
     * @param secret the secret key | 密钥
     * @return the signature string | 签名字符串
     */
    private String sign(String secret) {
        String data = getId() + getTimestamp() + getPayload();
        return hmacSha256(secret, data);
    }

    /**
     * Compute HMAC-SHA256
     * 计算HMAC-SHA256
     *
     * @param secret the secret key | 密钥
     * @param data   the data to sign | 要签名的数据
     * @return base64 encoded signature | base64编码的签名
     */
    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public boolean verify(String secret) {
        String expectedSignature = sign(secret);
        if (signature == null || expectedSignature == null) {
            return false;
        }
        // Constant-time comparison to prevent timing attacks
        // 使用常量时间比较以防止时序攻击
        return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8));
    }
}
