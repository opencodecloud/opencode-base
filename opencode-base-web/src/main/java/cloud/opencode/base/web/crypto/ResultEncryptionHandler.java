package cloud.opencode.base.web.crypto;

import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.web.Result;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result Encryption Handler
 * 响应加密处理器
 *
 * <p>Core handler that framework interceptors delegate to for encrypting/decrypting
 * {@link Result} objects. This class manages {@link ResultEncryptor} instances
 * and resolves encryption keys via {@link EncryptionKeyResolver}.</p>
 * <p>框架拦截器委托的核心处理器，用于加密/解密 {@link Result} 对象。
 * 此类管理 {@link ResultEncryptor} 实例，并通过 {@link EncryptionKeyResolver} 解析加密密钥。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create handler
 * EncryptionKeyResolver keyResolver = alias -> loadKey(alias);
 * ResultEncryptionHandler handler = new ResultEncryptionHandler(keyResolver);
 *
 * // In framework interceptor: encrypt response
 * EncryptResult annotation = method.getAnnotation(EncryptResult.class);
 * if (annotation != null && annotation.enabled()) {
 *     EncryptedResult encrypted = handler.encrypt(result, annotation);
 *     return encrypted;
 * }
 *
 * // In framework interceptor: decrypt request
 * DecryptResult annotation = param.getAnnotation(DecryptResult.class);
 * if (annotation != null) {
 *     Result<T> decrypted = handler.decrypt(encrypted, dataType, annotation);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public class ResultEncryptionHandler {

    private final EncryptionKeyResolver keyResolver;
    private final Map<String, ResultEncryptor> encryptorCache = new ConcurrentHashMap<>();

    /**
     * Create handler with key resolver
     * 使用密钥解析器创建处理器
     *
     * @param keyResolver the key resolver | 密钥解析器
     */
    public ResultEncryptionHandler(EncryptionKeyResolver keyResolver) {
        this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver must not be null");
    }

    /**
     * Encrypt a Result using annotation configuration
     * 使用注解配置加密 Result
     *
     * @param result the result to encrypt | 要加密的响应
     * @param annotation the encryption annotation | 加密注解
     * @param <T> the data type | 数据类型
     * @return the encrypted result | 加密后的响应
     */
    public <T> EncryptedResult encrypt(Result<T> result, EncryptResult annotation) {
        ResultEncryptor encryptor = resolveEncryptor(annotation.keyAlias(), annotation.algorithm());
        return encryptor.encrypt(result);
    }

    /**
     * Encrypt a Result with explicit key alias and algorithm
     * 使用指定的密钥别名和算法加密 Result
     *
     * @param result the result to encrypt | 要加密的响应
     * @param keyAlias the key alias | 密钥别名
     * @param algorithm the algorithm | 算法
     * @param <T> the data type | 数据类型
     * @return the encrypted result | 加密后的响应
     */
    public <T> EncryptedResult encrypt(Result<T> result, String keyAlias, String algorithm) {
        ResultEncryptor encryptor = resolveEncryptor(keyAlias, algorithm);
        return encryptor.encrypt(result);
    }

    /**
     * Decrypt an EncryptedResult using annotation configuration
     * 使用注解配置解密 EncryptedResult
     *
     * <p>Verifies signature first, then decrypts. Throws {@link OpenCryptoException}
     * if signature verification fails.</p>
     * <p>先验签再解密。验签失败抛出 {@link OpenCryptoException}。</p>
     *
     * @param encrypted the encrypted result | 加密的响应
     * @param dataType the data type class | 数据类型类
     * @param annotation the decryption annotation | 解密注解
     * @param <T> the data type | 数据类型
     * @return the decrypted result | 解密后的响应
     */
    public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType, DecryptResult annotation) {
        ResultEncryptor encryptor = resolveEncryptor(annotation.keyAlias(), annotation.algorithm());
        return encryptor.decrypt(encrypted, dataType);
    }

    /**
     * Decrypt an EncryptedResult with TypeReference using annotation configuration
     * 使用注解配置和 TypeReference 解密 EncryptedResult
     *
     * @param encrypted the encrypted result | 加密的响应
     * @param typeReference the type reference for generic types | 泛型类型引用
     * @param annotation the decryption annotation | 解密注解
     * @param <T> the data type | 数据类型
     * @return the decrypted result | 解密后的响应
     */
    public <T> Result<T> decrypt(EncryptedResult encrypted, TypeReference<T> typeReference, DecryptResult annotation) {
        ResultEncryptor encryptor = resolveEncryptor(annotation.keyAlias(), annotation.algorithm());
        return encryptor.decrypt(encrypted, typeReference);
    }

    /**
     * Decrypt an EncryptedResult with explicit key alias and algorithm
     * 使用指定的密钥别名和算法解密 EncryptedResult
     *
     * @param encrypted the encrypted result | 加密的响应
     * @param dataType the data type class | 数据类型类
     * @param keyAlias the key alias | 密钥别名
     * @param algorithm the algorithm | 算法
     * @param <T> the data type | 数据类型
     * @return the decrypted result | 解密后的响应
     */
    public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType, String keyAlias, String algorithm) {
        ResultEncryptor encryptor = resolveEncryptor(keyAlias, algorithm);
        return encryptor.decrypt(encrypted, dataType);
    }

    /**
     * Decrypt an EncryptedResult with TypeReference
     * 使用 TypeReference 解密 EncryptedResult
     *
     * @param encrypted the encrypted result | 加密的响应
     * @param typeReference the type reference | 类型引用
     * @param keyAlias the key alias | 密钥别名
     * @param algorithm the algorithm | 算法
     * @param <T> the data type | 数据类型
     * @return the decrypted result | 解密后的响应
     */
    public <T> Result<T> decrypt(EncryptedResult encrypted, TypeReference<T> typeReference, String keyAlias, String algorithm) {
        ResultEncryptor encryptor = resolveEncryptor(keyAlias, algorithm);
        return encryptor.decrypt(encrypted, typeReference);
    }

    /**
     * Check if a Result should be encrypted based on annotation
     * 根据注解判断 Result 是否需要加密
     *
     * @param annotation the annotation (may be null) | 注解（可能为null）
     * @return true if should encrypt | 如果需要加密返回true
     */
    public boolean shouldEncrypt(EncryptResult annotation) {
        return annotation != null && annotation.enabled();
    }

    private ResultEncryptor resolveEncryptor(String keyAlias, String algorithm) {
        String cacheKey = (keyAlias == null ? "" : keyAlias) + ":" + (algorithm == null ? "" : algorithm);
        return encryptorCache.computeIfAbsent(cacheKey, k -> createEncryptor(keyAlias, algorithm));
    }

    private ResultEncryptor createEncryptor(String keyAlias, String algorithm) {
        byte[] key = keyResolver.resolveKey(keyAlias == null ? "" : keyAlias);
        if (key == null) {
            throw OpenCryptoException.invalidKey("No key found for alias: " + keyAlias);
        }
        // Default to AES-GCM
        if (algorithm == null || algorithm.isEmpty() || "AES-GCM".equalsIgnoreCase(algorithm)) {
            return new AesResultEncryptor(key);
        }
        throw OpenCryptoException.encryptionFailed("Unsupported algorithm: " + algorithm
            + ". Register a custom ResultEncryptor for this algorithm.");
    }
}
