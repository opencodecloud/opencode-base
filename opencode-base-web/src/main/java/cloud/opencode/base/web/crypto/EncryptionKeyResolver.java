package cloud.opencode.base.web.crypto;

/**
 * Encryption Key Resolver SPI
 * 加密密钥解析器SPI
 *
 * <p>Resolves encryption keys by alias. Framework implementations should
 * provide concrete resolvers that load keys from configuration, key stores,
 * or external key management systems.</p>
 * <p>根据别名解析加密密钥。框架实现应提供具体的解析器，
 * 从配置、密钥库或外部密钥管理系统加载密钥。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple implementation loading from configuration
 * public class ConfigKeyResolver implements EncryptionKeyResolver {
 *     private final Map<String, byte[]> keys;
 *
 *     public byte[] resolveKey(String keyAlias) {
 *         return keys.getOrDefault(keyAlias, keys.get("default"));
 *     }
 * }
 *
 * // Spring Boot implementation
 * @Component
 * public class SpringKeyResolver implements EncryptionKeyResolver {
 *     @Value("${encrypt.default-key}")
 *     private String defaultKey;
 *
 *     public byte[] resolveKey(String keyAlias) { ... }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface EncryptionKeyResolver {

    /**
     * Resolve encryption key by alias
     * 根据别名解析加密密钥
     *
     * @param keyAlias the key alias, empty string for default key | 密钥别名，空字符串表示默认密钥
     * @return the encryption key bytes (32 bytes for AES-256) | 加密密钥字节（AES-256需要32字节）
     */
    byte[] resolveKey(String keyAlias);
}
