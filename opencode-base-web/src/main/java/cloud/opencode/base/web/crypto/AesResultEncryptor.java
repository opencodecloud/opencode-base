package cloud.opencode.base.web.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * AES Result Encryptor
 * AES响应加密器
 *
 * <p>AES-GCM encryption for result data.</p>
 * <p>使用AES-GCM加密响应数据。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Algorithm: AES-GCM - 算法: AES-GCM</li>
 *   <li>Key size: 256 bits - 密钥长度: 256位</li>
 *   <li>IV size: 12 bytes - IV长度: 12字节</li>
 *   <li>Tag size: 128 bits - 标签长度: 128位</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-256-GCM encryption for result data - AES-256-GCM响应数据加密</li>
 *   <li>Random IV per encryption - 每次加密使用随机IV</li>
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
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

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
        this.secretKey = new SecretKeySpec(key, "AES");
        this.secureRandom = new SecureRandom();
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
        // Hash the string to get a 32-byte key
        byte[] keyBytes = hashToKey(keyString);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    protected byte[] doEncrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encrypted = cipher.doFinal(data);

        // Prepend IV to encrypted data
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }

    @Override
    protected byte[] doDecrypt(byte[] data) throws Exception {
        if (data.length < GCM_IV_LENGTH) {
            throw OpenCryptoException.decryptionFailed("Invalid encrypted data length");
        }

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // Extract IV from data
        byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(data, GCM_IV_LENGTH, data.length);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        return cipher.doFinal(encrypted);
    }

    /**
     * Hash string to 32-byte key using SHA-256
     * 使用SHA-256将字符串散列为32字节密钥
     *
     * @param keyString the key string | 密钥字符串
     * @return the 32-byte key | 32字节密钥
     */
    private byte[] hashToKey(String keyString) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return digest.digest(keyString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw OpenCryptoException.invalidKey("Failed to hash key: " + e.getMessage());
        }
    }

    /**
     * Create encryptor with random key
     * 使用随机密钥创建加密器
     *
     * @return the encryptor and generated key | 加密器和生成的密钥
     */
    public static KeyAndEncryptor withRandomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
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
