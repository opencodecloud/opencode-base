package cloud.opencode.base.web.crypto;

import cloud.opencode.base.crypto.OpenDigest;
import cloud.opencode.base.crypto.kdf.Hkdf;
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.mac.HmacSha256;
import cloud.opencode.base.crypto.symmetric.AesGcmCipher;

import java.nio.charset.StandardCharsets;

/**
 * AES Result Encryptor
 * AES响应加密器
 *
 * <p>AES-GCM encryption with HMAC-SHA256 signature for result data.</p>
 * <p>使用AES-GCM加密和HMAC-SHA256签名保护响应数据。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Encryption: AES-256-GCM (via opencode-base-crypto) - 加密: AES-256-GCM</li>
 *   <li>Signature: HMAC-SHA256 (via opencode-base-crypto) - 签名: HMAC-SHA256</li>
 *   <li>Key size: 256 bits - 密钥长度: 256位</li>
 *   <li>Separate encryption and signing keys - 加密密钥与签名密钥独立派生</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-256-GCM encryption for result data - AES-256-GCM响应数据加密</li>
 *   <li>HMAC-SHA256 signature covering all plaintext fields - HMAC-SHA256签名覆盖所有明文字段</li>
 *   <li>Signature verification before decryption - 解密前先验签</li>
 *   <li>SHA-256 key derivation from string - 从字符串SHA-256密钥派生</li>
 *   <li>Random key generation support - 随机密钥生成支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AesResultEncryptor encryptor = new AesResultEncryptor("my-secret-key");
 * EncryptedResult encrypted = encryptor.encrypt(result);
 * Result<String> decrypted = encryptor.decrypt(encrypted, String.class);
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public class AesResultEncryptor extends AbstractResultEncryptor {

    private static final String ALGORITHM = "AES-GCM";

    private final AesGcmCipher cipher;
    private final HmacSha256 hmac;

    /**
     * Create AES encryptor with key
     * 使用密钥创建AES加密器
     *
     * @param key the encryption key (32 bytes for AES-256) | 加密密钥（AES-256需要32字节）
     */
    public AesResultEncryptor(byte[] key) {
        if (key == null || key.length != 32) {
            throw OpenCryptoException.invalidKey("Key must be 32 bytes for AES-256");
        }
        this.cipher = AesGcmCipher.aes256Gcm().setKey(key);
        this.hmac = HmacSha256.of(deriveHmacKey(key));
    }

    /**
     * Create AES encryptor with key string
     * 使用密钥字符串创建AES加密器
     *
     * @param keyString the key string (will be hashed to 32 bytes) | 密钥字符串（将被散列为32字节）
     */
    public AesResultEncryptor(String keyString) {
        if (keyString == null || keyString.isBlank()) {
            throw OpenCryptoException.invalidKey("Key string cannot be empty");
        }
        byte[] keyBytes = hashToKey(keyString);
        this.cipher = AesGcmCipher.aes256Gcm().setKey(keyBytes);
        this.hmac = HmacSha256.of(deriveHmacKey(keyBytes));
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    protected byte[] doEncrypt(byte[] data) throws Exception {
        return cipher.encrypt(data);
    }

    @Override
    protected byte[] doDecrypt(byte[] data) throws Exception {
        return cipher.decrypt(data);
    }

    @Override
    protected byte[] doSign(byte[] data) throws Exception {
        return hmac.compute(data);
    }

    /**
     * Derive HMAC key from encryption key using HKDF
     * 使用HKDF从加密密钥派生HMAC密钥
     *
     * <p>Uses HKDF-SHA256 with info="hmac" to derive a separate 32-byte key for HMAC,
     * ensuring encryption and signing use independent keys.</p>
     * <p>使用HKDF-SHA256（info="hmac"）派生独立的32字节HMAC密钥，
     * 确保加密和签名使用不同的密钥。</p>
     *
     * @param encryptionKey the encryption key | 加密密钥
     * @return the derived HMAC key | 派生的HMAC密钥
     */
    private byte[] deriveHmacKey(byte[] encryptionKey) {
        return Hkdf.sha256().deriveKey(encryptionKey, "hmac".getBytes(StandardCharsets.UTF_8), 32);
    }

    /**
     * Hash string to 32-byte key using SHA-256
     * 使用SHA-256将字符串散列为32字节密钥
     *
     * @param keyString the key string | 密钥字符串
     * @return the 32-byte key | 32字节密钥
     */
    private byte[] hashToKey(String keyString) {
        return OpenDigest.sha256().digest(keyString);
    }

    /**
     * Create encryptor with random key
     * 使用随机密钥创建加密器
     *
     * @return the encryptor and generated key | 加密器和生成的密钥
     */
    public static KeyAndEncryptor withRandomKey() {
        byte[] key = KeyGenerator.generateAesKey(256).getEncoded();
        return new KeyAndEncryptor(key, new AesResultEncryptor(key));
    }

    /**
     * Key and Encryptor pair
     * 密钥和加密器对
     *
     * @param key the generated key | 生成的密钥
     * @param encryptor the encryptor | 加密器
     */
    public record KeyAndEncryptor(byte[] key, AesResultEncryptor encryptor) {
        public KeyAndEncryptor {
            key = key.clone();
        }

        @Override
        public byte[] key() {
            return key.clone();
        }
    }
}
