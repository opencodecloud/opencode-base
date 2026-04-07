package cloud.opencode.base.web.crypto;

import java.lang.annotation.*;

/**
 * Decrypt Result Annotation
 * 解密响应注解
 *
 * <p>Marks a method parameter or class indicating that incoming
 * {@link EncryptedResult} should be automatically verified and decrypted
 * before method execution. Framework interceptors should check this annotation
 * and delegate to {@link ResultEncryptionHandler}.</p>
 * <p>标记方法参数或类，表示传入的 {@link EncryptedResult} 在方法执行前
 * 应自动验签并解密。框架拦截器应检查此注解并委托给 {@link ResultEncryptionHandler} 处理。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Decrypt incoming encrypted request body
 * @PostMapping("/callback")
 * public Result<Void> handleCallback(@DecryptResult EncryptedResult encrypted) { ... }
 *
 * // Decrypt with specific key alias
 * @PostMapping("/partner/callback")
 * public Result<Void> handlePartner(@DecryptResult(keyAlias = "partner-key") EncryptedResult encrypted) { ... }
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
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecryptResult {

    /**
     * Key alias for decryption key resolution.
     * 解密密钥别名，用于密钥解析。
     *
     * @return the key alias | 密钥别名
     */
    String keyAlias() default "";

    /**
     * Encryption algorithm name.
     * 加密算法名称。
     *
     * @return the algorithm name | 算法名称
     */
    String algorithm() default "";
}
