package cloud.opencode.base.web.crypto;

import java.lang.annotation.*;

/**
 * Encrypt Result Annotation
 * 加密响应注解
 *
 * <p>Marks a method or class whose {@link cloud.opencode.base.web.Result} responses
 * should be automatically encrypted. Framework interceptors (e.g., Spring AOP, Jakarta Interceptor)
 * should check this annotation and delegate to {@link ResultEncryptionHandler}.</p>
 * <p>标记方法或类，其 {@link cloud.opencode.base.web.Result} 响应将被自动加密。
 * 框架拦截器（如 Spring AOP、Jakarta 拦截器）应检查此注解并委托给 {@link ResultEncryptionHandler} 处理。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Encrypt a single method's response
 * @EncryptResult
 * public Result<User> getUser(Long id) { ... }
 *
 * // Encrypt with specific key alias
 * @EncryptResult(keyAlias = "partner-api-key")
 * public Result<Order> getOrder(Long id) { ... }
 *
 * // Encrypt all methods in a class
 * @EncryptResult
 * public class SecureController { ... }
 *
 * // Skip encryption on a specific method in an encrypted class
 * @EncryptResult(enabled = false)
 * public Result<Config> getPublicConfig() { ... }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation, immutable) - 线程安全: 是（注解，不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptResult {

    /**
     * Key alias for encryption key resolution.
     * 加密密钥别名，用于密钥解析。
     *
     * <p>The alias is passed to {@link EncryptionKeyResolver} to obtain
     * the actual encryption key. Empty string means use the default key.</p>
     * <p>别名传递给 {@link EncryptionKeyResolver} 以获取实际加密密钥。
     * 空字符串表示使用默认密钥。</p>
     *
     * @return the key alias | 密钥别名
     */
    String keyAlias() default "";

    /**
     * Encryption algorithm name.
     * 加密算法名称。
     *
     * <p>Must match a registered {@link ResultEncryptor#getAlgorithm()}.
     * Empty string means use the default algorithm (AES-GCM).</p>
     * <p>必须匹配已注册的 {@link ResultEncryptor#getAlgorithm()}。
     * 空字符串表示使用默认算法（AES-GCM）。</p>
     *
     * @return the algorithm name | 算法名称
     */
    String algorithm() default "";

    /**
     * Whether encryption is enabled.
     * 是否启用加密。
     *
     * <p>Set to {@code false} to disable encryption on a method
     * when the class is annotated with {@code @EncryptResult}.</p>
     * <p>当类上标注了 {@code @EncryptResult} 时，设置为 {@code false}
     * 可以在特定方法上禁用加密。</p>
     *
     * @return true if encryption is enabled | 如果启用加密则返回true
     */
    boolean enabled() default true;
}
