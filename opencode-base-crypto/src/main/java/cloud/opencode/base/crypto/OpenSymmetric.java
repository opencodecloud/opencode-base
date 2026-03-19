package cloud.opencode.base.crypto;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.symmetric.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Symmetric encryption facade for encrypt/decrypt operations - Provides convenient API for various symmetric algorithms
 * 对称加密门面类 - 为各种对称加密算法提供便捷的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-CBC and AES-CTR encryption - AES-CBC 和 AES-CTR 加密</li>
 *   <li>SM4-CBC encryption support - SM4-CBC 加密支持</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 *   <li>Key and IV generation - 密钥和 IV 生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenSymmetric cipher = OpenSymmetric.aesCbc();
 * SecretKey key = cipher.generateKey(256);
 * cipher.setKey(key);
 * byte[] iv = cipher.generateIv();
 * cipher.setIv(iv);
 * String encrypted = cipher.encryptBase64("Hello, World!");
 * String decrypted = cipher.decryptBase64ToString(encrypted);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenSymmetric {

    private final SymmetricCipher cipher;
    private SecretKey secretKey;
    private byte[] iv;

    private OpenSymmetric(SymmetricCipher cipher) {
        this.cipher = cipher;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create AES-CBC cipher
     * 创建 AES-CBC 加密
     *
     * @return OpenSymmetric instance
     */
    public static OpenSymmetric aesCbc() {
        return new OpenSymmetric(AesCipher.cbc());
    }

    /**
     * Create AES-CTR cipher
     * 创建 AES-CTR 加密
     *
     * @return OpenSymmetric instance
     */
    public static OpenSymmetric aesCtr() {
        return new OpenSymmetric(AesCipher.ctr());
    }

    /**
     * Create cipher by algorithm enum
     * 根据算法枚举创建加密器
     *
     * @param algorithm symmetric algorithm
     * @return OpenSymmetric instance
     */
    public static OpenSymmetric of(SymmetricAlgorithm algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return switch (algorithm) {
            case AES_GCM_128, AES_GCM_256 -> throw new IllegalArgumentException("Use OpenCrypto.aesGcm() for AEAD ciphers");
            case AES_CBC_128, AES_CBC_256 -> aesCbc();
            case AES_CTR_128, AES_CTR_256 -> aesCtr();
            case CHACHA20_POLY1305 -> throw new IllegalArgumentException("Use OpenCrypto.chacha20Poly1305() for AEAD ciphers");
            case SM4_GCM -> throw new IllegalArgumentException("Use AEAD cipher for SM4-GCM");
            case SM4_CBC -> new OpenSymmetric(Sm4Cipher.cbc());
        };
    }

    // ==================== Key Configuration ====================

    /**
     * Set secret key
     * 设置密钥
     *
     * @param key secret key
     * @return this instance for chaining
     */
    public OpenSymmetric setKey(SecretKey key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        this.secretKey = key;
        cipher.setKey(key);
        return this;
    }

    /**
     * Set secret key from bytes
     * 从字节设置密钥
     *
     * @param key key bytes
     * @return this instance for chaining
     */
    public OpenSymmetric setKey(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        return setKey(new SecretKeySpec(key, cipher.getAlgorithm().split("/")[0]));
    }

    /**
     * Set initialization vector
     * 设置初始化向量
     *
     * @param iv IV bytes
     * @return this instance for chaining
     */
    public OpenSymmetric setIv(byte[] iv) {
        if (iv == null) {
            throw new NullPointerException("IV cannot be null");
        }
        this.iv = iv.clone();
        cipher.setIv(iv);
        return this;
    }

    // ==================== Encryption ====================

    /**
     * Encrypt data
     * 加密数据
     *
     * @param plaintext plaintext bytes
     * @return ciphertext bytes
     */
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        if (secretKey == null) {
            throw new OpenCryptoException(cipher.getAlgorithm(), "encrypt", "Key not set");
        }
        return cipher.encrypt(plaintext);
    }

    /**
     * Encrypt string (UTF-8)
     * 加密字符串（UTF-8）
     *
     * @param plaintext plaintext string
     * @return ciphertext bytes
     */
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypt and return as hex string
     * 加密并返回十六进制字符串
     *
     * @param plaintext plaintext bytes
     * @return hex ciphertext
     */
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    /**
     * Encrypt string and return as hex string
     * 加密字符串并返回十六进制字符串
     *
     * @param plaintext plaintext string
     * @return hex ciphertext
     */
    public String encryptHex(String plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    /**
     * Encrypt and return as Base64 string
     * 加密并返回 Base64 字符串
     *
     * @param plaintext plaintext bytes
     * @return Base64 ciphertext
     */
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    /**
     * Encrypt string and return as Base64 string
     * 加密字符串并返回 Base64 字符串
     *
     * @param plaintext plaintext string
     * @return Base64 ciphertext
     */
    public String encryptBase64(String plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    // ==================== Decryption ====================

    /**
     * Decrypt data
     * 解密数据
     *
     * @param ciphertext ciphertext bytes
     * @return plaintext bytes
     */
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        if (secretKey == null) {
            throw new OpenCryptoException(cipher.getAlgorithm(), "decrypt", "Key not set");
        }
        return cipher.decrypt(ciphertext);
    }

    /**
     * Decrypt and return as string (UTF-8)
     * 解密并返回字符串（UTF-8）
     *
     * @param ciphertext ciphertext bytes
     * @return plaintext string
     */
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt hex-encoded ciphertext
     * 解密十六进制编码的密文
     *
     * @param hexCiphertext hex-encoded ciphertext
     * @return plaintext bytes
     */
    public byte[] decryptHex(String hexCiphertext) {
        if (hexCiphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        return decrypt(HexCodec.decode(hexCiphertext));
    }

    /**
     * Decrypt hex-encoded ciphertext to string
     * 解密十六进制编码的密文为字符串
     *
     * @param hexCiphertext hex-encoded ciphertext
     * @return plaintext string
     */
    public String decryptHexToString(String hexCiphertext) {
        return new String(decryptHex(hexCiphertext), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt Base64-encoded ciphertext
     * 解密 Base64 编码的密文
     *
     * @param base64Ciphertext Base64-encoded ciphertext
     * @return plaintext bytes
     */
    public byte[] decryptBase64(String base64Ciphertext) {
        if (base64Ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        return decrypt(OpenBase64.decode(base64Ciphertext));
    }

    /**
     * Decrypt Base64-encoded ciphertext to string
     * 解密 Base64 编码的密文为字符串
     *
     * @param base64Ciphertext Base64-encoded ciphertext
     * @return plaintext string
     */
    public String decryptBase64ToString(String base64Ciphertext) {
        return new String(decryptBase64(base64Ciphertext), StandardCharsets.UTF_8);
    }

    // ==================== Key/IV Generation ====================

    /**
     * Generate random IV
     * 生成随机 IV
     *
     * @return IV bytes
     */
    public byte[] generateIv() {
        return cipher.generateIv();
    }

    /**
     * Generate random key
     * 生成随机密钥
     *
     * @param keySize key size in bits
     * @return generated secret key
     */
    public SecretKey generateKey(int keySize) {
        return cipher.generateKey(keySize);
    }

    // ==================== Info Methods ====================

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return cipher.getAlgorithm();
    }

    /**
     * Get IV length in bytes
     * 获取 IV 长度（字节）
     *
     * @return IV length
     */
    public int getIvLength() {
        return cipher.getIvLength();
    }
}
