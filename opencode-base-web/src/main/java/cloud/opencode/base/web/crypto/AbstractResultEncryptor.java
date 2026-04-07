package cloud.opencode.base.web.crypto;

import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.web.Result;
import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.internal.TraceIdResolver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Abstract Result Encryptor
 * 抽象响应加密器
 *
 * <p>Base class for result encryptors providing common functionality.</p>
 * <p>响应加密器基类，提供通用功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base class for result encryptors - 响应加密器基类</li>
 *   <li>JSON serialization/deserialization via OpenJson - 通过OpenJson进行JSON序列化/反序列化</li>
 *   <li>Base64 encoding of encrypted data - 加密数据的Base64编码</li>
 *   <li>Generic type support via TypeReference - 通过TypeReference支持泛型类型</li>
 *   <li>HMAC signature for tamper detection - HMAC签名防篡改</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MyEncryptor extends AbstractResultEncryptor {
 *     protected byte[] doEncrypt(byte[] data) { ... }
 *     protected byte[] doDecrypt(byte[] data) { ... }
 *     protected byte[] doSign(byte[] data) { ... }
 *     public String getAlgorithm() { return "MY-ALG"; }
 * }
 *
 * // Decrypt with generic type
 * Result<List<User>> result = encryptor.decrypt(encrypted, new TypeReference<List<User>>() {});
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: No (result must not be null) - 否（结果不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public abstract class AbstractResultEncryptor implements ResultEncryptor {

    @Override
    public <T> EncryptedResult encrypt(Result<T> result) {
        try {
            String json = OpenJson.toJson(result.data());
            byte[] encrypted = doEncrypt(json.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(encrypted);
            EncryptedResult unsigned = EncryptedResult.of(
                result.code(),
                result.message(),
                encoded,
                getAlgorithm(),
                result.traceId()
            );
            String sign = computeSign(unsigned);
            return unsigned.withSign(sign);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(e.getMessage());
        }
    }

    @Override
    public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType) {
        verifySign(encrypted);
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted.encryptedData());
            byte[] decrypted = doDecrypt(decoded);
            String json = new String(decrypted, StandardCharsets.UTF_8);
            T data = OpenJson.fromJson(json, dataType);
            return buildResult(encrypted, data);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(e.getMessage());
        }
    }

    @Override
    public <T> Result<T> decrypt(EncryptedResult encrypted, TypeReference<T> typeReference) {
        verifySign(encrypted);
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted.encryptedData());
            byte[] decrypted = doDecrypt(decoded);
            String json = new String(decrypted, StandardCharsets.UTF_8);
            T data = OpenJson.fromJson(json, typeReference);
            return buildResult(encrypted, data);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(e.getMessage());
        }
    }

    private String computeSign(EncryptedResult result) {
        try {
            byte[] payload = result.signPayload().getBytes(StandardCharsets.UTF_8);
            byte[] signature = doSign(payload);
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed("Failed to compute signature: " + e.getMessage());
        }
    }

    private void verifySign(EncryptedResult encrypted) {
        if (encrypted.sign() == null || encrypted.sign().isEmpty()) {
            throw OpenCryptoException.decryptionFailed(
                    "Signature is missing: cannot verify data integrity");
        }
        String expected = computeSign(encrypted);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = encrypted.sign().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
            throw OpenCryptoException.decryptionFailed(
                    "Signature verification failed: data may have been tampered with");
        }
    }

    private <T> Result<T> buildResult(EncryptedResult encrypted, T data) {
        return new Result<>(
            encrypted.code(),
            encrypted.message(),
            data,
            CommonResultCode.SUCCESS.getCode().equals(encrypted.code()),
            Instant.now(),
            TraceIdResolver.resolve()
        );
    }

    /**
     * Perform encryption
     * 执行加密
     *
     * @param data the data to encrypt | 要加密的数据
     * @return the encrypted data | 加密后的数据
     * @throws Exception if encryption fails | 如果加密失败
     */
    protected abstract byte[] doEncrypt(byte[] data) throws Exception;

    /**
     * Perform decryption
     * 执行解密
     *
     * @param data the data to decrypt | 要解密的数据
     * @return the decrypted data | 解密后的数据
     * @throws Exception if decryption fails | 如果解密失败
     */
    protected abstract byte[] doDecrypt(byte[] data) throws Exception;

    /**
     * Compute HMAC signature
     * 计算HMAC签名
     *
     * @param data the data to sign | 要签名的数据
     * @return the HMAC signature bytes | HMAC签名字节
     * @throws Exception if signing fails | 如果签名失败
     */
    protected abstract byte[] doSign(byte[] data) throws Exception;
}
